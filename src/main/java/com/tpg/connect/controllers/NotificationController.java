package com.tpg.connect.controllers;

import com.tpg.connect.model.dto.NotificationRequest;
import com.tpg.connect.model.notifications.Notification;
import com.tpg.connect.services.NotificationService;
import com.tpg.connect.utilities.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve notifications for the current user with pagination")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String userId = extractUserIdFromToken(authHeader);
        
        List<Notification> notifications = notificationService.getUserNotifications(userId, page, size);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);
        response.put("totalCount", notifications.size());
        response.put("page", page);
        response.put("size", size);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications for the current user")
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        
        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("notifications", unreadNotifications);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(summary = "Get unread count", description = "Get the count of unread notifications")
    public ResponseEntity<Map<String, Object>> getUnreadCount(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        long unreadCount = notificationService.getUnreadCount(userId);
        
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification (admin only)")
    public ResponseEntity<Map<String, Object>> createNotification(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody NotificationRequest request) {
        
        // In a real app, you'd check admin permissions here
        
        notificationService.createAndSendNotification(request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notification created successfully"
        ));
    }

    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String notificationId) {
        
        String userId = extractUserIdFromToken(authHeader);
        notificationService.markAsRead(userId, notificationId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notification marked as read"
        ));
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the current user")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        notificationService.markAllAsRead(userId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All notifications marked as read"
        ));
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String notificationId) {
        
        String userId = extractUserIdFromToken(authHeader);
        notificationService.deleteNotification(userId, notificationId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Notification deleted successfully"
        ));
    }

    @DeleteMapping
    @Operation(summary = "Delete all notifications", description = "Delete all notifications for the current user")
    public ResponseEntity<Map<String, Object>> deleteAllNotifications(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        notificationService.deleteAllUserNotifications(userId);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All notifications deleted successfully"
        ));
    }


    @PostMapping("/test")
    @Operation(summary = "Send test notification", description = "Send a test notification to the current user")
    public ResponseEntity<Map<String, Object>> sendTestNotification(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = extractUserIdFromToken(authHeader);
        
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.SYSTEM_MAINTENANCE);
        request.setTitle("Test Notification");
        request.setMessage("This is a test notification from Connect!");
        request.setPriority(Notification.NotificationPriority.NORMAL);
        
        notificationService.createAndSendNotification(request);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Test notification sent successfully"
        ));
    }

    private String extractUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // Extract username/userId from JWT token
                String username = jwtUtil.extractUsername(token);
                
                // Validate token
                if (jwtUtil.isTokenValid(token, username)) {
                    return username; // In this app, username is the userId
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}