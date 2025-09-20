package com.tpg.connect.controllers.safety;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.BlockUserRequest;
import com.tpg.connect.model.dto.ReportUserRequest;
import com.tpg.connect.model.dto.SafetyBlockRequest;
import com.tpg.connect.model.BlockedUser;
import com.tpg.connect.model.SafetyBlock;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.services.SafetyService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
public class SafetyController extends BaseController implements SafetyControllerApi {

    @Autowired
    private AuthService authService;

    @Autowired
    private SafetyService safetyService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Map<String, Object>> blockUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BlockUserRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            safetyService.blockUser(userId, request.getTargetUserId(), request.getReason());
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

    @Override
    public ResponseEntity<Map<String, Object>> unblockUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            safetyService.unblockUser(userId, targetUserId);
            return successResponse(Map.of(
                "message", "User unblocked successfully",
                "unblockedUserId", targetUserId
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to unblock user: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<BlockedUser>> getBlockedUsers(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<BlockedUser> blockedUsers = safetyService.getBlockedUsers(userId);
            return ResponseEntity.ok(blockedUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> reportUser(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReportUserRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            String reportId = safetyService.reportUser(userId, request);
            return successResponse(Map.of(
                "message", "User reported successfully",
                "reportId", reportId,
                "reportedUserId", request.getTargetUserId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to report user: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<SafetyBlock>> getSafetyBlocks(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<SafetyBlock> safetyBlocks = safetyService.getUserSafetyBlocks(userId);
            return ResponseEntity.ok(safetyBlocks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<SafetyBlock> createSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SafetyBlockRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SafetyBlock safetyBlock = safetyService.createSafetyBlock(userId, request);
            return ResponseEntity.ok(safetyBlock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<SafetyBlock> updateSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String safetyBlockId,
            @Valid @RequestBody SafetyBlockRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            SafetyBlock safetyBlock = safetyService.updateSafetyBlock(userId, safetyBlockId, request);
            return ResponseEntity.ok(safetyBlock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> deleteSafetyBlock(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String safetyBlockId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            safetyService.deleteSafetyBlock(userId, safetyBlockId);
            return successResponse(Map.of(
                "message", "Safety block deleted successfully",
                "deletedSafetyBlockId", safetyBlockId
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

    protected ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", message));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("success", false, "message", message));
    }
}