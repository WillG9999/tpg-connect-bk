package com.tpg.connect.services;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.tpg.connect.model.api.PotentialMatchesResponse;
import com.tpg.connect.model.dto.MatchActionsRequest;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserMatchPoolService {

    private static final Logger logger = LoggerFactory.getLogger(UserMatchPoolService.class);
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    
    @Autowired
    private UserActionsService userActionsService;

    /**
     * Get next 3 unviewed matches from the user's queue (chronological across all days)
     */
    public PotentialMatchesResponse getNextMatches(String userId) {
        try {
            logger.info("🔍 Getting next matches for user: {}", userId);
            
            // Read UserMatchPools document for this user
            DocumentSnapshot doc = firestore.collection("UserMatchPools")
                    .document(userId)
                    .get()
                    .get();
            
            if (!doc.exists()) {
                logger.warn("❌ No UserMatchPools document found for user: {}", userId);
                return new PotentialMatchesResponse(true, "No matches available yet", 
                        Collections.emptyList(), null, 0, true);
            }
            
            // Debug: Log the document structure for user 299335545418
            if ("299335545418".equals(userId)) {
                Map<String, Object> docData = doc.getData();
                logger.info("🐛 DEBUG - Document data keys: {}", docData != null ? docData.keySet() : "null");
                if (docData != null && docData.containsKey("dailyEntries")) {
                    List<?> dailyEntries = (List<?>) docData.get("dailyEntries");
                    logger.info("🐛 DEBUG - Daily entries count: {}", dailyEntries != null ? dailyEntries.size() : 0);
                    if (dailyEntries != null && !dailyEntries.isEmpty()) {
                        Object firstEntry = dailyEntries.get(0);
                        if (firstEntry instanceof Map) {
                            Map<?, ?> entryMap = (Map<?, ?>) firstEntry;
                            logger.info("🐛 DEBUG - First entry keys: {}", entryMap.keySet());
                            if (entryMap.containsKey("matches")) {
                                List<?> matches = (List<?>) entryMap.get("matches");
                                logger.info("🐛 DEBUG - First entry matches count: {}", matches != null ? matches.size() : 0);
                                if (matches != null && !matches.isEmpty()) {
                                    Object firstMatch = matches.get(0);
                                    if (firstMatch instanceof Map) {
                                        Map<?, ?> matchMap = (Map<?, ?>) firstMatch;
                                        logger.info("🐛 DEBUG - First match keys: {}", matchMap.keySet());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Extract queue of unviewed matches chronologically
            List<QueueMatch> queue = extractQueueMatches(doc, userId);
            
            if (queue.isEmpty()) {
                logger.info("✅ No unviewed matches in queue for user: {}", userId);
                return new PotentialMatchesResponse(true, "No new matches available", 
                        Collections.emptyList(), null, 0, true);
            }
            
            // Take up to 3 matches from queue
            List<QueueMatch> nextMatches = queue.stream()
                    .limit(3)
                    .collect(Collectors.toList());
            
            // Convert to CompleteUserProfile objects
            List<CompleteUserProfile> userProfiles = new ArrayList<>();
            for (QueueMatch queueMatch : nextMatches) {
                CompleteUserProfile profile = userProfileRepository.findByUserId(queueMatch.getMatchConnectId());
                if (profile != null) {
                    userProfiles.add(profile);
                } else {
                    logger.warn("⚠️ Profile not found for match: {}", queueMatch.getMatchConnectId());
                }
            }
            
            logger.info("✅ Returning {} matches for user {} (queue has {} total unviewed)", 
                    userProfiles.size(), userId, queue.size());
            
            // Log the first profile to debug frontend parsing issues
            if (!userProfiles.isEmpty()) {
                CompleteUserProfile firstProfile = userProfiles.get(0);
                logger.info("🔍 Sample profile data - connectId: {}, firstName: {}, lastName: {}, age: {}, photos: {}", 
                        firstProfile.getConnectId(), firstProfile.getFirstName(), firstProfile.getLastName(), 
                        firstProfile.getAge(), firstProfile.getPhotos() != null ? firstProfile.getPhotos().size() : "null");
                logger.info("🔍 Sample profile photos: {}", firstProfile.getPhotos());
            }
            
            return new PotentialMatchesResponse(true, "Matches retrieved from queue", 
                    userProfiles, generateQueueId(userId), userProfiles.size(), false);
                    
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error getting matches for user {}: {}", userId, e.getMessage(), e);
            return new PotentialMatchesResponse(false, "Failed to retrieve matches: " + e.getMessage(), 
                    null, null, 0, false);
        }
    }

    /**
     * Update viewed status for multiple matches in a single batch
     */
    public Map<String, Object> updateViewedStatus(String userId, MatchActionsRequest request) {
        try {
            logger.info("🔄 Updating viewed status for user {} with {} actions", 
                    userId, request.getActions().size());
            
            // Use transaction to ensure atomic updates
            var docRef = firestore.collection("UserMatchPools").document(userId);
            
            Map<String, Object> result = firestore.runTransaction(transaction -> {
                DocumentSnapshot doc = transaction.get(docRef).get();
                
                if (!doc.exists()) {
                    throw new IllegalArgumentException("UserMatchPools document not found for user: " + userId);
                }
                
                // Extract current data
                Map<String, Object> docData = new HashMap<>(doc.getData());
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dailyEntries = (List<Map<String, Object>>) docData.get("dailyEntries");
                
                if (dailyEntries == null) {
                    throw new IllegalStateException("No dailyEntries found in UserMatchPools for user: " + userId);
                }
                
                // Create deep copy to avoid modifying original
                List<Map<String, Object>> newDailyEntries = new ArrayList<>();
                for (Map<String, Object> entry : dailyEntries) {
                    Map<String, Object> newEntry = new HashMap<>(entry);
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> matches = (List<Map<String, Object>>) entry.get("matches");
                    if (matches != null) {
                        List<Map<String, Object>> newMatches = new ArrayList<>();
                        for (Map<String, Object> match : matches) {
                            newMatches.add(new HashMap<>(match));
                        }
                        newEntry.put("matches", newMatches);
                    }
                    newDailyEntries.add(newEntry);
                }
                
                // Process each action and update viewed status
                int actionsProcessed = 0;
                int newMatches = 0;
                List<String> matchedUserIds = new ArrayList<>();
                
                for (MatchActionsRequest.UserActionDto action : request.getActions()) {
                    boolean matchFound = updateMatchViewedStatusInTransaction(newDailyEntries, action.getTargetUserId());
                    
                    if (matchFound) {
                        actionsProcessed++;
                        
                        // Record the action using new UserActionsService
                        try {
                            boolean isMutualMatch = false;
                            
                            if ("LIKE".equals(action.getAction())) {
                                isMutualMatch = userActionsService.addLikeAction(userId, action.getTargetUserId());
                            } else {
                                userActionsService.addPassAction(userId, action.getTargetUserId());
                            }
                            
                            // Handle mutual match
                            if (isMutualMatch) {
                                logger.info("🎉 Mutual match detected in transaction: {} ↔ {}", userId, action.getTargetUserId());
                                // UserActionsService already handles match creation in the matches array
                                newMatches++;
                                matchedUserIds.add(action.getTargetUserId());
                            }
                                            
                        } catch (Exception e) {
                            logger.warn("⚠️ Failed to record action {} -> {} in transaction: {}", 
                                    userId, action.getTargetUserId(), e.getMessage());
                        }
                    } else {
                        logger.warn("⚠️ Match not found for targetUserId: {} in user {}'s queue", 
                                action.getTargetUserId(), userId);
                    }
                }
                
                // Update the document with new viewed statuses and lastUpdated timestamp
                docData.put("dailyEntries", newDailyEntries);
                docData.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
                
                // Write back to Firestore within transaction
                transaction.set(docRef, docData);
                
                Map<String, Object> transactionResult = new HashMap<>();
                transactionResult.put("success", true);
                transactionResult.put("actionsProcessed", actionsProcessed);
                transactionResult.put("newMatches", newMatches);
                transactionResult.put("matchedUserIds", matchedUserIds);
                transactionResult.put("message", String.format("Processed %d actions, %d new matches", 
                        actionsProcessed, newMatches));
                
                return transactionResult;
            }).get();
            
            logger.info("✅ Updated viewed status: {} actions processed, {} new matches for user {}", 
                    result.get("actionsProcessed"), result.get("newMatches"), userId);
            
            return result;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error updating viewed status for user {}: {}", userId, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Failed to update viewed status: " + e.getMessage());
            return result;
        }
    }

    /**
     * Extract chronological queue of unviewed matches from UserMatchPools document
     */
    private List<QueueMatch> extractQueueMatches(DocumentSnapshot doc, String userId) {
        List<QueueMatch> queue = new ArrayList<>();
        
        Map<String, Object> docData = doc.getData();
        if (docData == null) return queue;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> dailyEntries = (List<Map<String, Object>>) docData.get("dailyEntries");
        
        if (dailyEntries == null) return queue;
        
        // Sort daily entries by date (chronological order - oldest first)
        dailyEntries.sort((a, b) -> {
            String dateA = (String) a.get("date");
            String dateB = (String) b.get("date");
            return dateA.compareTo(dateB);
        });
        
        // Get already acted-on users to filter them out
        Set<String> actedOnUsers = userActionsService.getActedOnUsers(userId);
        logger.info("🚫 User {} has acted on {} users, filtering them out", userId, actedOnUsers.size());
        
        // Extract unviewed matches across all days
        for (Map<String, Object> dailyEntry : dailyEntries) {
            String date = (String) dailyEntry.get("date");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) dailyEntry.get("matches");
            
            if (matches != null) {
                logger.info("🔍 Processing {} matches for date: {}", matches.size(), date);
                for (Map<String, Object> match : matches) {
                    Boolean viewed = (Boolean) match.get("viewed");
                    String matchConnectId = (String) match.get("matchConnectId");
                    
                    // Skip if user has already acted on this person
                    if (actedOnUsers.contains(matchConnectId)) {
                        logger.info("🚫 Skipping already-actioned user: {} (in userActions)", matchConnectId);
                        continue;
                    }
                    
                    if (viewed == null || !viewed) {
                        Double compatibilityScore = (Double) match.get("compatibilityScore");
                        Number stabilityRankNumber = (Number) match.get("stabilityRank");
                        Integer stabilityRank = stabilityRankNumber != null ? stabilityRankNumber.intValue() : null;
                        
                        if (matchConnectId != null) {
                            queue.add(new QueueMatch(
                                    matchConnectId,
                                    date,
                                    compatibilityScore != null ? compatibilityScore : 0.0,
                                    stabilityRank != null ? stabilityRank : 0
                            ));
                            logger.info("✅ Added unviewed match: {} (viewed: {})", matchConnectId, viewed);
                        }
                    } else {
                        logger.info("⏭️ Skipping viewed match: {} (viewed: {})", matchConnectId, viewed);
                    }
                }
            }
        }
        
        logger.info("📊 Extracted {} unviewed matches from queue for user {}", queue.size(), userId);
        
        // Debug: Log queue details for user 299335545418
        if ("299335545418".equals(userId)) {
            logger.info("🐛 DEBUG - Queue extraction details:");
            logger.info("🐛 DEBUG - Daily entries processed: {}", dailyEntries.size());
            logger.info("🐛 DEBUG - Total queue matches found: {}", queue.size());
            for (int i = 0; i < Math.min(3, queue.size()); i++) {
                QueueMatch match = queue.get(i);
                logger.info("🐛 DEBUG - Queue match {}: {} (score: {}, date: {})", 
                          i+1, match.getMatchConnectId(), match.getCompatibilityScore(), match.getDate());
            }
        }
        
        return queue;
    }

    /**
     * Update viewed status for a specific match in the dailyEntries structure
     */
    private boolean updateMatchViewedStatus(List<Map<String, Object>> dailyEntries, String targetUserId) {
        for (Map<String, Object> dailyEntry : dailyEntries) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) dailyEntry.get("matches");
            
            if (matches != null) {
                for (Map<String, Object> match : matches) {
                    String matchConnectId = (String) match.get("matchConnectId");
                    if (targetUserId.equals(matchConnectId)) {
                        Boolean previousViewed = (Boolean) match.get("viewed");
                        match.put("viewed", true);
                        logger.info("🔄 Marked match {} as viewed (was: {})", targetUserId, previousViewed);
                        return true;
                    }
                }
            }
        }
        logger.warn("⚠️ Target match {} not found in dailyEntries", targetUserId);
        return false;
    }

    /**
     * Update viewed status for a specific match in the dailyEntries structure (for transaction use)
     */
    private boolean updateMatchViewedStatusInTransaction(List<Map<String, Object>> dailyEntries, String targetUserId) {
        for (Map<String, Object> dailyEntry : dailyEntries) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matches = (List<Map<String, Object>>) dailyEntry.get("matches");
            
            if (matches != null) {
                for (Map<String, Object> match : matches) {
                    String matchConnectId = (String) match.get("matchConnectId");
                    if (targetUserId.equals(matchConnectId)) {
                        Boolean previousViewed = (Boolean) match.get("viewed");
                        match.put("viewed", true);
                        logger.info("🔄 [Transaction] Marked match {} as viewed (was: {})", targetUserId, previousViewed);
                        return true;
                    }
                }
            }
        }
        logger.warn("⚠️ [Transaction] Target match {} not found in dailyEntries", targetUserId);
        return false;
    }

    /**
     * Record user action for analytics and mutual match detection
     */
    private void recordUserAction(String userId, String targetUserId, String action) {
        try {
            if ("LIKE".equals(action)) {
                // Use new UserActionsService for like actions
                boolean isMutualMatch = userActionsService.addLikeAction(userId, targetUserId);
                
                if (isMutualMatch) {
                    logger.info("🎉 Mutual match detected via UserActionsService: {} ↔ {}", userId, targetUserId);
                    // UserActionsService already handles match creation in the matches array
                }
            } else {
                // Use new UserActionsService for pass actions
                userActionsService.addPassAction(userId, targetUserId);
            }
            
                            
        } catch (Exception e) {
            logger.warn("⚠️ Failed to record user action for user {} -> {}: {}", 
                    userId, targetUserId, e.getMessage());
        }
    }

    /**
     * Generate a queue identifier for tracking
     */
    private String generateQueueId(String userId) {
        return "queue_" + userId + "_" + LocalDate.now().toString();
    }

    /**
     * Data class to represent a match in the queue
     */
    private static class QueueMatch {
        private final String matchConnectId;
        private final String date;
        private final double compatibilityScore;
        private final int stabilityRank;

        public QueueMatch(String matchConnectId, String date, double compatibilityScore, int stabilityRank) {
            this.matchConnectId = matchConnectId;
            this.date = date;
            this.compatibilityScore = compatibilityScore;
            this.stabilityRank = stabilityRank;
        }

        public String getMatchConnectId() { return matchConnectId; }
        public String getDate() { return date; }
        public double getCompatibilityScore() { return compatibilityScore; }
        public int getStabilityRank() { return stabilityRank; }
    }
}