package com.tpg.connect.controllers.user;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.CloudStorageService;
import com.tpg.connect.services.ProfileManagementService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController {

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private ProfileManagementService profileService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CloudStorageService cloudStorageService;

    // Get current user (frontend expects /api/users/me)
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "false") boolean includePreferences,
            @RequestParam(defaultValue = "false") boolean includeMatches) {
        
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
            return errorResponse("Failed to retrieve user: " + e.getMessage());
        }
    }

    // Update user (frontend expects PUT /api/users/{userId})
    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId,
            @Valid @RequestBody Map<String, Object> updateData) {
        
        String currentUserId = validateAndExtractUserId(authHeader);
        if (currentUserId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        // Users can only update their own profile
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Cannot update another user's profile"));
        }

        try {
            // Create update request from the data
            com.tpg.connect.model.dto.UpdateProfileRequest request = new com.tpg.connect.model.dto.UpdateProfileRequest();
            
            // Map the fields from frontend format to backend format
            if (updateData.containsKey("name")) request.setName((String) updateData.get("name"));
            if (updateData.containsKey("location")) request.setLocation((String) updateData.get("location"));
            if (updateData.containsKey("interests")) request.setInterests((List<String>) updateData.get("interests"));
            if (updateData.containsKey("languages")) request.setLanguages((List<String>) updateData.get("languages"));
            if (updateData.containsKey("jobTitle")) request.setJobTitle((String) updateData.get("jobTitle"));
            if (updateData.containsKey("company")) request.setCompany((String) updateData.get("company"));
            
            // Identity fields
            if (updateData.containsKey("pronouns")) request.setPronouns((String) updateData.get("pronouns"));
            if (updateData.containsKey("gender")) request.setGender((String) updateData.get("gender"));
            if (updateData.containsKey("sexuality")) request.setSexuality((String) updateData.get("sexuality"));
            if (updateData.containsKey("interestedIn")) request.setInterestedIn((String) updateData.get("interestedIn"));
            
            // Professional fields
            if (updateData.containsKey("university")) request.setUniversity((String) updateData.get("university"));
            if (updateData.containsKey("educationLevel")) request.setEducationLevel((String) updateData.get("educationLevel"));
            
            // Personal fields
            if (updateData.containsKey("religiousBeliefs")) request.setReligiousBeliefs((String) updateData.get("religiousBeliefs"));
            if (updateData.containsKey("hometown")) request.setHometown((String) updateData.get("hometown"));
            if (updateData.containsKey("politics")) request.setPolitics((String) updateData.get("politics"));
            if (updateData.containsKey("datingIntentions")) request.setDatingIntentions((String) updateData.get("datingIntentions"));
            if (updateData.containsKey("relationshipType")) request.setRelationshipType((String) updateData.get("relationshipType"));
            
            // Physical/lifestyle fields
            if (updateData.containsKey("height")) request.setHeight((String) updateData.get("height"));
            if (updateData.containsKey("ethnicity")) request.setEthnicity((String) updateData.get("ethnicity"));
            if (updateData.containsKey("children")) request.setChildren((String) updateData.get("children"));
            if (updateData.containsKey("familyPlans")) request.setFamilyPlans((String) updateData.get("familyPlans"));
            if (updateData.containsKey("pets")) request.setPets((String) updateData.get("pets"));
            if (updateData.containsKey("zodiacSign")) request.setZodiacSign((String) updateData.get("zodiacSign"));
            
            // Prompt fields
            if (updateData.containsKey("photoPrompts")) {
                request.setPhotoPrompts((Map<String, Map<String, String>>) updateData.get("photoPrompts"));
            }
            if (updateData.containsKey("writtenPrompts")) {
                request.setWrittenPrompts((List<Map<String, String>>) updateData.get("writtenPrompts"));
            }
            if (updateData.containsKey("pollPrompts")) {
                request.setPollPrompts((List<Map<String, Object>>) updateData.get("pollPrompts"));
            }
            
            CompleteUserProfile updatedProfile = profileService.updateBasicInfo(userId, request);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            return successResponse(dto);
        } catch (Exception e) {
            return errorResponse("Failed to update user: " + e.getMessage());
        }
    }

    // Get user by ID (frontend expects /api/users/{id})
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        
        String currentUserId = validateAndExtractUserId(authHeader);
        if (currentUserId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            CompleteUserProfile profile = profileService.getCurrentProfile(userId, false);
            if (profile == null) {
                return notFoundResponse("User not found");
            }
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(profile);
            return successResponse(dto);
        } catch (Exception e) {
            return errorResponse("Failed to retrieve user: " + e.getMessage());
        }
    }

    // Search users (frontend expects /api/users/search)
    @GetMapping("/search")
    public ResponseEntity<List<UserProfileDTO>> searchUsers(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> interests,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // For now, return basic search results using discovery service
            // In a real implementation, you'd implement proper search logic
            List<CompleteUserProfile> profiles = profileService.searchUsers(
                userId, minAge, maxAge, location, interests, page, size, sortBy);
            
            List<UserProfileDTO> dtos = profiles.stream()
                .map(UserProfileDTO::fromCompleteUserProfile)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // Like user (frontend expects /api/users/{id}/like)
    @PostMapping("/{targetUserId}/like")
    public ResponseEntity<Map<String, Object>> likeUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            // Delegate to discovery service for actual like logic
            Map<String, Object> result = userService.likeUser(userId, targetUserId);
            return successResponse(result);
        } catch (Exception e) {
            return errorResponse("Failed to like user: " + e.getMessage());
        }
    }

    // Dislike user (frontend expects /api/users/{id}/dislike)
    @PostMapping("/{targetUserId}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            // Delegate to discovery service for actual dislike logic
            Map<String, Object> result = userService.dislikeUser(userId, targetUserId);
            return successResponse(result);
        } catch (Exception e) {
            return errorResponse("Failed to dislike user: " + e.getMessage());
        }
    }

    // Block user (frontend expects /api/users/{id}/block)
    @PostMapping("/{targetUserId}/block")
    public ResponseEntity<Map<String, Object>> blockUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> result = userService.blockUser(userId, targetUserId);
            return successResponse(result);
        } catch (Exception e) {
            return errorResponse("Failed to block user: " + e.getMessage());
        }
    }

    // Report user (frontend expects /api/users/{id}/report)
    @PostMapping("/{targetUserId}/report")
    public ResponseEntity<Map<String, Object>> reportUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String targetUserId,
            @Valid @RequestBody Map<String, String> reportData) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            String reason = reportData.get("reason");
            Map<String, Object> result = userService.reportUser(userId, targetUserId, reason);
            return successResponse(result);
        } catch (Exception e) {
            return errorResponse("Failed to report user: " + e.getMessage());
        }
    }

    // Upload profile photo (frontend expects POST /api/users/photos)
    @PostMapping("/photos")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            // Upload photo to cloud storage
            String photoUrl = cloudStorageService.uploadProfilePhoto(userId, photo);
            
            // Update profile with new photo
            CompleteUserProfile updatedProfile = profileService.addPhoto(userId, photoUrl, isPrimary);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            
            return successResponse(Map.of(
                "profile", dto,
                "photoUrl", photoUrl,
                "message", "Photo uploaded successfully"
            ));
        } catch (Exception e) {
            return errorResponse("Failed to upload photo: " + e.getMessage());
        }
    }

    // Update user photos (frontend expects PUT /api/users/photos)
    @PutMapping("/photos")
    public ResponseEntity<Map<String, Object>> updatePhotos(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> photoUrls = (List<String>) request.get("photos");
            
            if (photoUrls == null || photoUrls.isEmpty()) {
                return errorResponse("At least one photo URL is required");
            }

            CompleteUserProfile updatedProfile = profileService.updatePhotos(userId, photoUrls);
            UserProfileDTO dto = UserProfileDTO.fromCompleteUserProfile(updatedProfile);
            
            return successResponse(Map.of(
                "profile", dto,
                "message", "Photos updated successfully"
            ));
        } catch (Exception e) {
            return errorResponse("Failed to update photos: " + e.getMessage());
        }
    }

    // Remove photo (frontend expects DELETE /api/users/photos/{photoId})
    @DeleteMapping("/photos/{photoId}")
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
            
            return successResponse(Map.of(
                "profile", dto,
                "message", "Photo removed successfully"
            ));
        } catch (Exception e) {
            return errorResponse("Failed to remove photo: " + e.getMessage());
        }
    }

    // Delete user (frontend expects DELETE /api/users/{userId})
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        
        String currentUserId = validateAndExtractUserId(authHeader);
        if (currentUserId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        // Users can only delete their own account
        if (!currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Cannot delete another user's account"));
        }

        try {
            // Delegate to user service for deletion
            boolean deleted = userService.deactivateUser(currentUserId);
            if (deleted) {
                return successResponse(Map.of("message", "User account deleted successfully"));
            } else {
                return errorResponse("Failed to delete user account");
            }
        } catch (Exception e) {
            return errorResponse("Failed to delete user: " + e.getMessage());
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


}