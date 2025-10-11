package com.tpg.connect.services;

import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.api.ProfileUpdateRequest;
import com.tpg.connect.model.dto.UpdateProfileRequest;
import com.tpg.connect.model.user.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfileManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileManagementService.class);

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private Validator validator;

    @Autowired
    private PhotoCleanupService photoCleanupService;

    @Autowired
    private CloudStorageService cloudStorageService;

    @Cacheable(value = "userProfiles", key = "'user_profile_' + #userId", unless = "#result == null")
    public CompleteUserProfile getCurrentProfile(String userId, boolean includePreferences) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            return null;
        }

        if (!includePreferences) {
            profile.setPreferences(null);
        }

        return profile;
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updateProfile(String userId, ProfileUpdateRequest request) {
        CompleteUserProfile existingProfile = userProfileRepository.findByUserId(userId);
        
        if (existingProfile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        // Update basic info
        if (request.getName() != null) existingProfile.setName(request.getName());
        if (request.getAge() != null) existingProfile.setAge(request.getAge());
        if (request.getLocation() != null) existingProfile.setLocation(request.getLocation());
        if (request.getInterests() != null) existingProfile.setInterests(request.getInterests());

        // Update profile details
        DetailedProfile profile = existingProfile.getProfile();
        if (profile == null) {
            profile = new DetailedProfile();
            existingProfile.setProfile(profile);
        }

        updateDetailedProfileFields(profile, request);

        // Store old photos for cleanup after successful update
        List<String> oldPhotoUrls = null;
        if (request.getPhotos() != null && existingProfile.getPhotos() != null) {
            oldPhotoUrls = existingProfile.getPhotos().stream()
                .map(EnhancedPhoto::getUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .toList();
        }

        // Update photos
        if (request.getPhotos() != null) {
            existingProfile.setPhotos(convertToEnhancedPhotos(request.getPhotos()));
        }

        // Update prompts
        if (request.getWrittenPrompts() != null) {
            existingProfile.setWrittenPrompts(convertToWrittenPrompts(request.getWrittenPrompts()));
        }
        
        if (request.getPollPrompts() != null) {
            existingProfile.setPollPrompts(convertToPollPrompts(request.getPollPrompts()));
        }

        // Update field visibility
        if (request.getFieldVisibility() != null) {
            FieldVisibility visibility = existingProfile.getFieldVisibility();
            if (visibility == null) {
                visibility = new FieldVisibility();
                existingProfile.setFieldVisibility(visibility);
            }
            updateFieldVisibility(visibility, request.getFieldVisibility());
        }

        // Update preferences
        if (request.getPreferences() != null) {
            UserPreferences preferences = existingProfile.getPreferences();
            if (preferences == null) {
                preferences = new UserPreferences();
                existingProfile.setPreferences(preferences);
            }
            updateUserPreferences(preferences, request.getPreferences());
        }

        // Validate the updated profile
        validateProfile(existingProfile);

        // Update metadata
        existingProfile.setUpdatedAt(LocalDateTime.now());
        existingProfile.setVersion(existingProfile.getVersion() + 1);

        // Save the updated profile
        CompleteUserProfile updatedProfile = userProfileRepository.save(existingProfile);

        // Trigger async cleanup of old photos (after successful update)
        if (request.getPhotos() != null && oldPhotoUrls != null && !oldPhotoUrls.isEmpty()) {
            List<String> newPhotoUrls = request.getPhotos();
            photoCleanupService.cleanupOldPhotos(userId, oldPhotoUrls, newPhotoUrls);
            logger.info("🚀 Async photo cleanup triggered for user: {} - {} old photos to process", 
                       userId, oldPhotoUrls.size());
        }

        return updatedProfile;
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updateBasicInfo(String userId, UpdateProfileRequest request) {
        CompleteUserProfile existingProfile = userProfileRepository.findByUserId(userId);
        
        if (existingProfile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        if (request.getName() != null) existingProfile.setName(request.getName());
        if (request.getLocation() != null) existingProfile.setLocation(request.getLocation());
        if (request.getInterests() != null) existingProfile.setInterests(request.getInterests());
        if (request.getLanguages() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setLanguages(request.getLanguages());
        }
        if (request.getJobTitle() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setJobTitle(request.getJobTitle());
        }
        if (request.getCompany() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setCompany(request.getCompany());
        }
        
        // Identity fields
        if (request.getPronouns() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setPronouns(request.getPronouns());
        }
        if (request.getGender() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setGender(request.getGender());
        }
        if (request.getSexuality() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setSexuality(request.getSexuality());
        }
        if (request.getInterestedIn() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setInterestedIn(request.getInterestedIn());
        }
        
        // Professional fields
        if (request.getUniversity() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setUniversity(request.getUniversity());
        }
        if (request.getEducationLevel() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setEducationLevel(request.getEducationLevel());
        }
        
        // Personal fields
        if (request.getReligiousBeliefs() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setReligiousBeliefs(request.getReligiousBeliefs());
        }
        if (request.getHometown() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setHometown(request.getHometown());
        }
        if (request.getPolitics() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setPolitics(request.getPolitics());
        }
        if (request.getDatingIntentions() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setDatingIntentions(request.getDatingIntentions());
        }
        if (request.getRelationshipType() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setRelationshipType(request.getRelationshipType());
        }
        
        // Physical/lifestyle fields
        if (request.getHeight() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setHeight(request.getHeight());
        }
        if (request.getEthnicity() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setEthnicity(request.getEthnicity());
        }
        if (request.getChildren() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setChildren(request.getChildren());
        }
        if (request.getFamilyPlans() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setFamilyPlans(request.getFamilyPlans());
        }
        if (request.getPets() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setPets(request.getPets());
        }
        if (request.getZodiacSign() != null) {
            DetailedProfile profile = getOrCreateDetailedProfile(existingProfile);
            profile.setZodiacSign(request.getZodiacSign());
        }
        
        // Prompt fields
        if (request.getPhotoPrompts() != null) {
            updatePhotoPrompts(existingProfile, request.getPhotoPrompts());
        }
        if (request.getWrittenPrompts() != null) {
            existingProfile.setWrittenPrompts(convertToWrittenPrompts(request.getWrittenPrompts()));
        }
        if (request.getPollPrompts() != null) {
            existingProfile.setPollPrompts(convertToPollPrompts(request.getPollPrompts()));
        }

        validateProfile(existingProfile);

        existingProfile.setUpdatedAt(LocalDateTime.now());
        existingProfile.setVersion(existingProfile.getVersion() + 1);

        return userProfileRepository.save(existingProfile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile addPhoto(String userId, String photoUrl, boolean isPrimary) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        List<EnhancedPhoto> photos = profile.getPhotos();
        if (photos == null) {
            photos = new ArrayList<>();
            profile.setPhotos(photos);
        }

        if (photos.size() >= 6) {
            throw new IllegalArgumentException("Maximum of 6 photos allowed");
        }

        // If setting as primary, remove primary flag from existing photos
        if (isPrimary) {
            photos.forEach(photo -> photo.setPrimary(false));
        }

        EnhancedPhoto newPhoto = new EnhancedPhoto();
        newPhoto.setId(UUID.randomUUID().toString());
        newPhoto.setUrl(photoUrl);
        newPhoto.setPrimary(isPrimary || photos.isEmpty()); // First photo is always primary
        newPhoto.setOrder(photos.size() + 1);

        photos.add(newPhoto);

        validateProfile(profile);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        CompleteUserProfile updatedProfile = userProfileRepository.save(profile);

        // If we exceed 6 photos, cleanup excess photos asynchronously
        if (photos.size() > 6) {
            List<String> allPhotoUrls = photos.stream()
                .map(EnhancedPhoto::getUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .toList();
            
            photoCleanupService.cleanupExcessPhotos(userId, allPhotoUrls, 6);
            logger.info("🚀 Excess photo cleanup triggered for user: {} - {} photos total", 
                       userId, photos.size());
        }

        return updatedProfile;
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile removePhoto(String userId, String photoId) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        List<EnhancedPhoto> photos = profile.getPhotos();
        if (photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException("No photos to remove");
        }

        if (photos.size() == 1) {
            throw new IllegalArgumentException("Cannot remove the last photo. Profile must have at least one photo.");
        }

        // Find the photo URL before removal for cleanup
        String photoUrlToDelete = photos.stream()
            .filter(photo -> photo.getId().equals(photoId))
            .map(EnhancedPhoto::getUrl)
            .findFirst()
            .orElse(null);

        boolean removed = photos.removeIf(photo -> photo.getId().equals(photoId));
        
        if (!removed) {
            throw new IllegalArgumentException("Photo not found with ID: " + photoId);
        }

        // Reorder photos and ensure there's always a primary photo
        reorderPhotos(photos);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        CompleteUserProfile updatedProfile = userProfileRepository.save(profile);

        // Trigger async cleanup of the removed photo
        if (photoUrlToDelete != null) {
            photoCleanupService.cleanupSinglePhoto(userId, photoUrlToDelete);
            logger.info("🚀 Single photo cleanup triggered for user: {} - Photo: {}", userId, photoUrlToDelete);
        }

        return updatedProfile;
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updatePhotos(String userId, List<String> photoUrls) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        if (photoUrls == null || photoUrls.isEmpty()) {
            throw new IllegalArgumentException("At least one photo is required");
        }

        if (photoUrls.size() > 6) {
            throw new IllegalArgumentException("Maximum of 6 photos allowed");
        }

        // Store old photos for cleanup
        List<String> oldPhotoUrls = profile.getPhotos() != null ? 
            profile.getPhotos().stream()
                .map(EnhancedPhoto::getUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .toList() : new ArrayList<>();

        List<EnhancedPhoto> photos = new ArrayList<>();
        for (int i = 0; i < photoUrls.size(); i++) {
            EnhancedPhoto photo = new EnhancedPhoto();
            photo.setId(UUID.randomUUID().toString());
            photo.setUrl(photoUrls.get(i));
            photo.setPrimary(i == 0); // First photo is primary
            photo.setOrder(i + 1);
            photos.add(photo);
        }

        profile.setPhotos(photos);

        validateProfile(profile);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        CompleteUserProfile updatedProfile = userProfileRepository.save(profile);

        // Trigger async cleanup of old photos
        if (!oldPhotoUrls.isEmpty()) {
            photoCleanupService.cleanupOldPhotos(userId, oldPhotoUrls, photoUrls);
            logger.info("🚀 Async photo cleanup triggered for user: {} - {} old photos to process", 
                       userId, oldPhotoUrls.size());
        }

        return updatedProfile;
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updateWrittenPrompts(String userId, List<Map<String, String>> writtenPromptsData) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        List<WrittenPrompt> prompts = convertToWrittenPrompts(writtenPromptsData);
        profile.setWrittenPrompts(prompts);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updatePollPrompts(String userId, List<Map<String, Object>> pollPromptsData) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        List<PollPrompt> prompts = convertToPollPrompts(pollPromptsData);
        profile.setPollPrompts(prompts);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updateFieldVisibility(String userId, Map<String, Boolean> visibilityData) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        FieldVisibility visibility = profile.getFieldVisibility();
        if (visibility == null) {
            visibility = new FieldVisibility();
            profile.setFieldVisibility(visibility);
        }

        updateFieldVisibility(visibility, visibilityData);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updatePreferences(String userId, Map<String, Object> preferencesData) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        UserPreferences preferences = profile.getPreferences();
        if (preferences == null) {
            preferences = new UserPreferences();
            profile.setPreferences(preferences);
        }

        updateUserPreferences(preferences, preferencesData);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
    }

    private DetailedProfile getOrCreateDetailedProfile(CompleteUserProfile completeProfile) {
        DetailedProfile profile = completeProfile.getProfile();
        if (profile == null) {
            profile = new DetailedProfile();
            completeProfile.setProfile(profile);
        }
        return profile;
    }

    private void updateDetailedProfileFields(DetailedProfile profile, ProfileUpdateRequest request) {
        if (request.getPronouns() != null) profile.setPronouns(request.getPronouns());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getSexuality() != null) profile.setSexuality(request.getSexuality());
        if (request.getInterestedIn() != null) profile.setInterestedIn(request.getInterestedIn());
        if (request.getJobTitle() != null) profile.setJobTitle(request.getJobTitle());
        if (request.getCompany() != null) profile.setCompany(request.getCompany());
        if (request.getUniversity() != null) profile.setUniversity(request.getUniversity());
        if (request.getEducationLevel() != null) profile.setEducationLevel(request.getEducationLevel());
        if (request.getReligiousBeliefs() != null) profile.setReligiousBeliefs(request.getReligiousBeliefs());
        if (request.getHometown() != null) profile.setHometown(request.getHometown());
        if (request.getPolitics() != null) profile.setPolitics(request.getPolitics());
        if (request.getLanguages() != null) profile.setLanguages(request.getLanguages());
        if (request.getDatingIntentions() != null) profile.setDatingIntentions(request.getDatingIntentions());
        if (request.getRelationshipType() != null) profile.setRelationshipType(request.getRelationshipType());
        if (request.getHeight() != null) profile.setHeight(request.getHeight());
        if (request.getEthnicity() != null) profile.setEthnicity(request.getEthnicity());
        if (request.getChildren() != null) profile.setChildren(request.getChildren());
        if (request.getFamilyPlans() != null) profile.setFamilyPlans(request.getFamilyPlans());
        if (request.getPets() != null) profile.setPets(request.getPets());
        if (request.getZodiacSign() != null) profile.setZodiacSign(request.getZodiacSign());
    }

    private List<EnhancedPhoto> convertToEnhancedPhotos(List<String> photoUrls) {
        if (photoUrls == null) return null;
        
        List<EnhancedPhoto> photos = new ArrayList<>();
        for (int i = 0; i < photoUrls.size(); i++) {
            EnhancedPhoto photo = new EnhancedPhoto();
            photo.setId(UUID.randomUUID().toString());
            photo.setUrl(photoUrls.get(i));
            photo.setPrimary(i == 0);
            photo.setOrder(i + 1);
            photos.add(photo);
        }
        return photos;
    }

    private List<WrittenPrompt> convertToWrittenPrompts(List<Map<String, String>> promptsData) {
        if (promptsData == null) return null;
        
        return promptsData.stream()
            .map(data -> new WrittenPrompt(data.get("prompt"), data.get("answer")))
            .collect(Collectors.toList());
    }

    private List<PollPrompt> convertToPollPrompts(List<Map<String, Object>> promptsData) {
        if (promptsData == null) return null;
        
        return promptsData.stream()
            .map(data -> {
                @SuppressWarnings("unchecked")
                List<String> options = (List<String>) data.get("options");
                return new PollPrompt(
                    (String) data.get("prompt"),
                    (String) data.get("question"),
                    options,
                    (String) data.get("selectedOption")
                );
            })
            .collect(Collectors.toList());
    }

    private void updateFieldVisibility(FieldVisibility visibility, Map<String, Boolean> data) {
        if (data.containsKey("jobTitle")) visibility.setJobTitle(data.get("jobTitle"));
        if (data.containsKey("company")) visibility.setCompany(data.get("company"));
        if (data.containsKey("university")) visibility.setUniversity(data.get("university"));
        if (data.containsKey("religiousBeliefs")) visibility.setReligiousBeliefs(data.get("religiousBeliefs"));
        if (data.containsKey("politics")) visibility.setPolitics(data.get("politics"));
        if (data.containsKey("hometown")) visibility.setHometown(data.get("hometown"));
        if (data.containsKey("height")) visibility.setHeight(data.get("height"));
        if (data.containsKey("ethnicity")) visibility.setEthnicity(data.get("ethnicity"));
    }

    private void updateUserPreferences(UserPreferences preferences, Map<String, Object> data) {
        if (data.containsKey("preferredGender")) preferences.setPreferredGender((String) data.get("preferredGender"));
        if (data.containsKey("minAge")) preferences.setMinAge(safeToInteger(data.get("minAge")));
        if (data.containsKey("maxAge")) preferences.setMaxAge(safeToInteger(data.get("maxAge")));
        if (data.containsKey("maxDistance")) preferences.setMaxDistance(safeToInteger(data.get("maxDistance")));
        if (data.containsKey("minHeight")) preferences.setMinHeight(safeToInteger(data.get("minHeight")));
        if (data.containsKey("maxHeight")) preferences.setMaxHeight(safeToInteger(data.get("maxHeight")));
        if (data.containsKey("datingIntention")) preferences.setDatingIntention((String) data.get("datingIntention"));
        if (data.containsKey("drinkingPreference")) preferences.setDrinkingPreference((String) data.get("drinkingPreference"));
        if (data.containsKey("smokingPreference")) preferences.setSmokingPreference((String) data.get("smokingPreference"));
        if (data.containsKey("drugPreference")) preferences.setDrugPreference((String) data.get("drugPreference"));
        if (data.containsKey("religionImportance")) preferences.setReligionImportance((String) data.get("religionImportance"));
        if (data.containsKey("wantsChildren")) preferences.setWantsChildren(safeToBoolean(data.get("wantsChildren")));
    }
    
    // Helper method to safely convert Long/Integer/String to Integer
    private Integer safeToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    // Helper method to safely convert Object to Boolean
    private boolean safeToBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    private void reorderPhotos(List<EnhancedPhoto> photos) {
        // Ensure there's always a primary photo
        boolean hasPrimary = photos.stream().anyMatch(EnhancedPhoto::isPrimary);
        if (!hasPrimary && !photos.isEmpty()) {
            photos.get(0).setPrimary(true);
        }

        // Reorder photos
        for (int i = 0; i < photos.size(); i++) {
            photos.get(i).setOrder(i + 1);
        }
    }

    private void validateProfile(CompleteUserProfile profile) {
        Set<ConstraintViolation<CompleteUserProfile>> violations = validator.validate(profile);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Profile validation failed: " + errorMessage);
        }

        // Additional business rule validations
        if (profile.getAge() < 18 || profile.getAge() > 100) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }

        if (profile.getPhotos() != null && (profile.getPhotos().size() < 1 || profile.getPhotos().size() > 6)) {
            throw new IllegalArgumentException("Profile must have between 1 and 6 photos");
        }
    }
    
    // Search users method for UserController
    public List<CompleteUserProfile> searchUsers(String userId, Integer minAge, Integer maxAge, 
            String location, List<String> interests, int page, int size, String sortBy) {
        // For now, return a basic search - in a real implementation you'd implement proper search logic
        try {
            // This is a simplified search using the sample data
            // Get all available profiles by checking known user IDs
            List<CompleteUserProfile> allProfiles = new ArrayList<>();
            
            // Add sample profiles from the repository
            CompleteUserProfile profile1 = userProfileRepository.findByUserId("1");
            CompleteUserProfile profile2 = userProfileRepository.findByUserId("2");
            CompleteUserProfile profile3 = userProfileRepository.findByUserId("user_123");
            
            if (profile1 != null) allProfiles.add(profile1);
            if (profile2 != null) allProfiles.add(profile2);
            if (profile3 != null) allProfiles.add(profile3);
            
            // Apply basic filtering
            return allProfiles.stream()
                .filter(profile -> !profile.getId().equals(userId)) // Don't include the searching user
                .filter(profile -> minAge == null || profile.getAge() >= minAge)
                .filter(profile -> maxAge == null || profile.getAge() <= maxAge)
                .filter(profile -> location == null || 
                    (profile.getLocation() != null && profile.getLocation().toLowerCase().contains(location.toLowerCase())))
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search users: " + e.getMessage());
        }
    }
    
    private void updatePhotoPrompts(CompleteUserProfile profile, Map<String, Map<String, String>> photoPromptsData) {
        if (photoPromptsData == null || photoPromptsData.isEmpty() || profile.getPhotos() == null) {
            return;
        }
        
        System.out.println("🔍 ProfileManagementService: Updating photo prompts for " + photoPromptsData.size() + " photos");
        
        List<EnhancedPhoto> photos = profile.getPhotos();
        
        // Update prompts for each photo
        for (Map.Entry<String, Map<String, String>> entry : photoPromptsData.entrySet()) {
            try {
                int photoIndex = Integer.parseInt(entry.getKey());
                Map<String, String> promptData = entry.getValue();
                
                if (photoIndex >= 0 && photoIndex < photos.size()) {
                    EnhancedPhoto photo = photos.get(photoIndex);
                    String promptText = promptData.get("prompt");
                    
                    if (promptText != null && !promptText.isEmpty()) {
                        // Create a simple photo prompt
                        PhotoPrompt photoPrompt = new PhotoPrompt();
                        photoPrompt.setId(java.util.UUID.randomUUID().toString());
                        photoPrompt.setText(promptText);
                        
                        // Set default position and style
                        PhotoPrompt.PhotoPosition position = new PhotoPrompt.PhotoPosition(0.5, 0.8); // Bottom center
                        PhotoPrompt.PhotoStyle style = new PhotoPrompt.PhotoStyle("#000000", "#FFFFFF", 14);
                        photoPrompt.setPosition(position);
                        photoPrompt.setStyle(style);
                        
                        // Replace existing prompts with new one
                        photo.setPrompts(List.of(photoPrompt));
                        
                        System.out.println("✅ Added photo prompt '" + promptText + "' to photo " + photoIndex);
                    } else {
                        // Clear prompts if text is empty
                        photo.setPrompts(List.of());
                        System.out.println("🗑️ Cleared photo prompts for photo " + photoIndex);
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid photo index: " + entry.getKey());
            }
        }
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile refreshPhotoUrls(String userId) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new RuntimeException("Profile not found for user: " + userId);
        }

        List<EnhancedPhoto> photos = profile.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            int refreshedCount = 0;
            int skippedCount = 0;
            
            for (EnhancedPhoto photo : photos) {
                if (photo.getUrl() != null) {
                    // Smart expiration check - only refresh if URL expires within 2 hours
                    if (isUrlExpiringWithinHours(photo.getUrl(), 2)) {
                        logger.debug("🔄 URL expiring soon, refreshing: {}", photo.getUrl().substring(0, Math.min(100, photo.getUrl().length())));
                        String newUrl = cloudStorageService.refreshSignedUrl(photo.getUrl());
                        if (newUrl != null) {
                            photo.setUrl(newUrl);
                            refreshedCount++;
                        }
                    } else {
                        logger.debug("⏭️ URL still valid, skipping refresh: {}", photo.getUrl().substring(0, Math.min(100, photo.getUrl().length())));
                        skippedCount++;
                    }
                }
            }
            
            if (refreshedCount > 0) {
                // Save the updated profile with new URLs
                CompleteUserProfile updatedProfile = userProfileRepository.save(profile);
                logger.info("✅ Smart refresh completed for user {}: {} refreshed, {} skipped (still valid)", 
                           userId, refreshedCount, skippedCount);
                return updatedProfile;
            } else {
                logger.info("ℹ️ No photo URLs needed refresh for user {}: {} URLs still valid", userId, skippedCount);
            }
        }
        
        return profile;
    }
    
    /**
     * Check if a Firebase Storage signed URL is expiring within the specified hours
     */
    private boolean isUrlExpiringWithinHours(String signedUrl, int hours) {
        try {
            // Extract expiration timestamp from Firebase Storage URL
            if (signedUrl.contains("Expires=")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Expires=(\\d+)");
                java.util.regex.Matcher matcher = pattern.matcher(signedUrl);
                if (matcher.find()) {
                    long expiresTimestamp = Long.parseLong(matcher.group(1));
                    long expiresMillis = expiresTimestamp * 1000L; // Convert to milliseconds
                    long now = System.currentTimeMillis();
                    long hoursInMillis = hours * 60 * 60 * 1000L;
                    
                    // Check if URL expires within the specified hours
                    boolean isExpiring = (expiresMillis - now) <= hoursInMillis;
                    logger.debug("🕐 URL expiration check: expires in {} hours, threshold {} hours, needs refresh: {}", 
                               (expiresMillis - now) / (60 * 60 * 1000L), hours, isExpiring);
                    return isExpiring;
                }
            }
            
            // If we can't parse expiration, assume it needs refresh to be safe
            logger.warn("⚠️ Could not parse expiration from URL, defaulting to refresh");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error checking URL expiration: {}", e.getMessage());
            // If error parsing, refresh to be safe
            return true;
        }
    }

}