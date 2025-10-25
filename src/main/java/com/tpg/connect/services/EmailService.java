package com.tpg.connect.services;

import com.tpg.connect.services.email.EmailProvider;
import com.tpg.connect.model.email.EmailRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private EmailProvider emailProvider;
    
    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;
    
    @Value("${email.from-name:Connect Dating App}")
    private String fromName;
    
    @Value("${email.enabled:true}")
    private boolean emailEnabled;
    
    public void sendEmailVerification(String email, String firstName, String verificationToken) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Verification email sent to {} with token: {}", email, verificationToken);
            return;
        }
        
        try {
            String subject = "Verify Your Email - Connect";
            String htmlContent = buildEmailVerificationTemplate(firstName, verificationToken);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Email verification sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send email verification to {}: {}", email, e.getMessage());
            // Don't rethrow - email failures shouldn't break the registration flow
        }
    }
    
    public void sendPasswordReset(String email, String resetToken) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Password reset sent to {} with token: {}", email, resetToken);
            return;
        }
        
        try {
            String subject = "Reset Your Password - Connect";
            String resetLink = buildResetLink(resetToken);
            String htmlContent = buildPasswordResetTemplate(email, resetLink, resetToken);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            // Don't rethrow - email failures shouldn't break the password reset flow
        }
    }
    
    public void sendPasswordResetConfirmation(String email) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Password reset confirmation sent to {}", email);
            return;
        }
        
        try {
            String subject = "Password Changed Successfully - Connect";
            String htmlContent = buildPasswordResetConfirmationTemplate(email);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Password reset confirmation sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password reset confirmation to {}: {}", email, e.getMessage());
        }
    }
    
    public void sendPasswordChangeConfirmation(String email) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Password change confirmation sent to {}", email);
            return;
        }
        
        try {
            String subject = "Password Changed - Connect";
            String htmlContent = buildPasswordChangeConfirmationTemplate(email);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Password change confirmation sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send password change confirmation to {}: {}", email, e.getMessage());
        }
    }
    
    public void sendAccountDeletionConfirmation(String email) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Account deletion confirmation sent to {}", email);
            return;
        }
        
        try {
            String subject = "Account Deleted - Connect";
            String htmlContent = buildAccountDeletionTemplate(email);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Account deletion confirmation sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send account deletion confirmation to {}: {}", email, e.getMessage());
        }
    }
    
    public void sendWelcomeEmail(String email) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Welcome email sent to {}", email);
            return;
        }
        
        try {
            String subject = "Welcome to Connect!";
            String htmlContent = buildWelcomeTemplate(email);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to {}: {}", email, e.getMessage());
        }
    }
    
    public void sendApplicationApprovalEmail(String email, String firstName) {
        if (!emailEnabled) {
            logger.info("Email disabled - Mock Email: Application approval email sent to {} ({})", email, firstName);
            return;
        }
        
        try {
            String subject = "🎉 Your Connect Application Has Been Approved!";
            String htmlContent = buildApplicationApprovalTemplate(email, firstName);
            
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(email)
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();
                    
            emailProvider.sendEmail(emailRequest);
            logger.info("Application approval email sent to: {} ({})", email, firstName);
        } catch (Exception e) {
            logger.error("Failed to send application approval email to {}: {}", email, e.getMessage());
            // Don't rethrow - email failures shouldn't break the approval process
        }
    }
    
    private String buildResetLink(String resetToken) {
        return String.format("%s/reset-password?token=%s", frontendBaseUrl, resetToken);
    }
    
    private String buildEmailVerificationTemplate(String firstName, String verificationToken) {
        String verificationLink = String.format("%s/verify-email?token=%s", frontendBaseUrl, verificationToken);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background: #667eea; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Connect!</h1>
                    </div>
                    <div class="content">
                        <h2>Hi %s,</h2>
                        <p>Thanks for signing up! Please verify your email address to complete your registration.</p>
                        <p>Click the button below to verify your email:</p>
                        <a href="%s" class="button">Verify Email Address</a>
                        <p>Or copy and paste this link into your browser:<br>
                        <code>%s</code></p>
                        <p>This link will expire in 7 days.</p>
                    </div>
                    <div class="footer">
                        <p>If you didn't create an account with Connect, please ignore this email.</p>
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, verificationLink, verificationLink);
    }
    
    private String buildPasswordResetTemplate(String email, String resetLink, String resetToken) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background: #e74c3c; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Reset Your Password</h1>
                    </div>
                    <div class="content">
                        <h2>Password Reset Request</h2>
                        <p>We received a request to reset the password for your Connect account (%s).</p>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong> If you didn't request this password reset, please ignore this email. Your password will remain unchanged.
                        </div>
                        
                        <p>To reset your password, click the button below:</p>
                        <a href="%s" class="button">Reset My Password</a>
                        
                        <p>Or copy and paste this link into your browser:<br>
                        <code>%s</code></p>
                        
                        <p><strong>This link will expire in 1 hour</strong> for security reasons.</p>
                        
                        <p>After clicking the link, you'll be taken to a secure page where you can create a new password.</p>
                    </div>
                    <div class="footer">
                        <p>If you're having trouble clicking the button, copy and paste the URL into your web browser.</p>
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, email, resetLink, resetLink);
    }
    
    private String buildPasswordResetConfirmationTemplate(String email) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #00b894 0%%, #00a085 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                    .success { background: #d1edff; border: 1px solid #74b9ff; padding: 15px; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Password Reset Successful</h1>
                    </div>
                    <div class="content">
                        <div class="success">
                            <strong>Your password has been successfully changed!</strong>
                        </div>
                        
                        <p>This confirms that the password for your Connect account (%s) has been updated.</p>
                        
                        <p>If you made this change, no further action is needed.</p>
                        
                        <p><strong>If you didn't change your password:</strong></p>
                        <ul>
                            <li>Someone may have unauthorized access to your account</li>
                            <li>Please contact our support team immediately</li>
                            <li>Consider enabling two-factor authentication when available</li>
                        </ul>
                        
                        <p>For your security, we recommend:</p>
                        <ul>
                            <li>Using a unique, strong password</li>
                            <li>Not sharing your login credentials with anyone</li>
                            <li>Logging out of shared devices</li>
                        </ul>
                    </div>
                    <div class="footer">
                        <p>If you have concerns about your account security, please contact support.</p>
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, email);
    }
    
    private String buildPasswordChangeConfirmationTemplate(String email) {
        return buildPasswordResetConfirmationTemplate(email); // Same template for now
    }
    
    private String buildAccountDeletionTemplate(String email) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #636e72 0%%, #2d3436 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Account Deleted</h1>
                    </div>
                    <div class="content">
                        <h2>Your Connect account has been deleted</h2>
                        <p>This confirms that your Connect account (%s) has been permanently deleted from our system.</p>
                        <p>We're sorry to see you go! If you change your mind, you're always welcome to create a new account.</p>
                        <p>Thank you for being part of the Connect community.</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, email);
    }
    
    private String buildWelcomeTemplate(String email) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to Connect!</h1>
                    </div>
                    <div class="content">
                        <h2>Your email has been verified!</h2>
                        <p>Congratulations! Your Connect account (%s) is now active and ready to use.</p>
                        <p>You can now:</p>
                        <ul>
                            <li>Complete your profile</li>
                            <li>Upload photos</li>
                            <li>Start discovering amazing people</li>
                            <li>Make meaningful connections</li>
                        </ul>
                        <p>We're excited to have you as part of the Connect community!</p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, email);
    }
    
    private String buildApplicationApprovalTemplate(String email, String firstName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #00b894 0%%, #00a085 100%%); color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background: #00b894; color: white; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                    .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; }
                    .success { background: #d1f2eb; border: 1px solid #00b894; padding: 15px; border-radius: 5px; margin: 10px 0; }
                    .highlight { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Application Approved!</h1>
                    </div>
                    <div class="content">
                        <div class="success">
                            <strong>Congratulations %s! Your Connect application has been approved!</strong>
                        </div>
                        
                        <h2>Welcome to Connect!</h2>
                        <p>We're thrilled to welcome you to the Connect community. After careful review, we're excited to have you join our exclusive network of exceptional individuals.</p>
                        
                        <div class="highlight">
                            <strong>Next Steps:</strong>
                            <ul>
                                <li>Complete your profile with photos and personal details</li>
                                <li>Set your discovery preferences</li>
                                <li>Start connecting with like-minded people</li>
                                <li>Explore premium features to enhance your experience</li>
                            </ul>
                        </div>
                        
                        <p>Your account (%s) is now active and ready to use. You can sign in and start building meaningful connections right away.</p>
                        
                        <a href="%s" class="button">Start Using Connect</a>
                        
                        <p>We're here to help you make the most of your Connect experience. If you have any questions, don't hesitate to reach out to our support team.</p>
                        
                        <p>Welcome aboard!</p>
                        <p><strong>The Connect Team</strong></p>
                    </div>
                    <div class="footer">
                        <p>This email was sent because your Connect application was approved.</p>
                        <p>&copy; 2024 Connect Dating App. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, firstName, email, frontendBaseUrl);
    }
    
    // Additional methods for subscription services
    public void sendSubscriptionConfirmation(String email, Object subscription) {
        System.out.println("Mock Email: Subscription confirmation sent to " + email);
    }
    
    public void sendSubscriptionCancellation(String email, Object subscription) {
        System.out.println("Mock Email: Subscription cancellation sent to " + email);
    }
    
    public void sendPaymentFailedNotification(String email, Object subscription) {
        System.out.println("Mock Email: Payment failed notification sent to " + email);
    }
    
    public void sendSubscriptionExpired(String email, Object subscription) {
        System.out.println("Mock Email: Subscription expired notification sent to " + email);
    }
    
    public void sendAccountDeactivationConfirmation(String email) {
        System.out.println("Mock Email: Account deactivation confirmation sent to " + email);
    }
    
    public void sendAccountReactivationConfirmation(String email) {
        System.out.println("Mock Email: Account reactivation confirmation sent to " + email);
    }
    
    public void sendDataExportRequestConfirmation(String email, String requestId) {
        System.out.println("Mock Email: Data export request confirmation sent to " + email + " for request: " + requestId);
    }
    
    /**
     * Health check method for container orchestration
     * @return true if email service is healthy and functional
     */
    public boolean isHealthy() {
        try {
            // Check if email is enabled
            if (!emailEnabled) {
                logger.debug("Email service health check: DISABLED");
                return true; // Disabled is considered healthy for optional service
            }
            
            // Check if email provider is available
            if (emailProvider == null) {
                logger.warn("Email service health check: EMAIL_PROVIDER_NULL");
                return false;
            }
            
            // Check email provider health if method exists
            try {
                if (emailProvider instanceof com.tpg.connect.services.email.EmailProvider) {
                    // Basic health check - service is configured and injectable
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Email service health check failed: {}", e.getMessage());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("Email service health check error: {}", e.getMessage(), e);
            return false;
        }
    }
}