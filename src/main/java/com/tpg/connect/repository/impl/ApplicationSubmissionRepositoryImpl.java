package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.application.ApplicationSubmission;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.repository.ApplicationSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Repository
public class ApplicationSubmissionRepositoryImpl implements ApplicationSubmissionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationSubmissionRepositoryImpl.class);
    private static final String COLLECTION_NAME = "applicationSubmissions";
    
    @Autowired
    private Firestore firestore;
    
    
    @Override
    public ApplicationSubmission save(ApplicationSubmission application) {
        try {
            if (application.getSubmittedAt() == null) {
                application.setSubmittedAt(Timestamp.now());
            }
            
            // Use connectId as document ID
            DocumentReference docRef = firestore.collection(COLLECTION_NAME)
                    .document(application.getConnectId());
            
            docRef.set(application).get();
            logger.info("Application saved successfully for ConnectID: {}", application.getConnectId());
            
            return application;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error saving application: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save application", e);
        }
    }
    
    @Override
    public Optional<ApplicationSubmission> findById(String connectId) {
        try {
            logger.info("🔍 Looking for application with document ID: {}", connectId);
            DocumentSnapshot document = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
            
            if (document.exists()) {
                ApplicationSubmission application = document.toObject(ApplicationSubmission.class);
                logger.info("✅ Found application with status: {} for connectId: {}", application.getStatus(), connectId);
                return Optional.ofNullable(application);
            } else {
                logger.warn("❌ No document found with ID: {}, trying query by connectId field", connectId);
                // Fallback: try to find by connectId field instead of document ID
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereEqualTo("connectId", connectId)
                        .limit(1)
                        .get()
                        .get();
                
                if (!querySnapshot.isEmpty()) {
                    ApplicationSubmission application = querySnapshot.getDocuments().get(0).toObject(ApplicationSubmission.class);
                    logger.info("✅ Found application via query with status: {} for connectId: {}", application.getStatus(), connectId);
                    return Optional.ofNullable(application);
                }
            }
            
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding application by ConnectID {}: {}", connectId, e.getMessage(), e);
            throw new RuntimeException("Failed to find application", e);
        }
    }
    
    @Override
    public Optional<ApplicationSubmission> findByConnectId(String connectId) {
        // Since connectId is now the document ID, just use findById
        return findById(connectId);
    }
    
    @Override
    public Optional<ApplicationSubmission> findByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();
            
            if (!querySnapshot.isEmpty()) {
                ApplicationSubmission application = querySnapshot.getDocuments().get(0).toObject(ApplicationSubmission.class);
                return Optional.ofNullable(application);
            }
            
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding application by email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to find application", e);
        }
    }
    
    @Override
    public List<ApplicationSubmission> findByStatus(ApplicationStatus status) {
        try {
            // Remove orderBy to avoid composite index requirement for now
            // TODO: Create Firestore composite index for status + submittedAt ordering
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", status.toString())
                    .get()
                    .get();
            
            List<ApplicationSubmission> applications = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                ApplicationSubmission application = document.toObject(ApplicationSubmission.class);
                if (application != null) {
                    applications.add(application);
                }
            }
            
            // Sort by action date (reviewedAt) if available, otherwise submittedAt - most recent first
            applications.sort((a, b) -> {
                Timestamp aDate = a.getReviewedAt() != null ? a.getReviewedAt() : a.getSubmittedAt();
                Timestamp bDate = b.getReviewedAt() != null ? b.getReviewedAt() : b.getSubmittedAt();
                
                if (aDate == null && bDate == null) return 0;
                if (aDate == null) return 1;
                if (bDate == null) return -1;
                return bDate.compareTo(aDate); // Most recent first
            });
            
            return applications;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding applications by status {}: {}", status, e.getMessage(), e);
            throw new RuntimeException("Failed to find applications", e);
        }
    }
    
    @Override
    public List<ApplicationSubmission> findPendingApplications() {
        return findByStatus(ApplicationStatus.PENDING_APPROVAL);
    }
    
    @Override
    public List<ApplicationSubmission> findRecentApplications(int days) {
        try {
            Timestamp cutoffTime = Timestamp.of(java.util.Date.from(
                    LocalDateTime.now().minusDays(days).atZone(ZoneId.systemDefault()).toInstant()
            ));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereGreaterThanOrEqualTo("submittedAt", cutoffTime)
                    .orderBy("submittedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            List<ApplicationSubmission> applications = new ArrayList<>();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                ApplicationSubmission application = document.toObject(ApplicationSubmission.class);
                if (application != null) {
                    applications.add(application);
                }
            }
            
            return applications;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error finding recent applications: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find recent applications", e);
        }
    }
    
    @Override
    public ApplicationSubmission updateStatus(String connectId, ApplicationStatus status, String reviewedBy) {
        return updateReview(connectId, status, null, reviewedBy);
    }
    
    @Override
    public ApplicationSubmission updateReview(String connectId, ApplicationStatus status, String reviewNotes, String reviewedBy) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            // Build update data
            WriteBatch batch = firestore.batch();
            batch.update(docRef, "status", status.toString());
            batch.update(docRef, "reviewedBy", reviewedBy);
            batch.update(docRef, "reviewedAt", Timestamp.now());
            
            if (reviewNotes != null) {
                batch.update(docRef, "reviewNotes", reviewNotes);
            }
            
            if (status == ApplicationStatus.APPROVED) {
                batch.update(docRef, "approvedAt", Timestamp.now());
            } else if (status == ApplicationStatus.REJECTED) {
                batch.update(docRef, "rejectedAt", Timestamp.now());
                if (reviewNotes != null) {
                    batch.update(docRef, "rejectionReason", reviewNotes);
                }
            }
            
            batch.commit().get();
            
            // Fetch and return updated document
            return findById(connectId).orElse(null);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error updating application review {}: {}", connectId, e.getMessage(), e);
            throw new RuntimeException("Failed to update application review", e);
        }
    }
    
    @Override
    public boolean deleteById(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .delete()
                    .get();
            
            logger.info("Application deleted successfully for ConnectID: {}", connectId);
            return true;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error deleting application {}: {}", connectId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean existsByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();
            
            return !querySnapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking if application exists by email {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to check application existence", e);
        }
    }
    
    @Override
    public boolean existsByConnectId(String connectId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("connectId", connectId)
                    .limit(1)
                    .get()
                    .get();
            
            return !querySnapshot.isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error checking if application exists by connectId {}: {}", connectId, e.getMessage(), e);
            throw new RuntimeException("Failed to check application existence", e);
        }
    }
    
    @Override
    public ApplicationStats getApplicationStats() {
        try {
            // Get all applications
            QuerySnapshot allApps = firestore.collection(COLLECTION_NAME).get().get();
            long total = allApps.size();
            
            // Count by status
            long pending = 0, approved = 0, rejected = 0;
            
            for (DocumentSnapshot doc : allApps.getDocuments()) {
                String status = doc.getString("status");
                if (status != null) {
                    switch (ApplicationStatus.valueOf(status)) {
                        case PENDING_APPROVAL -> pending++;
                        case APPROVED -> approved++;
                        case REJECTED -> rejected++;
                        // ACTIVE and SUSPENDED are not counted as they're user statuses, not application statuses
                    }
                }
            }
            
            return new ApplicationStats(total, pending, approved, rejected);
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error getting application stats: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get application statistics", e);
        }
    }
    
    @Override
    public int cleanupLegacyFields() {
        try {
            logger.info("🧹 Starting cleanup of legacy boolean fields...");
            
            // Get all applications
            QuerySnapshot allApps = firestore.collection(COLLECTION_NAME).get().get();
            int cleanedCount = 0;
            
            WriteBatch batch = firestore.batch();
            int batchCount = 0;
            
            for (DocumentSnapshot doc : allApps.getDocuments()) {
                Map<String, Object> data = doc.getData();
                if (data != null) {
                    boolean needsCleanup = false;
                    
                    // Check if document has legacy boolean fields (including null values)
                    if (data.containsKey("pending") || data.containsKey("approved") || data.containsKey("rejected")) {
                        needsCleanup = true;
                        logger.info("🧹 Cleaning legacy fields from application: {}", doc.getId());
                        
                        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(doc.getId());
                        
                        // Remove legacy boolean fields - handle each field individually
                        if (data.containsKey("pending")) {
                            batch.update(docRef, FieldPath.of("pending"), FieldValue.delete());
                            logger.info("🗑️ Removing 'pending' field from {}", doc.getId());
                        }
                        if (data.containsKey("approved")) {
                            batch.update(docRef, FieldPath.of("approved"), FieldValue.delete());
                            logger.info("🗑️ Removing 'approved' field from {}", doc.getId());
                        }
                        if (data.containsKey("rejected")) {
                            batch.update(docRef, FieldPath.of("rejected"), FieldValue.delete());
                            logger.info("🗑️ Removing 'rejected' field from {}", doc.getId());
                        }
                        
                        cleanedCount++;
                        batchCount++;
                        
                        // Firestore batch limit is 500 operations
                        if (batchCount >= 100) {
                            batch.commit().get();
                            batch = firestore.batch();
                            batchCount = 0;
                        }
                    }
                }
            }
            
            // Commit remaining batch
            if (batchCount > 0) {
                batch.commit().get();
            }
            
            logger.info("✅ Cleanup completed. Cleaned {} applications", cleanedCount);
            return cleanedCount;
            
        } catch (InterruptedException | ExecutionException e) {
            logger.error("❌ Error during legacy fields cleanup: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cleanup legacy fields", e);
        }
    }
}