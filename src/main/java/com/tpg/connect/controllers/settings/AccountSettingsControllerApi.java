package com.tpg.connect.controllers.settings;

import com.tpg.connect.model.dto.AccountSettingsRequest;
import com.tpg.connect.model.dto.NotificationSettingsRequest;
import com.tpg.connect.model.dto.PrivacySettingsRequest;
import com.tpg.connect.model.settings.AccountSettings;
import com.tpg.connect.model.settings.NotificationSettings;
import com.tpg.connect.model.settings.PrivacySettings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "Account Settings", description = "User account settings and preferences management")
public interface AccountSettingsControllerApi {

    @Operation(summary = "Get account settings", description = "Retrieve user's account settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/account")
    ResponseEntity<AccountSettings> getAccountSettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Update account settings", description = "Update user's account settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid settings data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/account")
    ResponseEntity<AccountSettings> updateAccountSettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Account settings to update", required = true)
        @Valid @RequestBody AccountSettingsRequest request
    );

    @Operation(summary = "Get privacy settings", description = "Retrieve user's privacy settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Privacy settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/privacy")
    ResponseEntity<PrivacySettings> getPrivacySettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Update privacy settings", description = "Update user's privacy settings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Privacy settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid privacy settings"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/privacy")
    ResponseEntity<PrivacySettings> updatePrivacySettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Privacy settings to update", required = true)
        @Valid @RequestBody PrivacySettingsRequest request
    );

    @Operation(summary = "Get notification settings", description = "Retrieve user's notification preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/notifications")
    ResponseEntity<NotificationSettings> getNotificationSettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Update notification settings", description = "Update user's notification preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid notification settings"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/notifications")
    ResponseEntity<NotificationSettings> updateNotificationSettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Notification settings to update", required = true)
        @Valid @RequestBody NotificationSettingsRequest request
    );

    @Operation(summary = "Deactivate account", description = "Temporarily deactivate user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/deactivate")
    ResponseEntity<Map<String, Object>> deactivateAccount(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Reactivate account", description = "Reactivate previously deactivated account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account reactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/reactivate")
    ResponseEntity<Map<String, Object>> reactivateAccount(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Request data export", description = "Request export of all user data for download")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Data export requested successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/export-data")
    ResponseEntity<Map<String, Object>> requestDataExport(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );
}