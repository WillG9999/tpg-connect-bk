package com.tpg.connect.repository;

import com.tpg.connect.model.application.ApplicationSubmission;
import com.tpg.connect.model.user.ApplicationStatus;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository {
    
    /**
     * Save or update an application submission
     */
    ApplicationSubmission save(ApplicationSubmission application);
    
    /**
     * Find application by ConnectID (now the primary key)
     */
    Optional<ApplicationSubmission> findById(String connectId);
    
    /**
     * Find application by user's connect ID
     */
    Optional<ApplicationSubmission> findByConnectId(String connectId);
    
    /**
     * Find application by email
     */
    Optional<ApplicationSubmission> findByEmail(String email);
    
    /**
     * Get all applications with a specific status
     */
    List<ApplicationSubmission> findByStatus(ApplicationStatus status);
    
    /**
     * Get all pending applications that need review
     */
    List<ApplicationSubmission> findPendingApplications();
    
    /**
     * Get all applications submitted in the last N days
     */
    List<ApplicationSubmission> findRecentApplications(int days);
    
    /**
     * Update application status
     */
    ApplicationSubmission updateStatus(String connectId, ApplicationStatus status, String reviewedBy);
    
    /**
     * Update application with review notes
     */
    ApplicationSubmission updateReview(String connectId, ApplicationStatus status, String reviewNotes, String reviewedBy);
    
    /**
     * Delete an application (soft delete)
     */
    boolean deleteById(String connectId);
    
    /**
     * Check if application exists for email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if application exists for connectId
     */
    boolean existsByConnectId(String connectId);
    
    /**
     * Get application statistics
     */
    ApplicationStats getApplicationStats();
    
    /**
     * Clean up legacy boolean fields from all applications
     */
    int cleanupLegacyFields();
    
    /**
     * Application statistics data class
     */
    class ApplicationStats {
        private final long totalApplications;
        private final long pendingApplications;
        private final long approvedApplications;
        private final long rejectedApplications;
        private final double approvalRate;
        
        public ApplicationStats(long total, long pending, long approved, long rejected) {
            this.totalApplications = total;
            this.pendingApplications = pending;
            this.approvedApplications = approved;
            this.rejectedApplications = rejected;
            this.approvalRate = total > 0 ? (double) approved / total * 100.0 : 0.0;
        }
        
        public long getTotalApplications() { return totalApplications; }
        public long getPendingApplications() { return pendingApplications; }
        public long getApprovedApplications() { return approvedApplications; }
        public long getRejectedApplications() { return rejectedApplications; }
        public double getApprovalRate() { return approvalRate; }
    }
}