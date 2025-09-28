package com.tpg.connect.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.DetailedProfile;
import com.tpg.connect.model.user.EnhancedPhoto;
import com.tpg.connect.model.user.WrittenPrompt;
import com.tpg.connect.model.user.PollPrompt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Component
public class TestDataUploader {

    private static final Logger logger = LoggerFactory.getLogger(TestDataUploader.class);
    
    @Autowired
    private Firestore firestore;

    // Remove automatic execution - make it a manual utility

    @SuppressWarnings("unchecked")
    public void uploadTestProfiles(String jsonFilePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Read JSON file
        File jsonFile = new File(jsonFilePath);
        if (!jsonFile.exists()) {
            throw new RuntimeException("JSON file not found: " + jsonFilePath);
        }
        
        List<Map<String, Object>> profilesData = mapper.readValue(jsonFile, List.class);
        logger.info("📖 Loaded {} profiles from JSON", profilesData.size());
        
        // Process in batches of 10 (Firestore batch write limit is 500, but 10 is safer)
        int batchSize = 10;
        int totalUploaded = 0;
        
        for (int i = 0; i < profilesData.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, profilesData.size());
            List<Map<String, Object>> batch = profilesData.subList(i, endIndex);
            
            uploadBatch(batch, i / batchSize + 1);
            totalUploaded += batch.size();
            
            logger.info("📊 Progress: {}/{} profiles uploaded", totalUploaded, profilesData.size());
            
            // Small delay between batches to avoid rate limiting
            Thread.sleep(100);
        }
        
