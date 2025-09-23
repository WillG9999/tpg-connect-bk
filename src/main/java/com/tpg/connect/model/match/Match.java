package com.tpg.connect.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private String id;
    private String user1Id;
    private String user2Id;
    private LocalDateTime matchedAt;
    private MatchStatus status;
    private String conversationId;
    private LocalDateTime lastActivityAt;
    
    public enum MatchStatus {
        ACTIVE,
        UNMATCHED,
        BLOCKED_BY_USER1,
        BLOCKED_BY_USER2,
        REPORTED
    }
}