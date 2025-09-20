package com.tpg.connect.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncResponse {
    // Sync processing results
    private SyncResults results;
    
    // Actions that failed to sync
    private List<FailedSync> failures;
    
    // Server-side updates since last sync
    private List<ServerUpdate> serverUpdates;
    
    // New sync timestamp
    private LocalDateTime newSyncTime;
    
    // Next sync recommendations
    private SyncRecommendations recommendations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResults {
        private int totalProcessed;
        private int successful;
        private int failed;
        private int duplicates;
        private int newMutualMatches;
        private int newMessages;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedSync {
        private String actionId;
        private SyncRequest.ActionType actionType;
        private String reason;
        private String errorCode;
        private boolean retryable;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerUpdate {
        private UpdateType type;
        private String entityId;
        private Object data;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncRecommendations {
        private int nextSyncInSeconds;
        private boolean forceFullSync;
        private boolean backgroundSyncEnabled;
        private List<String> priorityUpdates;
    }
    
    public enum UpdateType {
        NEW_MATCH,
        NEW_MESSAGE,
        MATCH_EXPIRED,
        CONVERSATION_UPDATED,
        PROFILE_UPDATED,
        APP_CONFIG_UPDATED
    }
}