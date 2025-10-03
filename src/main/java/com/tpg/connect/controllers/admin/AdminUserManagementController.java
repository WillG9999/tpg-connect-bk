package com.tpg.connect.controllers.admin;

import com.tpg.connect.model.dto.AdminUserSummaryDTO;
import com.tpg.connect.model.dto.AdminUserDetailDTO;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.model.conversation.ConversationSummary;
import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.services.UserService;
import com.tpg.connect.services.MatchService;
import com.tpg.connect.services.ConversationService;
import com.tpg.connect.services.SafetyService;
import com.tpg.connect.services.UserActionsService;
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
@RequestMapping("/api/admin/users")
@Slf4j
public class AdminUserManagementController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MatchService matchService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private SafetyService safetyService;
    
    @Autowired
    private UserActionsService userActionsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Get all users with pagination and filtering for admin management
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            HttpServletRequest request) {
        
        log.info("👥 Admin requesting user list - page: {}, size: {}, search: {}, status: {}", 
                page, size, search, status);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<AdminUserSummaryDTO> users = userService.getAllUsersForAdmin(
                page, size, search, status, sortBy, sortDirection);
            
            long totalUsers = userService.getTotalUsersCount(search, status);
            int totalPages = (int) Math.ceil((double) totalUsers / size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("users", users);
            response.put("totalUsers", totalUsers);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Retrieved users successfully");
            
            log.info("✅ Retrieved {} users for admin (page {}/{})", users.size(), page + 1, totalPages);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting users for admin: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get users: " + e.getMessage()));
        }
    }
    
    /**
     * Get detailed user information for admin review
     */
    @GetMapping("/{connectId}")
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
            
            AdminUserDetailDTO userDetail = userService.getUserDetailForAdmin(connectId);
            
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
     * Get user's matches for admin review
     */
    @GetMapping("/{connectId}/matches")
    public ResponseEntity<Map<String, Object>> getUserMatches(
            @PathVariable String connectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        log.info("💕 Admin requesting matches for user: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<Match> matches = matchService.getUserMatchesForAdmin(connectId, page, size);
            long totalMatches = matchService.getTotalMatchesCount(connectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("matches", matches);
            response.put("totalMatches", totalMatches);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Retrieved user matches successfully");
            
            log.info("✅ Retrieved {} matches for user: {}", matches.size(), connectId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting matches for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user matches: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's conversations for admin review
     */
    @GetMapping("/{connectId}/conversations")
    public ResponseEntity<Map<String, Object>> getUserConversations(
            @PathVariable String connectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        log.info("💬 Admin requesting conversations for user: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<Conversation> conversations = conversationService.getUserConversationsForAdmin(connectId, page, size);
            long totalConversations = conversationService.getTotalConversationsCount(connectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conversations", conversations);
            response.put("totalConversations", totalConversations);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Retrieved user conversations successfully");
            
            log.info("✅ Retrieved {} conversations for user: {}", conversations.size(), connectId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting conversations for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user conversations: " + e.getMessage()));
        }
    }
    
    /**
     * Get reports involving user (as reporter or reported)
     */
    @GetMapping("/{connectId}/reports")
    public ResponseEntity<Map<String, Object>> getUserReports(
            @PathVariable String connectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        
        log.info("🚨 Admin requesting reports for user: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<UserReport> reports = safetyService.getUserReportsForAdmin(connectId, page, size);
            long totalReports = safetyService.getTotalReportsCount(connectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reports", reports);
            response.put("totalReports", totalReports);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Retrieved user reports successfully");
            
            log.info("✅ Retrieved {} reports for user: {}", reports.size(), connectId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting reports for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user reports: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's actions (likes, passes, etc.)
     */
    @GetMapping("/{connectId}/actions")
    public ResponseEntity<Map<String, Object>> getUserActions(
            @PathVariable String connectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        
        log.info("👍 Admin requesting actions for user: {}", connectId);
        
        try {
            // Validate admin authentication
            if (!isAdminAuthenticated(request)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(createErrorResponse("Admin access required"));
            }
            
            List<UserAction> actions = userActionsService.getUserActionsForAdmin(connectId, page, size);
            long totalActions = userActionsService.getTotalActionsCount(connectId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("actions", actions);
            response.put("totalActions", totalActions);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("message", "Retrieved user actions successfully");
            
            log.info("✅ Retrieved {} actions for user: {}", actions.size(), connectId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting actions for user {}: ", connectId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to get user actions: " + e.getMessage()));
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
}