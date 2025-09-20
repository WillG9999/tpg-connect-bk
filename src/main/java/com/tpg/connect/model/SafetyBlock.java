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
public class SafetyBlock {
    private String connectId;
    private String userId;
    private String blockType;
    private String severity;
    private String reason;
    private Timestamp blockedAt;
    private Timestamp expiresAt;
    private Integer duration;
    private String status;
    private Boolean isActive;
    private String createdBy;
    private String reviewedBy;
    private String liftedBy;
    private List<String> reportIds;
    private List<String> evidenceUrls;
    private String context;
    private List<String> restrictedFeatures;
    private String warningMessage;
    private Boolean appealSubmitted;
    private Timestamp appealedAt;
    private String appealReason;
    private String appealReviewedBy;
    private String appealDecision;
    private Timestamp appealReviewedAt;
    private Boolean triggeredBySystem;
    private String triggerRule;
    private Boolean humanReviewRequired;
    private String adminNotes;
    private String publicReason;
}