package com.tpg.connect.model.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppConfig {
    private FeatureFlags features;
    private AppLimits limits;
    private MatchesConfig matchesConfig;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureFlags {
        private boolean potentialMatchesEnabled;
        private boolean conversationsEnabled;
        private boolean photoUploadEnabled;
        private boolean superLikeEnabled;
        private boolean boostEnabled;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppLimits {
        private int maxPhotos;
        private int maxBioLength;
        private int maxMessageLength;
        private int maxInterests;
        private int maxReportsPerDay;
        private int maxMessagesPerMinute;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchesConfig {
        private String releaseTime;
        private int matchSetSize;
        private String timezone;
        private int maxDailyMatchSets;
    }
}