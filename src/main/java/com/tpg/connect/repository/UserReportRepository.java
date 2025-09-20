package com.tpg.connect.repository;

import com.tpg.connect.model.UserReport;
import com.google.cloud.Timestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserReportRepository {
    
    // Create Operations
    UserReport createReport(UserReport userReport);
    UserReport save(UserReport userReport);
    
    // Read Operations
    Optional<UserReport> findByConnectId(String connectId);
    List<UserReport> findByReporterId(String reporterId);
    List<UserReport> findByReportedUserId(String reportedUserId);
    List<UserReport> findByReportedUserIdAndReportedAtAfter(String reportedUserId, LocalDateTime cutoffDate);
    List<UserReport> findByReporterIdAndReportedUserIdAndReportedAtAfter(String reporterId, String reportedUserId, LocalDateTime cutoffDate);
    boolean existsByConnectId(String connectId);
    
    // Update Operations
    UserReport updateReport(UserReport userReport);
    UserReport updateStatus(String connectId, String status);
    UserReport assignForReview(String connectId, String reviewedBy, Timestamp reviewedAt);
    UserReport resolveReport(String connectId, String actionTaken, Timestamp resolvedAt, String adminNotes);
    UserReport escalateReport(String connectId, String escalatedTo, Timestamp escalatedAt);
    UserReport updatePriority(String connectId, String priority);
    
    // Delete Operations
    void deleteReport(String connectId);
    
    // Query Operations
    List<UserReport> findPendingReports();
    List<UserReport> findReportsByStatus(String status);
    List<UserReport> findReportsByPriority(String priority);
    List<UserReport> findReportsNeedingFollowUp();
    List<UserReport> findEscalatedReports();
    List<UserReport> findReportsByContext(String context);
}