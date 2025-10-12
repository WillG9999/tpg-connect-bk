package com.tpg.connect.controllers.admin;

import com.tpg.connect.model.dto.AdminUserSummaryDTO;
import com.tpg.connect.model.dto.AdminUserDetailDTO;
import com.tpg.connect.model.user.UserStatus;
import com.tpg.connect.services.AdminUserManagementService;
import com.tpg.connect.utilities.JwtUtil;
import com.tpg.connect.services.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user-management")
@Slf4j
public class AdminUserManagementController {
    
    @Autowired
    private AdminUserManagementService adminUserManagementService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Get ALL users for admin dashboard initialization (preload)
     * This loads everything upfront so user management is instant
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsersForDashboard(HttpServletRequest request) {
        log.info("🚀 Admin requesting ALL users for dashboard preload");
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<AdminUserSummaryDTO> users = adminUserManagementService.getAllUsersForAdminDashboard();
            long totalUsers = adminUserManagementService.getTotalUsersCount();
            
            // Group users by status for easy filtering
            Map<String, Long> userCounts = new HashMap<>();
            userCounts.put("total", totalUsers);
            userCounts.put("active", users.stream().filter(u -> u.getUserStatus() == UserStatus.ACTIVE).count());
            userCounts.put("suspended", users.stream().filter(u -> u.getUserStatus() == UserStatus.SUSPENDED).count());
            userCounts.put("banned", users.stream().filter(u -> u.getUserStatus() == UserStatus.BANNED).count());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("totalUsers", totalUsers);
            response.put("userCounts", userCounts);
            response.put("message", "All users loaded for admin dashboard");
            
            log.info("✅ Preloaded {} users for admin dashboard", users.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error preloading users for admin dashboard: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to load users: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed user information for admin review
     */
    @GetMapping("/users/{connectId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(
            @PathVariable String connectId,
            HttpServletRequest request) {
        
        log.info("👤 Admin requesting user detail for: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            AdminUserDetailDTO userDetail = adminUserManagementService.getUserDetailForAdmin(connectId);
            
            if (userDetail == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", userDetail);
            response.put("message", "Retrieved user detail successfully");
            
            log.info("✅ Retrieved user detail for: {}", connectId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting user detail for {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user detail: " + e.getMessage()));
        }
    }
    
    /**
     * Get raw JSON data from database for a user (both userAuth and userProfiles)
     */
    @GetMapping("/users/{connectId}/raw")
    public ResponseEntity<Map<String, Object>> getUserRawData(
            @PathVariable String connectId,
            @RequestParam(defaultValue = "false") boolean format,
            @RequestParam(defaultValue = "false") boolean chunks,
            @RequestParam(defaultValue = "") String collections,
            HttpServletRequest request) {
        
        log.info("🔍 Admin requesting raw JSON data for user: {} (format={}, chunks={}, collections={})", 
                connectId, format, chunks, collections);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            Map<String, Object> rawData;
            
            // If specific collections requested, filter them
            if (!collections.isEmpty()) {
                String[] requestedCollections = collections.split(",");
                rawData = adminUserManagementService.getUserRawDataFiltered(connectId, requestedCollections);
            } else {
                rawData = adminUserManagementService.getUserRawData(connectId);
            }
            
            if (rawData.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectId", connectId);
            
            if (chunks) {
                // Split large data into manageable chunks for UI display
                response.put("rawDataChunks", adminUserManagementService.chunkRawData(rawData));
                response.put("totalCollections", rawData.keySet().size() - 1); // -1 for metadata
            } else {
                response.put("rawData", rawData);
            }
            
            if (format) {
                // Add formatted/prettified versions for display
                response.put("formattedData", adminUserManagementService.formatRawDataForDisplay(rawData));
            }
            
            response.put("displayOptions", Map.of(
                "format", format,
                "chunks", chunks,
                "collectionsFilter", collections,
                "totalSize", adminUserManagementService.calculateDataSize(rawData)
            ));
            
            response.put("message", "Retrieved raw user data successfully");
            
            log.info("✅ Retrieved raw data for user: {} with options: format={}, chunks={}", 
                    connectId, format, chunks);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting raw data for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get raw user data: " + e.getMessage()));
        }
    }
    
    /**
     * Get available collections for a user (for filtering in UI)
     */
    @GetMapping("/users/{connectId}/raw/collections")
    public ResponseEntity<Map<String, Object>> getAvailableCollections(
            @PathVariable String connectId,
            HttpServletRequest request) {
        
        log.info("📋 Admin requesting available collections for user: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<String> availableCollections = adminUserManagementService.getAvailableCollections(connectId);
            Map<String, Integer> collectionSizes = adminUserManagementService.getCollectionSizes(connectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectId", connectId);
            response.put("availableCollections", availableCollections);
            response.put("collectionSizes", collectionSizes);
            response.put("message", "Retrieved available collections successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting available collections for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get available collections: " + e.getMessage()));
        }
    }
    
    /**
     * Update user status (ACTIVE, SUSPENDED, BANNED)
     */
    @PutMapping("/users/{connectId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable String connectId,
            @RequestBody UserStatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("🔄 Admin updating user {} status to {}", connectId, request.getStatus());
        
        try {
            // Validate admin authentication
            String adminId = getAdminIdFromRequest(httpRequest);
            if (adminId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            // Validate status
            UserStatus newStatus;
            try {
                newStatus = UserStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid status: " + request.getStatus()));
            }
            
            // Update user status
            boolean success = adminUserManagementService.updateUserStatus(
                    connectId, newStatus, adminId, request.getReason());
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("connectId", connectId);
                response.put("newStatus", newStatus.toString());
                response.put("message", "User status updated successfully");
                
                log.info("✅ User {} status updated to {} by admin {}", connectId, newStatus, adminId);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Failed to update user status"));
            }
            
        } catch (Exception e) {
            log.error("❌ Error updating user status for {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update user status: " + e.getMessage()));
        }
    }
    
    /**
     * Refresh user management data (manual refresh)
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshUserData(HttpServletRequest request) {
        log.info("🔄 Admin requesting user data refresh");
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            // This just calls the same method as the preload since we don't cache
            List<AdminUserSummaryDTO> users = adminUserManagementService.getAllUsersForAdminDashboard();
            long totalUsers = adminUserManagementService.getTotalUsersCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("totalUsers", totalUsers);
            response.put("message", "User data refreshed successfully");
            
            log.info("✅ Refreshed user data for admin - {} users", users.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error refreshing user data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to refresh user data: " + e.getMessage()));
        }
    }
    
    /**
     * Get user counts by status for dashboard stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(HttpServletRequest request) {
        log.info("📊 Admin requesting user statistics");
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            long totalUsers = adminUserManagementService.getTotalUsersCount();
            long activeUsers = adminUserManagementService.getUserCountByStatus(UserStatus.ACTIVE);
            long suspendedUsers = adminUserManagementService.getUserCountByStatus(UserStatus.SUSPENDED);
            long bannedUsers = adminUserManagementService.getUserCountByStatus(UserStatus.BANNED);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", totalUsers);
            stats.put("active", activeUsers);
            stats.put("suspended", suspendedUsers);
            stats.put("banned", bannedUsers);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            response.put("message", "User statistics retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting user statistics: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user statistics: " + e.getMessage()));
        }
    }
    
    // Helper methods
    
    private boolean isAdminAuthenticated(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return false;
            
            if (!jwtUtil.validateToken(token)) return false;
            
            String userId = jwtUtil.extractSubject(token);
            if (userId == null) return false;
            
            // TODO: Re-enable admin role check when CORS is fixed
            // For now, any authenticated user can access admin endpoints for testing
            log.info("🔓 Allowing admin access for user: {} (role check disabled)", userId);
            return true;
            
        } catch (Exception e) {
            log.warn("❌ Admin authentication failed: {}", e.getMessage());
            return false;
        }
    }
    
    private String getAdminIdFromRequest(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null) return null;
            
            if (!jwtUtil.validateToken(token)) return null;
            
            String userId = jwtUtil.extractSubject(token);
            if (userId == null) return null;
            
            // TODO: Verify user has admin role when CORS is fixed
            // For now, return the userId as adminId
            return userId;
            
        } catch (Exception e) {
            log.warn("❌ Failed to get admin ID: {}", e.getMessage());
            return null;
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
    
    // Request DTOs
    
    public static class UserStatusUpdateRequest {
        private String status;
        private String reason;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}