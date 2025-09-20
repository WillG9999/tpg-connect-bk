package com.tpg.connect.repository;

import com.tpg.connect.model.match.Match;
import java.util.List;
import java.util.Optional;

public interface MatchRepository {
    
    // Create Operations
    Match save(Match match);
    
    // Read Operations
    Optional<Match> findById(String id);
    Optional<Match> findByUserIds(String userId1, String userId2);
    Optional<Match> findByUserIdsAndStatus(String userId1, String userId2, Object status);
    List<Match> findByUserId(String userId);
    List<Match> findByUserIdAndStatus(String userId, Object status);
    List<Match> findActiveMatchesByUserId(String userId);
    List<Match> findRecentMatchesByUserId(String userId, int days);
    List<Match> findRecentByUserId(String userId, Object status, int days);
    long countMatchesByUserId(String userId);
    long countByUserIdAndStatus(String userId, Object status);
    
    // Update Operations
    Match updateLastActivity(String id);
    Match updateMatchStatus(String id, String status);
    
    // Delete Operations
    void deleteById(String id);
    void deleteByUserId(String userId);
    void unmatchUsers(String userId1, String userId2);
}