        logger.info("🎉 Successfully uploaded {} test profiles to Firebase!", totalUploaded);
    }

    @SuppressWarnings("unchecked")
    private void uploadBatch(List<Map<String, Object>> batch, int batchNumber) throws InterruptedException, ExecutionException {
        WriteBatch writeBatch = firestore.batch();
        
        logger.info("📦 Processing batch {} with {} profiles", batchNumber, batch.size());
        
        for (Map<String, Object> profileData : batch) {
            try {
                // Convert JSON data to CompleteUserProfile
                CompleteUserProfile profile = convertToCompleteUserProfile(profileData);
                
                // Prepare document for Firestore
                Map<String, Object> firestoreData = convertToFirestoreMap(profile);
                
                // Add to batch
                String connectId = profile.getConnectId();
                writeBatch.set(firestore.collection("user_profiles").document(connectId), firestoreData);
                
                logger.debug("✅ Prepared profile for upload: {} - {} {}", 
                           connectId, profile.getFirstName(), profile.getLastName());
                
            } catch (Exception e) {
                logger.error("❌ Error processing profile in batch {}: {}", batchNumber, e.getMessage());
                throw e;
            }
        }
        
        // Execute batch write
        writeBatch.commit().get();
        logger.info("🔥 Batch {} written to Firestore", batchNumber);
    }

    @SuppressWarnings("unchecked")
    private CompleteUserProfile convertToCompleteUserProfile(Map<String, Object> data) {
        CompleteUserProfile profile = new CompleteUserProfile();
        
        // Basic fields
        profile.setConnectId((String) data.get("connectId"));
        profile.setFirstName((String) data.get("firstName"));
        profile.setLastName((String) data.get("lastName"));
        profile.setGender((String) data.get("gender"));
        profile.setEmail((String) data.get("email"));
        profile.setLocation((String) data.get("location"));
        profile.setActive((Boolean) data.get("active"));
        profile.setOnline((Boolean) data.get("isOnline"));
        profile.setPremium((Boolean) data.get("isPremium"));
        profile.setVerified((Boolean) data.get("isVerified"));
        profile.setSubscriptionType((String) data.get("subscriptionType"));
        
        // Parse date of birth
        String dobString = (String) data.get("dateOfBirth");
        if (dobString != null) {
            profile.setDateOfBirth(LocalDate.parse(dobString));
        }
        
        // Interests
        List<String> interests = (List<String>) data.get("interests");
        profile.setInterests(interests);
        
        // Version
        Object version = data.get("version");
        if (version instanceof Integer) {
            profile.setVersion((Integer) version);
        }
        
        // Timestamps (simplified - using current time)
        profile.setCreatedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());
        
        // Photos
        List<Map<String, Object>> photosData = (List<Map<String, Object>>) data.get("photos");
        if (photosData != null) {
            List<EnhancedPhoto> photos = new ArrayList<>();
            for (Map<String, Object> photoData : photosData) {
                EnhancedPhoto photo = new EnhancedPhoto();
                photo.setId((String) photoData.get("id"));
                photo.setUrl((String) photoData.get("url"));
                photo.setPrimary((Boolean) photoData.get("isPrimary"));
                Object order = photoData.get("order");
                photo.setOrder(order instanceof Integer ? (Integer) order : 1);
                photos.add(photo);
            }
            profile.setPhotos(photos);
        }
        
        // Detailed profile
        Map<String, Object> profileData = (Map<String, Object>) data.get("profile");
        if (profileData != null) {
            DetailedProfile detailedProfile = new DetailedProfile();
            detailedProfile.setPronouns((String) profileData.get("pronouns"));
            detailedProfile.setGender((String) profileData.get("gender"));
            detailedProfile.setSexuality((String) profileData.get("sexuality"));
            detailedProfile.setInterestedIn((String) profileData.get("interestedIn"));
            detailedProfile.setJobTitle((String) profileData.get("jobTitle"));
            detailedProfile.setCompany((String) profileData.get("company"));
            detailedProfile.setUniversity((String) profileData.get("university"));
            detailedProfile.setEducationLevel((String) profileData.get("educationLevel"));
            detailedProfile.setReligiousBeliefs((String) profileData.get("religiousBeliefs"));
            detailedProfile.setHometown((String) profileData.get("hometown"));
            detailedProfile.setPolitics((String) profileData.get("politics"));
            detailedProfile.setLanguages((List<String>) profileData.get("languages"));
            detailedProfile.setDatingIntentions((String) profileData.get("datingIntentions"));
            detailedProfile.setRelationshipType((String) profileData.get("relationshipType"));
            detailedProfile.setHeight((String) profileData.get("height"));
            detailedProfile.setEthnicity((String) profileData.get("ethnicity"));
            detailedProfile.setChildren((String) profileData.get("children"));
            detailedProfile.setFamilyPlans((String) profileData.get("familyPlans"));
            detailedProfile.setPets((String) profileData.get("pets"));
            detailedProfile.setZodiacSign((String) profileData.get("zodiacSign"));
            
            profile.setProfile(detailedProfile);
        }
        
        // Written prompts
        List<Map<String, Object>> writtenPromptsData = (List<Map<String, Object>>) data.get("writtenPrompts");
        if (writtenPromptsData != null) {
            List<WrittenPrompt> writtenPrompts = new ArrayList<>();
            for (Map<String, Object> promptData : writtenPromptsData) {
                WrittenPrompt prompt = new WrittenPrompt();
                prompt.setQuestion((String) promptData.get("question"));
                prompt.setAnswer((String) promptData.get("answer"));
                writtenPrompts.add(prompt);
            }
            profile.setWrittenPrompts(writtenPrompts);
        }
        
        // Poll prompts
        List<Map<String, Object>> pollPromptsData = (List<Map<String, Object>>) data.get("pollPrompts");
        if (pollPromptsData != null) {
            List<PollPrompt> pollPrompts = new ArrayList<>();
            for (Map<String, Object> promptData : pollPromptsData) {
                PollPrompt prompt = new PollPrompt();
                prompt.setQuestion((String) promptData.get("question"));
                prompt.setDescription((String) promptData.get("description"));
                prompt.setOptions((List<String>) promptData.get("options"));
                prompt.setSelectedOption((String) promptData.get("selectedOption"));
                pollPrompts.add(prompt);
            }
            profile.setPollPrompts(pollPrompts);
        }
        
        return profile;
    }
    
    private Map<String, Object> convertToFirestoreMap(CompleteUserProfile profile) {
        // Use the existing repository conversion logic
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper.convertValue(profile, Map.class);
    }
}