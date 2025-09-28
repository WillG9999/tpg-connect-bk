package com.tpg.connect.controllers.match;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.UserService;
import com.tpg.connect.services.UserActionsService;
import com.tpg.connect.services.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


    // Get user matches (frontend expects /api/matches)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMatches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String since) {
        
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
            
            // Convert user IDs to match objects with user profiles
            List<Map<String, Object>> matches = matchedUserIds.stream()
                .map(matchedUserId -> {
                    try {
                        CompleteUserProfile matchedUser = userService.getUserProfile(matchedUserId);
                        if (matchedUser != null) {
                            // Generate deterministic conversation ID
                            String conversationId = generateConversationId(userId, matchedUserId);
                            
                            return Map.of(
                                "id", conversationId, // Use conversation ID as match ID
                                "user", Map.of(
                                    "connectId", matchedUser.getConnectId(),
                                    "firstName", matchedUser.getFirstName(),
                                    "photos", matchedUser.getPhotos() != null ? matchedUser.getPhotos() : List.of(),
                                    "age", matchedUser.getAge()
                                ),
                                "conversationId", conversationId,
                                "lastActivity", System.currentTimeMillis(),
                                "unreadCount", 0
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
            // For now, just return success - in a real implementation you'd handle the unmatch
            return successResponse(Map.of("message", "Unmatched successfully", "matchId", matchId));
        } catch (Exception e) {
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

}