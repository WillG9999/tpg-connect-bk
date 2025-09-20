package com.tpg.connect.repository;

import com.tpg.connect.model.UserActivity;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserActivityRepository {
    
    // Create Operations
    UserActivity createUserActivity(UserActivity userActivity);
    
    // Read Operations
    Optional<UserActivity> findByConnectId(String connectId);
    boolean existsByConnectId(String connectId);
    
    // Action Operations
    UserActivity addAction(String connectId, UserActivity.Action action);
    UserActivity updateActionCounts(String connectId, Integer totalActions, Integer totalLikes, Integer totalPasses, Integer totalDislikes, Integer totalMatches);
    UserActivity updateMatchStats(String connectId, Double matchSuccessRate, Integer avgActionsPerDay);
    UserActivity updateStreakInfo(String connectId, Integer currentStreak, Integer longestStreak);
    UserActivity updateLastAction(String connectId, Timestamp lastActionAt, Integer actionsToday);
    
    // Daily Summary Operations
    UserActivity updateDailySummary(String connectId, String date, UserActivity.DailySummary summary);
    UserActivity incrementDailyAction(String connectId, String date, String actionType);
    UserActivity addDailyViewTime(String connectId, String date, Integer viewTimeSeconds);
    UserActivity incrementBatchesCompleted(String connectId, String date);
    
    // Analytics Operations  
    List<UserActivity.Action> getActionsForDateRange(String connectId, Timestamp startDate, Timestamp endDate);
    Map<String, UserActivity.DailySummary> getDailySummariesForMonth(String connectId, String yearMonth);
    UserActivity calculateAndUpdateStats(String connectId);
    
    // Delete Operations
    void deleteUserActivity(String connectId);
    
    // Batch Operations
    List<UserActivity> findUserActivitiesByConnectIds(List<String> connectIds);
    Map<String, UserActivity> findUserActivityMapByConnectIds(List<String> connectIds);
}