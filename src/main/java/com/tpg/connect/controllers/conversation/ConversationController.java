package com.tpg.connect.controllers.conversation;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.conversation.Message;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController extends BaseController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ConversationService conversationService;

    // Get conversations (frontend expects /api/conversations)
    @GetMapping
    public ResponseEntity<?> getConversations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Conversation> conversations = conversationService.getUserConversations(userId, includeArchived);
            return successResponse(conversations, "Conversations retrieved successfully");
        } catch (Exception e) {
            return errorResponse("Failed to retrieve conversations: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get specific conversation
    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            Optional<Conversation> conversation = conversationService.getConversationById(conversationId);
            if (conversation.isEmpty()) {
                return errorResponse("Conversation not found", HttpStatus.NOT_FOUND);
            }
            
            // Verify user is participant
            if (!conversation.get().getParticipantIds().contains(userId)) {
                return errorResponse("Access denied", HttpStatus.FORBIDDEN);
            }
            
            return successResponse(conversation.get(), "Conversation retrieved successfully");
        } catch (Exception e) {
            return errorResponse("Failed to retrieve conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get conversation messages
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Message> messages = conversationService.getConversationMessages(conversationId, userId, page, limit);
            return successResponse(messages, "Messages retrieved successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to retrieve messages: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Send message
    @PostMapping("/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId,
            @RequestBody Map<String, String> requestBody) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        String content = requestBody.get("content");
        if (content == null || content.trim().isEmpty()) {
            return errorResponse("Message content is required", HttpStatus.BAD_REQUEST);
        }

        try {
            Message message = conversationService.sendMessage(conversationId, userId, content.trim());
            return successResponse(message, "Message sent successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to send message: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Mark messages as read
    @PostMapping("/{conversationId}/read")
    public ResponseEntity<?> markMessagesAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            conversationService.markMessagesAsRead(conversationId, userId);
            return successResponse(null, "Messages marked as read");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to mark messages as read: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Archive conversation
    @PostMapping("/{conversationId}/archive")
    public ResponseEntity<?> archiveConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            conversationService.archiveConversation(conversationId, userId);
            return successResponse(null, "Conversation archived successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to archive conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Unarchive conversation
    @PostMapping("/{conversationId}/unarchive")
    public ResponseEntity<?> unarchiveConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            conversationService.unarchiveConversation(conversationId, userId);
            return successResponse(null, "Conversation unarchived successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to unarchive conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get archived conversations
    @GetMapping("/archived")
    public ResponseEntity<?> getArchivedConversations(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            List<Conversation> archivedConversations = conversationService.getArchivedConversations(userId);
            return successResponse(archivedConversations, "Archived conversations retrieved successfully");
        } catch (Exception e) {
            return errorResponse("Failed to retrieve archived conversations: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // End conversation (unmatch)
    @PostMapping("/{conversationId}/unmatch")
    public ResponseEntity<?> unmatchConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            conversationService.endConversation(conversationId, userId);
            return successResponse(null, "Conversation ended successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to end conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Create conversation between two users
    @PostMapping("/create")
    public ResponseEntity<?> createConversation(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> requestBody) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        String connectId1 = requestBody.get("connectId1");
        String connectId2 = requestBody.get("connectId2");
        String matchId = requestBody.get("matchId");

        if (connectId1 == null || connectId2 == null) {
            return errorResponse("Both connectId1 and connectId2 are required", HttpStatus.BAD_REQUEST);
        }

        // Verify user is one of the participants
        if (!connectId1.equals(userId) && !connectId2.equals(userId)) {
            return errorResponse("User must be one of the conversation participants", HttpStatus.FORBIDDEN);
        }

        try {
            // Generate deterministic conversation ID
            String conversationId = generateConversationId(connectId1, connectId2);
            
            // Check if conversation already exists
            Optional<Conversation> existingConversation = conversationService.findConversationBetweenUsers(connectId1, connectId2);
            if (existingConversation.isPresent()) {
                return successResponse(existingConversation.get(), "Conversation already exists");
            }

            // Create new conversation
            Conversation conversation = new Conversation();
            conversation.setId(conversationId);
            conversation.setMatchId(matchId);
            conversation.setParticipantIds(List.of(connectId1, connectId2));
            conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
            conversation.setUnreadCount(0);
            conversation.setUpdatedAt(java.time.LocalDateTime.now());

            // Save conversation using the service
            Conversation savedConversation = conversationService.saveConversation(conversation);
            return successResponse(savedConversation, "Conversation created successfully");
        } catch (Exception e) {
            return errorResponse("Failed to create conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Delete conversation
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> deleteConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            conversationService.deleteConversation(conversationId, userId);
            return successResponse(null, "Conversation deleted successfully");
        } catch (IllegalArgumentException e) {
            return errorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            return errorResponse("Failed to delete conversation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Get conversation statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getConversationStats(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return errorResponse("Invalid or missing authorization", HttpStatus.UNAUTHORIZED);
        }

        try {
            Map<String, Object> stats = Map.of(
                "unreadCount", conversationService.getUnreadConversationCount(userId),
                "activeCount", conversationService.getActiveConversationCount(userId)
            );
            return successResponse(stats, "Conversation statistics retrieved successfully");
        } catch (Exception e) {
            return errorResponse("Failed to retrieve conversation statistics: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.isTokenValid(token)) {
            return null;
        }

        return authService.extractUserIdFromToken(token);
    }

    /**
     * Generate deterministic conversation ID from two connect IDs
     * Format: connectid1_connectid2 (sorted to ensure consistency)
     */
    private String generateConversationId(String connectId1, String connectId2) {
        // Sort IDs to ensure consistency regardless of order
        if (connectId1.compareTo(connectId2) <= 0) {
            return connectId1 + "_" + connectId2;
        } else {
            return connectId2 + "_" + connectId1;
        }
    }

}