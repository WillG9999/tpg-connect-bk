package com.tpg.connect.controllers.profile;

import com.tpg.connect.model.api.ProfileUpdateRequest;
import com.tpg.connect.model.api.ProfileUpdateResponse;
import com.tpg.connect.model.dto.PhotoUploadRequest;
import com.tpg.connect.model.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "Profile Management", description = "Complete profile management operations")
public interface ProfileControllerApi {

    @Operation(summary = "Get current user profile", description = "Retrieves the complete profile of the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    @GetMapping
    ResponseEntity<Map<String, Object>> getCurrentProfile(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Include user preferences in response")
        @RequestParam(defaultValue = "false") boolean includePreferences
    );

    @Operation(summary = "Update complete profile", description = "Updates all profile fields at once")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid profile data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping
    ResponseEntity<ProfileUpdateResponse> updateProfile(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Complete profile update data", required = true)
        @Valid @RequestBody ProfileUpdateRequest request
    );

    @Operation(summary = "Update basic profile info", description = "Updates basic profile information (name, age, bio, location, interests)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Basic info updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/basic")
    ResponseEntity<Map<String, Object>> updateBasicInfo(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Basic profile update data", required = true)
        @Valid @RequestBody UpdateProfileRequest request
    );

    @Operation(summary = "Upload a profile photo", description = "Adds a new photo to the user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid photo data or too many photos"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/photos")
    ResponseEntity<Map<String, Object>> uploadPhoto(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Photo upload data", required = true)
        @Valid @RequestBody PhotoUploadRequest request
    );

    @Operation(summary = "Remove a profile photo", description = "Removes a specific photo from the user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photo removed successfully"),
        @ApiResponse(responseCode = "400", description = "Photo not found or cannot remove last photo"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/photos/{photoId}")
    ResponseEntity<Map<String, Object>> removePhoto(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "ID of the photo to remove", required = true)
        @PathVariable String photoId
    );

    @Operation(summary = "Update all profile photos", description = "Replaces all profile photos with a new set")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Photos updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid photo data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/photos")
    ResponseEntity<Map<String, Object>> updatePhotos(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "List of photo URLs", required = true)
        @RequestBody List<String> photoUrls
    );

    @Operation(summary = "Update written prompts", description = "Updates all written prompt responses")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Written prompts updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid prompt data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/prompts/written")
    ResponseEntity<Map<String, Object>> updateWrittenPrompts(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "List of written prompts", required = true)
        @RequestBody List<Map<String, String>> writtenPrompts
    );

    @Operation(summary = "Update poll prompts", description = "Updates all poll prompt questions and options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Poll prompts updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid poll data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/prompts/polls")
    ResponseEntity<Map<String, Object>> updatePollPrompts(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "List of poll prompts", required = true)
        @RequestBody List<Map<String, Object>> pollPrompts
    );

    @Operation(summary = "Update field visibility", description = "Updates which profile fields are visible to other users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Field visibility updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid visibility data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/visibility")
    ResponseEntity<Map<String, Object>> updateFieldVisibility(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Field visibility settings", required = true)
        @RequestBody Map<String, Boolean> fieldVisibility
    );

    @Operation(summary = "Update user preferences", description = "Updates user dating and discovery preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid preference data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/preferences")
    ResponseEntity<Map<String, Object>> updatePreferences(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "User preferences", required = true)
        @RequestBody Map<String, Object> preferences
    );
}