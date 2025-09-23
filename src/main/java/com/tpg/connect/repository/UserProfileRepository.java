package com.tpg.connect.repository;

import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.DetailedProfile;
import com.tpg.connect.model.user.EnhancedPhoto;
import com.tpg.connect.model.user.WrittenPrompt;
import com.tpg.connect.model.user.PollPrompt;
import com.tpg.connect.model.user.FieldVisibility;
import com.tpg.connect.model.user.UserPreferences;
import com.tpg.connect.model.user.NotificationSettings;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserProfileRepository {
    
    // Create Operations
    CompleteUserProfile save(CompleteUserProfile profile);
    
    // Read Operations
    Optional<CompleteUserProfile> findByConnectId(String connectId);
    CompleteUserProfile findByUserId(String userId);
    List<CompleteUserProfile> findActiveProfiles();
    List<CompleteUserProfile> findAll();
    boolean existsByConnectId(String connectId);
    
    // Update Operations
    CompleteUserProfile updateProfile(CompleteUserProfile profile);
    CompleteUserProfile updateBasicInfo(String connectId, String firstName, String lastName, Integer age, String location);
    CompleteUserProfile updateEmailVerification(String connectId, boolean verified, Timestamp verifiedAt);
    CompleteUserProfile addPhoto(String connectId, EnhancedPhoto photo);
    CompleteUserProfile removePhoto(String connectId, String photoId);
    CompleteUserProfile updatePhotoOrder(String connectId, List<EnhancedPhoto> photos);
    CompleteUserProfile updateInterests(String connectId, List<String> interests);
    CompleteUserProfile updateDetailedProfile(String connectId, DetailedProfile profile);
    CompleteUserProfile updateWrittenPrompts(String connectId, List<WrittenPrompt> prompts);
    CompleteUserProfile updatePollPrompts(String connectId, List<PollPrompt> prompts);
    CompleteUserProfile updateFieldVisibility(String connectId, FieldVisibility visibility);
    CompleteUserProfile updatePreferences(String connectId, UserPreferences preferences);
    CompleteUserProfile updateNotificationSettings(String connectId, NotificationSettings settings);
    CompleteUserProfile updateLastActive(String connectId, Timestamp lastActive);
    
    // Delete Operations
    void deactivateProfile(String connectId);
    void deleteProfile(String connectId);
    
    // Batch Operations
    List<CompleteUserProfile> findProfilesByConnectIds(List<String> connectIds);
    Map<String, CompleteUserProfile> findProfileMapByConnectIds(List<String> connectIds);
}