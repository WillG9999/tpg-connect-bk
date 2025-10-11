package com.tpg.connect.controllers.match;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.UserService;
import com.tpg.connect.services.UserActionsService;
import com.tpg.connect.services.ConversationService;
import com.tpg.connect.services.MatchService;
import com.tpg.connect.services.ProfileManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController extends BaseController {

    @Autowired
    private AuthenticationService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserActionsService userActionsService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private ProfileManagementService profileService;


    // Get user matches (frontend expects /api/matches)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMatches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String since,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        System.out.println("🚀 MatchController: getMatches() called!");
        System.out.println("🔑 MatchController: authHeader present: " + (authHeader != null));
        System.out.println("🔑 MatchController: About to validate and extract user ID...");
        
        String userId = validateAndExtractUserId(authHeader);
        System.out.println("🔑 MatchController: validateAndExtractUserId returned: " + userId);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            System.out.println("🔍 MatchController: Getting matches for user: " + userId);
            // Get user's matches from UserActions
            List<String> matchedUserIds = userActionsService.getMatches(userId);
            System.out.println("🔍 MatchController: Found " + matchedUserIds.size() + " matched user IDs: " + matchedUserIds);
            
            // DEFENSIVE FIX: Filter out current user's own ID from matches (should never happen but defensive)
            List<String> filteredMatchedUserIds = matchedUserIds.stream()
                .filter(matchedUserId -> !matchedUserId.equals(userId))
                .collect(Collectors.toList());
            
            if (filteredMatchedUserIds.size() != matchedUserIds.size()) {
                System.out.println("⚠️ MatchController: DETECTED SELF-MATCH! Filtered out user's own ID from matches");
                System.out.println("⚠️ MatchController: Original list: " + matchedUserIds);
                System.out.println("⚠️ MatchController: Filtered list: " + filteredMatchedUserIds);
            }
            
            matchedUserIds = filteredMatchedUserIds;
            
            // Convert user IDs to match objects with user profiles
            List<Map<String, Object>> matches = matchedUserIds.stream()
                .map(matchedUserId -> {
                    try {
                        // Auto-refresh match user photos before fetching profile
                        try {
                            profileService.refreshPhotoUrls(matchedUserId);
                            System.out.println("✅ Auto-refreshed match user photos for: " + matchedUserId);
                        } catch (Exception e) {
                            System.out.println("⚠️ Failed to auto-refresh match user photos, continuing: " + e.getMessage());
                        }
                        
                        CompleteUserProfile matchedUser = userService.getUserProfile(matchedUserId);
                        if (matchedUser != null) {
                            // Generate deterministic conversation ID
                            String conversationId = generateConversationId(userId, matchedUserId);
                            
                            // Get actual match data from database to get real timestamp
                            com.tpg.connect.model.match.Match matchEntity = matchService.getMatch(conversationId);
                            
                            long matchTimestamp = System.currentTimeMillis(); // fallback
                            if (matchEntity != null && matchEntity.getMatchedAt() != null) {
                                matchTimestamp = matchEntity.getMatchedAt()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli();
                            }
                            
                            // Get conversation data for latest message and unread count
                            Map<String, Object> lastMessage = Map.of(
                                "content", "Start a conversation!",
                                "timestamp", matchTimestamp,
                                "senderId", ""
                            );
                            int unreadCount = 0;
                            
                            try {
                                System.out.println("🔍 MatchController: Attempting to get conversation: " + conversationId);
                                java.util.Optional<com.tpg.connect.model.conversation.Conversation> conversationOpt = 
                                    conversationService.getConversationById(conversationId);
                                
                                if (conversationOpt.isPresent()) {
                                    com.tpg.connect.model.conversation.Conversation conversation = conversationOpt.get();
                                    System.out.println("✅ MatchController: Found conversation: " + conversationId);
                                    System.out.println("🔍 MatchController: Conversation archived status: " + conversation.isArchived());
                                    
                                    // No filtering - return all matches with their archived status
                                    // The frontend will split them based on the archived field
                                    
                                    System.out.println("🔍 MatchController: Conversation lastMessage is null: " + (conversation.getLastMessage() == null));
                                    
                                    if (conversation.getLastMessage() != null) {
                                        System.out.println("✅ MatchController: Using actual lastMessage: " + conversation.getLastMessage().getContent());
                                        lastMessage = Map.of(
                                            "content", conversation.getLastMessage().getContent(),
                                            "timestamp", conversation.getLastMessage().getSentAt()
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toInstant()
                                                .toEpochMilli(),
                                            "senderId", conversation.getLastMessage().getSenderId()
                                        );
                                        unreadCount = conversation.getUnreadCount();
                                    } else {
                                        System.out.println("❌ MatchController: Conversation lastMessage is null, trying to get latest message directly...");
                                        // Fallback: get the latest message directly from messages
                                        try {
                                            java.util.List<com.tpg.connect.model.conversation.Message> messages = 
                                                conversationService.getConversationMessages(conversationId, userId, 0, 1);
                                            if (!messages.isEmpty()) {
                                                com.tpg.connect.model.conversation.Message latestMessage = messages.get(messages.size() - 1);
                                                System.out.println("✅ MatchController: Found latest message directly: " + latestMessage.getContent());
                                                lastMessage = Map.of(
                                                    "content", latestMessage.getContent(),
                                                    "timestamp", latestMessage.getSentAt()
                                                        .atZone(java.time.ZoneId.systemDefault())
                                                        .toInstant()
                                                        .toEpochMilli(),
                                                    "senderId", latestMessage.getSenderId()
                                                );
                                            } else {
                                                System.out.println("❌ MatchController: No messages found in conversation");
                                            }
                                        } catch (Exception e2) {
                                            System.err.println("❌ MatchController: Failed to get latest message directly: " + e2.getMessage());
                                        }
                                    }
                                } else {
                                    System.out.println("❌ MatchController: Conversation not found: " + conversationId);
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to get conversation data for " + conversationId + ": " + e.getMessage());
                            }
                            
                            // Get archived status from conversation
                            boolean isArchived = false;
                            try {
                                java.util.Optional<com.tpg.connect.model.conversation.Conversation> convOpt = 
                                    conversationService.getConversationById(conversationId);
                                if (convOpt.isPresent()) {
                                    isArchived = convOpt.get().isArchived();
                                }
                            } catch (Exception e) {
                                System.err.println("Failed to get archived status: " + e.getMessage());
                            }
                            
                            return Map.of(
                                "id", conversationId, // Use conversation ID as match ID
                                "user", Map.of(
                                    "connectId", matchedUser.getConnectId(),
                                    "firstName", matchedUser.getFirstName(),
                                    "photos", matchedUser.getPhotos() != null ? matchedUser.getPhotos() : List.of(),
                                    "age", matchedUser.getAge()
                                ),
                                "conversationId", conversationId,
                                "matchedAt", matchTimestamp,
                                "lastActivity", matchTimestamp,
                                "lastMessage", lastMessage,
                                "unreadCount", unreadCount,
                                "archived", isArchived
                            );
                        }
                        return null;
                    } catch (Exception e) {
                        System.err.println("Error getting profile for matched user " + matchedUserId + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(match -> match != null)
                .collect(Collectors.toList());
            
            System.out.println("🎉 MatchController: Returning " + matches.size() + " matches");
            if (!matches.isEmpty()) {
                System.out.println("🔍 MatchController: First match sample: " + matches.get(0));
            }
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            System.err.println("Error getting matches for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }




    // Get conversations (frontend expects /api/conversations)
    @GetMapping("/conversations")
    public ResponseEntity<List<Map<String, Object>>> getConversations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get user's matches from UserActions and return as conversations
            List<String> matchedUserIds = userActionsService.getMatches(userId);
            
            // Convert to conversation format (same as matches but conversation-focused)
            List<Map<String, Object>> conversations = matchedUserIds.stream()
                .map(matchedUserId -> {
                    try {
                        // Auto-refresh match user photos before fetching profile
                        try {
                            profileService.refreshPhotoUrls(matchedUserId);
                            System.out.println("✅ Auto-refreshed conversation user photos for: " + matchedUserId);
                        } catch (Exception e) {
                            System.out.println("⚠️ Failed to auto-refresh conversation user photos, continuing: " + e.getMessage());
                        }
                        
                        CompleteUserProfile matchedUser = userService.getUserProfile(matchedUserId);
                        if (matchedUser != null) {
                            String conversationId = generateConversationId(userId, matchedUserId);
                            
                            return Map.of(
                                "id", conversationId,
                                "participant", Map.of(
                                    "connectId", matchedUser.getConnectId(),
                                    "firstName", matchedUser.getFirstName(),
                                    "photos", matchedUser.getPhotos() != null ? matchedUser.getPhotos() : List.of(),
                                    "age", matchedUser.getAge()
                                ),
                                "lastMessage", Map.of(
                                    "content", "Start a conversation!",
                                    "timestamp", System.currentTimeMillis(),
                                    "senderId", ""
                                ),
                                "unreadCount", 0,
                                "lastActivity", System.currentTimeMillis()
                            );
                        }
                        return null;
                    } catch (Exception e) {
                        System.err.println("Error getting profile for conversation " + matchedUserId + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(conversation -> conversation != null)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            System.err.println("Error getting conversations for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Send message (frontend expects /api/matches/{matchId}/messages POST)
    @PostMapping("/{matchId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String matchId,
            @RequestBody Map<String, Object> messageData) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            String content = (String) messageData.get("content");
            String messageType = (String) messageData.getOrDefault("type", "text");
            
            // For now, just return success - in a real implementation you'd save the message
            Map<String, Object> message = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "matchId", matchId,
                "senderId", userId,
                "content", content,
                "type", messageType,
                "timestamp", java.time.LocalDateTime.now().toString()
            );
            
            return successResponse(Map.of("message", "Message sent successfully", "data", message));
        } catch (Exception e) {
            return errorResponse("Failed to send message: " + e.getMessage());
        }
    }

    // Get messages (frontend expects /api/matches/{matchId}/messages GET)
    @GetMapping("/{matchId}/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String matchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String since) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // For now, return empty messages - in a real implementation you'd query the database
            List<Map<String, Object>> messages = List.of();
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Archive match (frontend expects /api/matches/{matchId}/archive-match)
    @PostMapping("/{matchId}/archive-match")
    public ResponseEntity<Map<String, Object>> archiveMatch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String matchId) {
        
        System.out.println("🎯 MatchController.archiveMatch CALLED");
        System.out.println("🎯 matchId parameter: " + matchId);
        System.out.println("🎯 authHeader present: " + (authHeader != null));
        
        String userId = validateAndExtractUserId(authHeader);
        System.out.println("🎯 Extracted userId: " + userId);
        
        if (userId == null) {
            System.out.println("❌ MatchController: Invalid authorization, returning 401");
            return unauthorizedResponse("Invalid or missing authorization");
        }
        
        try {
            System.out.println("📁 MatchController: Starting archive operation for match " + matchId + " for user " + userId);
            
            // Archive the conversation but keep match active
            System.out.println("📁 MatchController: Calling conversationService.archiveConversation...");
            conversationService.archiveConversation(matchId, userId);
            System.out.println("📁 MatchController: conversationService.archiveConversation completed");
            
            Map<String, Object> response = Map.of(
                "message", "Match archived successfully",
                "matchId", matchId,
                "action", "ARCHIVED",
                "timestamp", java.time.Instant.now().toString()
            );
            
            System.out.println("📁 MatchController: Response prepared: " + response);
            System.out.println("✅ MatchController: Match archived successfully: " + matchId);
            return successResponse(response);
            
        } catch (Exception e) {
            System.err.println("❌ MatchController: Failed to archive match " + matchId + ": " + e.getMessage());
            System.err.println("❌ MatchController: Exception type: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return errorResponse("Failed to archive match: " + e.getMessage());
        }
    }

    // Unarchive match (frontend expects /api/matches/{matchId}/unarchive)
    @PostMapping("/{matchId}/unarchive")
    public ResponseEntity<Map<String, Object>> unarchiveMatch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String matchId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }
        
        try {
            System.out.println("📂 MatchController: Unarchiving match " + matchId + " for user " + userId);
            
            // Unarchive the conversation
            conversationService.unarchiveConversation(matchId, userId);
            
            Map<String, Object> response = Map.of(
                "message", "Match unarchived successfully",
                "matchId", matchId,
                "action", "UNARCHIVED",
                "timestamp", java.time.Instant.now().toString()
            );
            
            System.out.println("✅ MatchController: Match unarchived successfully: " + matchId);
            return successResponse(response);
            
        } catch (Exception e) {
            System.err.println("❌ MatchController: Failed to unarchive match " + matchId + ": " + e.getMessage());
            return errorResponse("Failed to unarchive match: " + e.getMessage());
        }
    }

    // Unmatch (frontend expects /api/matches/{matchId}/unmatch)
    @PostMapping("/{matchId}/unmatch")
    public ResponseEntity<Map<String, Object>> unmatch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String matchId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }
        
        try {
            System.out.println("🚫 MatchController: Unmatching " + matchId + " for user " + userId);
            
            // Get the match to find the other user
            var match = matchService.getMatch(matchId);
            if (match == null) {
                return errorResponse("Match not found");
            }
            
            // Determine the other user ID
            String otherUserId = match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id();
            System.out.println("🔍 MatchController: Other user ID: " + otherUserId);
            
            // 1. Add to unmatched arrays (prevents future matching)
            userActionsService.addUnmatchAction(userId, otherUserId);
            
            // 2. Update match status to UNMATCHED
            matchService.unmatchUsers(matchId);
            
            // 3. Update conversation status to UNMATCHED
            conversationService.unmatchConversation(matchId);
            
            Map<String, Object> response = Map.of(
                "message", "Unmatched successfully",
                "matchId", matchId,
                "action", "UNMATCHED",
                "timestamp", java.time.Instant.now().toString()
            );
            
            System.out.println("✅ MatchController: Unmatch completed successfully: " + matchId);
            return successResponse(response);
            
        } catch (Exception e) {
            System.err.println("❌ MatchController: Failed to unmatch " + matchId + ": " + e.getMessage());
            return errorResponse("Failed to unmatch: " + e.getMessage());
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        System.out.println("🔍 validateAndExtractUserId: Starting validation...");
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            System.out.println("🔍 validateAndExtractUserId: Invalid header format");
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        System.out.println("🔍 validateAndExtractUserId: Extracted token, checking validity...");
        
        if (!authService.isTokenValid(token)) {
            System.out.println("🔍 validateAndExtractUserId: Token is invalid");
            return null;
        }

        System.out.println("🔍 validateAndExtractUserId: Token valid, extracting user ID...");
        // Extract user ID directly from JWT token subject claim
        String userId = authService.extractUserIdFromToken(token);
        System.out.println("🔍 validateAndExtractUserId: Extracted user ID: " + userId);
        return userId;
    }
    
    /**
     * Generate deterministic conversation ID based on user IDs
     * Format: {lowerUserId}_{higherUserId}
     */
    private String generateConversationId(String userId1, String userId2) {
        // Ensure deterministic ordering (alphabetical by user ID)
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    // Get users who liked the current user (for "Likes You" page)
    @GetMapping("/liked-by")
    public ResponseEntity<List<Map<String, Object>>> getLikedByUsers(
            @RequestHeader("Authorization") String authHeader) {
        
        System.out.println("🚀 MatchController: getLikedByUsers() called!");
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            System.out.println("🔍 MatchController: Getting users who liked user: " + userId);
            
            // Get users who liked this user from UserActions
            List<String> likedByUserIds = userActionsService.getLikedByUsers(userId);
            System.out.println("🔍 MatchController: Found " + likedByUserIds.size() + " users who liked this user: " + likedByUserIds);
            
            // Only filter out users the current user has PASSED on (not liked/matched)
            List<String> passedUsers = userActionsService.getPassedUsers(userId);
            
            System.out.println("🔍 MatchController: User has passed on " + passedUsers.size() + " users: " + passedUsers);
            
            // Only filter out users current user has passed on - show everyone else who liked them
            List<String> availableLikes = likedByUserIds.stream()
                .filter(likedUserId -> !passedUsers.contains(likedUserId))
                .collect(Collectors.toList());
                
            System.out.println("🔍 MatchController: After filtering, " + availableLikes.size() + " available likes remain");
            
            // Convert user IDs to user profile objects
            List<Map<String, Object>> likesYou = availableLikes.stream()
                .map(likedUserId -> {
                    try {
                        // Auto-refresh liked user photos before fetching profile
                        try {
                            profileService.refreshPhotoUrls(likedUserId);
                            System.out.println("✅ Auto-refreshed liked user photos for: " + likedUserId);
                        } catch (Exception e) {
                            System.out.println("⚠️ Failed to auto-refresh liked user photos, continuing: " + e.getMessage());
                        }
                        
                        CompleteUserProfile likedUser = userService.getUserProfile(likedUserId);
                        if (likedUser != null) {
                            return Map.of(
                                "connectId", likedUser.getConnectId(),
                                "firstName", likedUser.getFirstName(),
                                "age", likedUser.getAge(),
                                "photos", likedUser.getPhotos() != null ? likedUser.getPhotos() : List.of(),
                                "location", likedUser.getLocation() != null ? likedUser.getLocation() : "",
                                "jobTitle", likedUser.getProfile() != null && likedUser.getProfile().getJobTitle() != null ? likedUser.getProfile().getJobTitle() : "",
                                "university", likedUser.getProfile() != null && likedUser.getProfile().getUniversity() != null ? likedUser.getProfile().getUniversity() : "",
                                "interests", likedUser.getInterests() != null ? likedUser.getInterests() : List.of(),
                                "writtenPrompts", likedUser.getWrittenPrompts() != null ? likedUser.getWrittenPrompts() : List.of()
                            );
                        }
                        return null;
                    } catch (Exception e) {
                        System.err.println("Error getting profile for liked by user " + likedUserId + ": " + e.getMessage());
                        return null;
                    }
                })
                .filter(profile -> profile != null)
                .collect(Collectors.toList());
            
            System.out.println("🎉 MatchController: Returning " + likesYou.size() + " users who liked this user");
            return ResponseEntity.ok(likesYou);
            
        } catch (Exception e) {
            System.err.println("Error getting liked by users for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Direct like action for Likes You page (bypasses discovery batch system)
    @PostMapping("/like/{targetUserId}")
    public ResponseEntity<Map<String, Object>> likeUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        System.out.println("🚀 MatchController: likeUser() called for target: " + targetUserId);
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            System.out.println("🔍 MatchController: User " + userId + " is liking user " + targetUserId);
            
            // Add like action directly through UserActionsService
            boolean isMutualMatch = userActionsService.addLikeAction(userId, targetUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Like action successful");
            response.put("mutualMatch", isMutualMatch);
            
            if (isMutualMatch) {
                response.put("message", "It's a match!");
                System.out.println("🎉 MatchController: It's a match! " + userId + " ↔ " + targetUserId);
            }
            
            System.out.println("✅ MatchController: Like action completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing like action " + userId + " -> " + targetUserId + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process like action");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Direct pass action for Likes You page (bypasses discovery batch system)
    @PostMapping("/pass/{targetUserId}")
    public ResponseEntity<Map<String, Object>> passUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        System.out.println("🚀 MatchController: passUser() called for target: " + targetUserId);
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            System.out.println("🔍 MatchController: User " + userId + " is passing on user " + targetUserId);
            
            // Add pass action directly through UserActionsService
            userActionsService.addPassAction(userId, targetUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Pass action successful");
            
            System.out.println("✅ MatchController: Pass action completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ Error processing pass action " + userId + " -> " + targetUserId + ": " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process pass action");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}