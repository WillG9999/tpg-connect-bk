package com.tpg.connect.services;

import com.tpg.connect.model.application.ApplicationSubmission;
import com.tpg.connect.model.User;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.UserStatus;
import com.tpg.connect.repository.ApplicationSubmissionRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.util.ConnectIdGenerator;
import com.google.cloud.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApplicationService {
    
    @Autowired
    private ApplicationSubmissionRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    /**
     * Submit a new application
     */
    public ApplicationSubmission submitApplication(ApplicationSubmission application) {
        log.info("📝 Processing application submission for: {} {} (ConnectID: {})", 
                application.getFirstName(), application.getLastName(), application.getConnectId());
        
        try {
            // ConnectID is already set and used as primary key - no UUID generation needed
            
            // Set initial status and timestamp
            application.setStatus(ApplicationStatus.PENDING_APPROVAL);
            application.setSubmittedAt(Timestamp.now());
            
            // Save to repository using connectId as document ID
            ApplicationSubmission saved = applicationRepository.save(application);
            
            log.info("✅ Application submitted successfully for ConnectID: {}", saved.getConnectId());
            return saved;
            
        } catch (Exception e) {
            log.error("❌ Error submitting application: ", e);
            throw new RuntimeException("Failed to submit application", e);
        }
    }
    
    /**
     * Get application by ConnectID
     */
    public ApplicationSubmission getApplicationByConnectId(String connectId) {
        try {
            return applicationRepository.findByConnectId(connectId).orElse(null);
        } catch (Exception e) {
            log.error("❌ Error getting application by ConnectID {}: ", connectId, e);
            return null;
        }
    }
    
    /**
     * Get application by ConnectID (same as getApplicationByConnectId - kept for compatibility)
     */
    public ApplicationSubmission getApplicationById(String connectId) {
        return getApplicationByConnectId(connectId);
    }
    
    /**
     * Update an existing application (only for pending applications)
     */
    public ApplicationSubmission updateApplication(String connectId, ApplicationSubmission updatedApplication) {
        log.info("📝 Updating application for ConnectID: {}", connectId);
        
        try {
            ApplicationSubmission existing = applicationRepository.findByConnectId(connectId).orElse(null);
            if (existing == null) {
                throw new IllegalArgumentException("Application not found for ConnectID: " + connectId);
            }
            
            if (existing.getStatus() != ApplicationStatus.PENDING_APPROVAL) {
                throw new IllegalStateException("Cannot update application after review");
            }
            
            // Update fields (preserve system fields) - connectId stays the same as primary key
            updatedApplication.setConnectId(connectId);
            updatedApplication.setStatus(existing.getStatus());
            updatedApplication.setSubmittedAt(existing.getSubmittedAt());
            updatedApplication.setReviewedAt(existing.getReviewedAt());
            updatedApplication.setReviewedBy(existing.getReviewedBy());
            
            ApplicationSubmission updated = applicationRepository.save(updatedApplication);
            
            log.info("✅ Application updated successfully for ConnectID: {}", connectId);
            return updated;
            
        } catch (Exception e) {
            log.error("❌ Error updating application {}: ", connectId, e);
            throw new RuntimeException("Failed to update application", e);
        }
    }
    
    /**
     * Get all pending applications for admin review
     */
    public List<ApplicationSubmission> getPendingApplications() {
        try {
            return applicationRepository.findByStatus(ApplicationStatus.PENDING_APPROVAL);
        } catch (Exception e) {
            log.error("❌ Error getting pending applications: ", e);
            throw new RuntimeException("Failed to get pending applications", e);
        }
    }

    /**
     * Get applications by status for admin filtering
     */
    public List<ApplicationSubmission> getApplicationsByStatus(ApplicationStatus status) {
        try {
            return applicationRepository.findByStatus(status);
        } catch (Exception e) {
            log.error("❌ Error getting applications by status {}: ", status, e);
            throw new RuntimeException("Failed to get applications by status", e);
        }
    }

    /**
     * Get all applications for admin review (all statuses)
     */
    public List<ApplicationSubmission> getAllApplications() {
        try {
            // Get applications from all statuses combined
            List<ApplicationSubmission> allApps = new ArrayList<>();
            for (ApplicationStatus status : ApplicationStatus.values()) {
                allApps.addAll(applicationRepository.findByStatus(status));
            }
            return allApps;
        } catch (Exception e) {
            log.error("❌ Error getting all applications: ", e);
            throw new RuntimeException("Failed to get all applications", e);
        }
    }
    
    /**
     * Approve an application
     */
    public ApplicationSubmission approveApplication(String applicationId, String reviewedBy, String notes) {
        log.info("✅ Approving application: {}", applicationId);
        
        try {
            ApplicationSubmission application = applicationRepository.findById(applicationId).orElse(null);
            if (application == null) {
                throw new IllegalArgumentException("Application not found: " + applicationId);
            }
            
            if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL && application.getStatus() != ApplicationStatus.REJECTED) {
                throw new IllegalStateException("Application cannot be approved from current status: " + application.getStatus());
            }
            
            // Update status and review information
            application.setStatus(ApplicationStatus.APPROVED);
            application.setReviewedAt(Timestamp.now());
            application.setApprovedAt(Timestamp.now());
            application.setReviewedBy(reviewedBy);
            application.setReviewNotes(notes);
            
            ApplicationSubmission updated = applicationRepository.save(application);
            
            // TODO: When payment processing is implemented, this should set UserStatus based on payment completion.
            // For now, directly set to ACTIVE since we're not using payment processing yet.
            updateUserStatusAfterApproval(application.getConnectId());
            
            // Send approval notification email to the applicant
            try {
                emailService.sendApplicationApprovalEmail(application.getEmail(), application.getFirstName());
                log.info("📧 Application approval email sent to: {} ({})", application.getEmail(), application.getFirstName());
            } catch (Exception emailEx) {
                log.error("⚠️ Failed to send approval email to {} ({}): {}", application.getEmail(), application.getFirstName(), emailEx.getMessage());
                // Don't fail the approval process if email sending fails
            }
            
            log.info("✅ Application approved successfully: {}", applicationId);
            return updated;
            
        } catch (Exception e) {
            log.error("❌ Error approving application {}: ", applicationId, e);
            throw new RuntimeException("Failed to approve application", e);
        }
    }
    
    /**
     * Reject an application
     */
    public ApplicationSubmission rejectApplication(String applicationId, String reviewedBy, String rejectionReason, String notes) {
        log.info("❌ Rejecting application: {}", applicationId);
        
        try {
            ApplicationSubmission application = applicationRepository.findById(applicationId).orElse(null);
            if (application == null) {
                throw new IllegalArgumentException("Application not found: " + applicationId);
            }
            
            if (application.getStatus() != ApplicationStatus.PENDING_APPROVAL && application.getStatus() != ApplicationStatus.APPROVED) {
                throw new IllegalStateException("Application cannot be rejected from current status: " + application.getStatus());
            }
            
            // Update status and review information
            application.setStatus(ApplicationStatus.REJECTED);
            application.setReviewedAt(Timestamp.now());
            application.setRejectedAt(Timestamp.now());
            application.setReviewedBy(reviewedBy);
            application.setRejectionReason(rejectionReason);
            application.setReviewNotes(notes);
            
            ApplicationSubmission updated = applicationRepository.save(application);
            
            log.info("❌ Application rejected: {}", applicationId);
            return updated;
            
        } catch (Exception e) {
            log.error("❌ Error rejecting application {}: ", applicationId, e);
            throw new RuntimeException("Failed to reject application", e);
        }
    }
    
    /**
     * Get application statistics
     */
    public ApplicationStats getApplicationStats() {
        try {
            // Use the repository's built-in stats method
            ApplicationSubmissionRepository.ApplicationStats repoStats = applicationRepository.getApplicationStats();
            
            return new ApplicationStats(
                repoStats.getTotalApplications(),
                repoStats.getPendingApplications(),
                repoStats.getApprovedApplications(),
                repoStats.getRejectedApplications()
            );
            
        } catch (Exception e) {
            log.error("❌ Error getting application statistics: ", e);
            throw new RuntimeException("Failed to get application statistics", e);
        }
    }
    
    /**
     * Clean up legacy boolean fields from all applications
     */
    public int cleanupLegacyFields() {
        try {
            log.info("🧹 Starting application legacy fields cleanup...");
            int cleaned = applicationRepository.cleanupLegacyFields();
            log.info("✅ Legacy fields cleanup completed. {} applications cleaned", cleaned);
            return cleaned;
        } catch (Exception e) {
            log.error("❌ Error cleaning up legacy fields: ", e);
            throw new RuntimeException("Failed to cleanup legacy fields", e);
        }
    }
    
    /**
     * Update user status to ACTIVE after application approval
     */
    private void updateUserStatusAfterApproval(String connectId) {
        try {
            log.info("🔄 Setting user status to ACTIVE for approved application: {}", connectId);
            
            User user = userRepository.findByConnectId(connectId).orElse(null);
            if (user == null) {
                log.warn("⚠️ User not found for connectId: {} - cannot set UserStatus", connectId);
                return;
            }
            
            // Set user status to ACTIVE and application status to APPROVED
            user.setUserStatus(UserStatus.ACTIVE);
            user.setApplicationStatus(ApplicationStatus.APPROVED);
            user.setActive(true);
            
            userRepository.save(user);
            
            log.info("✅ User {} status set to ACTIVE after application approval", connectId);
            
        } catch (Exception e) {
            log.error("❌ Error updating user status for {}: ", connectId, e);
            // Don't throw exception here - application approval should still succeed
            // even if user status update fails
        }
    }
    
    // Helper class for statistics
    public static class ApplicationStats {
        private final long total;
        private final long pending;
        private final long approved;
        private final long rejected;
        
        public ApplicationStats(long total, long pending, long approved, long rejected) {
            this.total = total;
            this.pending = pending;
            this.approved = approved;
            this.rejected = rejected;
        }
        
        public long getTotal() { return total; }
        public long getPending() { return pending; }
        public long getApproved() { return approved; }
        public long getRejected() { return rejected; }
        public double getApprovalRate() { 
            return total > 0 ? (double) approved / total * 100 : 0;
        }
    }
}