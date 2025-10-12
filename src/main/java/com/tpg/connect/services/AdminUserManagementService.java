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
import com.tpg.connect.repository.UserActionRepository;
import com.tpg.connect.repository.UserBlockedRepository;
import com.tpg.connect.repository.BlockedUserRepository;
import com.tpg.connect.repository.NotificationRepository;
import com.tpg.connect.repository.SubscriptionRepository;
import com.tpg.connect.repository.MatchSetRepository;
import com.tpg.connect.repository.UserActivityRepository;
import com.tpg.connect.model.UserBlocked;
import com.tpg.connect.model.UserActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private UserActionRepository userActionRepository;
    
    @Autowired
    private UserBlockedRepository userBlockedRepository;
    
    @Autowired
    private BlockedUserRepository blockedUserRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    @Autowired
    private MatchSetRepository matchSetRepository;
    
    @Autowired
    private UserActivityRepository userActivityRepository;
    
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
    
    /**
     * Get raw JSON data from ALL database collections containing this user's connectId
     */
    public Map<String, Object> getUserRawData(String connectId) {
        try {
            log.info("🔍 Getting raw database data from ALL collections for user: {}", connectId);
            
            Map<String, Object> rawData = new HashMap<>();
            
            // 1. userAuth collection
            try {
                User user = userRepository.findByConnectId(connectId).orElse(null);
                if (user != null) {
                    rawData.put("userAuth", convertUserToRawMap(user));
                    log.info("✅ Found data in userAuth collection");
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userAuth: {}", e.getMessage());
            }
            
            // 2. userProfiles collection  
            try {
                Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(connectId);
                if (profileOpt.isPresent()) {
                    rawData.put("userProfiles", convertProfileToRawMap(profileOpt.get()));
                    log.info("✅ Found data in userProfiles collection");
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userProfiles: {}", e.getMessage());
            }
            
            // 3. matches collection (where user is participant)
            try {
                List<Object> matches = matchRepository.findByUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!matches.isEmpty()) {
                    rawData.put("matches", matches);
                    log.info("✅ Found {} matches in matches collection", matches.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from matches: {}", e.getMessage());
            }
            
            // 4. conversations collection (where user is participant)
            try {
                List<Object> conversations = conversationRepository.findByParticipantId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!conversations.isEmpty()) {
                    rawData.put("conversations", conversations);
                    log.info("✅ Found {} conversations in conversations collection", conversations.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from conversations: {}", e.getMessage());
            }
            
            // 5. userActions collection (user's swipe actions)
            try {
                List<Object> userActions = userActionRepository.findByUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!userActions.isEmpty()) {
                    rawData.put("userActions", userActions);
                    log.info("✅ Found {} user actions in userActions collection", userActions.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userActions: {}", e.getMessage());
            }
            
            // 6. userReports collection (reports involving this user)
            try {
                // Get reports where this user was reported
                List<Object> reports = userReportRepository.findByReportedUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                
                // Also get reports this user made
                List<Object> reportsMade = userReportRepository.findByReporterId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                
                if (!reports.isEmpty() || !reportsMade.isEmpty()) {
                    Map<String, Object> allReports = new HashMap<>();
                    if (!reports.isEmpty()) {
                        allReports.put("reportsAgainstUser", reports);
                    }
                    if (!reportsMade.isEmpty()) {
                        allReports.put("reportsByUser", reportsMade);
                    }
                    rawData.put("userReports", allReports);
                    log.info("✅ Found {} reports against user and {} reports by user in userReports collection", 
                        reports.size(), reportsMade.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userReports: {}", e.getMessage());
            }
            
            // 7. userBlocked collection (users this user has blocked)
            try {
                UserBlocked blockedConfig = userBlockedRepository.findByConnectId(connectId);
                if (blockedConfig != null) {
                    rawData.put("userBlocked", convertObjectToRawMap(blockedConfig));
                    log.info("✅ Found blocking configuration in userBlocked collection");
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userBlocked: {}", e.getMessage());
            }
            
            // 8. blockedUsers collection (users who have blocked this user)
            try {
                List<Object> blockedBy = blockedUserRepository.findByBlockedUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!blockedBy.isEmpty()) {
                    rawData.put("blockedUsers", blockedBy);
                    log.info("✅ Found {} users who blocked this user in blockedUsers collection", blockedBy.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from blockedUsers: {}", e.getMessage());
            }
            
            // 9. notifications collection
            try {
                List<Object> notifications = notificationRepository.findByUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!notifications.isEmpty()) {
                    rawData.put("notifications", notifications);
                    log.info("✅ Found {} notifications in notifications collection", notifications.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from notifications: {}", e.getMessage());
            }
            
            // 10. subscriptions collection
            try {
                List<Object> subscriptions = subscriptionRepository.findByUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!subscriptions.isEmpty()) {
                    rawData.put("subscriptions", subscriptions);
                    log.info("✅ Found {} subscriptions in subscriptions collection", subscriptions.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from subscriptions: {}", e.getMessage());
            }
            
            // 11. matchSets collection (daily batches for this user)
            try {
                List<Object> matchSets = matchSetRepository.findByUserId(connectId).stream()
                    .map(this::convertObjectToRawMap)
                    .collect(Collectors.toList());
                if (!matchSets.isEmpty()) {
                    rawData.put("matchSets", matchSets);
                    log.info("✅ Found {} match sets in matchSets collection", matchSets.size());
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from matchSets: {}", e.getMessage());
            }
            
            // 12. userActivity collection (if exists)
            try {
                Optional<UserActivity> activityOpt = userActivityRepository.findByConnectId(connectId);
                if (activityOpt.isPresent()) {
                    rawData.put("userActivity", convertObjectToRawMap(activityOpt.get()));
                    log.info("✅ Found user activity in userActivity collection");
                }
            } catch (Exception e) {
                log.warn("⚠️ Error fetching from userActivity: {}", e.getMessage());
            }
            
            // Add comprehensive metadata
            rawData.put("metadata", Map.of(
                "connectId", connectId,
                "totalCollections", rawData.keySet().size(),
                "collectionsFound", rawData.keySet().stream()
                    .filter(key -> !key.equals("metadata"))
                    .collect(Collectors.toList()),
                "retrievedAt", LocalDateTime.now().toString(),
                "note", "Complete raw data from ALL Firestore collections containing this user's connectId"
            ));
            
            log.info("✅ Retrieved complete raw data for user: {} from {} collections", 
                connectId, rawData.keySet().size() - 1);
            return rawData;
            
        } catch (Exception e) {
            log.error("❌ Failed to get complete raw data for user {}: ", connectId, e);
            return new HashMap<>();
        }
    }
    
    private Map<String, Object> convertUserToRawMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", user.getConnectId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("passwordHash", "***HIDDEN***"); // Don't expose password hash
        map.put("role", user.getRole());
        map.put("active", user.getActive());
        map.put("emailVerified", user.getEmailVerified());
        map.put("applicationStatus", user.getApplicationStatus() != null ? user.getApplicationStatus().toString() : null);
        map.put("userStatus", user.getUserStatus() != null ? user.getUserStatus().toString() : null);
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        map.put("lastLoginAt", user.getLastLoginAt());
        map.put("emailVerifiedAt", user.getEmailVerifiedAt());
        map.put("deletedAt", user.getDeletedAt());
        map.put("lastLoginDevice", user.getLastLoginDevice());
        map.put("fcmTokens", user.getFcmTokens());
        return map;
    }
    
    private Map<String, Object> convertProfileToRawMap(CompleteUserProfile profile) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", profile.getConnectId());
        map.put("firstName", profile.getFirstName());
        map.put("lastName", profile.getLastName());
        map.put("gender", profile.getGender());
        map.put("email", profile.getEmail());
        map.put("location", profile.getLocation());
        map.put("dateOfBirth", profile.getDateOfBirth());
        map.put("active", profile.getActive());
        map.put("isOnline", profile.getOnline());
        map.put("isPremium", profile.getPremium());
        map.put("isVerified", profile.getVerified());
        map.put("subscriptionType", profile.getSubscriptionType());
        map.put("interests", profile.getInterests());
        map.put("createdAt", profile.getCreatedAt());
        map.put("updatedAt", profile.getUpdatedAt());
        map.put("lastActive", profile.getLastActive());
        map.put("version", profile.getVersion());
        map.put("photos", profile.getPhotos());
        map.put("profile", profile.getProfile());
        map.put("writtenPrompts", profile.getWrittenPrompts());
        map.put("pollPrompts", profile.getPollPrompts());
        map.put("fieldVisibility", profile.getFieldVisibility());
        map.put("preferences", profile.getPreferences());
        map.put("notifications", profile.getNotifications());
        return map;
    }
    
    /**
     * Convert any object to a raw map for JSON display
     * This uses reflection to extract all fields safely
     */
    private Map<String, Object> convertObjectToRawMap(Object obj) {
        Map<String, Object> map = new HashMap<>();
        
        if (obj == null) {
            return map;
        }
        
        try {
            // Use reflection to get all fields from the object
            Class<?> clazz = obj.getClass();
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    
                    // Skip null values and static fields
                    if (value != null && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        map.put(field.getName(), value);
                    }
                } catch (Exception e) {
                    // Skip fields that can't be accessed
                    log.debug("Could not access field {}: {}", field.getName(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.warn("Could not convert object to raw map: {}", e.getMessage());
            // Fallback to toString
            map.put("rawValue", obj.toString());
        }
        
        return map;
    }
    
    /**
     * Get filtered raw data for specific collections only
     */
    public Map<String, Object> getUserRawDataFiltered(String connectId, String[] collections) {
        try {
            log.info("🔍 Getting filtered raw data for user {} - collections: {}", connectId, String.join(",", collections));
            
            Map<String, Object> rawData = new HashMap<>();
            java.util.Set<String> requestedCollections = new java.util.HashSet<>(java.util.Arrays.asList(collections));
            
            // Only fetch requested collections
            if (requestedCollections.contains("userAuth")) {
                try {
                    User user = userRepository.findByConnectId(connectId).orElse(null);
                    if (user != null) {
                        rawData.put("userAuth", convertUserToRawMap(user));
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching userAuth: {}", e.getMessage());
                }
            }
            
            if (requestedCollections.contains("userProfiles")) {
                try {
                    Optional<CompleteUserProfile> profileOpt = userProfileRepository.findByConnectId(connectId);
                    if (profileOpt.isPresent()) {
                        rawData.put("userProfiles", convertProfileToRawMap(profileOpt.get()));
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching userProfiles: {}", e.getMessage());
                }
            }
            
            if (requestedCollections.contains("matches")) {
                try {
                    List<Object> matches = matchRepository.findByUserId(connectId).stream()
                        .map(this::convertObjectToRawMap)
                        .collect(Collectors.toList());
                    if (!matches.isEmpty()) {
                        rawData.put("matches", matches);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching matches: {}", e.getMessage());
                }
            }
            
            if (requestedCollections.contains("conversations")) {
                try {
                    List<Object> conversations = conversationRepository.findByParticipantId(connectId).stream()
                        .map(this::convertObjectToRawMap)
                        .collect(Collectors.toList());
                    if (!conversations.isEmpty()) {
                        rawData.put("conversations", conversations);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching conversations: {}", e.getMessage());
                }
            }
            
            if (requestedCollections.contains("userActions")) {
                try {
                    List<Object> userActions = userActionRepository.findByUserId(connectId).stream()
                        .map(this::convertObjectToRawMap)
                        .collect(Collectors.toList());
                    if (!userActions.isEmpty()) {
                        rawData.put("userActions", userActions);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching userActions: {}", e.getMessage());
                }
            }
            
            if (requestedCollections.contains("userReports")) {
                try {
                    List<Object> reports = userReportRepository.findByReportedUserId(connectId).stream()
                        .map(this::convertObjectToRawMap)
                        .collect(Collectors.toList());
                    List<Object> reportsMade = userReportRepository.findByReporterId(connectId).stream()
                        .map(this::convertObjectToRawMap)
                        .collect(Collectors.toList());
                    
                    if (!reports.isEmpty() || !reportsMade.isEmpty()) {
                        Map<String, Object> allReports = new HashMap<>();
                        if (!reports.isEmpty()) {
                            allReports.put("reportsAgainstUser", reports);
                        }
                        if (!reportsMade.isEmpty()) {
                            allReports.put("reportsByUser", reportsMade);
                        }
                        rawData.put("userReports", allReports);
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Error fetching userReports: {}", e.getMessage());
                }
            }
            
            // Add similar blocks for other collections if requested...
            
            // Add metadata
            rawData.put("metadata", Map.of(
                "connectId", connectId,
                "filteredCollections", java.util.Arrays.asList(collections),
                "totalCollections", rawData.keySet().size(),
                "retrievedAt", LocalDateTime.now().toString()
            ));
            
            return rawData;
            
        } catch (Exception e) {
            log.error("❌ Failed to get filtered raw data for user {}: ", connectId, e);
            return new HashMap<>();
        }
    }
    
    /**
     * Split raw data into manageable chunks for UI display
     */
    public Map<String, Object> chunkRawData(Map<String, Object> rawData) {
        Map<String, Object> chunks = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String collectionName = entry.getKey();
            Object data = entry.getValue();
            
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                if (list.size() > 50) { // Chunk large arrays
                    Map<String, Object> chunkedCollection = new HashMap<>();
                    chunkedCollection.put("totalItems", list.size());
                    chunkedCollection.put("itemsPerChunk", 50);
                    chunkedCollection.put("totalChunks", (int) Math.ceil(list.size() / 50.0));
                    
                    // Create chunks
                    Map<String, List<?>> dataChunks = new HashMap<>();
                    for (int i = 0; i < list.size(); i += 50) {
                        int endIndex = Math.min(i + 50, list.size());
                        dataChunks.put("chunk_" + (i / 50 + 1), list.subList(i, endIndex));
                    }
                    chunkedCollection.put("chunks", dataChunks);
                    chunks.put(collectionName, chunkedCollection);
                } else {
                    chunks.put(collectionName, data);
                }
            } else {
                chunks.put(collectionName, data);
            }
        }
        
        return chunks;
    }
    
    /**
     * Format raw data for better display in UI
     */
    public Map<String, Object> formatRawDataForDisplay(Map<String, Object> rawData) {
        Map<String, Object> formatted = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String collectionName = entry.getKey();
            Object data = entry.getValue();
            
            Map<String, Object> collectionInfo = new HashMap<>();
            
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                collectionInfo.put("type", "array");
                collectionInfo.put("count", list.size());
                collectionInfo.put("preview", list.stream().limit(3).collect(Collectors.toList()));
                collectionInfo.put("data", data);
            } else if (data instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) data;
                collectionInfo.put("type", "object");
                collectionInfo.put("fieldCount", map.size());
                collectionInfo.put("fields", new java.util.ArrayList<>(map.keySet()));
                collectionInfo.put("data", data);
            } else {
                collectionInfo.put("type", "primitive");
                collectionInfo.put("value", data);
                collectionInfo.put("data", data);
            }
            
            formatted.put(collectionName, collectionInfo);
        }
        
        return formatted;
    }
    
    /**
     * Get list of available collections for a user
     */
    public List<String> getAvailableCollections(String connectId) {
        List<String> available = new java.util.ArrayList<>();
        
        // Check each collection to see if user has data
        try {
            if (userRepository.findByConnectId(connectId).isPresent()) {
                available.add("userAuth");
            }
        } catch (Exception e) { /* ignore */ }
        
        try {
            if (userProfileRepository.findByConnectId(connectId).isPresent()) {
                available.add("userProfiles");
            }
        } catch (Exception e) { /* ignore */ }
        
        try {
            if (!matchRepository.findByUserId(connectId).isEmpty()) {
                available.add("matches");
            }
        } catch (Exception e) { /* ignore */ }
        
        try {
            if (!conversationRepository.findByParticipantId(connectId).isEmpty()) {
                available.add("conversations");
            }
        } catch (Exception e) { /* ignore */ }
        
        try {
            if (!userActionRepository.findByUserId(connectId).isEmpty()) {
                available.add("userActions");
            }
        } catch (Exception e) { /* ignore */ }
        
        // Add checks for other collections...
        
        return available;
    }
    
    /**
     * Get size of each collection for the user
     */
    public Map<String, Integer> getCollectionSizes(String connectId) {
        Map<String, Integer> sizes = new HashMap<>();
        
        try {
            sizes.put("userAuth", userRepository.findByConnectId(connectId).isPresent() ? 1 : 0);
        } catch (Exception e) { sizes.put("userAuth", 0); }
        
        try {
            sizes.put("userProfiles", userProfileRepository.findByConnectId(connectId).isPresent() ? 1 : 0);
        } catch (Exception e) { sizes.put("userProfiles", 0); }
        
        try {
            sizes.put("matches", matchRepository.findByUserId(connectId).size());
        } catch (Exception e) { sizes.put("matches", 0); }
        
        try {
            sizes.put("conversations", conversationRepository.findByParticipantId(connectId).size());
        } catch (Exception e) { sizes.put("conversations", 0); }
        
        try {
            sizes.put("userActions", userActionRepository.findByUserId(connectId).size());
        } catch (Exception e) { sizes.put("userActions", 0); }
        
        return sizes;
    }
    
    /**
     * Calculate approximate data size for display
     */
    public String calculateDataSize(Map<String, Object> rawData) {
        try {
            // Simple approximation - count total fields/items
            int totalItems = 0;
            for (Object value : rawData.values()) {
                if (value instanceof List) {
                    totalItems += ((List<?>) value).size();
                } else if (value instanceof Map) {
                    totalItems += ((Map<?, ?>) value).size();
                } else {
                    totalItems += 1;
                }
            }
            
            if (totalItems < 100) return "Small";
            else if (totalItems < 1000) return "Medium";
            else return "Large";
            
        } catch (Exception e) {
            return "Unknown";
        }
    }
}