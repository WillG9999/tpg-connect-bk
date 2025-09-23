package com.tpg.connect.model.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUser {
    private String id;
    private String userId;           // User who did the blocking
    private String blockedUserId;    // User who was blocked
    private String reason;
    private LocalDateTime blockedAt;
    private BlockStatus status;
    
    public enum BlockStatus {
        ACTIVE,
        REMOVED
    }
}