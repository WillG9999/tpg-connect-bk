package com.tpg.connect.repository.impl;

import com.tpg.connect.model.UserProfile;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.repository.UserProfileRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private static final String COLLECTION_NAME = "user_profiles";
    
    @Autowired
    private Firestore firestore;

    @Override
    public List<CompleteUserProfile> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToCompleteUserProfile)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find all user profiles", e);
        }
    }

    @Override
    public UserProfile createProfile(UserProfile profile) {
        try {
            profile.setCreatedAt(Timestamp.now());
            profile.setUpdatedAt(Timestamp.now());
            profile.setActive(true);
            profile.setVersion(1);
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(profile.getConnectId());
            docRef.set(convertToMap(profile)).get();
            
            return profile;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create profile", e);
        }
    }

    @Override
    public Optional<UserProfile> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToProfile(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find profile by connectId", e);
        }
    }

    @Override
    public List<UserProfile> findActiveProfiles() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("active", true)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToProfile)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active profiles", e);
        }
    }

    @Override
    public boolean existsByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
            return doc.exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if profile exists", e);
        }
    }

    @Override
    public UserProfile updateProfile(UserProfile profile) {
        try {
            profile.setUpdatedAt(Timestamp.now());
            profile.setVersion(profile.getVersion() != null ? profile.getVersion() + 1 : 1);
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(profile.getConnectId());
            docRef.set(convertToMap(profile)).get();
            
            return profile;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update profile", e);
        }
    }

    @Override
    public UserProfile updateBasicInfo(String connectId, String firstName, String lastName, Integer age, String location) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (firstName != null) updates.put("firstName", firstName);
            if (lastName != null) updates.put("lastName", lastName);
            if (firstName != null && lastName != null) {
                updates.put("name", firstName + " " + lastName);
            }
            if (age != null) updates.put("age", age);
            if (location != null) updates.put("location", location);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update basic info", e);
        }
    }

    @Override
    public UserProfile updateEmailVerification(String connectId, boolean verified, Timestamp verifiedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("emailVerified", verified);
            updates.put("emailVerifiedAt", verifiedAt);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update email verification", e);
        }
    }

    @Override
    public UserProfile addPhoto(String connectId, UserProfile.Photo photo) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("photos", FieldValue.arrayUnion(convertPhotoToMap(photo)));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add photo", e);
        }
    }

    @Override
    public UserProfile removePhoto(String connectId, String photoId) {
        try {
            Optional<UserProfile> profileOpt = findByConnectId(connectId);
            if (!profileOpt.isPresent()) {
                throw new RuntimeException("Profile not found");
            }
            
            UserProfile profile = profileOpt.get();
            if (profile.getPhotos() != null) {
                List<UserProfile.Photo> updatedPhotos = profile.getPhotos().stream()
                        .filter(photo -> !photoId.equals(photo.getId()))
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("photos", updatedPhotos.stream()
                        .map(this::convertPhotoToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to remove photo", e);
        }
    }

    @Override
    public UserProfile updatePhotoOrder(String connectId, List<UserProfile.Photo> photos) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("photos", photos.stream()
                    .map(this::convertPhotoToMap)
                    .collect(Collectors.toList()));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update photo order", e);
        }
    }

    @Override
    public UserProfile updateInterests(String connectId, List<String> interests) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("interests", interests);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update interests", e);
        }
    }

    @Override
    public UserProfile updateDetailedProfile(String connectId, UserProfile.Profile profile) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("profile", convertDetailedProfileToMap(profile));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update detailed profile", e);
        }
    }

    @Override
    public UserProfile updateWrittenPrompts(String connectId, List<UserProfile.WrittenPrompt> prompts) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("writtenPrompts", prompts.stream()
                    .map(this::convertWrittenPromptToMap)
                    .collect(Collectors.toList()));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update written prompts", e);
        }
    }

    @Override
    public UserProfile updatePollPrompts(String connectId, List<UserProfile.PollPrompt> prompts) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("pollPrompts", prompts.stream()
                    .map(this::convertPollPromptToMap)
                    .collect(Collectors.toList()));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update poll prompts", e);
        }
    }

    @Override
    public UserProfile updateFieldVisibility(String connectId, UserProfile.FieldVisibility visibility) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("fieldVisibility", convertFieldVisibilityToMap(visibility));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update field visibility", e);
        }
    }

    @Override
    public UserProfile updatePreferences(String connectId, UserProfile.Preferences preferences) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("preferences", convertPreferencesToMap(preferences));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update preferences", e);
        }
    }

    @Override
    public UserProfile updateNotificationSettings(String connectId, UserProfile.NotificationSettings settings) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("notificationSettings", convertNotificationSettingsToMap(settings));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update notification settings", e);
        }
    }

    @Override
    public UserProfile updateLastActive(String connectId, Timestamp lastActive) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastActive", lastActive);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last active", e);
        }
    }

    @Override
    public void deactivateProfile(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("active", false);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to deactivate profile", e);
        }
    }

    @Override
    public void deleteProfile(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete profile", e);
        }
    }

    @Override
    public List<UserProfile> findProfilesByConnectIds(List<String> connectIds) {
        try {
            List<UserProfile> profiles = new ArrayList<>();
            
            List<List<String>> batches = new ArrayList<>();
            for (int i = 0; i < connectIds.size(); i += 10) {
                batches.add(connectIds.subList(i, Math.min(i + 10, connectIds.size())));
            }
            
            for (List<String> batch : batches) {
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .get();
                        
                profiles.addAll(querySnapshot.getDocuments().stream()
                        .map(this::convertToProfile)
                        .collect(Collectors.toList()));
            }
            
            return profiles;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find profiles by connectIds", e);
        }
    }

    @Override
    public Map<String, UserProfile> findProfileMapByConnectIds(List<String> connectIds) {
        List<UserProfile> profiles = findProfilesByConnectIds(connectIds);
        return profiles.stream()
                .collect(Collectors.toMap(UserProfile::getConnectId, profile -> profile));
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(UserProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", profile.getConnectId());
        map.put("userId", profile.getUserId());
        map.put("firstName", profile.getFirstName());
        map.put("lastName", profile.getLastName());
        map.put("name", profile.getName());
        map.put("age", profile.getAge());
        map.put("dateOfBirth", profile.getDateOfBirth());
        map.put("location", profile.getLocation());
        map.put("emailVerified", profile.getEmailVerified());
        map.put("emailVerifiedAt", profile.getEmailVerifiedAt());
        map.put("active", profile.getActive());
        map.put("createdAt", profile.getCreatedAt());
        map.put("updatedAt", profile.getUpdatedAt());
        map.put("lastActive", profile.getLastActive());
        map.put("version", profile.getVersion());
        
        if (profile.getPhotos() != null) {
            map.put("photos", profile.getPhotos().stream()
                    .map(this::convertPhotoToMap)
                    .collect(Collectors.toList()));
        }
        
        if (profile.getInterests() != null) {
            map.put("interests", profile.getInterests());
        }
        
        if (profile.getProfile() != null) {
            map.put("profile", convertDetailedProfileToMap(profile.getProfile()));
        }
        
        if (profile.getWrittenPrompts() != null) {
            map.put("writtenPrompts", profile.getWrittenPrompts().stream()
                    .map(this::convertWrittenPromptToMap)
                    .collect(Collectors.toList()));
        }
        
        if (profile.getPollPrompts() != null) {
            map.put("pollPrompts", profile.getPollPrompts().stream()
                    .map(this::convertPollPromptToMap)
                    .collect(Collectors.toList()));
        }
        
        if (profile.getFieldVisibility() != null) {
            map.put("fieldVisibility", convertFieldVisibilityToMap(profile.getFieldVisibility()));
        }
        
        if (profile.getPreferences() != null) {
            map.put("preferences", convertPreferencesToMap(profile.getPreferences()));
        }
        
        if (profile.getNotificationSettings() != null) {
            map.put("notificationSettings", convertNotificationSettingsToMap(profile.getNotificationSettings()));
        }
        
        return map;
    }

    private UserProfile convertToProfile(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return UserProfile.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .firstName((String) data.get("firstName"))
                .lastName((String) data.get("lastName"))
                .name((String) data.get("name"))
                .age((Integer) data.get("age"))
                .dateOfBirth((Timestamp) data.get("dateOfBirth"))
                .location((String) data.get("location"))
                .emailVerified((Boolean) data.get("emailVerified"))
                .emailVerifiedAt((Timestamp) data.get("emailVerifiedAt"))
                .photos(convertToPhotoList((List<Map<String, Object>>) data.get("photos")))
                .interests((List<String>) data.get("interests"))
                .profile(convertToDetailedProfile((Map<String, Object>) data.get("profile")))
                .writtenPrompts(convertToWrittenPromptList((List<Map<String, Object>>) data.get("writtenPrompts")))
                .pollPrompts(convertToPollPromptList((List<Map<String, Object>>) data.get("pollPrompts")))
                .fieldVisibility(convertToFieldVisibility((Map<String, Object>) data.get("fieldVisibility")))
                .preferences(convertToPreferences((Map<String, Object>) data.get("preferences")))
                .notificationSettings(convertToNotificationSettings((Map<String, Object>) data.get("notificationSettings")))
                .active((Boolean) data.get("active"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .lastActive((Timestamp) data.get("lastActive"))
                .version((Integer) data.get("version"))
                .build();
    }

    private Map<String, Object> convertPhotoToMap(UserProfile.Photo photo) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", photo.getId());
        map.put("url", photo.getUrl());
        map.put("isPrimary", photo.getIsPrimary());
        map.put("order", photo.getOrder());
        if (photo.getPrompts() != null) {
            map.put("prompts", photo.getPrompts().stream()
                    .map(this::convertPhotoPromptToMap)
                    .collect(Collectors.toList()));
        }
        return map;
    }

    private UserProfile.Photo convertToPhoto(Map<String, Object> photoMap) {
        if (photoMap == null) return null;
        
        return UserProfile.Photo.builder()
                .id((String) photoMap.get("id"))
                .url((String) photoMap.get("url"))
                .isPrimary((Boolean) photoMap.get("isPrimary"))
                .order((Integer) photoMap.get("order"))
                .prompts(convertToPhotoPromptList((List<Map<String, Object>>) photoMap.get("prompts")))
                .build();
    }

    private List<UserProfile.Photo> convertToPhotoList(List<Map<String, Object>> photoMaps) {
        if (photoMaps == null) return new ArrayList<>();
        
        return photoMaps.stream()
                .map(this::convertToPhoto)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertPhotoPromptToMap(UserProfile.PhotoPrompt prompt) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", prompt.getId());
        map.put("text", prompt.getText());
        if (prompt.getPosition() != null) {
            Map<String, Object> positionMap = new HashMap<>();
            positionMap.put("x", prompt.getPosition().getX());
            positionMap.put("y", prompt.getPosition().getY());
            map.put("position", positionMap);
        }
        if (prompt.getStyle() != null) {
            Map<String, Object> styleMap = new HashMap<>();
            styleMap.put("backgroundColor", prompt.getStyle().getBackgroundColor());
            styleMap.put("textColor", prompt.getStyle().getTextColor());
            styleMap.put("fontSize", prompt.getStyle().getFontSize());
            map.put("style", styleMap);
        }
        return map;
    }

    private UserProfile.PhotoPrompt convertToPhotoPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        UserProfile.Position position = null;
        Map<String, Object> positionMap = (Map<String, Object>) promptMap.get("position");
        if (positionMap != null) {
            position = UserProfile.Position.builder()
                    .x((Double) positionMap.get("x"))
                    .y((Double) positionMap.get("y"))
                    .build();
        }
        
        UserProfile.Style style = null;
        Map<String, Object> styleMap = (Map<String, Object>) promptMap.get("style");
        if (styleMap != null) {
            style = UserProfile.Style.builder()
                    .backgroundColor((String) styleMap.get("backgroundColor"))
                    .textColor((String) styleMap.get("textColor"))
                    .fontSize((Integer) styleMap.get("fontSize"))
                    .build();
        }
        
        return UserProfile.PhotoPrompt.builder()
                .id((String) promptMap.get("id"))
                .text((String) promptMap.get("text"))
                .position(position)
                .style(style)
                .build();
    }

    private List<UserProfile.PhotoPrompt> convertToPhotoPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return new ArrayList<>();
        
        return promptMaps.stream()
                .map(this::convertToPhotoPrompt)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertDetailedProfileToMap(UserProfile.Profile profile) {
        if (profile == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("pronouns", profile.getPronouns());
        map.put("gender", profile.getGender());
        map.put("sexuality", profile.getSexuality());
        map.put("interestedIn", profile.getInterestedIn());
        map.put("jobTitle", profile.getJobTitle());
        map.put("company", profile.getCompany());
        map.put("university", profile.getUniversity());
        map.put("educationLevel", profile.getEducationLevel());
        map.put("religiousBeliefs", profile.getReligiousBeliefs());
        map.put("hometown", profile.getHometown());
        map.put("politics", profile.getPolitics());
        map.put("languages", profile.getLanguages());
        map.put("datingIntentions", profile.getDatingIntentions());
        map.put("relationshipType", profile.getRelationshipType());
        map.put("height", profile.getHeight());
        map.put("ethnicity", profile.getEthnicity());
        map.put("children", profile.getChildren());
        map.put("familyPlans", profile.getFamilyPlans());
        map.put("pets", profile.getPets());
        map.put("zodiacSign", profile.getZodiacSign());
        return map;
    }

    private UserProfile.Profile convertToDetailedProfile(Map<String, Object> profileMap) {
        if (profileMap == null) return null;
        
        return UserProfile.Profile.builder()
                .pronouns((String) profileMap.get("pronouns"))
                .gender((String) profileMap.get("gender"))
                .sexuality((String) profileMap.get("sexuality"))
                .interestedIn((String) profileMap.get("interestedIn"))
                .jobTitle((String) profileMap.get("jobTitle"))
                .company((String) profileMap.get("company"))
                .university((String) profileMap.get("university"))
                .educationLevel((String) profileMap.get("educationLevel"))
                .religiousBeliefs((String) profileMap.get("religiousBeliefs"))
                .hometown((String) profileMap.get("hometown"))
                .politics((String) profileMap.get("politics"))
                .languages((List<String>) profileMap.get("languages"))
                .datingIntentions((String) profileMap.get("datingIntentions"))
                .relationshipType((String) profileMap.get("relationshipType"))
                .height((String) profileMap.get("height"))
                .ethnicity((String) profileMap.get("ethnicity"))
                .children((String) profileMap.get("children"))
                .familyPlans((String) profileMap.get("familyPlans"))
                .pets((String) profileMap.get("pets"))
                .zodiacSign((String) profileMap.get("zodiacSign"))
                .build();
    }

    private Map<String, Object> convertWrittenPromptToMap(UserProfile.WrittenPrompt prompt) {
        Map<String, Object> map = new HashMap<>();
        map.put("question", prompt.getQuestion());
        map.put("answer", prompt.getAnswer());
        return map;
    }

    private UserProfile.WrittenPrompt convertToWrittenPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        return UserProfile.WrittenPrompt.builder()
                .question((String) promptMap.get("question"))
                .answer((String) promptMap.get("answer"))
                .build();
    }

    private List<UserProfile.WrittenPrompt> convertToWrittenPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return new ArrayList<>();
        
        return promptMaps.stream()
                .map(this::convertToWrittenPrompt)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertPollPromptToMap(UserProfile.PollPrompt prompt) {
        Map<String, Object> map = new HashMap<>();
        map.put("question", prompt.getQuestion());
        map.put("description", prompt.getDescription());
        map.put("options", prompt.getOptions());
        map.put("selectedOption", prompt.getSelectedOption());
        return map;
    }

    private UserProfile.PollPrompt convertToPollPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        return UserProfile.PollPrompt.builder()
                .question((String) promptMap.get("question"))
                .description((String) promptMap.get("description"))
                .options((List<String>) promptMap.get("options"))
                .selectedOption((String) promptMap.get("selectedOption"))
                .build();
    }

    private List<UserProfile.PollPrompt> convertToPollPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return new ArrayList<>();
        
        return promptMaps.stream()
                .map(this::convertToPollPrompt)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertFieldVisibilityToMap(UserProfile.FieldVisibility visibility) {
        if (visibility == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("jobTitle", visibility.getJobTitle());
        map.put("university", visibility.getUniversity());
        map.put("religiousBeliefs", visibility.getReligiousBeliefs());
        map.put("politics", visibility.getPolitics());
        map.put("height", visibility.getHeight());
        map.put("ethnicity", visibility.getEthnicity());
        return map;
    }

    private UserProfile.FieldVisibility convertToFieldVisibility(Map<String, Object> visibilityMap) {
        if (visibilityMap == null) return null;
        
        return UserProfile.FieldVisibility.builder()
                .jobTitle((Boolean) visibilityMap.get("jobTitle"))
                .university((Boolean) visibilityMap.get("university"))
                .religiousBeliefs((Boolean) visibilityMap.get("religiousBeliefs"))
                .politics((Boolean) visibilityMap.get("politics"))
                .height((Boolean) visibilityMap.get("height"))
                .ethnicity((Boolean) visibilityMap.get("ethnicity"))
                .build();
    }

    private Map<String, Object> convertPreferencesToMap(UserProfile.Preferences preferences) {
        if (preferences == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("preferredGender", preferences.getPreferredGender());
        map.put("minAge", preferences.getMinAge());
        map.put("maxAge", preferences.getMaxAge());
        map.put("maxDistance", preferences.getMaxDistance());
        map.put("minHeight", preferences.getMinHeight());
        map.put("maxHeight", preferences.getMaxHeight());
        map.put("datingIntention", preferences.getDatingIntention());
        map.put("drinkingPreference", preferences.getDrinkingPreference());
        map.put("smokingPreference", preferences.getSmokingPreference());
        map.put("religionImportance", preferences.getReligionImportance());
        map.put("wantsChildren", preferences.getWantsChildren());
        return map;
    }

    private UserProfile.Preferences convertToPreferences(Map<String, Object> preferencesMap) {
        if (preferencesMap == null) return null;
        
        return UserProfile.Preferences.builder()
                .preferredGender((String) preferencesMap.get("preferredGender"))
                .minAge((Integer) preferencesMap.get("minAge"))
                .maxAge((Integer) preferencesMap.get("maxAge"))
                .maxDistance((Integer) preferencesMap.get("maxDistance"))
                .minHeight((Integer) preferencesMap.get("minHeight"))
                .maxHeight((Integer) preferencesMap.get("maxHeight"))
                .datingIntention((String) preferencesMap.get("datingIntention"))
                .drinkingPreference((String) preferencesMap.get("drinkingPreference"))
                .smokingPreference((String) preferencesMap.get("smokingPreference"))
                .religionImportance((String) preferencesMap.get("religionImportance"))
                .wantsChildren((Boolean) preferencesMap.get("wantsChildren"))
                .build();
    }

    private Map<String, Object> convertNotificationSettingsToMap(UserProfile.NotificationSettings settings) {
        if (settings == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("pushEnabled", settings.getPushEnabled());
        map.put("newMatches", settings.getNewMatches());
        map.put("messages", settings.getMessages());
        map.put("profileViews", settings.getProfileViews());
        map.put("matchReminders", settings.getMatchReminders());
        map.put("marketing", settings.getMarketing());
        map.put("safety", settings.getSafety());
        map.put("systemUpdates", settings.getSystemUpdates());
        
        if (settings.getQuietHours() != null) {
            Map<String, Object> quietHoursMap = new HashMap<>();
            quietHoursMap.put("enabled", settings.getQuietHours().getEnabled());
            quietHoursMap.put("start", settings.getQuietHours().getStart());
            quietHoursMap.put("end", settings.getQuietHours().getEnd());
            map.put("quietHours", quietHoursMap);
        }
        
        return map;
    }

    private UserProfile.NotificationSettings convertToNotificationSettings(Map<String, Object> settingsMap) {
        if (settingsMap == null) return null;
        
        UserProfile.QuietHours quietHours = null;
        Map<String, Object> quietHoursMap = (Map<String, Object>) settingsMap.get("quietHours");
        if (quietHoursMap != null) {
            quietHours = UserProfile.QuietHours.builder()
                    .enabled((Boolean) quietHoursMap.get("enabled"))
                    .start((String) quietHoursMap.get("start"))
                    .end((String) quietHoursMap.get("end"))
                    .build();
        }
        
        return UserProfile.NotificationSettings.builder()
                .pushEnabled((Boolean) settingsMap.get("pushEnabled"))
                .newMatches((Boolean) settingsMap.get("newMatches"))
                .messages((Boolean) settingsMap.get("messages"))
                .profileViews((Boolean) settingsMap.get("profileViews"))
                .matchReminders((Boolean) settingsMap.get("matchReminders"))
                .marketing((Boolean) settingsMap.get("marketing"))
                .safety((Boolean) settingsMap.get("safety"))
                .systemUpdates((Boolean) settingsMap.get("systemUpdates"))
                .quietHours(quietHours)
                .build();
    }

    @Override
    public CompleteUserProfile save(CompleteUserProfile profile) {
        try {
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(java.time.LocalDateTime.now());
            }
            profile.setUpdatedAt(java.time.LocalDateTime.now());
            
            DocumentReference docRef = firestore.collection("users").document(profile.getUserId());
            docRef.set(convertCompleteUserProfileToMap(profile)).get();
            
            return profile;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save complete user profile", e);
        }
    }

    @Override
    public CompleteUserProfile findByUserId(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .get();
                    
            return doc.exists() ? convertToCompleteUserProfile(doc) : null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find complete user profile by userId", e);
        }
    }

    private Map<String, Object> convertCompleteUserProfileToMap(CompleteUserProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", profile.getId());
        map.put("userId", profile.getUserId());
        map.put("name", profile.getName());
        map.put("firstName", profile.getFirstName());
        map.put("lastName", profile.getLastName());
        map.put("age", profile.getAge());
        map.put("bio", profile.getBio());
        map.put("location", profile.getLocation());
        map.put("interests", profile.getInterests());
        map.put("dateOfBirth", profile.getDateOfBirth());
        map.put("gender", profile.getGender());
        map.put("active", profile.isActive());
        map.put("jobTitle", profile.getJobTitle());
        map.put("university", profile.getUniversity());
        map.put("createdAt", profile.getCreatedAt());
        map.put("updatedAt", profile.getUpdatedAt());
        map.put("lastActive", profile.getLastActive());
        map.put("version", profile.getVersion());
        return map;
    }

    private CompleteUserProfile convertToCompleteUserProfile(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setId(doc.getId());
        profile.setUserId((String) data.get("userId"));
        profile.setName((String) data.get("name"));
        profile.setFirstName((String) data.get("firstName"));
        profile.setLastName((String) data.get("lastName"));
        profile.setAge(data.get("age") != null ? ((Number) data.get("age")).intValue() : 0);
        profile.setBio((String) data.get("bio"));
        profile.setLocation((String) data.get("location"));
        profile.setInterests((List<String>) data.get("interests"));
        if (data.get("dateOfBirth") != null) {
            profile.setDateOfBirth(((java.sql.Date) data.get("dateOfBirth")).toLocalDate());
        }
        profile.setGender((String) data.get("gender"));
        profile.setActive(data.get("active") != null ? (Boolean) data.get("active") : true);
        profile.setJobTitle((String) data.get("jobTitle"));
        profile.setUniversity((String) data.get("university"));
        if (data.get("createdAt") != null) {
            profile.setCreatedAt((java.time.LocalDateTime) data.get("createdAt"));
        }
        if (data.get("updatedAt") != null) {
            profile.setUpdatedAt((java.time.LocalDateTime) data.get("updatedAt"));
        }
        if (data.get("lastActive") != null) {
            profile.setLastActive((java.time.LocalDateTime) data.get("lastActive"));
        }
        profile.setVersion(data.get("version") != null ? ((Number) data.get("version")).intValue() : 1);
        return profile;
    }
}