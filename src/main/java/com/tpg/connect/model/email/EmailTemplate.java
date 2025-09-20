package com.tpg.connect.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplate {
    private String templateId;
    private String subject;
    private String htmlContent;
    private String textContent;
    private Map<String, Object> dynamicData;
    
    public enum TemplateType {
        EMAIL_VERIFICATION("email-verification", "Verify Your Connect Account"),
        PASSWORD_RESET("password-reset", "Reset Your Connect Password"),
        PASSWORD_RESET_CONFIRMATION("password-reset-confirmation", "Password Reset Successful"),
        PASSWORD_CHANGE_CONFIRMATION("password-change-confirmation", "Password Changed Successfully"),
        WELCOME("welcome", "Welcome to Connect!"),
        ACCOUNT_DELETION("account-deletion", "Account Deletion Confirmation"),
        ACCOUNT_DEACTIVATION("account-deactivation", "Account Deactivation Confirmation"),
        ACCOUNT_REACTIVATION("account-reactivation", "Welcome Back to Connect!"),
        DATA_EXPORT("data-export", "Your Data Export Request"),
        SUBSCRIPTION_CONFIRMATION("subscription-confirmation", "Connect Premium Subscription Active"),
        SUBSCRIPTION_CANCELLATION("subscription-cancellation", "Subscription Cancelled"),
        PAYMENT_FAILED("payment-failed", "Payment Failed - Update Required"),
        SUBSCRIPTION_EXPIRED("subscription-expired", "Premium Subscription Expired");
        
        private final String templateId;
        private final String defaultSubject;
        
        TemplateType(String templateId, String defaultSubject) {
            this.templateId = templateId;
            this.defaultSubject = defaultSubject;
        }
        
        public String getTemplateId() {
            return templateId;
        }
        
        public String getDefaultSubject() {
            return defaultSubject;
        }
    }
}