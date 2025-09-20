package com.tpg.connect.repository.impl;

import com.tpg.connect.model.UserActivity;
import com.tpg.connect.repository.UserActivityRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserActivityRepositoryImpl implements UserActivityRepository {

    private static final String COLLECTION_NAME = "user_activity";
    
    @Autowired
    private Firestore firestore;

    @Override
    public UserActivity createUserActivity(UserActivity userActivity) {
        try {
            userActivity.setCreatedAt(Timestamp.now());
            userActivity.setUpdatedAt(Timestamp.now());
            userActivity.setVersion(1);
            
            if (userActivity.getActions() == null) {
                userActivity.setActions(new ArrayList<>());
            }
            if (userActivity.getDailySummary() == null) {
                userActivity.setDailySummary(new HashMap<>());
            }
            if (userActivity.getTotalActions() == null) {
                userActivity.setTotalActions(0);
            }
            if (userActivity.getTotalLikes() == null) {
                userActivity.setTotalLikes(0);
            }
            if (userActivity.getTotalPasses() == null) {
                userActivity.setTotalPasses(0);
            }
            if (userActivity.getTotalDislikes() == null) {
                userActivity.setTotalDislikes(0);
            }
            if (userActivity.getTotalMatches() == null) {
                userActivity.setTotalMatches(0);
            }
            if (userActivity.getMatchSuccessRate() == null) {
                userActivity.setMatchSuccessRate(0.0);
            }
            if (userActivity.getAvgActionsPerDay() == null) {
                userActivity.setAvgActionsPerDay(0);
            }
            if (userActivity.getActionsToday() == null) {
                userActivity.setActionsToday(0);
            }
            if (userActivity.getCurrentStreak() == null) {
                userActivity.setCurrentStreak(0);
            }
            if (userActivity.getLongestStreak() == null) {
                userActivity.setLongestStreak(0);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userActivity.getConnectId());
            docRef.set(convertToMap(userActivity)).get();
            
            return userActivity;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create user activity", e);
        }
    }

    @Override
    public Optional<UserActivity> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToUserActivity(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user activity by connectId", e);
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
            throw new RuntimeException("Failed to check if user activity exists", e);
        }
    }

    @Override
    public UserActivity addAction(String connectId, UserActivity.Action action) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("actions", FieldValue.arrayUnion(convertActionToMap(action)));
            updates.put("totalActions", FieldValue.increment(1));
            updates.put("lastActionAt", action.getTimestamp());
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            // Increment specific action type counter
            switch (action.getAction().toLowerCase()) {
                case "like":
                    updates.put("totalLikes", FieldValue.increment(1));
                    break;
                case "pass":
                    updates.put("totalPasses", FieldValue.increment(1));
                    break;
                case "dislike":
                    updates.put("totalDislikes", FieldValue.increment(1));
                    break;
            }
            
            if (action.getResultedInMatch() != null && action.getResultedInMatch()) {
                updates.put("totalMatches", FieldValue.increment(1));
            }
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add action", e);
        }
    }

    @Override
    public UserActivity updateActionCounts(String connectId, Integer totalActions, Integer totalLikes, Integer totalPasses, Integer totalDislikes, Integer totalMatches) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (totalActions != null) updates.put("totalActions", totalActions);
            if (totalLikes != null) updates.put("totalLikes", totalLikes);
            if (totalPasses != null) updates.put("totalPasses", totalPasses);
            if (totalDislikes != null) updates.put("totalDislikes", totalDislikes);
            if (totalMatches != null) updates.put("totalMatches", totalMatches);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update action counts", e);
        }
    }

    @Override
    public UserActivity updateMatchStats(String connectId, Double matchSuccessRate, Integer avgActionsPerDay) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (matchSuccessRate != null) updates.put("matchSuccessRate", matchSuccessRate);
            if (avgActionsPerDay != null) updates.put("avgActionsPerDay", avgActionsPerDay);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match stats", e);
        }
    }

    @Override
    public UserActivity updateStreakInfo(String connectId, Integer currentStreak, Integer longestStreak) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (currentStreak != null) updates.put("currentStreak", currentStreak);
            if (longestStreak != null) updates.put("longestStreak", longestStreak);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update streak info", e);
        }
    }

    @Override
    public UserActivity updateLastAction(String connectId, Timestamp lastActionAt, Integer actionsToday) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (lastActionAt != null) updates.put("lastActionAt", lastActionAt);
            if (actionsToday != null) updates.put("actionsToday", actionsToday);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last action", e);
        }
    }

    @Override
    public UserActivity updateDailySummary(String connectId, String date, UserActivity.DailySummary summary) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("dailySummary." + date, convertDailySummaryToMap(summary));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update daily summary", e);
        }
    }

    @Override
    public UserActivity incrementDailyAction(String connectId, String date, String actionType) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("dailySummary." + date + ".totalActions", FieldValue.increment(1));
            
            switch (actionType.toLowerCase()) {
                case "like":
                    updates.put("dailySummary." + date + ".likes", FieldValue.increment(1));
                    break;
                case "pass":
                    updates.put("dailySummary." + date + ".passes", FieldValue.increment(1));
                    break;
                case "dislike":
                    updates.put("dailySummary." + date + ".dislikes", FieldValue.increment(1));
                    break;
            }
            
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to increment daily action", e);
        }
    }

    @Override
    public UserActivity addDailyViewTime(String connectId, String date, Integer viewTimeSeconds) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("dailySummary." + date + ".viewTime", FieldValue.increment(viewTimeSeconds));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add daily view time", e);
        }
    }

    @Override
    public UserActivity incrementBatchesCompleted(String connectId, String date) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("dailySummary." + date + ".batchesCompleted", FieldValue.increment(1));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User activity not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to increment batches completed", e);
        }
    }

    @Override
    public List<UserActivity.Action> getActionsForDateRange(String connectId, Timestamp startDate, Timestamp endDate) {
        Optional<UserActivity> userActivityOpt = findByConnectId(connectId);
        if (!userActivityOpt.isPresent() || userActivityOpt.get().getActions() == null) {
            return new ArrayList<>();
        }
        
        return userActivityOpt.get().getActions().stream()
                .filter(action -> action.getTimestamp() != null &&
                        action.getTimestamp().compareTo(startDate) >= 0 &&
                        action.getTimestamp().compareTo(endDate) <= 0)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, UserActivity.DailySummary> getDailySummariesForMonth(String connectId, String yearMonth) {
        Optional<UserActivity> userActivityOpt = findByConnectId(connectId);
        if (!userActivityOpt.isPresent() || userActivityOpt.get().getDailySummary() == null) {
            return new HashMap<>();
        }
        
        return userActivityOpt.get().getDailySummary().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(yearMonth))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public UserActivity calculateAndUpdateStats(String connectId) {
        Optional<UserActivity> userActivityOpt = findByConnectId(connectId);
        if (!userActivityOpt.isPresent()) {
            throw new RuntimeException("User activity not found");
        }
        
        UserActivity userActivity = userActivityOpt.get();
        
        // Calculate match success rate
        Double successRate = 0.0;
        if (userActivity.getTotalLikes() != null && userActivity.getTotalLikes() > 0) {
            Integer totalMatches = userActivity.getTotalMatches() != null ? userActivity.getTotalMatches() : 0;
            successRate = (totalMatches.doubleValue() / userActivity.getTotalLikes().doubleValue()) * 100;
        }
        
        // Calculate average actions per day
        Integer avgActions = 0;
        if (userActivity.getDailySummary() != null && !userActivity.getDailySummary().isEmpty()) {
            int totalDays = userActivity.getDailySummary().size();
            Integer totalActions = userActivity.getTotalActions() != null ? userActivity.getTotalActions() : 0;
            avgActions = totalActions / totalDays;
        }
        
        return updateMatchStats(connectId, successRate, avgActions);
    }

    @Override
    public void deleteUserActivity(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete user activity", e);
        }
    }

    @Override
    public List<UserActivity> findUserActivitiesByConnectIds(List<String> connectIds) {
        try {
            List<UserActivity> userActivities = new ArrayList<>();
            
            List<List<String>> batches = new ArrayList<>();
            for (int i = 0; i < connectIds.size(); i += 10) {
                batches.add(connectIds.subList(i, Math.min(i + 10, connectIds.size())));
            }
            
            for (List<String> batch : batches) {
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .get();
                        
                userActivities.addAll(querySnapshot.getDocuments().stream()
                        .map(this::convertToUserActivity)
                        .collect(Collectors.toList()));
            }
            
            return userActivities;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user activities by connectIds", e);
        }
    }

    @Override
    public Map<String, UserActivity> findUserActivityMapByConnectIds(List<String> connectIds) {
        List<UserActivity> userActivities = findUserActivitiesByConnectIds(connectIds);
        return userActivities.stream()
                .collect(Collectors.toMap(UserActivity::getConnectId, userActivity -> userActivity));
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(UserActivity userActivity) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", userActivity.getConnectId());
        map.put("userId", userActivity.getUserId());
        map.put("totalActions", userActivity.getTotalActions());
        map.put("totalLikes", userActivity.getTotalLikes());
        map.put("totalPasses", userActivity.getTotalPasses());
        map.put("totalDislikes", userActivity.getTotalDislikes());
        map.put("totalMatches", userActivity.getTotalMatches());
        map.put("matchSuccessRate", userActivity.getMatchSuccessRate());
        map.put("avgActionsPerDay", userActivity.getAvgActionsPerDay());
        map.put("lastActionAt", userActivity.getLastActionAt());
        map.put("actionsToday", userActivity.getActionsToday());
        map.put("currentStreak", userActivity.getCurrentStreak());
        map.put("longestStreak", userActivity.getLongestStreak());
        map.put("createdAt", userActivity.getCreatedAt());
        map.put("updatedAt", userActivity.getUpdatedAt());
        map.put("version", userActivity.getVersion());
        
        if (userActivity.getActions() != null) {
            map.put("actions", userActivity.getActions().stream()
                    .map(this::convertActionToMap)
                    .collect(Collectors.toList()));
        }
        
        if (userActivity.getDailySummary() != null) {
            Map<String, Object> dailySummaryMap = new HashMap<>();
            userActivity.getDailySummary().forEach((date, summary) -> 
                dailySummaryMap.put(date, convertDailySummaryToMap(summary)));
            map.put("dailySummary", dailySummaryMap);
        }
        
        return map;
    }

    private UserActivity convertToUserActivity(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return UserActivity.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .actions(convertToActionList((List<Map<String, Object>>) data.get("actions")))
                .dailySummary(convertToDailySummaryMap((Map<String, Object>) data.get("dailySummary")))
                .totalActions((Integer) data.get("totalActions"))
                .totalLikes((Integer) data.get("totalLikes"))
                .totalPasses((Integer) data.get("totalPasses"))
                .totalDislikes((Integer) data.get("totalDislikes"))
                .totalMatches((Integer) data.get("totalMatches"))
                .matchSuccessRate((Double) data.get("matchSuccessRate"))
                .avgActionsPerDay((Integer) data.get("avgActionsPerDay"))
                .lastActionAt((Timestamp) data.get("lastActionAt"))
                .actionsToday((Integer) data.get("actionsToday"))
                .currentStreak((Integer) data.get("currentStreak"))
                .longestStreak((Integer) data.get("longestStreak"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .version((Integer) data.get("version"))
                .build();
    }

    private Map<String, Object> convertActionToMap(UserActivity.Action action) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", action.getConnectId());
        map.put("targetUserId", action.getTargetUserId());
        map.put("targetUserName", action.getTargetUserName());
        map.put("action", action.getAction());
        map.put("timestamp", action.getTimestamp());
        map.put("source", action.getSource());
        map.put("matchSetId", action.getMatchSetId());
        map.put("batchDate", action.getBatchDate());
        map.put("resultedInMatch", action.getResultedInMatch());
        map.put("matchId", action.getMatchId());
        map.put("targetUserAge", action.getTargetUserAge());
        map.put("targetUserLocation", action.getTargetUserLocation());
        map.put("distance", action.getDistance());
        map.put("compatibilityScore", action.getCompatibilityScore());
        map.put("deviceType", action.getDeviceType());
        map.put("appVersion", action.getAppVersion());
        return map;
    }

    private UserActivity.Action convertToAction(Map<String, Object> actionMap) {
        if (actionMap == null) return null;
        
        return UserActivity.Action.builder()
                .connectId((String) actionMap.get("connectId"))
                .targetUserId((String) actionMap.get("targetUserId"))
                .targetUserName((String) actionMap.get("targetUserName"))
                .action((String) actionMap.get("action"))
                .timestamp((Timestamp) actionMap.get("timestamp"))
                .source((String) actionMap.get("source"))
                .matchSetId((String) actionMap.get("matchSetId"))
                .batchDate((String) actionMap.get("batchDate"))
                .resultedInMatch((Boolean) actionMap.get("resultedInMatch"))
                .matchId((String) actionMap.get("matchId"))
                .targetUserAge((Integer) actionMap.get("targetUserAge"))
                .targetUserLocation((String) actionMap.get("targetUserLocation"))
                .distance((Double) actionMap.get("distance"))
                .compatibilityScore((Double) actionMap.get("compatibilityScore"))
                .deviceType((String) actionMap.get("deviceType"))
                .appVersion((String) actionMap.get("appVersion"))
                .build();
    }

    private List<UserActivity.Action> convertToActionList(List<Map<String, Object>> actionMaps) {
        if (actionMaps == null) return new ArrayList<>();
        
        return actionMaps.stream()
                .map(this::convertToAction)
                .collect(Collectors.toList());
    }

    private Map<String, Object> convertDailySummaryToMap(UserActivity.DailySummary summary) {
        if (summary == null) return null;
        
        Map<String, Object> map = new HashMap<>();
        map.put("totalActions", summary.getTotalActions());
        map.put("likes", summary.getLikes());
        map.put("passes", summary.getPasses());
        map.put("dislikes", summary.getDislikes());
        map.put("matches", summary.getMatches());
        map.put("viewTime", summary.getViewTime());
        map.put("batchesCompleted", summary.getBatchesCompleted());
        return map;
    }

    private UserActivity.DailySummary convertToDailySummary(Map<String, Object> summaryMap) {
        if (summaryMap == null) return null;
        
        return UserActivity.DailySummary.builder()
                .totalActions((Integer) summaryMap.get("totalActions"))
                .likes((Integer) summaryMap.get("likes"))
                .passes((Integer) summaryMap.get("passes"))
                .dislikes((Integer) summaryMap.get("dislikes"))
                .matches((Integer) summaryMap.get("matches"))
                .viewTime((Integer) summaryMap.get("viewTime"))
                .batchesCompleted((Integer) summaryMap.get("batchesCompleted"))
                .build();
    }

    private Map<String, UserActivity.DailySummary> convertToDailySummaryMap(Map<String, Object> dailySummaryMap) {
        if (dailySummaryMap == null) return new HashMap<>();
        
        Map<String, UserActivity.DailySummary> result = new HashMap<>();
        dailySummaryMap.forEach((date, summaryObj) -> {
            if (summaryObj instanceof Map) {
                result.put(date, convertToDailySummary((Map<String, Object>) summaryObj));
            }
        });
        return result;
    }
}