package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import com.tpg.connect.model.user.ApplicationStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String connectId;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private Boolean active;
    private Boolean emailVerified;
    private ApplicationStatus applicationStatus;
    
    // Convenience methods for backwards compatibility
    public boolean isActive() {
        return active != null && active;
    }
    
    public boolean isEmailVerified() {
        return emailVerified != null && emailVerified;
    }
    
    public boolean canAccessApp() {
        return applicationStatus != null && applicationStatus.canLogin();
    }
    
    public boolean needsPayment() {
        return applicationStatus != null && applicationStatus.needsPayment();
    }
    
    public boolean isPendingApproval() {
        return applicationStatus != null && applicationStatus.isPending();
    }
    
    // Backwards compatibility method
    public String getPassword() {
        return passwordHash;
    }
    
    // Backwards compatibility method
    public String getId() {
        return connectId;
    }
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastLoginAt;
    private Timestamp emailVerifiedAt;
    private Timestamp deletedAt;
    private String lastLoginDevice;
    private List<FcmToken> fcmTokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FcmToken {
        private String token;
        private String deviceType;
        private String deviceId;
        private Timestamp addedAt;
        private Timestamp lastUsed;
        private Boolean isActive;
    }
}