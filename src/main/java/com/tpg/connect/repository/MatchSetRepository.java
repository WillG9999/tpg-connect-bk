package com.tpg.connect.repository;

import com.tpg.connect.model.MatchSet;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Optional;

public interface MatchSetRepository {
    
    // Create Operations
    MatchSet createMatchSet(MatchSet matchSet);
    MatchSet save(MatchSet matchSet);
    
    // Read Operations
    Optional<MatchSet> findByConnectId(String connectId);
    Optional<MatchSet> findById(String id);
    List<MatchSet> findByUserId(String userId);
    List<MatchSet> findByUserIdAndDate(String userId, String date);
    List<MatchSet> findByUserIdOrderByDateDesc(String userId, int offset, int limit);
    List<MatchSet> findActiveMatchSets(String userId);
    long countByUserId(String userId);
    boolean existsByConnectId(String connectId);
    
    // Update Operations
    MatchSet updateMatchSet(MatchSet matchSet);
    MatchSet updateStatus(String connectId, String status);
    MatchSet markCompleted(String connectId, Timestamp completedAt);
    MatchSet updateProgress(String connectId, Integer actionsSubmitted, Integer matchesFound);
    MatchSet updateViewTime(String connectId, Integer viewTime, Integer avgTimePerProfile);
    
    // Delete Operations
    void deleteMatchSet(String connectId);
    
    // Query Operations
    List<MatchSet> findMatchSetsByDateRange(String userId, String startDate, String endDate);
    List<MatchSet> findPendingMatchSets();
    List<MatchSet> findCompletedMatchSets(String userId);
}