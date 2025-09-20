package com.tpg.connect.model.api;

import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.safety.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionsBatchRequest {
    // Like actions
    @Size(max = 50)
    private List<LikeAction> likes;
    
    // Pass actions
    @Size(max = 50)
    private List<PassAction> passes;
    
    // Super like actions
    @Size(max = 10)
    private List<SuperLikeAction> superLikes;
    
    // Report actions
    @Size(max = 5)
    private List<ReportAction> reports;
    
    // Block actions
    @Size(max = 10)
    private List<BlockAction> blocks;
    
    // Unmatch actions
    @Size(max = 10)
    private List<UnmatchAction> unmatches;
    
    // Batch metadata
    private String batchDate;
    private LocalDateTime timestamp;
    private String deviceId;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LikeAction {
        @NotNull
        private String targetUserId;
        private String batchDate;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassAction {
        @NotNull
        private String targetUserId;
        private String batchDate;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuperLikeAction {
        @NotNull
        private String targetUserId;
        private String batchDate;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportAction {
        @NotNull
        private String targetUserId;
        @NotNull
        private ReportReason reason;
        @Size(max = 500)
        private String details;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BlockAction {
        @NotNull
        private String targetUserId;
        private String reason;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnmatchAction {
        @NotNull
        private String conversationId;
        private String reason;
        private LocalDateTime timestamp;
    }
}