package com.tpg.connect.controllers;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.UserBlocked;
import com.tpg.connect.model.UserBlocked.SafetyBlockRule;
import com.tpg.connect.model.dto.SafetyBlockRuleRequest;
import com.tpg.connect.model.dto.UserBlockedRequest;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.UserBlockedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-blocked")
public class UserBlockedController extends BaseController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private UserBlockedService userBlockedService;

    @GetMapping
    public ResponseEntity<UserBlocked> getUserBlockedConfig(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserBlocked config = userBlockedService.getUserBlockedConfig(userId);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/direct-blocks")
    public ResponseEntity<Map<String, Object>> addDirectBlock(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserBlockedRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            userBlockedService.addDirectBlock(userId, request);
            return successResponse(Map.of(
                "message", "User blocked successfully",
                "blockedUserId", request.getTargetUserId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to block user: " + e.getMessage());
        }
    }

    @DeleteMapping("/direct-blocks/{targetUserId}")
    public ResponseEntity<Map<String, Object>> removeDirectBlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            userBlockedService.removeDirectBlock(userId, targetUserId);
            return successResponse(Map.of(
                "message", "User unblocked successfully",
                "unblockedUserId", targetUserId
            ));
        } catch (Exception e) {
            return errorResponse("Failed to unblock user: " + e.getMessage());
        }
    }

    @GetMapping("/safety-blocks")
    public ResponseEntity<List<SafetyBlockRule>> getSafetyBlocks(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserBlocked config = userBlockedService.getUserBlockedConfig(userId);
            return ResponseEntity.ok(config.getSafetyBlocks());
        } catch (Exception e) {
            System.err.println("❌ Error in getSafetyBlocks: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/safety-blocks")
    public ResponseEntity<Map<String, Object>> addSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SafetyBlockRuleRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            UserBlocked config = userBlockedService.addSafetyBlock(userId, request);
            SafetyBlockRule addedRule = config.getSafetyBlocks().get(config.getSafetyBlocks().size() - 1);
            
            return successResponse(Map.of(
                "message", "Safety block added successfully",
                "rule", addedRule
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to add safety block: " + e.getMessage());
        }
    }

    @PutMapping("/safety-blocks/{ruleId}")
    public ResponseEntity<Map<String, Object>> updateSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String ruleId,
            @Valid @RequestBody SafetyBlockRuleRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            UserBlocked config = userBlockedService.updateSafetyBlock(userId, ruleId, request);
            SafetyBlockRule updatedRule = config.getSafetyBlockById(ruleId);
            
            return successResponse(Map.of(
                "message", "Safety block updated successfully",
                "rule", updatedRule
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to update safety block: " + e.getMessage());
        }
    }

    @DeleteMapping("/safety-blocks/{ruleId}")
    public ResponseEntity<Map<String, Object>> deleteSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String ruleId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            userBlockedService.deleteSafetyBlock(userId, ruleId);
            return successResponse(Map.of(
                "message", "Safety block deleted successfully",
                "deletedRuleId", ruleId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to delete safety block: " + e.getMessage());
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

    protected ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", message));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("success", false, "message", message));
    }
}