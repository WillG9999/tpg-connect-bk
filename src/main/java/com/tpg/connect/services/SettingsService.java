package com.tpg.connect.services;

import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.model.dto.AccountSettingsRequest;
import com.tpg.connect.model.dto.NotificationSettingsRequest;
import com.tpg.connect.model.dto.PrivacySettingsRequest;
import com.tpg.connect.model.settings.AccountSettings;
import com.tpg.connect.model.settings.NotificationSettings;
import com.tpg.connect.model.settings.PrivacySettings;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SettingsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Cacheable(value = "accountSettings", key = "#userId")
    public AccountSettings getAccountSettings(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (user == null || profile == null) {
            throw new IllegalArgumentException("User not found");
        }

        AccountSettings settings = new AccountSettings();
        settings.setUserId(userId);
        settings.setEmail(user.getEmail());
        settings.setEmailVerified(user.isEmailVerified());
        settings.setPhoneNumber(null); // Placeholder - would need phone field in User model
        settings.setPhoneVerified(false); // Placeholder
        settings.setTwoFactorEnabled(false); // Placeholder
        settings.setAccountStatus(user.isActive() ? "active" : "inactive");
        settings.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        settings.setLastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null);
        settings.setTimezone("UTC"); // Placeholder - would store user's timezone
        settings.setLanguage("en"); // Placeholder - would store user's language preference

        return settings;
    }

    @CacheEvict(value = "accountSettings", key = "#userId")
    public AccountSettings updateAccountSettings(String userId, AccountSettingsRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Update email if changed
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            // Normalize email to lowercase for case-insensitive comparison
            String normalizedEmail = request.getEmail().toLowerCase().trim();
            
            // Check if new email is already in use
            User existingUser = userRepository.findByEmail(normalizedEmail).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new IllegalArgumentException("Email is already in use");
            }
            
            user.setEmail(normalizedEmail);
            user.setEmailVerified(false); // Require re-verification for new email
            emailService.sendEmailVerification(normalizedEmail, "User", UUID.randomUUID().toString());
        }

        // Update other settings (placeholder implementations)
        if (request.getPhoneNumber() != null) {
            // Would update phone number
        }

        if (request.getTimezone() != null) {
            // Would update timezone preference
        }

        if (request.getLanguage() != null) {
            // Would update language preference
        }

        user.setUpdatedAt(com.google.cloud.Timestamp.now());
        userRepository.save(user);

        return getAccountSettings(userId);
    }

    @Cacheable(value = "privacySettings", key = "#userId")
    public PrivacySettings getPrivacySettings(String userId) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        PrivacySettings settings = new PrivacySettings();
        settings.setUserId(userId);
        settings.setProfileVisibility("public"); // Default visibility
        settings.setShowOnlineStatus(true);
        settings.setShowLastSeen(true);
        settings.setShowReadReceipts(true);
        settings.setAllowMessagesFromMatches(true);
        settings.setShowDistance(true);
        settings.setShowAge(true);
        settings.setIndexProfile(true); // Allow profile to be indexed/searchable
        settings.setShowProfileInSearch(true);
        settings.setDataProcessingConsent(true); // Required for dating app
        settings.setLocationSharing(true);

        return settings;
    }

    @CacheEvict(value = "privacySettings", key = "#userId")
    public PrivacySettings updatePrivacySettings(String userId, PrivacySettingsRequest request) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found");
        }

        // Update privacy settings in profile or separate settings model
        // For now, storing as placeholder since we'd need a separate settings model

        PrivacySettings settings = getPrivacySettings(userId);
        
        if (request.getProfileVisibility() != null) {
            settings.setProfileVisibility(request.getProfileVisibility());
        }
        if (request.getShowOnlineStatus() != null) {
            settings.setShowOnlineStatus(request.getShowOnlineStatus());
        }
        if (request.getShowLastSeen() != null) {
            settings.setShowLastSeen(request.getShowLastSeen());
        }
        if (request.getShowReadReceipts() != null) {
            settings.setShowReadReceipts(request.getShowReadReceipts());
        }
        if (request.getAllowMessagesFromMatches() != null) {
            settings.setAllowMessagesFromMatches(request.getAllowMessagesFromMatches());
        }
        if (request.getShowDistance() != null) {
            settings.setShowDistance(request.getShowDistance());
        }
        if (request.getShowAge() != null) {
            settings.setShowAge(request.getShowAge());
        }
        if (request.getLocationSharing() != null) {
            settings.setLocationSharing(request.getLocationSharing());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        return settings;
    }

    @Cacheable(value = "notificationSettings", key = "#userId")
    public NotificationSettings getNotificationSettings(String userId) {
        // Default notification settings
        NotificationSettings settings = new NotificationSettings();
        settings.setUserId(userId);
        settings.setPushNotificationsEnabled(true);
        settings.setEmailNotificationsEnabled(true);
        settings.setSmsNotificationsEnabled(false);
        
        // Match notifications
        settings.setNewMatchNotifications(true);
        settings.setNewMessageNotifications(true);
        settings.setLikeNotifications(true);
        settings.setSuperLikeNotifications(true);
        
        // Discovery notifications
        settings.setNewMatchesAvailableNotifications(true);
        settings.setProfileViewNotifications(false);
        
        // Marketing notifications
        settings.setPromotionalEmailsEnabled(false);
        settings.setTipsAndTricksEmailsEnabled(false);
        
        // Timing preferences
        settings.setQuietHoursEnabled(true);
        settings.setQuietHoursStart("22:00");
        settings.setQuietHoursEnd("08:00");

        return settings;
    }

    @CacheEvict(value = "notificationSettings", key = "#userId")
    public NotificationSettings updateNotificationSettings(String userId, NotificationSettingsRequest request) {
        NotificationSettings settings = getNotificationSettings(userId);
        
        // Update notification preferences
        if (request.getPushNotificationsEnabled() != null) {
            settings.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            settings.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getSmsNotificationsEnabled() != null) {
            settings.setSmsNotificationsEnabled(request.getSmsNotificationsEnabled());
        }
        if (request.getNewMatchNotifications() != null) {
            settings.setNewMatchNotifications(request.getNewMatchNotifications());
        }
        if (request.getNewMessageNotifications() != null) {
            settings.setNewMessageNotifications(request.getNewMessageNotifications());
        }
        if (request.getLikeNotifications() != null) {
            settings.setLikeNotifications(request.getLikeNotifications());
        }
        if (request.getNewMatchesAvailableNotifications() != null) {
            settings.setNewMatchesAvailableNotifications(request.getNewMatchesAvailableNotifications());
        }
        if (request.getPromotionalEmailsEnabled() != null) {
            settings.setPromotionalEmailsEnabled(request.getPromotionalEmailsEnabled());
        }
        if (request.getQuietHoursEnabled() != null) {
            settings.setQuietHoursEnabled(request.getQuietHoursEnabled());
        }
        if (request.getQuietHoursStart() != null) {
            settings.setQuietHoursStart(request.getQuietHoursStart());
        }
        if (request.getQuietHoursEnd() != null) {
            settings.setQuietHoursEnd(request.getQuietHoursEnd());
        }

        // In a real implementation, would save to database
        // For now, return updated settings

        return settings;
    }

    public void deactivateAccount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (user == null || profile == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Deactivate user account
        user.setActive(false);
        user.setUpdatedAt(com.google.cloud.Timestamp.now());
        userRepository.save(user);

        // Hide profile
        profile.setActive(false);
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        // Clear caches
        clearAllUserCaches(userId);

        // Send confirmation email
        emailService.sendAccountDeactivationConfirmation(user.getEmail());
    }

    public void reactivateAccount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (user == null || profile == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Reactivate user account
        user.setActive(true);
        user.setUpdatedAt(com.google.cloud.Timestamp.now());
        userRepository.save(user);

        // Show profile
        profile.setActive(true);
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);

        // Clear caches
        clearAllUserCaches(userId);

        // Send confirmation email
        emailService.sendAccountReactivationConfirmation(user.getEmail());
    }

    public String requestDataExport(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Generate export request ID
        String exportId = UUID.randomUUID().toString();
        
        // In a real implementation, would:
        // 1. Create export request record in database
        // 2. Queue background job to generate export
        // 3. Send email when export is ready
        
        // For now, just send confirmation email
        emailService.sendDataExportRequestConfirmation(user.getEmail(), exportId);
        
        return exportId;
    }

    @CacheEvict(value = {"accountSettings", "privacySettings", "notificationSettings"}, key = "#userId")
    private void clearAllUserCaches(String userId) {
        // Caches cleared by annotation
    }
}