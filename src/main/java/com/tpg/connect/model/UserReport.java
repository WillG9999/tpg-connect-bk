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
public class UserReport {
    private String connectId;
    private String reporterId;
    private String reportedUserId;
    private List<String> reasons;
    private String description;
    private List<String> evidenceUrls;
    private String context;
    private String matchId;
    private Timestamp reportedAt;
    private Timestamp reviewedAt;
    private Timestamp resolvedAt;
    private String status;
    private String priority;
    private String reviewedBy;
    private String adminNotes;
    private String actionTaken;
    private Boolean followUpRequired;
    private Boolean escalated;
    private String escalatedTo;
    private Timestamp escalatedAt;
}