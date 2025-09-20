package com.tpg.connect.services;

import com.tpg.connect.repository.BlockedUserRepository;
import com.tpg.connect.repository.SafetyBlockRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.UserReportRepository;
import com.tpg.connect.model.dto.ReportUserRequest;
import com.tpg.connect.model.dto.SafetyBlockRequest;
import com.tpg.connect.model.BlockedUser;
import com.tpg.connect.model.SafetyBlock;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

        // Verify target user exists
        CompleteUserProfile targetUser = userProfileRepository.findByUserId(request.getTargetUserId());
        if (targetUser == null) {
            throw new IllegalArgumentException("Target user not found");
        }

        // Check for duplicate reports (same reporter, same target, same day)
        List<UserReport> todayReports = userReportRepository.findByReporterIdAndReportedUserIdAndReportedAtAfter(
            reporterId, request.getTargetUserId(), LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        
        if (!todayReports.isEmpty()) {
            throw new IllegalArgumentException("You have already reported this user today");
        }

        // Create report
        UserReport report = UserReport.builder()
                .connectId(UUID.randomUUID().toString())
                .reporterId(reporterId)
                .reportedUserId(request.getTargetUserId())
                .reasons(request.getReasons())
                .description(request.getDescription())
                .evidenceUrls(request.getEvidenceUrls())
                .context(request.getContext())
                .reportedAt(com.google.cloud.Timestamp.now())
                .status("PENDING")
                .build();

        userReportRepository.save(report);

        // Send notification to moderation team
        notificationService.sendReportNotificationToModerators(report);

        // Auto-block if this user has multiple reports
        checkForAutoBlock(request.getTargetUserId());

        return report.getConnectId();
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
                // Check for inappropriate content in bio
                return user.getBio() != null && 
                       (user.getBio().toLowerCase().contains("spam") || 
                        user.getBio().toLowerCase().contains("fake"));
            
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
        // Count recent reports for this user (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<UserReport> recentReports = userReportRepository.findByReportedUserIdAndReportedAtAfter(userId, weekAgo);
        
        // If user has 5+ reports in the last week, automatically disable their account
        if (recentReports.size() >= 5) {
            CompleteUserProfile user = userProfileRepository.findByUserId(userId);
            if (user != null && user.isActive()) {
                user.setActive(false);
                user.setUpdatedAt(LocalDateTime.now());
                userProfileRepository.save(user);
                
                // Notify moderation team
                notificationService.sendAutoBlockNotification(userId, recentReports.size());
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
}