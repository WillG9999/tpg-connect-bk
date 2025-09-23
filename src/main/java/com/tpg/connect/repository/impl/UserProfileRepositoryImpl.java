package com.tpg.connect.repository.impl;

import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.DetailedProfile;
import com.tpg.connect.model.user.EnhancedPhoto;
import com.tpg.connect.model.user.WrittenPrompt;
import com.tpg.connect.model.user.PollPrompt;
import com.tpg.connect.model.user.FieldVisibility;
import com.tpg.connect.model.user.UserPreferences;
import com.tpg.connect.model.user.NotificationSettings;
import com.tpg.connect.model.user.PhotoPrompt;
import com.tpg.connect.repository.UserProfileRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private static final String COLLECTION_NAME = "user_profiles";
    
    @Autowired
    private Firestore firestore;

    @Override
    public CompleteUserProfile save(CompleteUserProfile profile) {
        try {
            if (profile.getCreatedAt() == null) {
                profile.setCreatedAt(LocalDateTime.now());
            }
            profile.setUpdatedAt(LocalDateTime.now());
            profile.setActive(true);
            if (profile.getVersion() == 0) {
                profile.setVersion(1);
            } else {
                profile.setVersion(profile.getVersion() + 1);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(profile.getConnectId());
            docRef.set(convertToMap(profile)).get();
            
            return profile;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save user profile", e);
        }
    }

    @Override
    public Optional<CompleteUserProfile> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToCompleteUserProfile(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find profile by connectId", e);
        }
    }

    @Override
    public CompleteUserProfile findByUserId(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(userId)
                    .get()
                    .get();
                    
            return doc.exists() ? convertToCompleteUserProfile(doc) : null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find profile by userId", e);
        }
    }

    @Override
    public List<CompleteUserProfile> findActiveProfiles() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("active", true)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToCompleteUserProfile)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active profiles", e);
        }
    }

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
    public CompleteUserProfile updateProfile(CompleteUserProfile profile) {
        return save(profile);
    }

    @Override
    public CompleteUserProfile updateBasicInfo(String connectId, String firstName, String lastName, Integer age, String location) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("firstName", firstName);
            updates.put("lastName", lastName);
            updates.put("age", age);
            updates.put("location", location);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update basic info", e);
        }
    }

    @Override
    public CompleteUserProfile updateEmailVerification(String connectId, boolean verified, Timestamp verifiedAt) {
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
    public CompleteUserProfile addPhoto(String connectId, EnhancedPhoto photo) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("photos", FieldValue.arrayUnion(convertEnhancedPhotoToMap(photo)));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add photo", e);
        }
    }

    @Override
    public CompleteUserProfile removePhoto(String connectId, String photoId) {
        try {
            CompleteUserProfile profile = findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
            
            if (profile.getPhotos() != null) {
                profile.getPhotos().removeIf(photo -> photoId.equals(photo.getId()));
                return save(profile);
            }
            
            return profile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove photo", e);
        }
    }

    @Override
    public CompleteUserProfile updatePhotoOrder(String connectId, List<EnhancedPhoto> photos) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("photos", photos.stream()
                    .map(this::convertEnhancedPhotoToMap)
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
    public CompleteUserProfile updateInterests(String connectId, List<String> interests) {
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
    public CompleteUserProfile updateDetailedProfile(String connectId, DetailedProfile profile) {
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
    public CompleteUserProfile updateWrittenPrompts(String connectId, List<WrittenPrompt> prompts) {
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
    public CompleteUserProfile updatePollPrompts(String connectId, List<PollPrompt> prompts) {
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
    public CompleteUserProfile updateFieldVisibility(String connectId, FieldVisibility visibility) {
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
    public CompleteUserProfile updatePreferences(String connectId, UserPreferences preferences) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("preferences", convertUserPreferencesToMap(preferences));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Profile not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update preferences", e);
        }
    }

    @Override
    public CompleteUserProfile updateNotificationSettings(String connectId, NotificationSettings settings) {
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
    public CompleteUserProfile updateLastActive(String connectId, Timestamp lastActive) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastActive", lastActive);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
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
    public List<CompleteUserProfile> findProfilesByConnectIds(List<String> connectIds) {
        try {
            List<CompleteUserProfile> profiles = new ArrayList<>();
            
            // Firestore has a limit of 10 documents per 'in' query
            List<List<String>> chunks = partitionList(connectIds, 10);
            
            for (List<String> chunk : chunks) {
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .get();
                        
                profiles.addAll(querySnapshot.getDocuments().stream()
                        .map(this::convertToCompleteUserProfile)
                        .collect(Collectors.toList()));
            }
            
            return profiles;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find profiles by connectIds", e);
        }
    }

    @Override
    public Map<String, CompleteUserProfile> findProfileMapByConnectIds(List<String> connectIds) {
        List<CompleteUserProfile> profiles = findProfilesByConnectIds(connectIds);
        return profiles.stream()
                .collect(Collectors.toMap(CompleteUserProfile::getConnectId, profile -> profile));
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(CompleteUserProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", profile.getConnectId());
        map.put("firstName", profile.getFirstName());
        map.put("lastName", profile.getLastName());
        map.put("gender", profile.getGender());
        map.put("email", profile.getEmail());
        map.put("location", profile.getLocation());
        map.put("dateOfBirth", profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : null);
        map.put("active", profile.getActive());
        map.put("isOnline", profile.getOnline());
        map.put("isPremium", profile.getPremium());
        map.put("isVerified", profile.getVerified());
        map.put("subscriptionType", profile.getSubscriptionType());
        map.put("interests", profile.getInterests());
        map.put("createdAt", profile.getCreatedAt());
        map.put("updatedAt", profile.getUpdatedAt());
        map.put("lastActive", profile.getLastActive());
        map.put("version", profile.getVersion());
        
        if (profile.getPhotos() != null) {
            map.put("photos", profile.getPhotos().stream()
                    .map(this::convertEnhancedPhotoToMap)
                    .collect(Collectors.toList()));
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
            map.put("preferences", convertUserPreferencesToMap(profile.getPreferences()));
        }
        
        if (profile.getNotifications() != null) {
            map.put("notifications", convertNotificationSettingsToMap(profile.getNotifications()));
        }
        
        return map;
    }

    private CompleteUserProfile convertToCompleteUserProfile(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setConnectId(doc.getId());
        profile.setFirstName((String) data.get("firstName"));
        profile.setLastName((String) data.get("lastName"));
        profile.setGender((String) data.get("gender"));
        profile.setEmail((String) data.get("email"));
        profile.setLocation((String) data.get("location"));
        profile.setActive(data.get("active") != null ? (Boolean) data.get("active") : true);
        profile.setOnline(data.get("isOnline") != null ? (Boolean) data.get("isOnline") : false);
        profile.setPremium(data.get("isPremium") != null ? (Boolean) data.get("isPremium") : false);
        profile.setVerified(data.get("isVerified") != null ? (Boolean) data.get("isVerified") : false);
        profile.setSubscriptionType((String) data.get("subscriptionType"));
        profile.setInterests((List<String>) data.get("interests"));
        Object versionObj = data.get("version");
        if (versionObj instanceof Long) {
            profile.setVersion(((Long) versionObj).intValue());
        } else if (versionObj instanceof Integer) {
            profile.setVersion((Integer) versionObj);
        } else {
            profile.setVersion(1); // Default version
        }
        
        if (data.get("dateOfBirth") != null) {
            Object dateOfBirth = data.get("dateOfBirth");
            if (dateOfBirth instanceof String) {
                profile.setDateOfBirth(LocalDate.parse((String) dateOfBirth));
            }
        }
        
        profile.setPhotos(convertToEnhancedPhotoList((List<Map<String, Object>>) data.get("photos")));
        profile.setProfile(convertToDetailedProfile((Map<String, Object>) data.get("profile")));
        profile.setWrittenPrompts(convertToWrittenPromptList((List<Map<String, Object>>) data.get("writtenPrompts")));
        profile.setPollPrompts(convertToPollPromptList((List<Map<String, Object>>) data.get("pollPrompts")));
        profile.setFieldVisibility(convertToFieldVisibility((Map<String, Object>) data.get("fieldVisibility")));
        profile.setPreferences(convertToUserPreferences((Map<String, Object>) data.get("preferences")));
        profile.setNotifications(convertToNotificationSettings((Map<String, Object>) data.get("notifications")));
        
        if (data.get("createdAt") != null) {
            Object createdAt = data.get("createdAt");
            if (createdAt instanceof Timestamp) {
                profile.setCreatedAt(((Timestamp) createdAt).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (createdAt instanceof Map) {
                profile.setCreatedAt(LocalDateTime.now());
            }
        }
        
        if (data.get("updatedAt") != null) {
            Object updatedAt = data.get("updatedAt");
            if (updatedAt instanceof Timestamp) {
                profile.setUpdatedAt(((Timestamp) updatedAt).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else if (updatedAt instanceof Map) {
                profile.setUpdatedAt(LocalDateTime.now());
            }
        }
        
        if (data.get("lastActive") != null) {
            Object lastActive = data.get("lastActive");
            if (lastActive instanceof Timestamp) {
                profile.setLastActive(((Timestamp) lastActive).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        }
        
        return profile;
    }

    // Enhanced Photo conversion methods
    private Map<String, Object> convertEnhancedPhotoToMap(EnhancedPhoto photo) {
        if (photo == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", photo.getId());
        map.put("url", photo.getUrl());
        map.put("isPrimary", photo.isPrimary());
        map.put("order", photo.getOrder());
        
        if (photo.getPrompts() != null) {
            map.put("prompts", photo.getPrompts().stream()
                    .map(this::convertPhotoPromptToMap)
                    .collect(Collectors.toList()));
        }
        
        return map;
    }

    private List<EnhancedPhoto> convertToEnhancedPhotoList(List<Map<String, Object>> photoMaps) {
        if (photoMaps == null) return null;
        
        return photoMaps.stream()
                .map(this::convertToEnhancedPhoto)
                .collect(Collectors.toList());
    }

    private EnhancedPhoto convertToEnhancedPhoto(Map<String, Object> photoMap) {
        if (photoMap == null) return null;
        
        EnhancedPhoto photo = new EnhancedPhoto();
        photo.setId((String) photoMap.get("id"));
        photo.setUrl((String) photoMap.get("url"));
        photo.setPrimary((Boolean) photoMap.get("isPrimary"));
        photo.setOrder((Integer) photoMap.get("order"));
        photo.setPrompts(convertToPhotoPromptList((List<Map<String, Object>>) photoMap.get("prompts")));
        
        return photo;
    }

    private Map<String, Object> convertPhotoPromptToMap(PhotoPrompt prompt) {
        if (prompt == null) return null;
        
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

    private List<PhotoPrompt> convertToPhotoPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return null;
        
        return promptMaps.stream()
                .map(this::convertToPhotoPrompt)
                .collect(Collectors.toList());
    }

    private PhotoPrompt convertToPhotoPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        PhotoPrompt prompt = new PhotoPrompt();
        prompt.setId((String) promptMap.get("id"));
        prompt.setText((String) promptMap.get("text"));
        
        Map<String, Object> positionMap = (Map<String, Object>) promptMap.get("position");
        if (positionMap != null) {
            PhotoPrompt.PhotoPosition position = new PhotoPrompt.PhotoPosition();
            position.setX((Double) positionMap.get("x"));
            position.setY((Double) positionMap.get("y"));
            prompt.setPosition(position);
        }
        
        Map<String, Object> styleMap = (Map<String, Object>) promptMap.get("style");
        if (styleMap != null) {
            PhotoPrompt.PhotoStyle style = new PhotoPrompt.PhotoStyle();
            style.setBackgroundColor((String) styleMap.get("backgroundColor"));
            style.setTextColor((String) styleMap.get("textColor"));
            style.setFontSize((Integer) styleMap.get("fontSize"));
            prompt.setStyle(style);
        }
        
        return prompt;
    }

    // Detailed Profile conversion methods
    private Map<String, Object> convertDetailedProfileToMap(DetailedProfile profile) {
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

    private DetailedProfile convertToDetailedProfile(Map<String, Object> profileMap) {
        if (profileMap == null) return null;
        
        DetailedProfile profile = new DetailedProfile();
        profile.setPronouns((String) profileMap.get("pronouns"));
        profile.setGender((String) profileMap.get("gender"));
        profile.setSexuality((String) profileMap.get("sexuality"));
        profile.setInterestedIn((String) profileMap.get("interestedIn"));
        profile.setJobTitle((String) profileMap.get("jobTitle"));
        profile.setCompany((String) profileMap.get("company"));
        profile.setUniversity((String) profileMap.get("university"));
        profile.setEducationLevel((String) profileMap.get("educationLevel"));
        profile.setReligiousBeliefs((String) profileMap.get("religiousBeliefs"));
        profile.setHometown((String) profileMap.get("hometown"));
        profile.setPolitics((String) profileMap.get("politics"));
        profile.setLanguages((List<String>) profileMap.get("languages"));
        profile.setDatingIntentions((String) profileMap.get("datingIntentions"));
        profile.setRelationshipType((String) profileMap.get("relationshipType"));
        profile.setHeight((String) profileMap.get("height"));
        profile.setEthnicity((String) profileMap.get("ethnicity"));
        profile.setChildren((String) profileMap.get("children"));
        profile.setFamilyPlans((String) profileMap.get("familyPlans"));
        profile.setPets((String) profileMap.get("pets"));
        profile.setZodiacSign((String) profileMap.get("zodiacSign"));
        
        return profile;
    }

    // Written Prompt conversion methods
    private Map<String, Object> convertWrittenPromptToMap(WrittenPrompt prompt) {
        if (prompt == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("question", prompt.getQuestion());
        map.put("answer", prompt.getAnswer());
        
        return map;
    }

    private List<WrittenPrompt> convertToWrittenPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return null;
        
        return promptMaps.stream()
                .map(this::convertToWrittenPrompt)
                .collect(Collectors.toList());
    }

    private WrittenPrompt convertToWrittenPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        WrittenPrompt prompt = new WrittenPrompt();
        prompt.setQuestion((String) promptMap.get("question"));
        prompt.setAnswer((String) promptMap.get("answer"));
        
        return prompt;
    }

    // Poll Prompt conversion methods
    private Map<String, Object> convertPollPromptToMap(PollPrompt prompt) {
        if (prompt == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("question", prompt.getQuestion());
        map.put("description", prompt.getDescription());
        map.put("options", prompt.getOptions());
        map.put("selectedOption", prompt.getSelectedOption());
        
        return map;
    }

    private List<PollPrompt> convertToPollPromptList(List<Map<String, Object>> promptMaps) {
        if (promptMaps == null) return null;
        
        return promptMaps.stream()
                .map(this::convertToPollPrompt)
                .collect(Collectors.toList());
    }

    private PollPrompt convertToPollPrompt(Map<String, Object> promptMap) {
        if (promptMap == null) return null;
        
        PollPrompt prompt = new PollPrompt();
        prompt.setQuestion((String) promptMap.get("question"));
        prompt.setDescription((String) promptMap.get("description"));
        prompt.setOptions((List<String>) promptMap.get("options"));
        prompt.setSelectedOption((String) promptMap.get("selectedOption"));
        
        return prompt;
    }

    // Field Visibility conversion methods
    private Map<String, Object> convertFieldVisibilityToMap(FieldVisibility visibility) {
        if (visibility == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("jobTitle", visibility.isJobTitle());
        map.put("company", visibility.isCompany());
        map.put("university", visibility.isUniversity());
        map.put("religiousBeliefs", visibility.isReligiousBeliefs());
        map.put("politics", visibility.isPolitics());
        map.put("height", visibility.isHeight());
        map.put("ethnicity", visibility.isEthnicity());
        map.put("hometown", visibility.isHometown());
        
        return map;
    }

    private FieldVisibility convertToFieldVisibility(Map<String, Object> visibilityMap) {
        if (visibilityMap == null) return null;
        
        FieldVisibility visibility = new FieldVisibility();
        visibility.setJobTitle((Boolean) visibilityMap.get("jobTitle"));
        visibility.setCompany((Boolean) visibilityMap.get("company"));
        visibility.setUniversity((Boolean) visibilityMap.get("university"));
        visibility.setReligiousBeliefs((Boolean) visibilityMap.get("religiousBeliefs"));
        visibility.setPolitics((Boolean) visibilityMap.get("politics"));
        visibility.setHeight((Boolean) visibilityMap.get("height"));
        visibility.setEthnicity((Boolean) visibilityMap.get("ethnicity"));
        visibility.setHometown((Boolean) visibilityMap.get("hometown"));
        
        return visibility;
    }

    // User Preferences conversion methods
    private Map<String, Object> convertUserPreferencesToMap(UserPreferences preferences) {
        if (preferences == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("preferredGender", preferences.getPreferredGender());
        map.put("maxDistance", preferences.getMaxDistance());
        map.put("datingIntentions", preferences.getDatingIntention());
        map.put("dealBreakers", preferences.getDealBreakers());
        map.put("mustHaves", preferences.getMustHaves());
        
        if (preferences.getAgeRange() != null) {
            Map<String, Object> ageRange = new HashMap<>();
            ageRange.put("min", preferences.getAgeRange().getMin());
            ageRange.put("max", preferences.getAgeRange().getMax());
            map.put("ageRange", ageRange);
        }
        
        if (preferences.getHeightRange() != null) {
            Map<String, Object> heightRange = new HashMap<>();
            heightRange.put("min", preferences.getHeightRange().getMin());
            heightRange.put("max", preferences.getHeightRange().getMax());
            map.put("heightRange", heightRange);
        }
        
        return map;
    }

    private UserPreferences convertToUserPreferences(Map<String, Object> preferencesMap) {
        if (preferencesMap == null) return null;
        
        UserPreferences preferences = new UserPreferences();
        preferences.setPreferredGender((String) preferencesMap.get("preferredGender"));
        preferences.setMaxDistance((Integer) preferencesMap.get("maxDistance"));
        preferences.setDatingIntention((String) preferencesMap.get("datingIntentions"));
        preferences.setDealBreakers((List<String>) preferencesMap.get("dealBreakers"));
        preferences.setMustHaves((List<String>) preferencesMap.get("mustHaves"));
        
        Map<String, Object> ageRangeMap = (Map<String, Object>) preferencesMap.get("ageRange");
        if (ageRangeMap != null) {
            UserPreferences.AgeRange ageRange = new UserPreferences.AgeRange();
            ageRange.setMin((Integer) ageRangeMap.get("min"));
            ageRange.setMax((Integer) ageRangeMap.get("max"));
            preferences.setAgeRange(ageRange);
        }
        
        Map<String, Object> heightRangeMap = (Map<String, Object>) preferencesMap.get("heightRange");
        if (heightRangeMap != null) {
            UserPreferences.HeightRange heightRange = new UserPreferences.HeightRange();
            heightRange.setMin((Integer) heightRangeMap.get("min"));
            heightRange.setMax((Integer) heightRangeMap.get("max"));
            preferences.setHeightRange(heightRange);
        }
        
        return preferences;
    }

    // Notification Settings conversion methods
    private Map<String, Object> convertNotificationSettingsToMap(NotificationSettings settings) {
        if (settings == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("newMatches", settings.isNewMatches());
        map.put("messages", settings.isMessages());
        map.put("likes", settings.isLikes());
        map.put("superLikes", settings.isSuperLikes());
        map.put("promotions", settings.isPromotions());
        map.put("emailUpdates", settings.isEmailUpdates());
        
        return map;
    }

    private NotificationSettings convertToNotificationSettings(Map<String, Object> settingsMap) {
        if (settingsMap == null) return null;
        
        NotificationSettings settings = new NotificationSettings();
        settings.setNewMatches((Boolean) settingsMap.get("newMatches"));
        settings.setMessages((Boolean) settingsMap.get("messages"));
        settings.setLikes((Boolean) settingsMap.get("likes"));
        settings.setSuperLikes((Boolean) settingsMap.get("superLikes"));
        settings.setPromotions((Boolean) settingsMap.get("promotions"));
        settings.setEmailUpdates((Boolean) settingsMap.get("emailUpdates"));
        
        return settings;
    }

    // Utility methods
    private <T> List<List<T>> partitionList(List<T> list, int partitionSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += partitionSize) {
            partitions.add(list.subList(i, Math.min(i + partitionSize, list.size())));
        }
        return partitions;
    }
}