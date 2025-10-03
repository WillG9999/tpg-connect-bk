package com.tpg.connect.services;

import com.tpg.connect.repository.BlockedUserRepository;
import com.tpg.connect.repository.SafetyBlockRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.UserReportRepository;
import com.tpg.connect.repository.ReportedUserRepository;
import com.tpg.connect.model.dto.ReportUserRequest;
import com.tpg.connect.model.dto.SafetyBlockRequest;
import com.tpg.connect.model.BlockedUser;
import com.tpg.connect.model.SafetyBlock;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.ReportedUser;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SafetyService {

    @Autowired
    private BlockedUserRepository blockedUserRepository;

    @Autowired
    private UserReportRepository userReportRepository;

    @Autowired
    private ReportedUserRepository reportedUserRepository;

    @Autowired
    private SafetyBlockRepository safetyBlockRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    public void blockUser(String userId, String targetUserId, String reason) {
        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }

        // Check if user is already blocked
        BlockedUser existingBlock = blockedUserRepository.findByUserIdAndBlockedUserId(userId, targetUserId);
        if (existingBlock != null && "ACTIVE".equals(existingBlock.getStatus())) {
            throw new IllegalArgumentException("User is already blocked");
        }

        // Verify target user exists
        CompleteUserProfile targetUser = userProfileRepository.findByUserId(targetUserId);
        if (targetUser == null) {
            throw new IllegalArgumentException("Target user not found");
        }

        // Create or update block record
        BlockedUser blockedUser;
        if (existingBlock != null) {
            // Reactivate existing block
            existingBlock.setStatus("ACTIVE");
            existingBlock.setReason(reason);
            existingBlock.setBlockedAt(com.google.cloud.Timestamp.now());
            blockedUser = existingBlock;
        } else {
            // Create new block
            blockedUser = BlockedUser.builder()
                    .connectId(UUID.randomUUID().toString())
                    .userId(userId)
                    .blockedUserId(targetUserId)
                    .reason(reason)
                    .blockedAt(com.google.cloud.Timestamp.now())
                    .status("ACTIVE")
                    .build();
        }

        blockedUserRepository.save(blockedUser);

        // Clear cache
        clearUserBlockCache(userId);

        // Send notification to admin if needed
        notificationService.sendBlockNotificationToAdmin(userId, targetUserId, reason);
    }

    public void unblockUser(String userId, String targetUserId) {
        BlockedUser blockedUser = blockedUserRepository.findByUserIdAndBlockedUserId(userId, targetUserId);
        if (blockedUser == null || !"ACTIVE".equals(blockedUser.getStatus())) {
            throw new IllegalArgumentException("User is not blocked");
        }

        blockedUser.setStatus("REMOVED");
        blockedUserRepository.save(blockedUser);

        // Clear cache
        clearUserBlockCache(userId);
    }

    @Cacheable(value = "blockedUsers", key = "#userId")
    public List<BlockedUser> getBlockedUsers(String userId) {
        return blockedUserRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    public boolean isUserBlocked(String userId, String targetUserId) {
        BlockedUser block = blockedUserRepository.findByUserIdAndBlockedUserId(userId, targetUserId);
        return block != null && "ACTIVE".equals(block.getStatus());
    }

    public boolean areUsersBlockingEachOther(String user1Id, String user2Id) {
        return isUserBlocked(user1Id, user2Id) || isUserBlocked(user2Id, user1Id);
    }

    public String reportUser(String reporterId, ReportUserRequest request) {
        if (reporterId.equals(request.getTargetUserId())) {
            throw new IllegalArgumentException("Cannot report yourself");
        }

        // Check if target user exists in database
        CompleteUserProfile targetUser = userProfileRepository.findByUserId(request.getTargetUserId());
        boolean isAdminReport = targetUser == null;
        
        // For admin reports, allow manual user identifiers
        // For regular user reports, require target user to exist
        if (isAdminReport) {
            System.out.println("📋 SafetyService: Admin report - target user '" + request.getTargetUserId() + "' not found in database, proceeding with manual identifier");
        } else {
            System.out.println("📋 SafetyService: Regular user report - target user '" + request.getTargetUserId() + "' found in database");
        }

        String reportId = UUID.randomUUID().toString();
        com.google.cloud.Timestamp now = com.google.cloud.Timestamp.now();

        // Create individual report
        ReportedUser.IndividualReport individualReport = ReportedUser.IndividualReport.builder()
                .reportId(reportId)
                .reporterId(reporterId)
                .reason(request.getReason())
                .location(request.getLocation())
                .description(request.getDescription())
                .reportedAt(now)
                .status("PENDING")
                .build();

        // Check if reported user already exists
        Optional<ReportedUser> existingReportedUser = reportedUserRepository.findByReportedUserId(request.getTargetUserId());
        
        if (existingReportedUser.isPresent()) {
            // Add to existing reported user
            ReportedUser reportedUser = existingReportedUser.get();
            
            // Check for duplicate reports from same reporter
            boolean alreadyReported = reportedUser.getReports().stream()
                    .anyMatch(report -> report.getReporterId().equals(reporterId));
            
            if (alreadyReported) {
                throw new IllegalArgumentException("You have already reported this user");
            }
            
            // Add new report to existing list
            reportedUser.getReports().add(individualReport);
            reportedUser.setTotalReports(reportedUser.getTotalReports() + 1);
            reportedUser.setUpdatedAt(now);
            
            reportedUserRepository.save(reportedUser);
            
        } else {
            // Create new reported user
            ReportedUser.AdminReview adminReview = ReportedUser.AdminReview.builder()
                    .status("PENDING")
                    .build();
            
            ReportedUser reportedUser = ReportedUser.builder()
                    .reportedUserId(request.getTargetUserId())
                    .reportedUserInfo(request.getUserInfo())
                    .totalReports(1)
                    .reports(List.of(individualReport))
                    .adminReview(adminReview)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            
            reportedUserRepository.save(reportedUser);
        }

        // Send notification to moderation team
        // notificationService.sendReportNotificationToModerators(individualReport);

        // Auto-block if this user has multiple reports (only for real users, not admin manual reports)
        if (!isAdminReport) {
            checkForAutoBlock(request.getTargetUserId());
        } else {
            System.out.println("📋 SafetyService: Skipping auto-block check for admin report with manual identifier");
        }

        System.out.println("✅ SafetyService: Report created with ID: " + reportId + " for user: " + request.getTargetUserId());
        return reportId;
    }

    @Cacheable(value = "safetyBlocks", key = "#userId")
    public List<SafetyBlock> getUserSafetyBlocks(String userId) {
        return safetyBlockRepository.findByUserIdAndEnabled(userId, true);
    }

    public SafetyBlock createSafetyBlock(String userId, SafetyBlockRequest request) {
        validateSafetyBlockRequest(request);

        SafetyBlock safetyBlock = SafetyBlock.builder()
                .connectId(UUID.randomUUID().toString())
                .userId(userId)
                .blockType(request.getBlockType())
                .severity("MEDIUM") // Default severity
                .reason(request.getDescription() != null ? request.getDescription() : "Pattern-based safety block")
                .blockedAt(com.google.cloud.Timestamp.now())
                .status("ACTIVE")
                .isActive(true)
                .createdBy("SYSTEM")
                .build();

        SafetyBlock saved = safetyBlockRepository.save(safetyBlock);

        // Clear cache
        clearSafetyBlockCache(userId);

        return saved;
    }

    public SafetyBlock updateSafetyBlock(String userId, String safetyBlockId, SafetyBlockRequest request) {
        SafetyBlock safetyBlock = safetyBlockRepository.findById(safetyBlockId).orElse(null);
        if (safetyBlock == null || !safetyBlock.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Safety block not found or not owned by user");
        }

        validateSafetyBlockRequest(request);

        // Update the safety block fields using builder pattern
        SafetyBlock updatedBlock = SafetyBlock.builder()
                .connectId(safetyBlock.getConnectId())
                .userId(safetyBlock.getUserId())
                .blockType(request.getBlockType())
                .severity(safetyBlock.getSeverity()) // Keep existing severity
                .reason(request.getDescription() != null ? request.getDescription() : safetyBlock.getReason())
                .blockedAt(safetyBlock.getBlockedAt())
                .status(safetyBlock.getStatus())
                .isActive(safetyBlock.getIsActive())
                .createdBy(safetyBlock.getCreatedBy())
                .build();

        SafetyBlock saved = safetyBlockRepository.save(updatedBlock);

        // Clear cache
        clearSafetyBlockCache(userId);

        return saved;
    }

    public void deleteSafetyBlock(String userId, String safetyBlockId) {
        SafetyBlock safetyBlock = safetyBlockRepository.findById(safetyBlockId).orElse(null);
        if (safetyBlock == null || !safetyBlock.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Safety block not found or not owned by user");
        }

        safetyBlockRepository.delete(safetyBlock);

        // Clear cache
        clearSafetyBlockCache(userId);
    }

    public boolean shouldBlockUserBySafetyRules(String userId, CompleteUserProfile candidateUser) {
        List<SafetyBlock> userSafetyBlocks = getUserSafetyBlocks(userId);
        
        for (SafetyBlock safetyBlock : userSafetyBlocks) {
            if (matchesSafetyBlock(safetyBlock, candidateUser)) {
                // Safety block matched - user should be blocked
                return true;
            }
        }
        
        return false;
    }

    public List<CompleteUserProfile> filterUsersBySafetyRules(String userId, List<CompleteUserProfile> candidates) {
        return candidates.stream()
                .filter(candidate -> !shouldBlockUserBySafetyRules(userId, candidate))
                .collect(Collectors.toList());
    }

    private boolean matchesSafetyBlock(SafetyBlock safetyBlock, CompleteUserProfile user) {
        // Check if safety block is active
        if (!safetyBlock.getIsActive()) {
            return false;
        }
        
        // For now, implement basic matching based on block type
        String blockType = safetyBlock.getBlockType();

        // Simple block type matching - can be enhanced later
        switch (blockType) {
            case "AGE_RESTRICTION":
                return user.getAge() < 18;
            
            case "LOCATION_BLOCK":
                // Block users from certain locations if specified in reason
                return false; // Placeholder
            
            case "CONTENT_FILTER":
                // Content filter checks removed (bio field no longer exists)
                return false;
            
            default:
                return false;
        }
    }

    private void validateSafetyBlockRequest(SafetyBlockRequest request) {
        if (request.getBlockType() == null || request.getBlockType().trim().isEmpty()) {
            throw new IllegalArgumentException("Block type cannot be empty");
        }

        // Validate block type - for now just check it's a valid string
        String blockType = request.getBlockType().toUpperCase();
        if (!blockType.matches("[A-Z_]+")) {
            throw new IllegalArgumentException("Invalid block type format: " + request.getBlockType());
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
    }

    private void checkForAutoBlock(String userId) {
        // Check total reports for this user
        Optional<ReportedUser> reportedUser = reportedUserRepository.findByReportedUserId(userId);
        
        if (reportedUser.isPresent()) {
            int totalReports = reportedUser.get().getTotalReports();
            
            // If user has 5+ reports, automatically disable their account
            if (totalReports >= 5) {
                CompleteUserProfile user = userProfileRepository.findByUserId(userId);
                if (user != null && user.getActive()) {
                    user.setActive(false);
                    user.setUpdatedAt(LocalDateTime.now());
                    userProfileRepository.save(user);
                    
                    // Update admin review status to escalated
                    ReportedUser reportedUserDoc = reportedUser.get();
                    if (reportedUserDoc.getAdminReview() != null) {
                        reportedUserDoc.getAdminReview().setStatus("AUTO_BLOCKED");
                        reportedUserDoc.getAdminReview().setActionTaken("Account automatically disabled due to multiple reports");
                        reportedUserDoc.getAdminReview().setReviewedAt(com.google.cloud.Timestamp.now());
                        reportedUserRepository.save(reportedUserDoc);
                    }
                    
                    // Notify moderation team
                    // notificationService.sendAutoBlockNotification(userId, totalReports);
                    System.out.println("🚨 SafetyService: Auto-blocked user " + userId + " due to " + totalReports + " reports");
                }
            }
        }
    }

    @CacheEvict(value = "blockedUsers", key = "#userId")
    private void clearUserBlockCache(String userId) {
        // Cache cleared by annotation
    }

    @CacheEvict(value = "safetyBlocks", key = "#userId")
    private void clearSafetyBlockCache(String userId) {
        // Cache cleared by annotation
    }
    
    // Admin-specific methods
    
    /**
     * Get reports involving user for admin review
     */
    public List<UserReport> getUserReportsForAdmin(String connectId, int page, int size) {
        return userReportRepository.findByUserId(connectId, size);
    }
    
    /**
     * Get total reports count for pagination
     */
    public long getTotalReportsCount(String connectId) {
        return userReportRepository.countByUserId(connectId);
    }
}