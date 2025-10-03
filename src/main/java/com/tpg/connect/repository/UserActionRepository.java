package com.tpg.connect.repository;

import com.tpg.connect.model.match.UserAction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserActionRepository {
    
    // Create Operations
    UserAction save(UserAction userAction);
    
    // Read Operations
    Optional<UserAction> findById(String id);
    Optional<UserAction> findByUserIdAndTargetUserId(String userId, String targetUserId);
    List<UserAction> findByUserId(String userId);
    List<UserAction> findByUserIdAndAction(String userId, String action);
    List<UserAction> findByTargetUserId(String targetUserId);
    List<UserAction> findLikesByUserId(String userId);
    List<UserAction> findPassesByUserId(String userId);
    boolean hasUserActedOnTarget(String userId, String targetUserId);
    boolean existsByUserIdAndTargetUserIdAndAction(String userId, String targetUserId, Object action);
    boolean existsByUserIdAndTargetUserId(String userId, String targetUserId);
    List<UserAction> findByUserIdAndTimestampAfter(String userId, LocalDateTime cutoffDate);
    
    // Update Operations
    UserAction updateAction(String id, String newAction);
    
    // Delete Operations
    void deleteById(String id);
    void deleteByUserId(String userId);
    void deleteByUserIdAndTargetUserId(String userId, String targetUserId);
    
    // Statistics Operations
    long countByUserIdAndAction(String userId, String action);
    List<UserAction> findRecentByUserId(String userId, int limit);
    long countByUserId(String userId);
}