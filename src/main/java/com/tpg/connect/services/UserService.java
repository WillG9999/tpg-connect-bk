package com.tpg.connect.services;

import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.MatchRepository;
import com.tpg.connect.repository.ConversationRepository;
import com.tpg.connect.repository.UserReportRepository;
import com.tpg.connect.repository.UserActionRepository;
import com.tpg.connect.model.User;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.UserProfile;
import com.tpg.connect.model.dto.AdminUserSummaryDTO;
import com.tpg.connect.model.dto.AdminUserDetailDTO;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.model.conversation.ConversationSummary;
import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.services.SafetyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

@Service
public class UserService {
    
    // TODO: Implement premium subscription validation
    // TODO: Add payment gateway integration (Stripe/Apple Pay/Google Pay)
    // TODO: Handle subscription status and expiry dates

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SafetyService safetyService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Autowired
    private MatchRepository matchRepository;
    
    @Autowired
    private ConversationRepository conversationRepository;
    
    @Autowired
    private UserReportRepository userReportRepository;
    
    @Autowired
    private UserActionRepository userActionRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    public User createUser(String username, String email, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        String userId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .connectId(userId)
                .username(username)
                .email(email)
                .passwordHash(password)
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null) {
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public boolean deactivateUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean activateUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    
    public Map<String, Object> blockUser(String userId, String targetUserId) {
        try {
            // Delegate to safety service
            safetyService.blockUser(userId, targetUserId, "Blocked by user");
            return Map.of("success", true, "message", "User blocked successfully", "blockedUserId", targetUserId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to block user: " + e.getMessage());
        }
    }
    
    public Map<String, Object> reportUser(String userId, String targetUserId, String reason) {
        try {
            // Create a basic report request
            com.tpg.connect.model.dto.ReportUserRequest request = new com.tpg.connect.model.dto.ReportUserRequest();
            request.setTargetUserId(targetUserId);
            request.setUserInfo("Reported user"); // Default value since it's required
            request.setReason(reason);
            request.setLocation("Through app messaging"); // Default value since it's required
            request.setDescription("User reported via admin action"); // Default value since it's required
            
            String reportId = safetyService.reportUser(userId, request);
            return Map.of(
                "success", true, 
                "message", "User reported successfully",
                "reportId", reportId,
                "reportedUserId", targetUserId
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to report user: " + e.getMessage());
        }
    }
    
    // Deactivate user method
    public boolean deactivateUser(String userId) {
        try {
            // Find user by ID and deactivate
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setActive(false);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage());
        }
    }
    
    public CompleteUserProfile getUserProfile(String userId) {
        return userProfileRepository.findByUserId(userId);
    }
    
    // Admin-specific methods for user management
    
    /**
     * Get all users for admin with pagination and filtering
     */
    public List<AdminUserSummaryDTO> getAllUsersForAdmin(int page, int size, String search, 
                                                        String status, String sortBy, String sortDirection) {
        try {
            List<User> users = userRepository.findAllForAdmin(page, size, search, status, sortBy, sortDirection);
            
            return users.stream().map(user -> {
                Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(user.getConnectId());
                CompleteUserProfile profile = profileOpt.orElse(null);
                
                // Calculate user stats
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
                        // Handle invalid date format
                    }
                }
                
                return AdminUserSummaryDTO.builder()
                        .connectId(user.getConnectId())
                        .firstName(profile != null ? profile.getFirstName() : null)
                        .lastName(profile != null ? profile.getLastName() : null)
                        .email(user.getEmail())
                        .profilePhotoUrl(profile != null && profile.getPhotos() != null && !profile.getPhotos().isEmpty() 
                                ? profile.getPhotos().get(0).getUrl() : null)
                        .applicationStatus(user.getApplicationStatus() != null ? user.getApplicationStatus() : ApplicationStatus.PENDING_APPROVAL)
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
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get users for admin: " + e.getMessage());
        }
    }
    
    /**
     * Get total users count for pagination
     */
    public long getTotalUsersCount(String search, String status) {
        return userRepository.countForAdmin(search, status);
    }
    
    /**
     * Get detailed user information for admin
     */
    public AdminUserDetailDTO getUserDetailForAdmin(String connectId) {
        try {
            User user = userRepository.findById(connectId).orElse(null);
            if (user == null) return null;
            
            Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(connectId);
            CompleteUserProfile profile = profileOpt.orElse(null);
            
            // Get activity stats
            AdminUserDetailDTO.UserActivityStats activityStats = getUserActivityStats(connectId);
            
            // Get recent data (limited for performance)
            List<Match> recentMatches = matchRepository.findRecentByUserId(connectId, null, 10);
            List<Conversation> recentConversations = conversationRepository.findRecentByUserId(connectId, 10);
            List<UserReport> reportsInvolving = userReportRepository.findByUserId(connectId, 20);
            List<UserAction> recentActions = userActionRepository.findRecentByUserId(connectId, 20);
            
            return AdminUserDetailDTO.builder()
                    .connectId(user.getConnectId())
                    .email(user.getEmail())
                    .applicationStatus(user.getApplicationStatus() != null ? user.getApplicationStatus() : ApplicationStatus.PENDING_APPROVAL)
                    .active(user.getActive())
                    .emailVerified(user.getEmailVerified())
                    .createdAt(user.getCreatedAt())
                    .lastActiveAt(user.getLastActiveAt())
                    .role(user.getRole())
                    .profile(profile)
                    .activityStats(activityStats)
                    .recentMatches(recentMatches)
                    .recentConversations(recentConversations)
                    .reportsInvolving(reportsInvolving)
                    .recentActions(recentActions)
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user detail for admin: " + e.getMessage());
        }
    }
    
    /**
     * Calculate user activity statistics
     */
    private AdminUserDetailDTO.UserActivityStats getUserActivityStats(String connectId) {
        try {
            int totalMatches = (int) matchRepository.countByUserId(connectId);
            int totalConversations = (int) conversationRepository.countByUserId(connectId);
            int totalMessages = (int) conversationRepository.countMessagesByUserId(connectId);
            int totalLikes = (int) userActionRepository.countByUserIdAndAction(connectId, "LIKE");
            int totalPasses = (int) userActionRepository.countByUserIdAndAction(connectId, "PASS");
            int totalReportsBy = (int) userReportRepository.countByReporterId(connectId);
            int totalReportsAgainst = (int) userReportRepository.countByReportedUserId(connectId);
            
            // Calculate days active (placeholder - would need login tracking)
            int daysActive = 0; // TODO: Implement based on login history
            int loginCount = 0; // TODO: Implement based on login history
            
            return AdminUserDetailDTO.UserActivityStats.builder()
                    .totalMatches(totalMatches)
                    .totalConversations(totalConversations)
                    .totalMessages(totalMessages)
                    .totalLikes(totalLikes)
                    .totalPasses(totalPasses)
                    .totalReportsBy(totalReportsBy)
                    .totalReportsAgainst(totalReportsAgainst)
                    .daysActive(daysActive)
                    .lastLogin(null) // TODO: Implement
                    .loginCount(loginCount)
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate user activity stats: " + e.getMessage());
        }
    }
}