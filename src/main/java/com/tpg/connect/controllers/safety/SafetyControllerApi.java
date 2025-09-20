package com.tpg.connect.controllers.safety;

import com.tpg.connect.model.dto.BlockUserRequest;
import com.tpg.connect.model.dto.ReportUserRequest;
import com.tpg.connect.model.dto.SafetyBlockRequest;
import com.tpg.connect.model.BlockedUser;
import com.tpg.connect.model.SafetyBlock;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "Safety", description = "User safety features including blocking, reporting, and proactive safety rules")
public interface SafetyControllerApi {

    @Operation(summary = "Block a user", description = "Block a user to prevent them from seeing your profile or contacting you")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User blocked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or user already blocked"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/block")
    ResponseEntity<Map<String, Object>> blockUser(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "User blocking details", required = true)
        @Valid @RequestBody BlockUserRequest request
    );

    @Operation(summary = "Unblock a user", description = "Remove a user from your blocked list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User unblocked successfully"),
        @ApiResponse(responseCode = "400", description = "User not found or not blocked"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/block/{targetUserId}")
    ResponseEntity<Map<String, Object>> unblockUser(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of user to unblock", required = true)
        @PathVariable String targetUserId
    );

    @Operation(summary = "Get blocked users", description = "Retrieve list of users you have blocked")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Blocked users retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/blocked-users")
    ResponseEntity<List<BlockedUser>> getBlockedUsers(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Report a user", description = "Report a user for inappropriate behavior or content")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User reported successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid report data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/report")
    ResponseEntity<Map<String, Object>> reportUser(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "User report details", required = true)
        @Valid @RequestBody ReportUserRequest request
    );

    @Operation(summary = "Get safety blocks", description = "Retrieve your proactive safety block rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Safety blocks retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/safety-blocks")
    ResponseEntity<List<SafetyBlock>> getSafetyBlocks(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Create safety block rule", description = "Create a proactive safety block rule (e.g., block by name pattern, location, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Safety block created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid safety block data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/safety-blocks")
    ResponseEntity<SafetyBlock> createSafetyBlock(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Safety block rule details", required = true)
        @Valid @RequestBody SafetyBlockRequest request
    );

    @Operation(summary = "Update safety block rule", description = "Update an existing safety block rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Safety block updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid safety block data or not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/safety-blocks/{safetyBlockId}")
    ResponseEntity<SafetyBlock> updateSafetyBlock(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of safety block to update", required = true)
        @PathVariable String safetyBlockId,
        @Parameter(description = "Updated safety block details", required = true)
        @Valid @RequestBody SafetyBlockRequest request
    );

    @Operation(summary = "Delete safety block rule", description = "Delete a safety block rule")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Safety block deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Safety block not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/safety-blocks/{safetyBlockId}")
    ResponseEntity<Map<String, Object>> deleteSafetyBlock(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of safety block to delete", required = true)
        @PathVariable String safetyBlockId
    );
}