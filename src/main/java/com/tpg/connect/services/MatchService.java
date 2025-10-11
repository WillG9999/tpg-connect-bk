package com.tpg.connect.services;

import com.tpg.connect.repository.MatchRepository;
import com.tpg.connect.repository.UserActionRepository;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.model.conversation.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatchService {
    
    // TODO: Implement push notifications for new matches using FCM
    // TODO: Add notification service integration for match events
    // TODO: Handle notification scheduling and delivery status

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserActionRepository userActionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ConversationService conversationService;

    public boolean checkForMutualLike(String userId, String targetUserId) {
        // Check if target user has already liked the current user
        return userActionRepository.existsByUserIdAndTargetUserIdAndAction(
                targetUserId, userId, UserAction.ActionType.LIKE);
    }

    // @CacheEvict(value = {"matches", "userMatches"}, allEntries = true) // Temporarily disabled due to cache config issue
    public Match createMatch(String userId, String targetUserId) {
        // Note: Mutual like validation is already done by UserActionsService before calling this method
        System.out.println("🔄 MatchService.createMatch() called for " + userId + " ↔ " + targetUserId);

        // Check if match already exists
        Match existingMatch = matchRepository.findByUserIdsAndStatus(userId, targetUserId, Match.MatchStatus.ACTIVE).orElse(null);
        if (existingMatch != null) {
            return existingMatch; // Return existing match
        }

        // Create new match
        Match match = new Match();
        match.setId(generateConversationId(userId, targetUserId)); // Use ConnectID format instead of UUID
        match.setUser1Id(userId);
        match.setUser2Id(targetUserId);
        match.setMatchedAt(LocalDateTime.now());
        match.setStatus(Match.MatchStatus.ACTIVE);
        match.setConversationId(generateConversationId(userId, targetUserId)); // Create deterministic conversation ID

        Match savedMatch = matchRepository.save(match);

        // Create conversation for the match
        try {
            System.out.println("🔄 Creating conversation for match: " + savedMatch.getId());
            System.out.println("🔄 Match participants: " + savedMatch.getUser1Id() + " ↔ " + savedMatch.getUser2Id());
            
            Conversation conversation = conversationService.createConversationFromMatch(savedMatch.getId());
            
            System.out.println("✅ Successfully created conversation: " + conversation.getId());
            System.out.println("✅ Conversation participants: " + conversation.getParticipantIds());
            System.out.println("✅ Conversation status: " + conversation.getStatus());
        } catch (Exception e) {
            // Log detailed error but don't fail the match creation
            System.err.println("❌ CRITICAL: Failed to create conversation for match " + savedMatch.getId());
            System.err.println("❌ Error details: " + e.getMessage());
            System.err.println("❌ Match user1: " + savedMatch.getUser1Id());
            System.err.println("❌ Match user2: " + savedMatch.getUser2Id());
            e.printStackTrace();
            
            // This is a critical error - conversations must be created for matches to work
            throw new RuntimeException("Failed to create conversation for match", e);
        }

        // Send notifications to both users
        notificationService.sendMatchNotification(userId, targetUserId, savedMatch.getId());
        notificationService.sendMatchNotification(targetUserId, userId, savedMatch.getId());

        return savedMatch;
    }

    // @Cacheable(value = "userMatches", key = "'matches_' + #userId") // Temporarily disabled due to cache config issue
    public List<Match> getUserMatches(String userId) {
        return matchRepository.findByUserIdAndStatus(userId, Match.MatchStatus.ACTIVE);
    }

    // @Cacheable(value = "matches", key = "'match_' + #matchId") // Temporarily disabled due to cache config issue
    public Match getMatch(String matchId) {
        return matchRepository.findById(matchId).orElse(null);
    }

    // @CacheEvict(value = {"matches", "userMatches"}, allEntries = true) // Temporarily disabled
    public void unmatch(String matchId, String userId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        // Verify user is part of this match
        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new IllegalArgumentException("User not part of this match");
        }

        // Update match status
        match.setStatus(Match.MatchStatus.UNMATCHED);
        matchRepository.save(match);

        // Notify the other user
        String otherUserId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
        notificationService.sendUnmatchNotification(otherUserId, matchId);
    }

    // @CacheEvict(value = {"matches", "userMatches"}, allEntries = true) // Temporarily disabled
    public void blockUser(String matchId, String userId, String blockedUserId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        // Verify user is part of this match
        if (!match.getUser1Id().equals(userId) && !match.getUser2Id().equals(userId)) {
            throw new IllegalArgumentException("User not part of this match");
        }

        // Update match status based on who is blocking
        if (match.getUser1Id().equals(userId)) {
            match.setStatus(Match.MatchStatus.BLOCKED_BY_USER1);
        } else {
            match.setStatus(Match.MatchStatus.BLOCKED_BY_USER2);
        }

        matchRepository.save(match);
    }

    // @CacheEvict(value = {"matches", "userMatches"}, allEntries = true) // Temporarily disabled
    public void reportMatch(String matchId, String reporterId, String reason) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        // Verify user is part of this match
        if (!match.getUser1Id().equals(reporterId) && !match.getUser2Id().equals(reporterId)) {
            throw new IllegalArgumentException("User not part of this match");
        }

        // Update match status
        match.setStatus(Match.MatchStatus.REPORTED);
        matchRepository.save(match);

        // Log the report (in real implementation, would create a Report entity)
        System.out.println("Match reported: " + matchId + " by " + reporterId + " for: " + reason);
    }

    public int getMatchCount(String userId) {
        return (int) matchRepository.countByUserIdAndStatus(userId, Match.MatchStatus.ACTIVE);
    }

    public List<Match> getRecentMatches(String userId, int limit) {
        return matchRepository.findRecentByUserId(userId, Match.MatchStatus.ACTIVE, limit);
    }

    public boolean hasMatchBetweenUsers(String userId1, String userId2) {
        return matchRepository.findByUserIdsAndStatus(userId1, userId2, Match.MatchStatus.ACTIVE) != null;
    }

    public void recordUserAction(String userId, String targetUserId, UserAction.ActionType action) {
        // Check if action already exists
        if (userActionRepository.existsByUserIdAndTargetUserId(userId, targetUserId)) {
            return; // Don't record duplicate actions
        }

        UserAction userAction = new UserAction();
        userAction.setId(UUID.randomUUID().toString());
        userAction.setUserId(userId);
        userAction.setTargetUserId(targetUserId);
        userAction.setAction(action);
        userAction.setTimestamp(LocalDateTime.now());

        userActionRepository.save(userAction);
    }

    public List<UserAction> getUserActions(String userId) {
        return userActionRepository.findByUserId(userId);
    }

    public List<UserAction> getUserActionsForTarget(String userId, String targetUserId) {
        Optional<UserAction> action = userActionRepository.findByUserIdAndTargetUserId(userId, targetUserId);
        return action.isPresent() ? List.of(action.get()) : List.of();
    }

    /**
     * Generate deterministic conversation ID from two connect IDs
     * Format: connectid1_connectid2 (sorted to ensure consistency)
     */
    private String generateConversationId(String connectId1, String connectId2) {
        // Sort IDs to ensure consistency regardless of order
        if (connectId1.compareTo(connectId2) <= 0) {
            return connectId1 + "_" + connectId2;
        } else {
            return connectId2 + "_" + connectId1;
        }
    }

    /**
     * Unmatch users by setting match status to UNMATCHED
     */
    public void unmatchUsers(String matchId) {
        System.out.println("🚫 MatchService: Unmatching users for match ID: " + matchId);
        
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            System.err.println("❌ MatchService: Match not found: " + matchId);
            throw new RuntimeException("Match not found: " + matchId);
        }
        
        Match match = matchOpt.get();
        match.setStatus(Match.MatchStatus.UNMATCHED);
        match.setLastActivityAt(LocalDateTime.now());
        
        matchRepository.save(match);
        System.out.println("✅ MatchService: Match status updated to UNMATCHED: " + matchId);
    }
    
    // Admin-specific methods
    
    /**
     * Get user's matches for admin review
     */
    public List<Match> getUserMatchesForAdmin(String connectId, int page, int size) {
        return matchRepository.findRecentByUserId(connectId, null, size);
    }
    
    /**
     * Get total matches count for pagination
     */
    public long getTotalMatchesCount(String connectId) {
        return matchRepository.countByUserId(connectId);
    }
}