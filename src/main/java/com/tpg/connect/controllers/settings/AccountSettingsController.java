package com.tpg.connect.controllers.settings;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.AccountSettingsRequest;
import com.tpg.connect.model.dto.NotificationSettingsRequest;
import com.tpg.connect.model.dto.PrivacySettingsRequest;
import com.tpg.connect.model.settings.AccountSettings;
import com.tpg.connect.model.settings.NotificationSettings;
import com.tpg.connect.model.settings.PrivacySettings;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.SettingsService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class AccountSettingsController extends BaseController implements AccountSettingsControllerApi {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<AccountSettings> getAccountSettings(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AccountSettings settings = settingsService.getAccountSettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<AccountSettings> updateAccountSettings(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AccountSettingsRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AccountSettings settings = settingsService.updateAccountSettings(userId, request);
            return ResponseEntity.ok(settings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<PrivacySettings> getPrivacySettings(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PrivacySettings settings = settingsService.getPrivacySettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<PrivacySettings> updatePrivacySettings(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PrivacySettingsRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            PrivacySettings settings = settingsService.updatePrivacySettings(userId, request);
            return ResponseEntity.ok(settings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<NotificationSettings> getNotificationSettings(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            NotificationSettings settings = settingsService.getNotificationSettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<NotificationSettings> updateNotificationSettings(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody NotificationSettingsRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            NotificationSettings settings = settingsService.updateNotificationSettings(userId, request);
            return ResponseEntity.ok(settings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> deactivateAccount(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            settingsService.deactivateAccount(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account deactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to deactivate account: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> reactivateAccount(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            settingsService.reactivateAccount(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Account reactivated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to reactivate account: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> requestDataExport(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            String exportId = settingsService.requestDataExport(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Data export requested successfully",
                "exportId", exportId,
                "estimatedTime", "24-48 hours"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to request data export: " + e.getMessage()));
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
}