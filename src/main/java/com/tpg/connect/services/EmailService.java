package com.tpg.connect.services;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    public void sendEmailVerification(String email, String firstName, String verificationToken) {
        // Mock implementation - just log for now
        System.out.println("Mock Email: Verification email sent to " + email + " with token: " + verificationToken);
    }
    
    public void sendPasswordReset(String email, String resetToken) {
        System.out.println("Mock Email: Password reset sent to " + email + " with token: " + resetToken);
    }
    
    public void sendPasswordResetConfirmation(String email) {
        System.out.println("Mock Email: Password reset confirmation sent to " + email);
    }
    
    public void sendPasswordChangeConfirmation(String email) {
        System.out.println("Mock Email: Password change confirmation sent to " + email);
    }
    
    public void sendAccountDeletionConfirmation(String email) {
        System.out.println("Mock Email: Account deletion confirmation sent to " + email);
    }
    
    public void sendWelcomeEmail(String email) {
        System.out.println("Mock Email: Welcome email sent to " + email);
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
}