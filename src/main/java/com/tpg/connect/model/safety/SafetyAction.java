package com.tpg.connect.model.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyAction {
    private String id;
    private String userId;
    private String targetUserId;
    private SafetyActionType actionType;
    private String reason;
    private LocalDateTime timestamp;
    private boolean processed;
    
    public enum SafetyActionType {
        BLOCK,
        UNBLOCK,
        REPORT,
        UNMATCH
    }
}