package com.tpg.connect.repository;

import com.tpg.connect.model.UserMatches;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserMatchesRepository {
    
    // Create Operations
    UserMatches createUserMatches(UserMatches userMatches);
    
    // Read Operations
    Optional<UserMatches> findByConnectId(String connectId);
    boolean existsByConnectId(String connectId);
    
    // Match Operations
    UserMatches addMatch(String connectId, UserMatches.Match match);
    UserMatches updateMatch(String connectId, String matchId, UserMatches.Match updatedMatch);
    UserMatches updateMatchStatus(String connectId, String matchId, String status);
    UserMatches markMatchAsRead(String connectId, String matchId, Timestamp readAt);
    UserMatches updateLastActivity(String connectId, String matchId, Timestamp lastActivity);
    UserMatches updateMatchMessage(String connectId, String matchId, String lastMessage, Timestamp lastMessageAt);
    UserMatches reportMatch(String connectId, String matchId, String reportedBy, Timestamp reportedAt, String adminNotes);
    
    // Aggregate Operations
    UserMatches updateMatchCounts(String connectId, Integer totalMatches, Integer activeMatches, Integer newMatches);
    UserMatches incrementConversationCount(String connectId);
    UserMatches updateLastMatchTime(String connectId, Timestamp lastMatchAt);
    
    // Delete Operations
    UserMatches removeMatch(String connectId, String matchId);
    void deleteUserMatches(String connectId);
    
    // Batch Operations
    List<UserMatches> findUserMatchesByConnectIds(List<String> connectIds);
    Map<String, UserMatches> findUserMatchesMapByConnectIds(List<String> connectIds);
    
    // Query Operations
    List<UserMatches.Match> findActiveMatchesForUser(String connectId);
    List<UserMatches.Match> findNewMatchesForUser(String connectId);
    List<UserMatches.Match> findUnreadMatchesForUser(String connectId);
}