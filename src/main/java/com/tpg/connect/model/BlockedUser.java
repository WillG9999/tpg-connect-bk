package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockedUser {
    private String connectId;
    private String userId;
    private String blockedUserId;
    private String reason;
    private Timestamp blockedAt;
    private String status;
    private String source;
    private String matchId;
    private String blockType;
    private String severity;
    private String reviewedBy;
    private Timestamp reviewedAt;
    private String adminNotes;
    private Timestamp unblockedAt;
    private String unblockReason;
    private String unblockedBy;
}