package com.tpg.connect.controllers.match;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.services.DiscoveryService;
import com.tpg.connect.services.UserService;
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
    private AuthService authService;

    @Autowired
    private DiscoveryService discoveryService;
    
    @Autowired
    private UserService userService;

    // Get potential matches for discovery (frontend expects /api/matches/discover)
    @GetMapping("/discover")
    public ResponseEntity<List<UserProfileDTO>> getPotentialMatches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer maxDistance,
            @RequestParam(required = false) String exclude) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Create discovery request from parameters
            com.tpg.connect.model.dto.DiscoveryRequest request = new com.tpg.connect.model.dto.DiscoveryRequest();
            request.setCount(count);
            request.setLatitude(lat);
            request.setLongitude(lng);
            request.setMaxDistance(maxDistance);
            
            // Parse exclude parameter (comma-separated string to list)
            List<String> excludeList = null;
            if (exclude != null && !exclude.trim().isEmpty()) {
                excludeList = List.of(exclude.split(","));
            }
            request.setExcludeUserIds(excludeList);
            
            List<CompleteUserProfile> potentialMatches = discoveryService.getPotentialMatches(userId, request);
            List<UserProfileDTO> dtos = potentialMatches.stream()
                .map(UserProfileDTO::fromCompleteUserProfile)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Get user matches (frontend expects /api/matches)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getMatches(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String since) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // For now, return empty matches - in a real implementation you'd query the match database
            List<Map<String, Object>> matches = List.of();
            return ResponseEntity.ok(matches);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Create match action (like/pass) (frontend expects /api/matches POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMatch(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> matchData) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            String targetUserId = (String) matchData.get("targetUserId");
            String action = (String) matchData.get("action");
            
            if ("LIKE".equals(action)) {
                Map<String, Object> result = userService.likeUser(userId, targetUserId);
                return successResponse(result);
            } else if ("PASS".equals(action)) {
                Map<String, Object> result = userService.dislikeUser(userId, targetUserId);
                return successResponse(result);
            } else {
                return errorResponse("Invalid action. Must be LIKE or PASS");
            }
        } catch (Exception e) {
            return errorResponse("Failed to create match: " + e.getMessage());
        }
    }

    // Daily batch status (frontend expects /api/matches/daily/status)
    @GetMapping("/daily/status")
    public ResponseEntity<Map<String, Object>> getDailyBatchStatus(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            // Return basic status - in a real implementation you'd check actual batch status
            Map<String, Object> status = Map.of(
                "batchReady", true,
                "nextBatchTime", "19:00:00",
                "currentBatchDate", java.time.LocalDate.now().toString(),
                "matchesAvailable", true
            );
            return successResponse(status);
        } catch (Exception e) {
            return errorResponse("Failed to get batch status: " + e.getMessage());
        }
    }

    // Daily matches (frontend expects /api/matches/daily/{date})
    @GetMapping("/daily/{date}")
    public ResponseEntity<Map<String, Object>> getDailyMatches(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String date,
            @RequestParam(defaultValue = "false") boolean includeResults) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            // Get potential matches for the day
            com.tpg.connect.model.dto.DiscoveryRequest request = new com.tpg.connect.model.dto.DiscoveryRequest();
            request.setCount(10);
            
            List<CompleteUserProfile> potentialMatches = discoveryService.getPotentialMatches(userId, request);
            List<UserProfileDTO> dtos = potentialMatches.stream()
                .map(UserProfileDTO::fromCompleteUserProfile)
                .collect(Collectors.toList());
            
            Map<String, Object> response = Map.of(
                "date", date,
                "matches", dtos,
                "totalCount", dtos.size(),
                "includeResults", includeResults
            );
            
            return successResponse(response);
        } catch (Exception e) {
            return errorResponse("Failed to get daily matches: " + e.getMessage());
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
            // For now, return empty conversations - in a real implementation you'd query the conversation database
            List<Map<String, Object>> conversations = List.of();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
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
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.validateToken(token)) {
            return null;
        }

        String username = authService.extractUsername(token);
        String userId = getUserIdFromUsername(username);
        return userId;
    }

    private String getUserIdFromUsername(String username) {
        // First check if the "username" is already a user ID (from JWT subject)
        if (username != null && username.matches("\\d+")) {
            return username;
        }
        
        // Fallback to hardcoded mappings for test users
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