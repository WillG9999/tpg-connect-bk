package com.tpg.connect.controllers.conversation;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController extends BaseController {

    @Autowired
    private AuthService authService;

    // Get conversations (frontend expects /api/conversations)
    @GetMapping
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
            // For now, return empty conversations - in a real implementation you'd query the conversation database
            List<Map<String, Object>> conversations = List.of();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Unmatch conversation (frontend expects /api/conversations/{conversationId}/unmatch)
    @PostMapping("/{conversationId}/unmatch")
    public ResponseEntity<Map<String, Object>> unmatchConversation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String conversationId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid or missing authorization"));
        }

        try {
            // For now, just return success - in a real implementation you'd handle the unmatch
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Conversation unmatched successfully", 
                "conversationId", conversationId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Failed to unmatch conversation: " + e.getMessage()));
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.validateToken(token)) {
            return null;
        }

        String username = authService.extractUsername(token);
        return getUserIdFromUsername(username);
    }

    private String getUserIdFromUsername(String username) {
        switch (username) {
            case "admin":
                return "1";
            case "user":
                return "2";
            case "alex":
                return "user_123";
            default:
                return null;
        }
    }
}