package com.tpg.connect.services;

import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.api.LoginResponse;
import com.tpg.connect.model.api.RegisterResponse;
import com.tpg.connect.model.dto.LoginRequest;
import com.tpg.connect.model.dto.RegisterRequest;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.User;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private ProfileManagementService profileManagementService;

    @Mock
    private EmailService emailService;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private AuthenticationService authenticationService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() throws Exception {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Mock MeterRegistry and its components
        Timer.Builder timerBuilder = mock(Timer.Builder.class);
        Timer timer = mock(Timer.class);
        Timer.Sample sample = mock(Timer.Sample.class);
        Counter.Builder counterBuilder = mock(Counter.Builder.class);
        Counter counter = mock(Counter.class);
        MeterRegistry.Config config = mock(MeterRegistry.Config.class);
        
        when(meterRegistry.timer(anyString())).thenReturn(timer);
        when(meterRegistry.counter(anyString())).thenReturn(counter);
        when(meterRegistry.config()).thenReturn(config);
        when(Timer.start(meterRegistry)).thenReturn(sample);
        when(sample.stop(timer)).thenReturn(0L);
        when(timer.recordCallable(any())).thenAnswer(invocation -> {
            return ((java.util.concurrent.Callable<?>) invocation.getArgument(0)).call();
        });
        doNothing().when(counter).increment();
        
        // Set JWT secret using reflection for testing
        Field jwtSecretField = AuthenticationService.class.getDeclaredField("jwtSecret");
        jwtSecretField.setAccessible(true);
        jwtSecretField.set(authenticationService, "test-jwt-secret-key-for-unit-tests-minimum-512-bits-long-key");
        
        // Set JWT expiration using reflection for testing
        Field jwtExpirationField = AuthenticationService.class.getDeclaredField("jwtExpirationInMilliseconds");
        jwtExpirationField.setAccessible(true);
        jwtExpirationField.set(authenticationService, 3600000L);
        
        Field refreshExpirationField = AuthenticationService.class.getDeclaredField("refreshTokenExpirationInMilliseconds");
        refreshExpirationField.setAccessible(true);
        refreshExpirationField.set(authenticationService, 86400000L);
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth("1995-01-01");
        request.setGender("Male");
        request.setLocation("Test City");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.createUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userProfileRepository.save(any(CompleteUserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(emailService).sendEmailVerification(anyString(), anyString(), anyString());

        // Act
        RegisterResponse response = authenticationService.registerUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Registration successful. Please verify your email.", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getUser());
        assertEquals("John Doe", ((UserProfileDTO) response.getUser()).getName());

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).createUser(any(User.class));
        verify(userProfileRepository).save(any(CompleteUserProfile.class));
        verify(emailService).sendEmailVerification(anyString(), anyString(), anyString());
    }

    @Test
    void testRegisterUser_PasswordMismatch() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setConfirmPassword("different_password");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("Passwords do not match", exception.getMessage());
    }

    @Test
    void testRegisterUser_UserTooYoung() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth("2010-01-01"); // Too young

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("User must be at least 18 years old", exception.getMessage());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        request.setDateOfBirth("1995-01-01");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.registerUser(request)
        );
        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDeviceType("WEB");

        User user = new User();
        user.setConnectId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setActive(true);

        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setUserId("user_123");
        profile.setName("Test User");
        profile.setAge(28);

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(userRepository.updateLastLogin(eq("user_123"), any(), eq("WEB"))).thenReturn(user);
        when(userProfileRepository.findByUserId("user_123")).thenReturn(profile);

        // Act
        LoginResponse response = authenticationService.loginUser(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotNull(response.getUser());
        assertEquals("Test User", ((UserProfileDTO) response.getUser()).getName());

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).updateLastLogin(eq("user_123"), any(), any());
        verify(userProfileRepository).findByUserId("user_123");
    }

    @Test
    void testLoginUser_InvalidEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("password123");
        request.setDeviceType("WEB");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(java.util.Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginUser_InvalidPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong_password");
        request.setDeviceType("WEB");

        User user = new User();
        user.setConnectId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("correct_password"));
        user.setActive(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginUser_InactiveAccount() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setDeviceType("WEB");

        User user = new User();
        user.setConnectId("user_123");
        user.setEmail("test@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setActive(false); // Inactive account

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> authenticationService.loginUser(request)
        );
        assertEquals("Account is deactivated", exception.getMessage());
    }
}