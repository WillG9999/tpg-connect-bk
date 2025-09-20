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
public class MatchSet {
    private String connectId;
    private String userId;
    private String date;
    private String status;
    private Timestamp createdAt;
    private Timestamp completedAt;
    private List<PotentialMatch> potentialMatches;
    private Integer totalMatches;
    private Integer actionsSubmitted;
    private Integer matchesFound;
    private String algorithmVersion;
    private Filters filters;
    private Integer viewTime;
    private Integer avgTimePerProfile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PotentialMatch {
        private String connectId;
        private String name;
        private Integer age;
        private List<Photo> photos;
        private String location;
        private List<String> interests;
        private Profile profile;
        private Double distance;
        private Double compatibilityScore;
        private List<String> commonInterests;
        private String algorithmReason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Photo {
        private String url;
        private Boolean isPrimary;
        private List<PhotoPrompt> prompts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoPrompt {
        private String text;
        private Position position;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private Double x;
        private Double y;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String jobTitle;
        private String datingIntentions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filters {
        private List<Integer> ageRange;
        private Integer maxDistance;
        private String preferredGender;
        private Map<String, Object> otherPreferences;
    }
}