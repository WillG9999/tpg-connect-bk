package com.tpg.connect.repository;

import com.tpg.connect.model.ReportedUser;

import java.util.List;
import java.util.Optional;

public interface ReportedUserRepository {
    
    /**
     * Save or update a reported user document
     */
    void save(ReportedUser reportedUser);
    
    /**
     * Find a reported user by their ConnectId
     */
    Optional<ReportedUser> findByReportedUserId(String reportedUserId);
    
    /**
     * Get all reported users with pending admin review
     */
    List<ReportedUser> findByAdminReviewStatus(String status);
    
    /**
     * Get all reported users for admin dashboard
     */
    List<ReportedUser> findAll();
    
    /**
     * Delete a reported user document (admin action)
     */
    void delete(String reportedUserId);
    
    /**
     * Count total reports for a user
     */
    int countReportsForUser(String reportedUserId);
}