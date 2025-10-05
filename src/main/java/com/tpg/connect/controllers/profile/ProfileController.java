package com.tpg.connect.controllers.profile;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.api.ProfileUpdateRequest;
import com.tpg.connect.model.api.ProfileUpdateResponse;
import com.tpg.connect.model.dto.PhotoUploadRequest;
import com.tpg.connect.model.dto.UpdateProfileRequest;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.ProfileManagementService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController extends BaseController implements ProfileControllerApi {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ProfileManagementService profileService;
    
    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Map<String, Object>> getCurrentProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "false") boolean includePreferences) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile profile = profileService.getCurrentProfile(userId, includePreferences);
            if (profile == null) {
                return notFoundResponse("Profile not found");
            }
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(profile);
            return successResponse(dto);
        } catch (Exception e) {
            return errorResponse("Failed to retrieve profile: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<ProfileUpdateResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProfileUpdateRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ProfileUpdateResponse(false, "Invalid or missing authorization", null));
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updateProfile(userId, request);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return ResponseEntity.ok(new ProfileUpdateResponse(true, "Profile updated successfully", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ProfileUpdateResponse(false, "Failed to update profile: " + e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateBasicInfo(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updateBasicInfo(userId, request);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Basic info updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update basic info: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PhotoUploadRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.addPhoto(userId, request.getPhotoUrl(), request.isPrimary());
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Photo uploaded successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to upload photo: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> removePhoto(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String photoId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.removePhoto(userId, photoId);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Photo removed successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to remove photo: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updatePhotos(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<String> photoUrls) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updatePhotos(userId, photoUrls);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Photos updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update photos: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateWrittenPrompts(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<Map<String, String>> writtenPrompts) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updateWrittenPrompts(userId, writtenPrompts);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Written prompts updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update written prompts: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updatePollPrompts(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody List<Map<String, Object>> pollPrompts) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updatePollPrompts(userId, pollPrompts);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Poll prompts updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update poll prompts: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updateFieldVisibility(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Boolean> fieldVisibility) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updateFieldVisibility(userId, fieldVisibility);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Field visibility updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update field visibility: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> preferences) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile updatedProfile = profileService.updatePreferences(userId, preferences);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(Map.of("profile", dto, "message", "Preferences updated successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to update preferences: " + e.getMessage());
        }
    }

    @PostMapping("/refresh-photo-urls")
    public ResponseEntity<Map<String, Object>> refreshPhotoUrls(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile refreshedProfile = profileService.refreshPhotoUrls(userId);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(refreshedProfile);
            return successResponse(Map.of("profile", dto, "message", "Photo URLs refreshed successfully"));
        } catch (Exception e) {
            return errorResponse("Failed to refresh photo URLs: " + e.getMessage());
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

    protected ResponseEntity<Map<String, Object>> notFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("success", false, "message", message));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("success", false, "message", message));
    }
}