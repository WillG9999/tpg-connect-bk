package com.tpg.connect.services;

import com.tpg.connect.client.EmailClient;
import com.tpg.connect.client.models.EmailMessage;
import com.tpg.connect.client.models.EmailResponse;
import com.tpg.connect.utilities.VerificationCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling email verification
 * 
 * Manages verification code generation, email sending,
 * and code validation for user email verification.
 */
@Service
public class EmailVerificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    
    @Autowired
    private EmailClient emailClient;
    
    @Autowired
    private VerificationCodeGenerator codeGenerator;
    
    @Value("${EMAIL_FROM_EMAIL}")
    private String fromEmail;
    
    @Value("${EMAIL_FROM_NAME}")
    private String fromName;
    
    // In-memory storage for verification codes (replace with Redis/database in production)
    private final ConcurrentHashMap<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();
    
    /**
     * Send verification email with 6-digit code
     * 
     * @param email Email address to send verification to
     * @param userName User's name for personalization
     * @return CompletableFuture with EmailResponse
     */
    public CompletableFuture<EmailResponse> sendVerificationEmail(String email, String userName) {
        logger.info("📧 Sending verification email to: {}", email);
        
        // Generate 6-digit verification code
        String verificationCode = codeGenerator.generateSixDigitCode();
        
        // Store the verification code
        storeVerificationCode(email, verificationCode);
        
        // Create email message
        EmailMessage message = EmailMessage.builder()
            .withFromEmail(fromEmail)
            .withFromName(fromName)
            .withToEmail(email)
            .withSubject("Verify Your Connect Account")
            .withTextContent(createTextContent(verificationCode, userName))
            .withHtmlContent(createHtmlContent(verificationCode, userName))
            .build();
        
        return emailClient.sendEmail(message)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    logger.info("✅ Verification email sent to: {} with code: {}", email, 
                        codeGenerator.formatCodeForDisplay(verificationCode));
                } else {
                    logger.error("❌ Failed to send verification email to: {} - {}", 
                        email, response.getErrorReason());
                    // Remove stored code if email failed
                    verificationCodes.remove(email);
                }
                return response;
            });
    }
    
    /**
     * Send password reset email with 6-digit code
     * 
     * @param email Email address to send reset code to
     * @param userName User's name for personalization
     * @return CompletableFuture with EmailResponse
     */
    public CompletableFuture<EmailResponse> sendPasswordResetEmail(String email, String userName) {
        logger.info("📧 Sending password reset email to: {}", email);
        
        // Generate 6-digit reset code
        String resetCode = codeGenerator.generateSixDigitCode();
        
        // Store the reset code
        storeVerificationCode(email, resetCode);
        
        // Create email message
        EmailMessage message = EmailMessage.builder()
            .withFromEmail(fromEmail)
            .withFromName(fromName)
            .withToEmail(email)
            .withSubject("Reset Your Connect Password")
            .withTextContent(createPasswordResetTextContent(resetCode, userName))
            .withHtmlContent(createPasswordResetHtmlContent(resetCode, userName))
            .build();
        
        return emailClient.sendEmail(message);
    }
    
    /**
     * Verify a code for an email address
     * 
     * @param email Email address
     * @param code Verification code to check
     * @return true if code is valid and not expired
     */
    public boolean verifyCode(String email, String code) {
        logger.debug("🔍 Verifying code for email: {}", email);
        
        VerificationCode storedCode = verificationCodes.get(email);
        
        if (storedCode == null) {
            logger.warn("⚠️ No verification code found for email: {}", email);
            return false;
        }
        
        if (codeGenerator.isExpired(storedCode.createdAt, 10)) { // 10 minutes expiration
            logger.warn("⏰ Verification code expired for email: {}", email);
            verificationCodes.remove(email);
            return false;
        }
        
        if (!storedCode.code.equals(code)) {
            logger.warn("❌ Invalid verification code for email: {}", email);
            return false;
        }
        
        // Code is valid - remove it
        verificationCodes.remove(email);
        logger.info("✅ Email verification successful for: {}", email);
        return true;
    }
    
    /**
     * Check if a verification code exists for an email
     * 
     * @param email Email address
     * @return true if code exists and not expired
     */
    public boolean hasValidCode(String email) {
        VerificationCode storedCode = verificationCodes.get(email);
        return storedCode != null && !codeGenerator.isExpired(storedCode.createdAt, 10);
    }
    
    /**
     * Get remaining time for verification code
     * 
     * @param email Email address
     * @return Remaining minutes (0 if expired or not found)
     */
    public long getRemainingTime(String email) {
        VerificationCode storedCode = verificationCodes.get(email);
        if (storedCode == null) {
            return 0;
        }
        return codeGenerator.getRemainingMinutes(storedCode.createdAt, 10);
    }
    
    /**
     * Resend verification code (if not sent recently)
     * 
     * @param email Email address
     * @param userName User's name
     * @return CompletableFuture with EmailResponse
     */
    public CompletableFuture<EmailResponse> resendVerificationCode(String email, String userName) {
        VerificationCode storedCode = verificationCodes.get(email);
        
        // Check if code was sent recently (less than 1 minute ago)
        if (storedCode != null && 
            codeGenerator.getRemainingMinutes(storedCode.createdAt.minusMinutes(9), 10) > 9) {
            logger.warn("⚠️ Verification code already sent recently for: {}", email);
            return CompletableFuture.completedFuture(
                EmailResponse.error(email, "RATE_LIMITED", "Code already sent recently")
            );
        }
        
        return sendVerificationEmail(email, userName);
    }
    
    /**
     * Clear verification code for an email
     * 
     * @param email Email address
     */
    public void clearVerificationCode(String email) {
        verificationCodes.remove(email);
        logger.debug("🗑️ Cleared verification code for: {}", email);
    }
    
    /**
     * Send test verification email
     * 
     * @param email Email address to test
     * @return CompletableFuture with EmailResponse
     */
    public CompletableFuture<EmailResponse> sendTestVerificationEmail(String email) {
        return sendVerificationEmail(email, "Test User");
    }
    
    // Private helper methods
    
    private void storeVerificationCode(String email, String code) {
        verificationCodes.put(email, new VerificationCode(code, LocalDateTime.now()));
        logger.debug("💾 Stored verification code for: {} (expires in 10 minutes)", email);
    }
    
    private String createTextContent(String code, String userName) {
        return String.format("""
            Hi %s,
            
            Welcome to Connect! Please verify your email address to complete your account setup.
            
            Your verification code is: %s
            
            This code will expire in 10 minutes.
            
            If you didn't create a Connect account, please ignore this email.
            
            Best regards,
            The Connect Team
            """, 
            userName != null ? userName : "there",
            codeGenerator.formatCodeForDisplay(code)
        );
    }
    
    private String createHtmlContent(String code, String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 28px; font-weight: bold; color: #e91e63; }
                    .verification-code { 
                        background: #f8f9fa; 
                        border: 2px solid #e91e63; 
                        border-radius: 8px; 
                        padding: 20px; 
                        text-align: center; 
                        margin: 20px 0; 
                    }
                    .code { 
                        font-size: 32px; 
                        font-weight: bold; 
                        color: #e91e63; 
                        letter-spacing: 4px; 
                        font-family: monospace;
                    }
                    .footer { 
                        margin-top: 30px; 
                        padding-top: 20px; 
                        border-top: 1px solid #ddd; 
                        font-size: 12px; 
                        color: #666; 
                        text-align: center; 
                    }
                    .warning { background: #fff3cd; padding: 15px; border-radius: 4px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">💕 Connect</div>
                        <h1>Verify Your Email Address</h1>
                    </div>
                    
                    <p>Hi %s,</p>
                    
                    <p>Welcome to <strong>Connect</strong>! We're excited to help you find meaningful connections.</p>
                    
                    <p>To complete your account setup, please verify your email address using the code below:</p>
                    
                    <div class="verification-code">
                        <p><strong>Your verification code:</strong></p>
                        <div class="code">%s</div>
                        <p><small>This code expires in 10 minutes</small></p>
                    </div>
                    
                    <div class="warning">
                        <p><strong>Security Notice:</strong> If you didn't create a Connect account, please ignore this email. Your email address will not be used without your consent.</p>
                    </div>
                    
                    <p>Questions? Contact our support team - we're here to help!</p>
                    
                    <p>Best regards,<br>
                    The Connect Team</p>
                    
                    <div class="footer">
                        <p>This is an automated message from Connect Dating App.<br>
                        Sent on %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            userName != null ? userName : "there",
            codeGenerator.formatCodeForDisplay(code),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
        );
    }
    
    private String createPasswordResetTextContent(String code, String userName) {
        return String.format("""
            Hi %s,
            
            We received a request to reset your Connect account password.
            
            Your password reset code is: %s
            
            This code will expire in 10 minutes.
            
            If you didn't request a password reset, please ignore this email or contact support.
            
            Best regards,
            The Connect Team
            """, 
            userName != null ? userName : "there",
            codeGenerator.formatCodeForDisplay(code)
        );
    }
    
    private String createPasswordResetHtmlContent(String code, String userName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .logo { font-size: 28px; font-weight: bold; color: #e91e63; }
                    .verification-code { 
                        background: #f8f9fa; 
                        border: 2px solid #dc3545; 
                        border-radius: 8px; 
                        padding: 20px; 
                        text-align: center; 
                        margin: 20px 0; 
                    }
                    .code { 
                        font-size: 32px; 
                        font-weight: bold; 
                        color: #dc3545; 
                        letter-spacing: 4px; 
                        font-family: monospace;
                    }
                    .footer { 
                        margin-top: 30px; 
                        padding-top: 20px; 
                        border-top: 1px solid #ddd; 
                        font-size: 12px; 
                        color: #666; 
                        text-align: center; 
                    }
                    .warning { background: #f8d7da; padding: 15px; border-radius: 4px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="logo">💕 Connect</div>
                        <h1>Password Reset Request</h1>
                    </div>
                    
                    <p>Hi %s,</p>
                    
                    <p>We received a request to reset your Connect account password.</p>
                    
                    <p>Use the code below to reset your password:</p>
                    
                    <div class="verification-code">
                        <p><strong>Your password reset code:</strong></p>
                        <div class="code">%s</div>
                        <p><small>This code expires in 10 minutes</small></p>
                    </div>
                    
                    <div class="warning">
                        <p><strong>Security Alert:</strong> If you didn't request a password reset, please ignore this email or contact support immediately.</p>
                    </div>
                    
                    <p>Questions? Contact our support team for assistance.</p>
                    
                    <p>Best regards,<br>
                    The Connect Team</p>
                    
                    <div class="footer">
                        <p>This is an automated security message from Connect Dating App.<br>
                        Sent on %s</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            userName != null ? userName : "there",
            codeGenerator.formatCodeForDisplay(code),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"))
        );
    }
    
    /**
     * Internal class to store verification codes with timestamps
     */
    private static class VerificationCode {
        final String code;
        final LocalDateTime createdAt;
        
        VerificationCode(String code, LocalDateTime createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }
}