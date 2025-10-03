package com.tpg.connect.repository.impl;

import com.tpg.connect.model.ReportedUser;
import com.tpg.connect.repository.ReportedUserRepository;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class ReportedUserRepositoryImpl implements ReportedUserRepository {

    private static final String COLLECTION_NAME = "ReportedUser";
    
    @Autowired
    private Firestore firestore;

    @Override
    public void save(ReportedUser reportedUser) {
        try {
            // Use reportedUserId as document ID for aggregation
            DocumentReference docRef = firestore.collection(COLLECTION_NAME)
                    .document(reportedUser.getReportedUserId());
            
            // Convert to Map for Firestore
            Map<String, Object> data = new HashMap<>();
            data.put("reportedUserId", reportedUser.getReportedUserId());
            data.put("reportedUserInfo", reportedUser.getReportedUserInfo());
            data.put("totalReports", reportedUser.getTotalReports());
            data.put("reports", reportedUser.getReports());
            data.put("adminReview", reportedUser.getAdminReview());
            data.put("createdAt", reportedUser.getCreatedAt());
            data.put("updatedAt", reportedUser.getUpdatedAt());
            
            docRef.set(data).get();
            System.out.println("✅ ReportedUserRepository: Saved reported user: " + reportedUser.getReportedUserId());
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ReportedUserRepository: Error saving reported user - " + e.getMessage());
            throw new RuntimeException("Failed to save reported user", e);
        }
    }

    @Override
    public Optional<ReportedUser> findByReportedUserId(String reportedUserId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(reportedUserId);
            DocumentSnapshot document = docRef.get().get();
            
            if (document.exists()) {
                ReportedUser reportedUser = documentToReportedUser(document);
                return Optional.of(reportedUser);
            }
            
            return Optional.empty();
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ReportedUserRepository: Error finding reported user - " + e.getMessage());
            throw new RuntimeException("Failed to find reported user", e);
        }
    }

    @Override
    public List<ReportedUser> findByAdminReviewStatus(String status) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("adminReview.status", status)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::documentToReportedUser)
                    .collect(Collectors.toList());
                    
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ReportedUserRepository: Error finding by admin status - " + e.getMessage());
            throw new RuntimeException("Failed to find reported users by admin status", e);
        }
    }

    @Override
    public List<ReportedUser> findAll() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::documentToReportedUser)
                    .collect(Collectors.toList());
                    
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ReportedUserRepository: Error finding all reported users - " + e.getMessage());
            throw new RuntimeException("Failed to find all reported users", e);
        }
    }

    @Override
    public void delete(String reportedUserId) {
        try {
            firestore.collection(COLLECTION_NAME).document(reportedUserId).delete().get();
            System.out.println("✅ ReportedUserRepository: Deleted reported user: " + reportedUserId);
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ReportedUserRepository: Error deleting reported user - " + e.getMessage());
            throw new RuntimeException("Failed to delete reported user", e);
        }
    }

    @Override
    public int countReportsForUser(String reportedUserId) {
        Optional<ReportedUser> reportedUser = findByReportedUserId(reportedUserId);
        return reportedUser.map(ReportedUser::getTotalReports).orElse(0);
    }

    private ReportedUser documentToReportedUser(DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        if (data == null) return null;

        ReportedUser.ReportedUserBuilder builder = ReportedUser.builder()
                .reportedUserId(document.getString("reportedUserId"))
                .reportedUserInfo(document.getString("reportedUserInfo"))
                .totalReports(document.getLong("totalReports") != null ? document.getLong("totalReports").intValue() : 0)
                .createdAt(document.getTimestamp("createdAt"))
                .updatedAt(document.getTimestamp("updatedAt"));

        // Convert reports list
        List<Map<String, Object>> reportsData = (List<Map<String, Object>>) data.get("reports");
        if (reportsData != null) {
            List<ReportedUser.IndividualReport> reports = reportsData.stream()
                    .map(this::mapToIndividualReport)
                    .collect(Collectors.toList());
            builder.reports(reports);
        }

        // Convert admin review
        Map<String, Object> adminReviewData = (Map<String, Object>) data.get("adminReview");
        if (adminReviewData != null) {
            ReportedUser.AdminReview adminReview = ReportedUser.AdminReview.builder()
                    .status((String) adminReviewData.get("status"))
                    .reviewedBy((String) adminReviewData.get("reviewedBy"))
                    .reviewedAt((com.google.cloud.Timestamp) adminReviewData.get("reviewedAt"))
                    .adminNotes((String) adminReviewData.get("adminNotes"))
                    .actionTaken((String) adminReviewData.get("actionTaken"))
                    .build();
            builder.adminReview(adminReview);
        }

        return builder.build();
    }

    private ReportedUser.IndividualReport mapToIndividualReport(Map<String, Object> reportData) {
        return ReportedUser.IndividualReport.builder()
                .reportId((String) reportData.get("reportId"))
                .reporterId((String) reportData.get("reporterId"))
                .reason((String) reportData.get("reason"))
                .location((String) reportData.get("location"))
                .description((String) reportData.get("description"))
                .reportedAt((com.google.cloud.Timestamp) reportData.get("reportedAt"))
                .status((String) reportData.get("status"))
                .build();
    }
}