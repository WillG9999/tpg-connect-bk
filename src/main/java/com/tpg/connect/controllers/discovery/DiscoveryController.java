package com.tpg.connect.controllers.discovery;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.api.DiscoveryResponse;
import com.tpg.connect.model.api.LikeResponse;
import com.tpg.connect.model.dto.DiscoveryRequest;
import com.tpg.connect.model.dto.DiscoverySettingsRequest;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.DiscoveryService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController extends BaseController implements DiscoveryControllerApi {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private DiscoveryService discoveryService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<DiscoveryResponse> getPotentialMatches(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody(required = false) DiscoveryRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new DiscoveryResponse(false, "Invalid or missing authorization", null, null));
        }

        try {
            if (request == null) {
                request = new DiscoveryRequest(); // Use defaults
            }

            List<CompleteUserProfile> potentialMatches = discoveryService.getPotentialMatches(userId, request);
            String batchId = discoveryService.getCurrentBatchId(userId);
            
            return ResponseEntity.ok(new DiscoveryResponse(true, "Potential matches retrieved successfully", potentialMatches, batchId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new DiscoveryResponse(false, "Failed to get potential matches: " + e.getMessage(), null, null));
        }
    }

    @Override
    public ResponseEntity<LikeResponse> likeUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        
        String currentUserId = validateAndExtractUserId(authHeader);
        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LikeResponse(false, "Invalid or missing authorization", false, null));
        }

        try {
            LikeResponse response = discoveryService.likeUser(currentUserId, userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new LikeResponse(false, "Failed to like user: " + e.getMessage(), false, null));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> dislikeUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        
        String currentUserId = validateAndExtractUserId(authHeader);
        if (currentUserId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            discoveryService.dislikeUser(currentUserId, userId);
            return successResponse(Map.of("message", "User disliked successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to dislike user: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getDiscoverySettings(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> settings = discoveryService.getDiscoverySettings(userId);
            return successResponse(settings);
        } catch (Exception e) {
            return errorResponse("Failed to get discovery settings: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateDiscoverySettings(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody DiscoverySettingsRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> updatedSettings = discoveryService.updateDiscoverySettings(userId, request);
            return successResponse(Map.of("settings", updatedSettings, "message", "Discovery settings updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update discovery settings: " + e.getMessage());
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

        // Extract user ID directly from JWT token subject claim
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