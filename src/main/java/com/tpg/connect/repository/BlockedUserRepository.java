package com.tpg.connect.repository;

import com.tpg.connect.model.BlockedUser;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository {
    
    // Create Operations
    BlockedUser createBlock(BlockedUser blockedUser);
    BlockedUser save(BlockedUser blockedUser);
    
    // Read Operations
    Optional<BlockedUser> findByConnectId(String connectId);
    BlockedUser findByUserIdAndBlockedUserId(String userId, String blockedUserId);
    List<BlockedUser> findByUserId(String userId);
    List<BlockedUser> findByUserIdAndStatus(String userId, Object status);
    List<BlockedUser> findByBlockedUserId(String blockedUserId);
    boolean isUserBlocked(String userId, String blockedUserId);
    boolean existsByConnectId(String connectId);
    
    // Update Operations
    BlockedUser updateBlock(BlockedUser blockedUser);
    BlockedUser updateStatus(String connectId, String status);
    BlockedUser addReview(String connectId, String reviewedBy, Timestamp reviewedAt, String adminNotes);
    BlockedUser unblockUser(String connectId, String unblockReason, String unblockedBy, Timestamp unblockedAt);
    
    // Delete Operations
    void deleteBlock(String connectId);
    
    // Query Operations
    List<BlockedUser> findActiveBlocks(String userId);
    List<BlockedUser> findBlocksByType(String blockType);
    List<BlockedUser> findBlocksBySeverity(String severity);
    List<BlockedUser> findBlocksNeedingReview();
}