package com.tpg.connect.repository;

import com.tpg.connect.model.UserProfile;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserProfileRepository {
    
    // Create Operations
    UserProfile createProfile(UserProfile profile);
    CompleteUserProfile save(CompleteUserProfile profile);
    
    // Read Operations
    Optional<UserProfile> findByConnectId(String connectId);
    CompleteUserProfile findByUserId(String userId);
    List<UserProfile> findActiveProfiles();
    List<CompleteUserProfile> findAll();
    boolean existsByConnectId(String connectId);
    
    // Update Operations
    UserProfile updateProfile(UserProfile profile);
    UserProfile updateBasicInfo(String connectId, String firstName, String lastName, Integer age, String location);
    UserProfile updateEmailVerification(String connectId, boolean verified, Timestamp verifiedAt);
    UserProfile addPhoto(String connectId, UserProfile.Photo photo);
    UserProfile removePhoto(String connectId, String photoId);
    UserProfile updatePhotoOrder(String connectId, List<UserProfile.Photo> photos);
    UserProfile updateInterests(String connectId, List<String> interests);
    UserProfile updateDetailedProfile(String connectId, UserProfile.Profile profile);
    UserProfile updateWrittenPrompts(String connectId, List<UserProfile.WrittenPrompt> prompts);
    UserProfile updatePollPrompts(String connectId, List<UserProfile.PollPrompt> prompts);
    UserProfile updateFieldVisibility(String connectId, UserProfile.FieldVisibility visibility);
    UserProfile updatePreferences(String connectId, UserProfile.Preferences preferences);
    UserProfile updateNotificationSettings(String connectId, UserProfile.NotificationSettings settings);
    UserProfile updateLastActive(String connectId, Timestamp lastActive);
    
    // Delete Operations
    void deactivateProfile(String connectId);
    void deleteProfile(String connectId);
    
    // Batch Operations
    List<UserProfile> findProfilesByConnectIds(List<String> connectIds);
    Map<String, UserProfile> findProfileMapByConnectIds(List<String> connectIds);
}