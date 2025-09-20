package com.tpg.connect.services;

import com.tpg.connect.config.EmailConfig;
import com.tpg.connect.model.email.EmailRequest;
import com.tpg.connect.model.email.EmailTemplate;
import com.tpg.connect.model.premium.Subscription;
import com.tpg.connect.services.email.AWSEmailProvider;
import com.tpg.connect.services.email.EmailProvider;
import com.tpg.connect.services.email.SMTPEmailProvider;
import com.tpg.connect.services.email.SendGridEmailProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private SendGridEmailProvider sendGridProvider;

    @Autowired
    private AWSEmailProvider awsProvider;

    @Autowired
    private SMTPEmailProvider smtpProvider;

    private EmailProvider getEmailProvider() {
        String provider = emailConfig.getEmailProvider().toLowerCase();
        
        switch (provider) {
            case "sendgrid":
                if (sendGridProvider.validateConfiguration()) {
                    return sendGridProvider;
                }
                break;
            case "aws":
            case "ses":
                if (awsProvider.validateConfiguration()) {
                    return awsProvider;
                }
                break;
            case "smtp":
            default:
                if (smtpProvider.validateConfiguration()) {
                    return smtpProvider;
                }
                break;
        }
        
        return smtpProvider;
    }

    public void sendEmailVerification(String email, String firstName, String verificationToken) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("firstName", firstName);
            templateData.put("verificationLink", "https://connectapp.com/verify-email?token=" + verificationToken);
            templateData.put("verificationToken", verificationToken);

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.EMAIL_VERIFICATION, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send email verification: " + e.getMessage());
            fallbackToConsole("Email verification", email, "Token: " + verificationToken);
        }
    }

    public void sendPasswordReset(String email, String resetToken) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("resetLink", "https://connectapp.com/reset-password?token=" + resetToken);
            templateData.put("resetToken", resetToken);

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.PASSWORD_RESET, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send password reset: " + e.getMessage());
            fallbackToConsole("Password reset", email, "Token: " + resetToken);
        }
    }

    public void sendPasswordResetConfirmation(String email) {
        try {
            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.PASSWORD_RESET_CONFIRMATION, new HashMap<>());
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send password reset confirmation: " + e.getMessage());
            fallbackToConsole("Password reset confirmation", email, "Your password has been successfully reset.");
        }
    }

    public void sendPasswordChangeConfirmation(String email) {
        try {
            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.PASSWORD_CHANGE_CONFIRMATION, new HashMap<>());
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send password change confirmation: " + e.getMessage());
            fallbackToConsole("Password change confirmation", email, "Your password has been successfully changed.");
        }
    }

    public void sendAccountDeletionConfirmation(String email) {
        try {
            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.ACCOUNT_DELETION, new HashMap<>());
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send account deletion confirmation: " + e.getMessage());
            fallbackToConsole("Account deletion confirmation", email, "Your account has been successfully deleted.");
        }
    }

    public void sendWelcomeEmail(String email) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("firstName", extractFirstNameFromEmail(email));

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.WELCOME, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
            fallbackToConsole("Welcome email", email, "Welcome to Connect! Your email has been verified.");
        }
    }

    public void sendAccountDeactivationConfirmation(String email) {
        try {
            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.ACCOUNT_DEACTIVATION, new HashMap<>());
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send account deactivation confirmation: " + e.getMessage());
            fallbackToConsole("Account deactivation confirmation", email, "Your account has been temporarily deactivated.");
        }
    }

    public void sendAccountReactivationConfirmation(String email) {
        try {
            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.ACCOUNT_REACTIVATION, new HashMap<>());
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send account reactivation confirmation: " + e.getMessage());
            fallbackToConsole("Account reactivation confirmation", email, "Your account has been reactivated. Welcome back!");
        }
    }

    public void sendDataExportRequestConfirmation(String email, String exportId) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("exportId", exportId);

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.DATA_EXPORT, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send data export confirmation: " + e.getMessage());
            fallbackToConsole("Data export confirmation", email, "Export ID: " + exportId);
        }
    }

    public void sendSubscriptionConfirmation(String email, Subscription subscription) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("plan", subscription.getPlan().name());
            templateData.put("duration", subscription.getDurationMonths());
            templateData.put("totalAmount", subscription.getTotalAmount());

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.SUBSCRIPTION_CONFIRMATION, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send subscription confirmation: " + e.getMessage());
            fallbackToConsole("Subscription confirmation", email, "Plan: " + subscription.getPlan().name() + 
                             ", Duration: " + subscription.getDurationMonths() + " months, Amount: £" + subscription.getTotalAmount());
        }
    }

    public void sendSubscriptionCancellation(String email, Subscription subscription) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("plan", subscription.getPlan().name());
            templateData.put("endDate", subscription.getEndDate());

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.SUBSCRIPTION_CANCELLATION, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send subscription cancellation: " + e.getMessage());
            fallbackToConsole("Subscription cancellation", email, "Your subscription will end on: " + subscription.getEndDate());
        }
    }

    public void sendPaymentFailedNotification(String email, Subscription subscription) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("plan", subscription.getPlan().name());

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.PAYMENT_FAILED, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send payment failed notification: " + e.getMessage());
            fallbackToConsole("Payment failed notification", email, "Payment failed for: " + subscription.getPlan().name());
        }
    }

    public void sendSubscriptionExpired(String email, Subscription subscription) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("plan", subscription.getPlan().name());

            EmailRequest request = new EmailRequest(email, EmailTemplate.TemplateType.SUBSCRIPTION_EXPIRED, templateData);
            getEmailProvider().sendTemplatedEmail(request);
        } catch (Exception e) {
            System.err.println("Failed to send subscription expired notification: " + e.getMessage());
            fallbackToConsole("Subscription expired", email, "Previous plan: " + subscription.getPlan().name());
        }
    }

    private void fallbackToConsole(String emailType, String email, String message) {
        System.out.println("FALLBACK - " + emailType + " to " + email);
        System.out.println(message);
    }

    private String extractFirstNameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "there";
        }
        String localPart = email.substring(0, email.indexOf("@"));
        if (localPart.contains(".")) {
            return capitalizeFirst(localPart.substring(0, localPart.indexOf(".")));
        }
        return capitalizeFirst(localPart);
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}