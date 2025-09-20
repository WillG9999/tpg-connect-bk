package com.tpg.connect.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionsBatchResponse {
    // Processing results
    private ProcessingResults results;
    
    // Any new mutual matches from this batch
    private List<NewMutualMatch> newMatches;
    
    // Actions that failed to process
    private List<FailedAction> failures;
    
    // Response metadata
    private LocalDateTime processedAt;
    private int totalProcessed;
    private int totalFailed;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessingResults {
        private int likesProcessed;
        private int passesProcessed;
        private int superLikesProcessed;
        private int reportsProcessed;
        private int blocksProcessed;
        private int unmatchesProcessed;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewMutualMatch {
        private String matchId;
        private String userId;
        private String name;
        private int age;
        private String primaryPhotoUrl;
        private LocalDateTime matchedAt;
        private String conversationId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedAction {
        private String actionType;
        private String targetId;
        private String reason;
        private String errorCode;
    }
}