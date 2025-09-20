package com.tpg.connect.model.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "device_tokens")
public class DeviceToken {
    
    @Id
    private String id;
    
    private String userId;
    
    private String token;
    
    private DeviceType deviceType;
    
    private String deviceId;
    
    private String appVersion;
    
    private String osVersion;
    
    private boolean isActive;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastUsedAt;
    
    private LocalDateTime expiresAt;
    
    public enum DeviceType {
        IOS,
        ANDROID,
        WEB
    }
    
    public boolean isValid() {
        return isActive && 
               token != null && 
               !token.isEmpty() &&
               (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }
    
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}