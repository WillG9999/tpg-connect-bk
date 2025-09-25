package com.tpg.connect.controllers.application;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.model.application.ApplicationSubmission;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.services.ApplicationService;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.CloudStorageService;
import com.tpg.connect.utilities.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
@Slf4j
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private AuthenticationService authService;
    
    @Autowired
    private CloudStorageService cloudStorageService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Submit a complete application with all required information
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitApplication(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ApplicationSubmission application) {
        
        log.info("📝 Received application submission for email: {}", application.getEmail());
        
        try {
            // Extract ConnectID from JWT token using consistent authentication
            String connectId = validateAndExtractConnectId(authHeader);
            if (connectId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing authorization"));
            }
            
            // Set the ConnectID from token
            application.setConnectId(connectId);
            
            // Submit the application
            ApplicationSubmission savedApplication = applicationService.submitApplication(application);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectId", savedApplication.getConnectId());
            response.put("status", savedApplication.getStatus().name());
            response.put("message", "Application submitted successfully");
            
            log.info("✅ Application submitted successfully for ConnectID: {}", savedApplication.getConnectId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error submitting application: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to submit application: " + e.getMessage()));
        }
    }
    
    /**
     * Upload a photo for an application
     */
    @PostMapping("/photos")
    public ResponseEntity<Map<String, Object>> uploadApplicationPhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("photoIndex") int photoIndex,
            @RequestParam("connectId") String connectId) {
        
        log.info("📸 Uploading application photo {} for ConnectID: {}", photoIndex + 1, connectId);
        
        try {
            // Debug logging for authentication
            log.info("🔍 Auth header present: {}", authHeader != null);
            log.info("🔍 ConnectID from request: {}", connectId);
            
            // Validate authentication using same pattern as UserController
            String tokenConnectId = validateAndExtractConnectId(authHeader);
            log.info("🔍 Token extracted ConnectID: {}", tokenConnectId);
            
            if (tokenConnectId == null) {
                log.error("❌ Authentication failed - no valid ConnectID extracted");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing authorization"));
            }
            
            // Verify user can only upload photos for their own application
            if (!connectId.equals(tokenConnectId)) {
                log.error("❌ Authorization failed - ConnectID mismatch. Request: {}, Token: {}", connectId, tokenConnectId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Cannot upload photos for another user"));
            }
            
            log.info("✅ Authentication successful for ConnectID: {}", tokenConnectId);
            
            // Validate photo
            if (photo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Photo file is required"));
            }
            
            if (!isValidImageType(photo.getContentType())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid image type. Only JPEG, PNG, and WebP are allowed"));
            }
            
            // Upload photo to applications/{connectId}/ folder using the same method as profile photos
            String photoUrl = cloudStorageService.uploadApplicationPhoto(connectId, photo, photoIndex);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("photoUrl", photoUrl);
            response.put("photoIndex", photoIndex);
            response.put("message", "Photo uploaded successfully");
            
            log.info("✅ Application photo uploaded successfully: {}", photoUrl);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error uploading application photo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload photo: " + e.getMessage()));
        }
    }
    
    /**
     * Get application status for a user
     */
    @GetMapping("/status/{connectId}")
    public ResponseEntity<Map<String, Object>> getApplicationStatus(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String connectId) {
        
        log.info("📋 Getting application status for ConnectID: {}", connectId);
        
        try {
            // Validate authentication using consistent pattern
            String tokenConnectId = validateAndExtractConnectId(authHeader);
            if (tokenConnectId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing authorization"));
            }
            
            // Verify user can only view their own application status
            if (!connectId.equals(tokenConnectId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Cannot view application status for another user"));
            }
            
            // Get application
            ApplicationSubmission application = applicationService.getApplicationByConnectId(connectId);
            
            if (application == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectId", application.getConnectId());
            response.put("status", application.getStatus().name());
            response.put("statusDescription", application.getStatus().getDescription());
            response.put("submittedAt", application.getSubmittedAt());
            response.put("reviewedAt", application.getReviewedAt());
            
            // Only include rejection reason if rejected
            if (application.getStatus() == ApplicationStatus.REJECTED && application.getRejectionReason() != null) {
                response.put("rejectionReason", application.getRejectionReason());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting application status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get application status: " + e.getMessage()));
        }
    }
    
    /**
     * Update an existing application (before approval)
     */
    @PutMapping("/{connectId}")
    public ResponseEntity<Map<String, Object>> updateApplication(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String connectId,
            @RequestBody ApplicationSubmission updatedApplication) {
        
        log.info("📝 Updating application for ConnectID: {}", connectId);
        
        try {
            // Validate authentication using consistent pattern
            String tokenConnectId = validateAndExtractConnectId(authHeader);
            if (tokenConnectId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Invalid or missing authorization"));
            }
            
            // Verify user can only update their own application
            if (!connectId.equals(tokenConnectId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Cannot update application for another user"));
            }
            
            // Get existing application
            ApplicationSubmission existingApplication = applicationService.getApplicationById(connectId);
            if (existingApplication == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Only allow updates for pending applications
            if (existingApplication.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Cannot update application after review"));
            }
            
            // Update the application
            ApplicationSubmission updated = applicationService.updateApplication(connectId, updatedApplication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectId", updated.getConnectId());
            response.put("message", "Application updated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error updating application: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update application: " + e.getMessage()));
        }
    }
    
    // Helper methods
    
    private String validateAndExtractConnectId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.isTokenValid(token)) {
            return null;
        }

        return authService.extractUserIdFromToken(token);
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/webp")
        );
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}