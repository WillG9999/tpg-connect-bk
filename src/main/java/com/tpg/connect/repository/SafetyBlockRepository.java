package com.tpg.connect.repository;

import com.tpg.connect.model.SafetyBlock;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Optional;

public interface SafetyBlockRepository {
    
    // Create Operations
    SafetyBlock createSafetyBlock(SafetyBlock safetyBlock);
    SafetyBlock save(SafetyBlock safetyBlock);
    
    // Read Operations
    Optional<SafetyBlock> findByConnectId(String connectId);
    Optional<SafetyBlock> findById(String id);
    List<SafetyBlock> findByUserId(String userId);
    List<SafetyBlock> findByUserIdAndEnabled(String userId, boolean enabled);
    List<SafetyBlock> findActiveSafetyBlocks(String userId);
    boolean isUserBlocked(String userId);
    boolean existsByConnectId(String connectId);
    
    // Delete Operations
    void delete(SafetyBlock safetyBlock);
    
    // Update Operations
    SafetyBlock updateSafetyBlock(SafetyBlock safetyBlock);
    SafetyBlock updateStatus(String connectId, String status, Boolean isActive);
    SafetyBlock liftBlock(String connectId, String liftedBy, Timestamp liftedAt);
    SafetyBlock submitAppeal(String connectId, String appealReason, Timestamp appealedAt);
    SafetyBlock reviewAppeal(String connectId, String appealReviewedBy, String appealDecision, Timestamp appealReviewedAt);
    SafetyBlock addReportId(String connectId, String reportId);
    SafetyBlock updateRestrictedFeatures(String connectId, List<String> restrictedFeatures);
    
    // Delete Operations
    void deleteSafetyBlock(String connectId);
    
    // Query Operations
    List<SafetyBlock> findActiveBlocks();
    List<SafetyBlock> findExpiredBlocks();
    List<SafetyBlock> findBlocksByType(String blockType);
    List<SafetyBlock> findBlocksBySeverity(String severity);
    List<SafetyBlock> findBlocksNeedingReview();
    List<SafetyBlock> findPendingAppeals();
    List<SafetyBlock> findSystemTriggeredBlocks();
}