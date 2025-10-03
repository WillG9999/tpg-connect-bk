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
public class ReportedUser {
    private String reportedUserId;
    private String reportedUserInfo; // from frontend _nameController
    private int totalReports;
    private List<IndividualReport> reports;
    private AdminReview adminReview;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualReport {
        private String reportId;
        private String reporterId;
        private String reason; // from frontend _selectedReason dropdown
        private String location; // from frontend _selectedLocation dropdown  
        private String description; // from frontend _descriptionController
        private Timestamp reportedAt;
        private String status; // "PENDING", "UNDER_REVIEW", "RESOLVED"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminReview {
        private String status; // "PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"
        private String reviewedBy;
        private Timestamp reviewedAt;
        private String adminNotes;
        private String actionTaken;
    }
}