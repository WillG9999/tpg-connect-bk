package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActivity {
    private String connectId;
    private String userId;
    private List<Action> actions;
    private Map<String, DailySummary> dailySummary;
    private Integer totalActions;
    private Integer totalLikes;
    private Integer totalPasses;
    private Integer totalDislikes;
    private Integer totalMatches;
    private Double matchSuccessRate;
    private Integer avgActionsPerDay;
    private Timestamp lastActionAt;
    private Integer actionsToday;
    private Integer currentStreak;
    private Integer longestStreak;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Action {
        private String connectId;
        private String targetUserId;
        private String targetUserName;
        private String action;
        private Timestamp timestamp;
        private String source;
        private String matchSetId;
        private String batchDate;
        private Boolean resultedInMatch;
        private String matchId;
        private Integer targetUserAge;
        private String targetUserLocation;
        private Double distance;
        private Double compatibilityScore;
        private String deviceType;
        private String appVersion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummary {
        private Integer totalActions;
        private Integer likes;
        private Integer passes;
        private Integer dislikes;
        private Integer matches;
        private Integer viewTime;
        private Integer batchesCompleted;
    }
}