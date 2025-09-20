package com.tpg.connect.services;

import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.api.ProfileUpdateRequest;
import com.tpg.connect.model.dto.UpdateProfileRequest;
import com.tpg.connect.model.user.*;
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

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private Validator validator;

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
        if (request.getBio() != null) existingProfile.setBio(request.getBio());
        if (request.getLocation() != null) existingProfile.setLocation(request.getLocation());
        if (request.getInterests() != null) existingProfile.setInterests(request.getInterests());

        // Update profile details
        UserProfile profile = existingProfile.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            existingProfile.setProfile(profile);
        }

        updateUserProfileFields(profile, request);

        // Update photos
        if (request.getPhotos() != null) {
            existingProfile.setPhotos(convertToPhotos(request.getPhotos()));
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

        return userProfileRepository.save(existingProfile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile updateBasicInfo(String userId, UpdateProfileRequest request) {
        CompleteUserProfile existingProfile = userProfileRepository.findByUserId(userId);
        
        if (existingProfile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        if (request.getName() != null) existingProfile.setName(request.getName());
        if (request.getBio() != null) existingProfile.setBio(request.getBio());
        if (request.getLocation() != null) existingProfile.setLocation(request.getLocation());
        if (request.getInterests() != null) existingProfile.setInterests(request.getInterests());
        if (request.getLanguages() != null) {
            UserProfile profile = getOrCreateUserProfile(existingProfile);
            profile.setLanguages(request.getLanguages());
        }
        if (request.getJobTitle() != null) {
            UserProfile profile = getOrCreateUserProfile(existingProfile);
            profile.setJobTitle(request.getJobTitle());
        }
        if (request.getCompany() != null) {
            UserProfile profile = getOrCreateUserProfile(existingProfile);
            profile.setCompany(request.getCompany());
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

        List<Photo> photos = profile.getPhotos();
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

        Photo newPhoto = new Photo();
        newPhoto.setId(UUID.randomUUID().toString());
        newPhoto.setUrl(photoUrl);
        newPhoto.setPrimary(isPrimary || photos.isEmpty()); // First photo is always primary
        newPhoto.setOrder(photos.size() + 1);

        photos.add(newPhoto);

        validateProfile(profile);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
    }

    @CacheEvict(value = "userProfiles", key = "'user_profile_' + #userId")
    public CompleteUserProfile removePhoto(String userId, String photoId) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }

        List<Photo> photos = profile.getPhotos();
        if (photos == null || photos.isEmpty()) {
            throw new IllegalArgumentException("No photos to remove");
        }

        if (photos.size() == 1) {
            throw new IllegalArgumentException("Cannot remove the last photo. Profile must have at least one photo.");
        }

        boolean removed = photos.removeIf(photo -> photo.getId().equals(photoId));
        
        if (!removed) {
            throw new IllegalArgumentException("Photo not found with ID: " + photoId);
        }

        // Reorder photos and ensure there's always a primary photo
        reorderPhotos(photos);

        profile.setUpdatedAt(LocalDateTime.now());
        profile.setVersion(profile.getVersion() + 1);

        return userProfileRepository.save(profile);
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

        List<Photo> photos = new ArrayList<>();
        for (int i = 0; i < photoUrls.size(); i++) {
            Photo photo = new Photo();
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

        return userProfileRepository.save(profile);
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

    private UserProfile getOrCreateUserProfile(CompleteUserProfile completeProfile) {
        UserProfile profile = completeProfile.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            completeProfile.setProfile(profile);
        }
        return profile;
    }

    private void updateUserProfileFields(UserProfile profile, ProfileUpdateRequest request) {
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

    private List<Photo> convertToPhotos(List<String> photoUrls) {
        if (photoUrls == null) return null;
        
        List<Photo> photos = new ArrayList<>();
        for (int i = 0; i < photoUrls.size(); i++) {
            Photo photo = new Photo();
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
                    options
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
        if (data.containsKey("minAge")) preferences.setMinAge((Integer) data.get("minAge"));
        if (data.containsKey("maxAge")) preferences.setMaxAge((Integer) data.get("maxAge"));
        if (data.containsKey("minHeight")) preferences.setMinHeight((Integer) data.get("minHeight"));
        if (data.containsKey("maxHeight")) preferences.setMaxHeight((Integer) data.get("maxHeight"));
        if (data.containsKey("datingIntention")) preferences.setDatingIntention((String) data.get("datingIntention"));
        if (data.containsKey("drinkingPreference")) preferences.setDrinkingPreference((String) data.get("drinkingPreference"));
        if (data.containsKey("smokingPreference")) preferences.setSmokingPreference((String) data.get("smokingPreference"));
    }

    private void reorderPhotos(List<Photo> photos) {
        // Ensure there's always a primary photo
        boolean hasPrimary = photos.stream().anyMatch(Photo::isPrimary);
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
}