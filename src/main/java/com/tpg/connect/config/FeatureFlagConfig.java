package com.tpg.connect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Feature Flag Configuration
 * 
 * Centralized configuration for controlling application features across different environments.
 * This allows for easy toggling of features between development, testing, and production.
 */
@Configuration
@ConfigurationProperties(prefix = "app.features")
public class FeatureFlagConfig {
    
    /**
     * Email verification feature flag
     * When true: Users must verify their email before accessing the application
     * When false: Users can login without email verification (useful for testing)
     */
    private boolean emailVerification = true;
    
    /**
     * Email service feature flag
     * When true: Email sending functionality is enabled
     * When false: Email operations are skipped (useful for testing without email providers)
     */
    private boolean emailService = true;
    
    /**
     * Push notifications feature flag
     * When true: Push notification functionality is enabled
     * When false: Push notifications are disabled
     */
    private boolean pushNotifications = true;
    
    /**
     * Premium features feature flag
     * When true: Subscription and premium features are enabled
     * When false: Premium features are disabled
     */
    private boolean premiumFeatures = true;
    
    /**
     * Admin features feature flag
     * When true: Admin functionality is enabled
     * When false: Admin features are disabled
     */
    private boolean adminFeatures = true;
    
    /**
     * Real-time messaging feature flag
     * When true: WebSocket and real-time messaging is enabled
     * When false: Real-time features are disabled
     */
    private boolean realtimeMessaging = true;
    
    /**
     * Analytics feature flag
     * When true: User analytics and tracking is enabled
     * When false: Analytics are disabled
     */
    private boolean analytics = true;
    
    /**
     * File upload feature flag
     * When true: Photo and file upload functionality is enabled
     * When false: File uploads are disabled
     */
    private boolean fileUploads = true;
    
    /**
     * Social sharing feature flag
     * When true: Social sharing features are enabled
     * When false: Social sharing is disabled
     */
    private boolean socialSharing = true;
    
    /**
     * Safety features flag
     * When true: User reporting, blocking, and safety features are enabled
     * When false: Safety features are disabled (not recommended for production)
     */
    private boolean safetyFeatures = true;
    
    // Getters and Setters
    
    public boolean isEmailVerification() {
        return emailVerification;
    }
    
    public void setEmailVerification(boolean emailVerification) {
        this.emailVerification = emailVerification;
    }
    
    public boolean isEmailService() {
        return emailService;
    }
    
    public void setEmailService(boolean emailService) {
        this.emailService = emailService;
    }
    
    public boolean isPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    public boolean isPremiumFeatures() {
        return premiumFeatures;
    }
    
    public void setPremiumFeatures(boolean premiumFeatures) {
        this.premiumFeatures = premiumFeatures;
    }
    
    public boolean isAdminFeatures() {
        return adminFeatures;
    }
    
    public void setAdminFeatures(boolean adminFeatures) {
        this.adminFeatures = adminFeatures;
    }
    
    public boolean isRealtimeMessaging() {
        return realtimeMessaging;
    }
    
    public void setRealtimeMessaging(boolean realtimeMessaging) {
        this.realtimeMessaging = realtimeMessaging;
    }
    
    public boolean isAnalytics() {
        return analytics;
    }
    
    public void setAnalytics(boolean analytics) {
        this.analytics = analytics;
    }
    
    public boolean isFileUploads() {
        return fileUploads;
    }
    
    public void setFileUploads(boolean fileUploads) {
        this.fileUploads = fileUploads;
    }
    
    public boolean isSocialSharing() {
        return socialSharing;
    }
    
    public void setSocialSharing(boolean socialSharing) {
        this.socialSharing = socialSharing;
    }
    
    public boolean isSafetyFeatures() {
        return safetyFeatures;
    }
    
    public void setSafetyFeatures(boolean safetyFeatures) {
        this.safetyFeatures = safetyFeatures;
    }
    
    /**
     * Get a summary of all feature flags for debugging
     */
    public String getFeatureSummary() {
        return String.format(
            "FeatureFlags{emailVerification=%s, emailService=%s, pushNotifications=%s, " +
            "premiumFeatures=%s, adminFeatures=%s, realtimeMessaging=%s, analytics=%s, " +
            "fileUploads=%s, socialSharing=%s, safetyFeatures=%s}",
            emailVerification, emailService, pushNotifications, premiumFeatures, 
            adminFeatures, realtimeMessaging, analytics, fileUploads, socialSharing, safetyFeatures
        );
    }
}