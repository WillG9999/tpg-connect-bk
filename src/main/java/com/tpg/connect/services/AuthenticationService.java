package com.tpg.connect.services;

import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.util.ConnectIdGenerator;
import com.tpg.connect.config.FeatureFlagConfig;
import com.tpg.connect.model.api.LoginResponse;
import com.tpg.connect.model.api.RegisterResponse;
import com.tpg.connect.model.dto.ChangePasswordRequest;
import com.tpg.connect.model.dto.LoginRequest;
import com.tpg.connect.model.dto.RegisterRequest;
import com.tpg.connect.model.dto.ResetPasswordRequest;
import com.tpg.connect.model.dto.TokenBasedResetRequest;
import com.tpg.connect.model.dto.TokenVerificationResponse;
import com.tpg.connect.model.User;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.ApplicationStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import javax.crypto.SecretKey;
import com.google.cloud.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private Timer loginTimer;
    private Timer registrationTimer;
    private Counter loginSuccessCounter;
    private Counter loginFailureCounter;
    private Counter registrationCounter;
    private Counter passwordResetCounter;
    
    // Redis key prefixes
    private static final String PASSWORD_RESET_PREFIX = "auth:password_reset:";
    private static final String EMAIL_VERIFICATION_PREFIX = "auth:email_verification:";
    private static final String INVALIDATED_TOKEN_PREFIX = "auth:invalidated:";
    
    // Token expiration times
    private static final long PASSWORD_RESET_TTL_MINUTES = 15;
    private static final long EMAIL_VERIFICATION_TTL_MINUTES = 60;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ApplicationService applicationService;

    // @Autowired
    // private ProfileManagementService profileManagementService;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConnectIdGenerator connectIdGenerator;

    @Autowired
    private FeatureFlagConfig featureFlagConfig;
    
    @Autowired
    private MeterRegistry meterRegistry;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;
    
    @Value("${app.jwt.access-token-expiration:3600000}")
    private long jwtExpirationInMilliseconds;
    
    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpirationInMilliseconds;
    
    @Value("${app.dev.expose-reset-tokens:false}")
    private boolean exposeResetTokens;
    
    @Value("${app.dev.auto-verify-users:false}")
    private boolean autoVerifyUsers;
    
    @Value("${app.dev.default-test-password:}")
    private String defaultTestPassword;
    
    @PostConstruct
    private void initializeMetrics() {
        // Initialize timing metrics
        loginTimer = Timer.builder("connect_auth_login_duration")
            .description("Time taken for user login operations")
            .tag("operation", "login")
            .register(meterRegistry);
            
        registrationTimer = Timer.builder("connect_auth_registration_duration")
            .description("Time taken for user registration operations")
            .tag("operation", "registration")
            .register(meterRegistry);
        
        // Initialize counter metrics
        loginSuccessCounter = Counter.builder("connect_auth_login_success")
            .description("Number of successful login attempts")
            .tag("result", "success")
            .register(meterRegistry);
            
        loginFailureCounter = Counter.builder("connect_auth_login_failure")
            .description("Number of failed login attempts")
            .tag("result", "failure")
            .register(meterRegistry);
            
        registrationCounter = Counter.builder("connect_auth_registration_total")
            .description("Total number of user registrations")
            .tag("operation", "registration")
            .register(meterRegistry);
            
        passwordResetCounter = Counter.builder("connect_auth_password_reset")
            .description("Number of password reset requests")
            .tag("operation", "password_reset")
            .register(meterRegistry);
    }

    // Redis-based token storage - replaced in-memory ConcurrentHashMaps for production scalability
    
    /**
     * Store password reset token in Redis with TTL
     */
    private void storePasswordResetToken(String token, PasswordResetToken resetToken) {
        try {
            String redisKey = PASSWORD_RESET_PREFIX + token;
            String tokenJson = objectMapper.writeValueAsString(resetToken);
            stringRedisTemplate.opsForValue().set(redisKey, tokenJson, PASSWORD_RESET_TTL_MINUTES, TimeUnit.MINUTES);
            logger.info("🔑 Password reset token stored in Redis with TTL: {} minutes", PASSWORD_RESET_TTL_MINUTES);
        } catch (Exception e) {
            logger.error("❌ Failed to store password reset token in Redis: {}", e.getMessage());
            throw new RuntimeException("Failed to store password reset token", e);
        }
    }
    
    /**
     * Retrieve password reset token from Redis
     */
    private PasswordResetToken getPasswordResetToken(String token) {
        try {
            String redisKey = PASSWORD_RESET_PREFIX + token;
            String tokenJson = stringRedisTemplate.opsForValue().get(redisKey);
            if (tokenJson != null) {
                return objectMapper.readValue(tokenJson, PasswordResetToken.class);
            }
            return null;
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve password reset token from Redis: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Remove password reset token from Redis
     */
    private void removePasswordResetToken(String token) {
        try {
            String redisKey = PASSWORD_RESET_PREFIX + token;
            stringRedisTemplate.delete(redisKey);
            logger.info("🗑️ Password reset token removed from Redis");
        } catch (Exception e) {
            logger.error("❌ Failed to remove password reset token from Redis: {}", e.getMessage());
        }
    }
    
    /**
     * Store email verification token in Redis with TTL
     */
    private void storeEmailVerificationToken(String token, EmailVerificationToken verificationToken) {
        try {
            String redisKey = EMAIL_VERIFICATION_PREFIX + token;
            String tokenJson = objectMapper.writeValueAsString(verificationToken);
            stringRedisTemplate.opsForValue().set(redisKey, tokenJson, EMAIL_VERIFICATION_TTL_MINUTES, TimeUnit.MINUTES);
            logger.info("📧 Email verification token stored in Redis with TTL: {} minutes", EMAIL_VERIFICATION_TTL_MINUTES);
        } catch (Exception e) {
            logger.error("❌ Failed to store email verification token in Redis: {}", e.getMessage());
            throw new RuntimeException("Failed to store email verification token", e);
        }
    }
    
    /**
     * Retrieve email verification token from Redis
     */
    private EmailVerificationToken getEmailVerificationToken(String token) {
        try {
            String redisKey = EMAIL_VERIFICATION_PREFIX + token;
            String tokenJson = stringRedisTemplate.opsForValue().get(redisKey);
            if (tokenJson != null) {
                return objectMapper.readValue(tokenJson, EmailVerificationToken.class);
            }
            return null;
        } catch (Exception e) {
            logger.error("❌ Failed to retrieve email verification token from Redis: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Remove email verification token from Redis
     */
    private void removeEmailVerificationToken(String token) {
        try {
            String redisKey = EMAIL_VERIFICATION_PREFIX + token;
            stringRedisTemplate.delete(redisKey);
            logger.info("🗑️ Email verification token removed from Redis");
        } catch (Exception e) {
            logger.error("❌ Failed to remove email verification token from Redis: {}", e.getMessage());
        }
    }
    
    /**
     * Mark token as invalidated in Redis
     */
    private void invalidateToken(String token) {
        try {
            String redisKey = INVALIDATED_TOKEN_PREFIX + token;
            // Store for the remaining lifetime of the token
            stringRedisTemplate.opsForValue().set(redisKey, "invalidated", 24, TimeUnit.HOURS);
            logger.info("🚫 Token marked as invalidated in Redis");
        } catch (Exception e) {
            logger.error("❌ Failed to invalidate token in Redis: {}", e.getMessage());
        }
    }
    
    /**
     * Store invalidated token in Redis (alias for invalidateToken)
     */
    private void storeInvalidatedToken(String token) {
        invalidateToken(token);
    }
    
    /**
     * Check if token is invalidated in Redis
     */
    private boolean isTokenInvalidated(String token) {
        try {
            String redisKey = INVALIDATED_TOKEN_PREFIX + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            logger.error("❌ Failed to check token invalidation in Redis: {}", e.getMessage());
            return false; // Fail open
        }
    }

    public RegisterResponse registerUser(RegisterRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Validate passwords match
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }

        // In bld profile, override password with test password if configured
        String actualPassword = request.getPassword();
        if (isDevelopmentMode() && !defaultTestPassword.isEmpty()) {
            actualPassword = defaultTestPassword;
            System.out.println("🛠️ Development mode: Using default test password for user registration");
        }

        // Validate age
        if (!isUserOldEnough(request.getDateOfBirth())) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        // Normalize email to lowercase for case-insensitive comparison
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        
        // Check if email already exists (using normalized email)
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create user account
        User user = User.builder()
                .connectId(connectIdGenerator.generateUniqueConnectId(userRepository))
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(actualPassword))
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .emailVerified(autoVerifyUsers) // Auto-verify in development mode
                .active(true)
                .role("USER")
                // Application status will be set when user submits application
                .build();

        user = userRepository.createUser(user);

        // Create complete user profile  
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setConnectId(user.getConnectId()); // Set connectId for document ID
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        profile.setGender(request.getGender());
        profile.setEmail(normalizedEmail);
        profile.setLocation(request.getLocation());
        profile.setCreatedAt(Timestamp.now().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        profile.setUpdatedAt(Timestamp.now().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        profile.setActive(true);

        userProfileRepository.save(profile);

        // Generate email verification token and send email only if features are enabled
        String verificationToken = null;
        if (featureFlagConfig.isEmailService() && featureFlagConfig.isEmailVerification() && !autoVerifyUsers) {
            verificationToken = generateEmailVerificationToken(user.getConnectId(), request.getEmail());
            emailService.sendEmailVerification(request.getEmail(), request.getFirstName(), verificationToken);
        } else if (featureFlagConfig.isEmailVerification() && !featureFlagConfig.isEmailService() && !autoVerifyUsers) {
            // If email verification is required but email service is disabled, generate token but don't send
            verificationToken = generateEmailVerificationToken(user.getConnectId(), request.getEmail());
        } else {
            // If email verification is disabled or auto-verify is enabled, mark user as verified immediately
            if (!user.getEmailVerified()) {
                user.setEmailVerified(true);
                userRepository.updateUser(user);
            }
            if (autoVerifyUsers) {
                System.out.println("🛠️ Development mode: Auto-verified user email for " + request.getEmail());
            }
        }

        // Generate JWT token for immediate login
        String accessToken = generateAccessToken(user.getConnectId(), request.getEmail());

        // Create UserProfileDTO for response (should work with fixed Jackson config)
        UserProfileDTO profileDTO = UserProfileDTO.fromCompleteUserProfile(profile);
        
        // Create appropriate response message based on feature flags
        String message;
        if (autoVerifyUsers) {
            message = "Registration successful. Email automatically verified in development environment.";
        } else if (!featureFlagConfig.isEmailVerification()) {
            message = "Registration successful. Email verification is disabled in this environment.";
        } else if (!featureFlagConfig.isEmailService()) {
            message = "Registration successful. Email verification is required but email service is disabled.";
        } else {
            message = "Registration successful. Please check your email for verification instructions.";
        }
        
            // In development mode, include verification token for testing
            if (isDevelopmentMode() && verificationToken != null) {
                registrationCounter.increment();
                sample.stop(registrationTimer);
                return new RegisterResponse(true, message, accessToken, profileDTO, verificationToken);
            }
            
            registrationCounter.increment();
            sample.stop(registrationTimer);
            return new RegisterResponse(true, message, accessToken, profileDTO);
        } catch (Exception e) {
            sample.stop(registrationTimer);
            throw e;
        }
    }

    public LoginResponse loginUser(LoginRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Normalize email to lowercase for case-insensitive lookup
            String normalizedEmail = request.getEmail().toLowerCase().trim();
            Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        User user = userOpt.get();
        if (!user.getActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        // Verify password - in development mode, also accept test password
        boolean passwordValid = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        
        // In bld profile, also allow login with default test password
        if (!passwordValid && isDevelopmentMode() && !defaultTestPassword.isEmpty()) {
            passwordValid = request.getPassword().equals(defaultTestPassword);
            if (passwordValid) {
                System.out.println("🛠️ Development mode: Login accepted with default test password for " + normalizedEmail);
            }
        }
        
        if (!passwordValid) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check email verification if feature flag is enabled
        if (featureFlagConfig.isEmailVerification() && !user.getEmailVerified()) {
            throw new IllegalArgumentException("EMAIL_NOT_VERIFIED: Please verify your email address before logging in");
        }

        // Check application status - determine where to direct users after login
        String applicationStatus = "NO_APPLICATION"; // Default for users without applications
        
        // Admin users bypass application status checks
        if ("183600102436".equals(user.getConnectId())) {
            applicationStatus = "ADMIN";
        } else {
            try {
                com.tpg.connect.model.application.ApplicationSubmission application = 
                    applicationService.getApplicationByConnectId(user.getConnectId());
                
                if (application != null) {
                    ApplicationStatus status = application.getStatus();
                    
                    if (status == ApplicationStatus.PENDING_APPROVAL) {
                        applicationStatus = "APPLICATION_PENDING";
                    } else if (status == ApplicationStatus.REJECTED) {
                        applicationStatus = "APPLICATION_REJECTED";
                    } else if (status == ApplicationStatus.SUSPENDED) {
                        throw new IllegalArgumentException("ACCOUNT_SUSPENDED"); // Still block suspended users
                    } else if (status == ApplicationStatus.APPROVED) {
                        applicationStatus = "APPROVED";
                    }
                    // All other statuses will show application under review
                }
            } catch (IllegalArgumentException e) {
                // Re-throw application status errors
                throw e;
            } catch (Exception e) {
                // Log application check errors but don't block login
                System.err.println("⚠️ Error checking application status for user " + user.getConnectId() + ": " + e.getMessage());
            }
        }

        try {
            // Update last login
            user = userRepository.updateLastLogin(user.getConnectId(), Timestamp.now(), request.getDeviceType());
        } catch (Exception e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // Get user profile (create if not exists for approved application users)
            CompleteUserProfile profile = userProfileRepository.findByUserId(user.getConnectId());
            
            if (profile == null) {
                System.out.println("⚠️ No profile found for user " + user.getConnectId() + " - creating profile from stored user data");
                
                // This is likely an approved application user logging in for the first time
                // Create their profile now
                profile = new CompleteUserProfile();
                profile.setConnectId(user.getConnectId());
                profile.setEmail(user.getEmail());
                profile.setFirstName("User"); // We don't have this stored in User entity
                profile.setLastName(""); // We don't have this stored in User entity  
                profile.setCreatedAt(LocalDateTime.now());
                profile.setUpdatedAt(LocalDateTime.now());
                profile.setActive(true);
                
                // Save the new profile
                userProfileRepository.save(profile);
                System.out.println("✅ Created new profile for approved user: " + user.getConnectId());
            }

            // Generate tokens
            String accessToken = generateAccessToken(user.getConnectId(), user.getEmail());
            String refreshToken = generateRefreshToken(user.getConnectId());

            UserProfileDTO profileDTO;
            try {
                profileDTO = UserProfileDTO.fromCompleteUserProfile(profile);
            } catch (Exception e) {
                // Fallback to minimal profile if conversion fails
                profileDTO = createMinimalProfile(user, profile);
                System.err.println("Profile conversion failed, using minimal profile: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Record successful login
            loginSuccessCounter.increment();
            sample.stop(loginTimer);
            
            return new LoginResponse(true, "Login successful", accessToken, refreshToken, profileDTO, applicationStatus);
        } catch (Exception e) {
            // Record failed login
            loginFailureCounter.increment();
            sample.stop(loginTimer);
            
            System.err.println("Error in login process: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void logoutUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            storeInvalidatedToken(token);
        }
    }

    public LoginResponse refreshToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }

        String refreshToken = authHeader.substring(7);
        
        if (isTokenInvalidated(refreshToken)) {
            throw new IllegalArgumentException("Token has been invalidated");
        }

        try {
            Claims claims = validateToken(refreshToken);
            String userId = claims.getSubject();
            String email = claims.get("email", String.class);

            Optional<User> userOpt = userRepository.findByConnectId(userId);
            if (!userOpt.isPresent() || !userOpt.get().getActive()) {
                throw new IllegalArgumentException("User not found or inactive");
            }

            User user = userOpt.get();
            CompleteUserProfile profile = userProfileRepository.findByUserId(userId);

            String newAccessToken = generateAccessToken(userId, email);
            String newRefreshToken = generateRefreshToken(userId);

            // Invalidate old refresh token
            storeInvalidatedToken(refreshToken);

            return new LoginResponse(true, "Token refreshed successfully", 
                                   newAccessToken, newRefreshToken, profile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
    }

    public String initiatePasswordReset(String email) {
        // Normalize email to lowercase for case-insensitive lookup
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent() && userOpt.get().getActive()) {
            User user = userOpt.get();
            String resetToken = generatePasswordResetToken(user.getConnectId(), normalizedEmail);
            emailService.sendPasswordReset(normalizedEmail, resetToken);
            
            // Record password reset request
            passwordResetCounter.increment();
            
            return resetToken; // Return token for development/testing
        }
        // Always succeed for security (don't reveal if email exists)
        return null;
    }

    //TODO: Complete email verification system and forgot password functionality with secure token-based reset
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = getPasswordResetToken(request.getToken());
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        Optional<User> userOpt = userRepository.findByConnectId(resetToken.getUserId());
        if (!userOpt.isPresent() || !userOpt.get().getActive()) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.now());
        userRepository.updateUser(user);

        // Remove used token
        removePasswordResetToken(request.getToken());

        emailService.sendPasswordResetConfirmation(user.getEmail());
    }

    public void changePassword(String userId, ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findByConnectId(userId);
        if (!userOpt.isPresent() || !userOpt.get().getActive()) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.now());
        userRepository.updateUser(user);

        emailService.sendPasswordChangeConfirmation(user.getEmail());
    }

    public void deleteAccount(String userId) {
        Optional<User> userOpt = userRepository.findByConnectId(userId);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        
        // Soft delete using repository method
        userRepository.softDeleteUser(userId);

        // Deactivate profile
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile != null) {
            profile.setActive(false);
            profile.setUpdatedAt(LocalDateTime.now());
            userProfileRepository.save(profile);
        }

        emailService.sendAccountDeletionConfirmation(user.getEmail());
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = getEmailVerificationToken(token);
        if (verificationToken == null || verificationToken.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        Optional<User> userOpt = userRepository.findByConnectId(verificationToken.getUserId());
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }

        User user = userOpt.get();
        userRepository.updateEmailVerificationStatus(user.getConnectId(), true, Timestamp.now());

        // Remove used token
        removeEmailVerificationToken(token);

        emailService.sendWelcomeEmail(user.getEmail());
    }

    public void resendEmailVerification(String email) {
        // Normalize email to lowercase for case-insensitive lookup
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);
        if (userOpt.isPresent() && userOpt.get().getActive() && !userOpt.get().getEmailVerified()) {
            User user = userOpt.get();
            CompleteUserProfile profile = userProfileRepository.findByUserId(user.getConnectId());
            String verificationToken = generateEmailVerificationToken(user.getConnectId(), normalizedEmail);
            emailService.sendEmailVerification(normalizedEmail, profile.getFirstName(), verificationToken);
        }
        // Always succeed for security
    }

    public String extractUserIdFromToken(String token) {
        if (isTokenInvalidated(token)) {
            throw new IllegalArgumentException("Token has been invalidated");
        }

        try {
            Claims claims = validateToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token");
        }
    }
    
    public boolean isUserAdmin(String userId) {
        try {
            Optional<User> userOpt = userRepository.findByConnectId(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return "ADMIN".equals(user.getRole());
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking admin status for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isTokenValid(String token) {
        if (isTokenInvalidated(token)) {
            return false;
        }

        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String generateAccessToken(String userId, String email) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInMilliseconds);
        
        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private String generateRefreshToken(String userId) {
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpirationInMilliseconds);
        
        return Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private String generatePasswordResetToken(String userId, String email) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(userId, email, 
                LocalDateTime.now().plusHours(1)); // 1 hour expiry
        storePasswordResetToken(token, resetToken);
        return token;
    }

    private String generateEmailVerificationToken(String userId, String email) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(userId, email,
                LocalDateTime.now().plusDays(7)); // 7 days expiry
        storeEmailVerificationToken(token, verificationToken);
        return token;
    }

    private Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token", e);
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    private boolean isUserOldEnough(String dateOfBirth) {
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    private int calculateAge(String dateOfBirth) {
        try {
            LocalDate birthDate = LocalDate.parse(dateOfBirth);
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private boolean isDevelopmentMode() {
        return exposeResetTokens;
    }
    

    // Helper classes for token management
    private static class PasswordResetToken {
        private final String userId;
        private final String email;
        private final LocalDateTime expiryTime;

        public PasswordResetToken(String userId, String email, LocalDateTime expiryTime) {
            this.userId = userId;
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiryTime); }
    }

    private static class EmailVerificationToken {
        private final String userId;
        private final String email;
        private final LocalDateTime expiryTime;

        public EmailVerificationToken(String userId, String email, LocalDateTime expiryTime) {
            this.userId = userId;
            this.email = email;
            this.expiryTime = expiryTime;
        }

        public String getUserId() { return userId; }
        public String getEmail() { return email; }
        public boolean isExpired() { return LocalDateTime.now().isAfter(expiryTime); }
    }

    private UserProfileDTO createMinimalProfile(User user, CompleteUserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setConnectId(user.getConnectId());
        dto.setName(profile.getFirstName() + " " + profile.getLastName());
        
        // Calculate age safely
        if (profile.getDateOfBirth() != null) {
            dto.setAge(Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears());
        } else {
            dto.setAge(25); // Default age
        }
        
        dto.setLocation(profile.getLocation() != null ? profile.getLocation() : "");
        // Gender is stored in the nested profile object
        if (dto.getProfile() == null) {
            dto.setProfile(new UserProfileDTO.DetailedProfileDTO());
        }
        dto.getProfile().setGender(profile.getGender() != null ? profile.getGender() : "");
        dto.setPhotos(List.of());
        dto.setWrittenPrompts(List.of());
        dto.setPollPrompts(List.of());
        
        // Default field visibility
        dto.setFieldVisibility(Map.of(
            "jobTitle", true,
            "company", true,
            "university", true,
            "religiousBeliefs", true,
            "politics", true,
            "hometown", true,
            "height", true,
            "ethnicity", true
        ));
        
        return dto;
    }

    /**
     * Verify password reset token and return user context
     */
    public TokenVerificationResponse verifyPasswordResetToken(String token) {
        PasswordResetToken resetToken = getPasswordResetToken(token);
        
        if (resetToken == null) {
            return TokenVerificationResponse.builder()
                .valid(false)
                .message("Invalid reset token")
                .expired(false)
                .build();
        }
        
        if (resetToken.isExpired()) {
            // Clean up expired token
            removePasswordResetToken(token);
            return TokenVerificationResponse.builder()
                .valid(false)
                .message("Reset token has expired")
                .expired(true)
                .build();
        }
        
        // Token is valid, return user context
        String maskedEmail = maskEmail(resetToken.getEmail());
        long expiresInMinutes = java.time.Duration.between(
            LocalDateTime.now(), 
            resetToken.getExpiryTime()
        ).toMinutes();
        
        return TokenVerificationResponse.builder()
            .valid(true)
            .message("Token is valid")
            .email(maskedEmail)
            .expired(false)
            .expiresInMinutes(expiresInMinutes)
            .build();
    }

    /**
     * Reset password using token-based request (unified flow)
     */
    public void resetPasswordWithToken(TokenBasedResetRequest request) {
        PasswordResetToken resetToken = getPasswordResetToken(request.getToken());
        if (resetToken == null || resetToken.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        Optional<User> userOpt = userRepository.findByConnectId(resetToken.getUserId());
        if (!userOpt.isPresent() || !userOpt.get().getActive()) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        User user = userOpt.get();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.now());
        userRepository.updateUser(user);

        // Remove used token
        removePasswordResetToken(request.getToken());

        // Send confirmation email
        emailService.sendPasswordResetConfirmation(user.getEmail());
    }

    /**
     * Generate password change token for logged-in users
     * This bypasses email sending and returns token directly
     */
    public String generatePasswordChangeTokenForLoggedInUser(String userId) {
        Optional<User> userOpt = userRepository.findByConnectId(userId);
        if (!userOpt.isPresent() || !userOpt.get().getActive()) {
            throw new IllegalArgumentException("User not found or inactive");
        }

        User user = userOpt.get();
        return generatePasswordResetToken(user.getConnectId(), user.getEmail());
    }

    /**
     * Mask email for privacy (e.g., j***@gmail.com)
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.com";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        String maskedLocal = localPart.length() > 1 ? 
            localPart.charAt(0) + "***" : "***";
            
        return maskedLocal + "@" + domain;
    }
}