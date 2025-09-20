package com.tpg.connect.controllers.discovery;

import com.tpg.connect.model.api.DiscoveryResponse;
import com.tpg.connect.model.api.LikeResponse;
import com.tpg.connect.model.dto.DiscoveryRequest;
import com.tpg.connect.model.dto.DiscoverySettingsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "Discovery", description = "User discovery and matching operations")
public interface DiscoveryControllerApi {

    @Operation(summary = "Get potential matches", description = "Retrieves a list of potential matches based on user preferences and filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Potential matches retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    @PostMapping("/potential-matches")
    ResponseEntity<DiscoveryResponse> getPotentialMatches(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Discovery filters and preferences")
        @Valid @RequestBody(required = false) DiscoveryRequest request
    );

    @Operation(summary = "Like a user", description = "Express interest in another user - may result in a match if mutual")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User liked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or already liked"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/like/{userId}")
    ResponseEntity<LikeResponse> likeUser(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of the user to like", required = true)
        @PathVariable String userId
    );

    @Operation(summary = "Dislike a user", description = "Pass on a user - they won't appear in discovery again")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User disliked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/dislike/{userId}")
    ResponseEntity<Map<String, Object>> dislikeUser(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of the user to dislike", required = true)
        @PathVariable String userId
    );

    @Operation(summary = "Get discovery settings", description = "Retrieves current discovery preferences (age range, distance, gender preference)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discovery settings retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/settings")
    ResponseEntity<Map<String, Object>> getDiscoverySettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Update discovery settings", description = "Updates discovery preferences and filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Discovery settings updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid settings data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/settings")
    ResponseEntity<Map<String, Object>> updateDiscoverySettings(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Updated discovery settings", required = true)
        @Valid @RequestBody DiscoverySettingsRequest request
    );
}