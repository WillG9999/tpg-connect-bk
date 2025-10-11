package com.tpg.connect.services;

import com.tpg.connect.model.User;
import com.tpg.connect.model.dto.AdminUserSummaryDTO;
import com.tpg.connect.model.dto.AdminUserDetailDTO;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.UserStatus;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.MatchRepository;
import com.tpg.connect.repository.ConversationRepository;
import com.tpg.connect.repository.UserReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminUserManagementService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserReportRepository userReportRepository;
    
    /**
     * Get ALL users with complete data for admin dashboard initialization
     * This loads everything upfront so admin user management is instant
     */
    public List<AdminUserSummaryDTO> getAllUsersForAdminDashboard() {
        try {
            log.info("🚀 Preloading ALL user data for admin dashboard...");
            
            // Get all users with APPROVED applications (only these can be managed)
            List<User> users = userRepository.findByApplicationStatus(ApplicationStatus.APPROVED);
            log.info("📊 Found {} users with APPROVED applications", users.size());
            
            return users.stream().map(user -> {
                try {
                    // Get user profile
                    Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(user.getConnectId());
                    CompleteUserProfile profile = profileOpt.orElse(null);
                    
                    // Calculate user stats in batch (this could be optimized further with batch queries)
                    int totalMatches = (int) matchRepository.countByUserId(user.getConnectId());
                    int totalConversations = (int) conversationRepository.countByUserId(user.getConnectId());
                    int totalReports = (int) userReportRepository.countByUserId(user.getConnectId());
                    boolean hasActiveReports = userReportRepository.hasActiveReports(user.getConnectId());
                    
                    // Calculate age if profile has date of birth
                    Integer age = null;
                    if (profile != null && profile.getDateOfBirth() != null) {
                        try {
                            LocalDate birthDate = profile.getDateOfBirth();
                            age = Period.between(birthDate, LocalDate.now()).getYears();
                        } catch (Exception e) {
                            log.warn("⚠️ Could not calculate age for user {}: {}", user.getConnectId(), e.getMessage());
                        }
                    }
                    
                    // Get profile photo URL
                    String profilePhotoUrl = null;
                    if (profile != null && profile.getPhotos() != null && !profile.getPhotos().isEmpty()) {
                        profilePhotoUrl = profile.getPhotos().get(0).getUrl();
                    }
                    
                    // Default to ACTIVE if no userStatus is set (for existing users)
                    UserStatus userStatus = user.getUserStatus() != null ? user.getUserStatus() : UserStatus.ACTIVE;
                    
                    return AdminUserSummaryDTO.builder()
                            .connectId(user.getConnectId())
                            .firstName(profile != null ? profile.getFirstName() : "")
                            .lastName(profile != null ? profile.getLastName() : "")
                            .email(user.getEmail())
                            .profilePhotoUrl(profilePhotoUrl)
                            .applicationStatus(user.getApplicationStatus())
                            .userStatus(userStatus)
                            .active(user.getActive())
                            .emailVerified(user.getEmailVerified())
                            .createdAt(user.getCreatedAt())
                            .lastActiveAt(user.getLastActiveAt())
                            .totalMatches(totalMatches)
                            .totalConversations(totalConversations)
                            .totalReports(totalReports)
                            .hasActiveReports(hasActiveReports)
                            .location(profile != null ? profile.getLocation() : null)
                            .age(age)
                            .gender(profile != null ? profile.getGender() : null)
                            .build();
                            
                } catch (Exception e) {
                    log.error("❌ Error processing user {} for admin dashboard: ", user.getConnectId(), e);
                    // Return minimal user data on error
                    return AdminUserSummaryDTO.builder()
                            .connectId(user.getConnectId())
                            .email(user.getEmail())
                            .applicationStatus(user.getApplicationStatus())
                            .userStatus(user.getUserStatus() != null ? user.getUserStatus() : UserStatus.ACTIVE)
                            .active(user.getActive())
                            .emailVerified(user.getEmailVerified())
                            .createdAt(user.getCreatedAt())
                            .totalMatches(0)
                            .totalConversations(0)
                            .totalReports(0)
                            .hasActiveReports(false)
                            .build();
                }
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Failed to preload user data for admin dashboard: ", e);
            throw new RuntimeException("Failed to load user management data", e);
        }
    }
    
    /**
     * Update user status (ACTIVE, SUSPENDED, BANNED)
     */
    public boolean updateUserStatus(String connectId, UserStatus newStatus, String adminId, String reason) {
        try {
            log.info("🔄 Admin {} updating user {} status to {}", adminId, connectId, newStatus);
            
            User user = userRepository.findByConnectId(connectId).orElse(null);
            if (user == null) {
                log.warn("⚠️ User not found: {}", connectId);
                return false;
            }
            
            // Only allow status changes for users with APPROVED applications
            if (user.getApplicationStatus() != ApplicationStatus.APPROVED) {
                log.warn("⚠️ Cannot change status for user {} - application not approved", connectId);
                return false;
            }
            
            UserStatus oldStatus = user.getUserStatus();
            user.setUserStatus(newStatus);
            
            // Update active flag based on status
            user.setActive(newStatus == UserStatus.ACTIVE);
            
            userRepository.save(user);
            
            log.info("✅ User {} status updated from {} to {} by admin {}", 
                    connectId, oldStatus, newStatus, adminId);
            
            // TODO: Send notification to user about status change
            // TODO: Log admin action for audit trail
            
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to update user status for {}: ", connectId, e);
            return false;
        }
    }
    
    /**
     * Get detailed user information for admin review
     */
    public AdminUserDetailDTO getUserDetailForAdmin(String connectId) {
        try {
            log.info("👤 Getting detailed user info for admin: {}", connectId);
            
            User user = userRepository.findByConnectId(connectId).orElse(null);
            if (user == null) {
                return null;
            }
            
            // Get complete user profile
            Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(connectId);
            CompleteUserProfile profile = profileOpt.orElse(null);
            
            // Build activity stats
            AdminUserDetailDTO.UserActivityStats activityStats = AdminUserDetailDTO.UserActivityStats.builder()
                    .totalMatches((int) matchRepository.countByUserId(connectId))
                    .totalConversations((int) conversationRepository.countByUserId(connectId))
                    .totalReportsAgainst((int) userReportRepository.countByUserId(connectId))
                    .lastLogin(user.getLastLoginAt())
                    .build();
            
            return AdminUserDetailDTO.builder()
                    .connectId(user.getConnectId())
                    .email(user.getEmail())
                    .applicationStatus(user.getApplicationStatus())
                    .userStatus(user.getUserStatus() != null ? user.getUserStatus() : UserStatus.ACTIVE)
                    .active(user.getActive())
                    .emailVerified(user.getEmailVerified())
                    .createdAt(user.getCreatedAt())
                    .lastActiveAt(user.getLastActiveAt())
                    .role(user.getRole())
                    .profile(profile)
                    .activityStats(activityStats)
                    .build();
                    
        } catch (Exception e) {
            log.error("❌ Failed to get user detail for admin: ", e);
            return null;
        }
    }
    
    /**
     * Get total count of users for admin dashboard stats
     */
    public long getTotalUsersCount() {
        return userRepository.countByApplicationStatus(ApplicationStatus.APPROVED);
    }
    
    /**
     * Get count by user status
     */
    public long getUserCountByStatus(UserStatus status) {
        return userRepository.countByUserStatus(status);
    }
}