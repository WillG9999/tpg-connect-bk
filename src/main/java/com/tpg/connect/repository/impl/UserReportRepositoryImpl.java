package com.tpg.connect.repository.impl;

import com.tpg.connect.model.UserReport;
import com.tpg.connect.repository.UserReportRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserReportRepositoryImpl implements UserReportRepository {

    private static final String COLLECTION_NAME = "user_reports";
    
    @Autowired
    private Firestore firestore;

    @Override
    public List<UserReport> findByReportedUserIdAndReportedAtAfter(String reportedUserId, java.time.LocalDateTime cutoffDate) {
        try {
            // Convert LocalDateTime to Timestamp for Firestore
            com.google.cloud.Timestamp firestoreTimestamp = com.google.cloud.Timestamp.of(
                java.util.Date.from(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant())
            );
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("reportedUserId", reportedUserId)
                    .whereGreaterThan("reportedAt", firestoreTimestamp)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by reported user and date", e);
        }
    }

    @Override
    public List<UserReport> findByReporterIdAndReportedUserIdAndReportedAtAfter(String reporterId, String reportedUserId, java.time.LocalDateTime cutoffDate) {
        try {
            // Convert LocalDateTime to Timestamp for Firestore
            com.google.cloud.Timestamp firestoreTimestamp = com.google.cloud.Timestamp.of(
                java.util.Date.from(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant())
            );
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("reporterId", reporterId)
                    .whereEqualTo("reportedUserId", reportedUserId)
                    .whereGreaterThan("reportedAt", firestoreTimestamp)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by reporter, reported user and date", e);
        }
    }

    @Override
    public UserReport createReport(UserReport userReport) {
        try {
            if (userReport.getReportedAt() == null) {
                userReport.setReportedAt(Timestamp.now());
            }
            if (userReport.getStatus() == null) {
                userReport.setStatus("PENDING");
            }
            if (userReport.getPriority() == null) {
                userReport.setPriority("MEDIUM");
            }
            if (userReport.getFollowUpRequired() == null) {
                userReport.setFollowUpRequired(false);
            }
            if (userReport.getEscalated() == null) {
                userReport.setEscalated(false);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userReport.getConnectId());
            docRef.set(convertToMap(userReport)).get();
            
            return userReport;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create report", e);
        }
    }

    @Override
    public Optional<UserReport> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToUserReport(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find report by connectId", e);
        }
    }

    @Override
    public List<UserReport> findByReporterId(String reporterId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("reporterId", reporterId)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by reporterId", e);
        }
    }

    @Override
    public List<UserReport> findByReportedUserId(String reportedUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("reportedUserId", reportedUserId)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by reportedUserId", e);
        }
    }

    @Override
    public boolean existsByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
            return doc.exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if report exists", e);
        }
    }

    @Override
    public UserReport updateReport(UserReport userReport) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userReport.getConnectId());
            docRef.set(convertToMap(userReport)).get();
            
            return userReport;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update report", e);
        }
    }

    @Override
    public UserReport updateStatus(String connectId, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Report not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update report status", e);
        }
    }

    @Override
    public UserReport assignForReview(String connectId, String reviewedBy, Timestamp reviewedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "UNDER_REVIEW");
            updates.put("reviewedBy", reviewedBy);
            updates.put("reviewedAt", reviewedAt);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Report not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to assign report for review", e);
        }
    }

    @Override
    public UserReport resolveReport(String connectId, String actionTaken, Timestamp resolvedAt, String adminNotes) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "RESOLVED");
            updates.put("actionTaken", actionTaken);
            updates.put("resolvedAt", resolvedAt);
            updates.put("adminNotes", adminNotes);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Report not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to resolve report", e);
        }
    }

    @Override
    public UserReport escalateReport(String connectId, String escalatedTo, Timestamp escalatedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("escalated", true);
            updates.put("escalatedTo", escalatedTo);
            updates.put("escalatedAt", escalatedAt);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Report not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to escalate report", e);
        }
    }

    @Override
    public UserReport updatePriority(String connectId, String priority) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("priority", priority);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Report not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update report priority", e);
        }
    }

    @Override
    public void deleteReport(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete report", e);
        }
    }

    @Override
    public List<UserReport> findPendingReports() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", "PENDING")
                    .orderBy("reportedAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find pending reports", e);
        }
    }

    @Override
    public List<UserReport> findReportsByStatus(String status) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", status)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by status", e);
        }
    }

    @Override
    public List<UserReport> findReportsByPriority(String priority) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("priority", priority)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by priority", e);
        }
    }

    @Override
    public List<UserReport> findReportsNeedingFollowUp() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("followUpRequired", true)
                    .orderBy("reportedAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports needing follow up", e);
        }
    }

    @Override
    public List<UserReport> findEscalatedReports() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("escalated", true)
                    .orderBy("escalatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find escalated reports", e);
        }
    }

    @Override
    public List<UserReport> findReportsByContext(String context) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("context", context)
                    .orderBy("reportedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserReport)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find reports by context", e);
        }
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(UserReport userReport) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", userReport.getConnectId());
        map.put("reporterId", userReport.getReporterId());
        map.put("reportedUserId", userReport.getReportedUserId());
        map.put("reasons", userReport.getReasons());
        map.put("description", userReport.getDescription());
        map.put("evidenceUrls", userReport.getEvidenceUrls());
        map.put("context", userReport.getContext());
        map.put("matchId", userReport.getMatchId());
        map.put("reportedAt", userReport.getReportedAt());
        map.put("reviewedAt", userReport.getReviewedAt());
        map.put("resolvedAt", userReport.getResolvedAt());
        map.put("status", userReport.getStatus());
        map.put("priority", userReport.getPriority());
        map.put("reviewedBy", userReport.getReviewedBy());
        map.put("adminNotes", userReport.getAdminNotes());
        map.put("actionTaken", userReport.getActionTaken());
        map.put("followUpRequired", userReport.getFollowUpRequired());
        map.put("escalated", userReport.getEscalated());
        map.put("escalatedTo", userReport.getEscalatedTo());
        map.put("escalatedAt", userReport.getEscalatedAt());
        return map;
    }

    private UserReport convertToUserReport(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return UserReport.builder()
                .connectId(doc.getId())
                .reporterId((String) data.get("reporterId"))
                .reportedUserId((String) data.get("reportedUserId"))
                .reasons((List<String>) data.get("reasons"))
                .description((String) data.get("description"))
                .evidenceUrls((List<String>) data.get("evidenceUrls"))
                .context((String) data.get("context"))
                .matchId((String) data.get("matchId"))
                .reportedAt((Timestamp) data.get("reportedAt"))
                .reviewedAt((Timestamp) data.get("reviewedAt"))
                .resolvedAt((Timestamp) data.get("resolvedAt"))
                .status((String) data.get("status"))
                .priority((String) data.get("priority"))
                .reviewedBy((String) data.get("reviewedBy"))
                .adminNotes((String) data.get("adminNotes"))
                .actionTaken((String) data.get("actionTaken"))
                .followUpRequired((Boolean) data.get("followUpRequired"))
                .escalated((Boolean) data.get("escalated"))
                .escalatedTo((String) data.get("escalatedTo"))
                .escalatedAt((Timestamp) data.get("escalatedAt"))
                .build();
    }

    @Override
    public UserReport save(UserReport userReport) {
        return updateReport(userReport);
    }
}