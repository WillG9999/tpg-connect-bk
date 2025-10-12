package com.tpg.connect.model.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import com.tpg.connect.model.user.ApplicationStatus;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSubmission {
    private String connectId; // Primary key - user's ConnectID
    private String email;
    
    // Basic Information
    private String firstName;
    private String lastName;
    private String dateOfBirth; // YYYY-MM-DD format
    private String gender;
    private String location;
    
    // Professional Information (for approval consideration)
    private String jobTitle;
    private String company;
    private String industry;
    private String education;
    
    // Contact Information
    private String linkedinProfile;
    
    // Application Content
    private String bio;
    private List<String> interests;
    private String whyJoinReason;
    private String applicationNotes; // Additional information provided by applicant
    
    // Photos (URLs to Firebase Storage in applications/{connectId}/ folder)
    private List<String> photoUrls;
    private List<PhotoMetadata> photoMetadata;
    
    // Application Status
    private ApplicationStatus status;
    private String reviewNotes; // Admin notes
    private String rejectionReason;
    
    // Timestamps
    private Timestamp submittedAt;
    private Timestamp reviewedAt;
    private Timestamp approvedAt;
    private Timestamp rejectedAt;
    
    // Admin who reviewed
    private String reviewedBy;
    
    // Application metrics for analytics
    private Integer qualityScore; // 1-10 rating
    private List<String> tags; // e.g., "verified_professional", "high_quality_photos"
    
    public boolean isPending() {
        return status == ApplicationStatus.PENDING_APPROVAL;
    }
    
    public boolean isApproved() {
        return status == ApplicationStatus.APPROVED;
    }
    
    public boolean isRejected() {
        return status == ApplicationStatus.REJECTED;
    }
    
    public boolean needsReview() {
        return status == ApplicationStatus.PENDING_APPROVAL && reviewedAt == null;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoMetadata {
        private String filename;
        private String storagePath;
        private Long fileSize;
        private Integer width;
        private Integer height;
        private String mimeType;
        private Timestamp uploadedAt;
        private Integer photoIndex; // 0-based index for ordering
    }
}