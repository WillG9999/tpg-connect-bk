package com.tpg.connect.controllers;

import com.tpg.connect.client.EmailClient;
import com.tpg.connect.client.models.EmailMessage;
import com.tpg.connect.client.models.EmailResponse;
import com.tpg.connect.config.FeatureFlagConfig;
import com.tpg.connect.services.EmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Test controller for email functionality
 * 
 * Provides endpoints for testing email sending, verification codes,
 * and email client configuration validation.
 */
@RestController
@RequestMapping("/api/test/email")
public class EmailTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailTestController.class);
    
    @Autowired
    private EmailClient emailClient;
    
    @Autowired
    private EmailVerificationService emailVerificationService;
    
    @Autowired
    private FeatureFlagConfig featureFlagConfig;
    
    /**
     * Send a simple test email
     * 
     * @param toEmail Email address to send to
     * @return Response indicating success or failure
     */
    @PostMapping("/simple")
    public ResponseEntity<Map<String, Object>> sendSimpleTestEmail(@RequestParam String toEmail) {
        logger.info("📧 Sending simple test email to: {}", toEmail);
        
        try {
            EmailMessage message = EmailMessage.builder()
                .withFromEmail("thepromeutheusgroup@gmail.com")
                .withFromName("Connect Dating App")
                .withToEmail(toEmail)
                .withSubject("Test Email from Connect")
                .withTextContent("This is a simple test email from your Connect dating app!")
                .withHtmlContent("""
                    <h2>Test Email ✅</h2>
                    <p>This is a <strong>test email</strong> from your Connect dating app!</p>
                    <p>If you received this, your email configuration is working correctly.</p>
                    <hr>
                    <p><small>Sent from Connect Dating App</small></p>
                    """)
                .build();
            
            CompletableFuture<EmailResponse> future = emailClient.sendEmail(message);
            EmailResponse response = future.get(); // Wait for completion
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("toEmail", toEmail);
            
            if (response.isSuccess()) {
                result.put("messageId", response.getMessageId());
                result.put("message", "Test email sent successfully!");
                logger.info("✅ Simple test email sent to: {} with ID: {}", toEmail, response.getMessageId());
            } else {
                result.put("error", response.getErrorReason());
                result.put("message", "Failed to send test email");
                logger.error("❌ Failed to send simple test email to: {} - {}", toEmail, response.getErrorReason());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("💥 Exception sending test email to: {}", toEmail, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("toEmail", toEmail);
            result.put("error", e.getMessage());
            result.put("message", "Exception occurred while sending email");
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * Send verification email with 6-digit code
     * 
     * @param toEmail Email address to send to
     * @param userName Optional user name for personalization
     * @return Response with verification details
     */
    @PostMapping("/verification")
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(
            @RequestParam String toEmail,
            @RequestParam(defaultValue = "Test User") String userName) {
        
        logger.info("📧 Sending verification email to: {} for user: {}", toEmail, userName);
        
        try {
            CompletableFuture<EmailResponse> future = emailVerificationService.sendVerificationEmail(toEmail, userName);
            EmailResponse response = future.get(); // Wait for completion
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("toEmail", toEmail);
            result.put("userName", userName);
            
            if (response.isSuccess()) {
                result.put("messageId", response.getMessageId());
                result.put("message", "Verification email sent successfully!");
                result.put("expiresInMinutes", 10);
                result.put("hasValidCode", emailVerificationService.hasValidCode(toEmail));
                logger.info("✅ Verification email sent to: {} with ID: {}", toEmail, response.getMessageId());
            } else {
                result.put("error", response.getErrorReason());
                result.put("message", "Failed to send verification email");
                logger.error("❌ Failed to send verification email to: {} - {}", toEmail, response.getErrorReason());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("💥 Exception sending verification email to: {}", toEmail, e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("toEmail", toEmail);
            result.put("error", e.getMessage());
            result.put("message", "Exception occurred while sending verification email");
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * Verify a code for an email address
     * 
     * @param email Email address
     * @param code Verification code to verify
     * @return Response indicating if code is valid
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestParam String email,
            @RequestParam String code) {
        
        logger.info("🔍 Verifying code for email: {}", email);
        
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("code", code);
        
        boolean isValid = emailVerificationService.verifyCode(email, code);
        result.put("valid", isValid);
        
        if (isValid) {
            result.put("message", "Verification successful!");
            logger.info("✅ Code verification successful for: {}", email);
        } else {
            result.put("message", "Invalid or expired verification code");
            result.put("hasValidCode", emailVerificationService.hasValidCode(email));
            result.put("remainingTime", emailVerificationService.getRemainingTime(email));
            logger.warn("❌ Code verification failed for: {}", email);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Check verification status for an email
     * 
     * @param email Email address to check
     * @return Current verification status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVerificationStatus(@RequestParam String email) {
        logger.debug("📊 Checking verification status for: {}", email);
        
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("hasValidCode", emailVerificationService.hasValidCode(email));
        result.put("remainingTime", emailVerificationService.getRemainingTime(email));
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Send test verification email to will_graham@live.com
     * 
     * @return Response with test results
     */
    @PostMapping("/test-will")
    public ResponseEntity<Map<String, Object>> sendTestToWill() {
        String testEmail = "will_graham@live.com";
        String userName = "Will Graham";
        
        logger.info("🎯 Sending test verification email to Will Graham at: {}", testEmail);
        
        try {
            CompletableFuture<EmailResponse> future = emailVerificationService.sendVerificationEmail(testEmail, userName);
            EmailResponse response = future.get(); // Wait for completion
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", response.isSuccess());
            result.put("testEmail", testEmail);
            result.put("userName", userName);
            result.put("timestamp", java.time.LocalDateTime.now().toString());
            
            if (response.isSuccess()) {
                result.put("messageId", response.getMessageId());
                result.put("message", "Test verification email sent successfully to Will!");
                result.put("instructions", "Check will_graham@live.com for the 6-digit verification code");
                logger.info("🎯✅ Test verification email sent to Will at: {} with ID: {}", testEmail, response.getMessageId());
            } else {
                result.put("error", response.getErrorReason());
                result.put("message", "Failed to send test email to Will");
                logger.error("🎯❌ Failed to send test email to Will at: {} - {}", testEmail, response.getErrorReason());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("🎯💥 Exception sending test email to Will: ", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("testEmail", testEmail);
            result.put("error", e.getMessage());
            result.put("message", "Exception occurred while sending test email to Will");
            
            return ResponseEntity.status(500).body(result);
        }
    }
    
    /**
     * Test email client configuration
     * 
     * @return Email client configuration status
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        logger.info("⚙️ Checking email client configuration");
        
        Map<String, Object> result = new HashMap<>();
        result.put("configured", emailClient.isConfigured());
        result.put("provider", emailClient.getConfig().getProvider());
        result.put("fromEmail", emailClient.getConfig().getFromEmail());
        result.put("fromName", emailClient.getConfig().getFromName());
        result.put("smtpHost", emailClient.getConfig().getSmtpHost());
        result.put("smtpPort", emailClient.getConfig().getSmtpPort());
        result.put("authEnabled", emailClient.getConfig().isAuthenticationEnabled());
        result.put("tlsEnabled", emailClient.getConfig().isStartTlsEnabled());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get current feature flag status
     * 
     * @return Current feature flag configuration
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getFeatureFlags() {
        logger.info("🚩 Checking feature flag status");
        
        Map<String, Object> result = new HashMap<>();
        result.put("emailVerification", featureFlagConfig.isEmailVerification());
        result.put("emailService", featureFlagConfig.isEmailService());
        result.put("pushNotifications", featureFlagConfig.isPushNotifications());
        result.put("premiumFeatures", featureFlagConfig.isPremiumFeatures());
        result.put("adminFeatures", featureFlagConfig.isAdminFeatures());
        result.put("realtimeMessaging", featureFlagConfig.isRealtimeMessaging());
        result.put("analytics", featureFlagConfig.isAnalytics());
        result.put("fileUploads", featureFlagConfig.isFileUploads());
        result.put("socialSharing", featureFlagConfig.isSocialSharing());
        result.put("safetyFeatures", featureFlagConfig.isSafetyFeatures());
        result.put("summary", featureFlagConfig.getFeatureSummary());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get email-specific feature status with helpful information
     * 
     * @return Email feature status and guidance
     */
    @GetMapping("/email-features")
    public ResponseEntity<Map<String, Object>> getEmailFeatureStatus() {
        logger.info("📧🚩 Checking email feature status");
        
        Map<String, Object> result = new HashMap<>();
        result.put("emailVerificationEnabled", featureFlagConfig.isEmailVerification());
        result.put("emailServiceEnabled", featureFlagConfig.isEmailService());
        result.put("emailConfigured", emailClient.isConfigured());
        
        // Provide guidance based on current configuration
        String status;
        String guidance;
        
        if (!featureFlagConfig.isEmailService()) {
            status = "EMAIL_SERVICE_DISABLED";
            guidance = "Email service is disabled. No emails will be sent. Users can login without email verification.";
        } else if (!featureFlagConfig.isEmailVerification()) {
            status = "EMAIL_VERIFICATION_DISABLED";
            guidance = "Email verification is disabled. Users can login without verifying their email address.";
        } else if (!emailClient.isConfigured()) {
            status = "EMAIL_NOT_CONFIGURED";
            guidance = "Email verification is enabled but email client is not properly configured.";
        } else {
            status = "FULLY_ENABLED";
            guidance = "Email service and verification are both enabled and configured.";
        }
        
        result.put("status", status);
        result.put("guidance", guidance);
        
        // Add test recommendations
        if (status.equals("EMAIL_SERVICE_DISABLED") || status.equals("EMAIL_VERIFICATION_DISABLED")) {
            result.put("testRecommendation", "Safe to use dummy emails for testing - no verification required");
        } else {
            result.put("testRecommendation", "Use real email addresses for testing or disable email verification for testing");
        }
        
        return ResponseEntity.ok(result);
    }
}