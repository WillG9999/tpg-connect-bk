package com.tpg.connect.model.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "blocked_users")
public class BlockedUser {
    @Id
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