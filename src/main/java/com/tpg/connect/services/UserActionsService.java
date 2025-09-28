package com.tpg.connect.services;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class UserActionsService {

    private static final Logger logger = LoggerFactory.getLogger(UserActionsService.class);
    private static final String COLLECTION_NAME = "userActions";
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private MatchService matchService;

    /**
     * Add a like action: user likes targetUser
     * - Adds targetUserId to user's likes array
     * - Adds userId to targetUser's likedBy array
     * - Returns true if this creates a mutual match
     */
    public boolean addLikeAction(String userId, String targetUserId) {
        try {
            logger.info("👍 Adding like action: {} likes {}", userId, targetUserId);
            logger.info("🔍 Starting transaction for like action {} -> {}", userId, targetUserId);
            
            DocumentReference userDoc = firestore.collection(COLLECTION_NAME).document(userId);
            DocumentReference targetDoc = firestore.collection(COLLECTION_NAME).document(targetUserId);
            
            boolean isMutualMatch = firestore.runTransaction(transaction -> {
                // Get both documents
                DocumentSnapshot userSnapshot = transaction.get(userDoc).get();
                DocumentSnapshot targetSnapshot = transaction.get(targetDoc).get();
                
                // Initialize user document if it doesn't exist
                Map<String, Object> userData;
                if (userSnapshot.exists()) {
                    userData = new HashMap<>(userSnapshot.getData());
                    logger.info("🔍 User {} document exists, loading data", userId);
                } else {
                    userData = initializeUserActionsDocument(userId);
                    logger.info("🆕 User {} document doesn't exist, initializing", userId);
                }
                
                // Initialize target document if it doesn't exist
                Map<String, Object> targetData;
                if (targetSnapshot.exists()) {
                    targetData = new HashMap<>(targetSnapshot.getData());
                    logger.info("🔍 Target {} document exists, loading data", targetUserId);
                } else {
                    targetData = initializeUserActionsDocument(targetUserId);
                    logger.info("🆕 Target {} document doesn't exist, initializing", targetUserId);
                }
                
                // Get current arrays
                @SuppressWarnings("unchecked")
                List<String> userLikes = (List<String>) userData.getOrDefault("likes", new ArrayList<>());
                @SuppressWarnings("unchecked")
                List<String> userLikedBy = (List<String>) userData.getOrDefault("likedBy", new ArrayList<>());
                @SuppressWarnings("unchecked")
                List<String> targetLikes = (List<String>) targetData.getOrDefault("likes", new ArrayList<>());
                @SuppressWarnings("unchecked")
                List<String> targetLikedBy = (List<String>) targetData.getOrDefault("likedBy", new ArrayList<>());
                
                // Add targetUserId to user's likes array (if not already there)
                if (!userLikes.contains(targetUserId)) {
                    userLikes.add(targetUserId);
                    userData.put("likes", userLikes);
                }
                
                // Add userId to target's likedBy array (if not already there)
                if (!targetLikedBy.contains(userId)) {
                    targetLikedBy.add(userId);
                    targetData.put("likedBy", targetLikedBy);
                }
                
                // Check for mutual match: has targetUser already liked this user?
                boolean mutualMatch = targetLikes.contains(userId);
                
                if (mutualMatch) {
                    logger.info("🎉 Mutual match detected: {} ↔ {}", userId, targetUserId);
                    
                    // Add to both users' matches arrays
                    @SuppressWarnings("unchecked")
                    List<String> userMatches = (List<String>) userData.getOrDefault("matches", new ArrayList<>());
                    @SuppressWarnings("unchecked")
                    List<String> targetMatches = (List<String>) targetData.getOrDefault("matches", new ArrayList<>());
                    
                    if (!userMatches.contains(targetUserId)) {
                        userMatches.add(targetUserId);
                        userData.put("matches", userMatches);
                    }
                    if (!targetMatches.contains(userId)) {
                        targetMatches.add(userId);
                        targetData.put("matches", targetMatches);
                    }
                    
                }
                
                // Update timestamps
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
                userData.put("lastUpdated", timestamp);
                targetData.put("lastUpdated", timestamp);
                
                // Write both documents
                logger.info("📝 Writing userActions document for user {}", userId);
                transaction.set(userDoc, userData);
                logger.info("📝 Writing userActions document for target {}", targetUserId);
                transaction.set(targetDoc, targetData);
                
                logger.info("✅ Transaction completed. Mutual match: {}", mutualMatch);
                return mutualMatch;
            }).get();
            
            // If mutual match detected, create Match entity and Conversation outside transaction
            if (isMutualMatch) {
                try {
                    logger.info("🏗️ Creating Match entity and Conversation for {} ↔ {}", userId, targetUserId);
                    matchService.createMatch(userId, targetUserId);
                    logger.info("✅ Match entity and Conversation created successfully");
                } catch (Exception e) {
                    logger.error("❌ Failed to create Match entity for {} ↔ {}: {}", userId, targetUserId, e.getMessage(), e);
                    // Don't fail the like action if Match creation fails - the arrays are already updated
                }
            }
            
            return isMutualMatch;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error adding like action {} -> {}: {}", userId, targetUserId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Add a pass action: user passes on targetUser
     * - Adds targetUserId to user's passes array
     */
    public void addPassAction(String userId, String targetUserId) {
        try {
            logger.info("👎 Adding pass action: {} passes {}", userId, targetUserId);
            
            DocumentReference userDoc = firestore.collection(COLLECTION_NAME).document(userId);
            
            firestore.runTransaction(transaction -> {
                DocumentSnapshot userSnapshot = transaction.get(userDoc).get();
                
                // Initialize user document if it doesn't exist
                Map<String, Object> userData = userSnapshot.exists() ? 
                    new HashMap<>(userSnapshot.getData()) : initializeUserActionsDocument(userId);
                
                // Get current passes array
                @SuppressWarnings("unchecked")
                List<String> userPasses = (List<String>) userData.getOrDefault("passes", new ArrayList<>());
                
                // Add targetUserId to user's passes array (if not already there)
                if (!userPasses.contains(targetUserId)) {
                    userPasses.add(targetUserId);
                    userData.put("passes", userPasses);
                }
                
                // Update timestamp
                userData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
                
                // Write document
                transaction.set(userDoc, userData);
                
                return null;
            }).get();
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error adding pass action {} -> {}: {}", userId, targetUserId, e.getMessage(), e);
        }
    }

    /**
     * Check if user has already acted on target user (liked or passed)
     */
    public boolean hasActedOnUser(String userId, String targetUserId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
            
            if (!doc.exists()) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            List<String> likes = (List<String>) doc.getData().getOrDefault("likes", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> passes = (List<String>) doc.getData().getOrDefault("passes", new ArrayList<>());
            
            return likes.contains(targetUserId) || passes.contains(targetUserId);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error checking if user {} acted on {}: {}", userId, targetUserId, e.getMessage());
            return false;
        }
    }

    /**
     * Get all users this user has acted on (liked or passed)
     */
    public Set<String> getActedOnUsers(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
            
            if (!doc.exists()) {
                return new HashSet<>();
            }
            
            @SuppressWarnings("unchecked")
            List<String> likes = (List<String>) doc.getData().getOrDefault("likes", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> passes = (List<String>) doc.getData().getOrDefault("passes", new ArrayList<>());
            
            Set<String> actedOnUsers = new HashSet<>();
            actedOnUsers.addAll(likes);
            actedOnUsers.addAll(passes);
            
            return actedOnUsers;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error getting acted on users for {}: {}", userId, e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * Get users who have liked this user
     */
    public List<String> getLikedByUsers(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
            
            if (!doc.exists()) {
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            List<String> likedBy = (List<String>) doc.getData().getOrDefault("likedBy", new ArrayList<>());
            
            return new ArrayList<>(likedBy);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error getting liked by users for {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get users this user has matched with
     */
    public List<String> getMatches(String userId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME).document(userId).get().get();
            
            if (!doc.exists()) {
                return new ArrayList<>();
            }
            
            @SuppressWarnings("unchecked")
            List<String> matches = (List<String>) doc.getData().getOrDefault("matches", new ArrayList<>());
            
            return new ArrayList<>(matches);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error getting matches for {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Initialize a new userActions document
     */
    private Map<String, Object> initializeUserActionsDocument(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("connectId", userId);
        userData.put("likes", new ArrayList<>());
        userData.put("passes", new ArrayList<>());
        userData.put("matches", new ArrayList<>());
        userData.put("likedBy", new ArrayList<>());
        userData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
        userData.put("totalActions", 0);
        
        logger.info("🆕 Initializing new userActions document for user: {}", userId);
        return userData;
    }
}