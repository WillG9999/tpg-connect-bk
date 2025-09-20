package com.tpg.connect.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncRequest {
    // Actions that were cached offline
    private List<PendingAction> pendingActions;
    
    // Last successful sync timestamp
    private LocalDateTime lastSyncTime;
    
    // Client sync metadata
    private SyncMetadata metadata;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingAction {
        @NotNull
        private String actionId; // client-generated UUID
        
        @NotNull
        private ActionType actionType;
        
        @NotNull
        private String targetId; // userId or conversationId
        
        private Object actionData; // specific data for the action
        
        private LocalDateTime timestamp;
        private int retryCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncMetadata {
        private String deviceId;
        private String appVersion;
        private String platform;
        private boolean backgroundSync;
        private int pendingActionsCount;
    }
    
    public enum ActionType {
        LIKE_USER,
        PASS_USER,
        SUPER_LIKE_USER,
        BLOCK_USER,
        REPORT_USER,
        SEND_MESSAGE,
        UNMATCH_CONVERSATION,
        UPDATE_PROFILE,
        UPDATE_PREFERENCES
    }
}