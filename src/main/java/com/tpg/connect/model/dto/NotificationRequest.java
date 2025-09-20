package com.tpg.connect.model.dto;

import com.tpg.connect.model.notifications.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "Notification type is required")
    private Notification.NotificationType type;
    
    private String title;
    
    private String message;
    
    private Map<String, Object> data;
    
    private Notification.NotificationChannel channel = Notification.NotificationChannel.PUSH;
    
    private Notification.NotificationPriority priority = Notification.NotificationPriority.NORMAL;
    
    private String actionUrl;
    
    private String imageUrl;
    
    private boolean sendImmediately = true;
}