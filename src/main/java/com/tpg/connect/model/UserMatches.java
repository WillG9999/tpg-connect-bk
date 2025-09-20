package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMatches {
    private String connectId;
    private String userId;
    private List<Match> matches;
    private Integer totalMatches;
    private Integer activeMatches;
    private Integer newMatches;
    private Integer conversationsStarted;
    private Timestamp lastMatchAt;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Match {
        private String connectId;
        private String otherUserId;
        private String otherUserName;
        private String otherUserPhoto;
        private Timestamp matchedAt;
        private Timestamp myActionAt;
        private Timestamp theirActionAt;
        private String status;
        private Timestamp lastActivityAt;
        private String matchSource;
        private String matchSetId;
        private Boolean hasMessaged;
        private Timestamp lastMessageAt;
        private String lastMessageText;
        private Timestamp myLastRead;
        private Integer unreadCount;
        private Double compatibilityScore;
        private List<String> commonInterests;
        private Double distance;
        private String reportedBy;
        private Timestamp reportedAt;
        private String adminNotes;
    }
}