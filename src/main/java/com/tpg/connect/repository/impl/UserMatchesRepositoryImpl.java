package com.tpg.connect.repository.impl;

import com.tpg.connect.model.UserMatches;
import com.tpg.connect.repository.UserMatchesRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserMatchesRepositoryImpl implements UserMatchesRepository {

    private static final String COLLECTION_NAME = "user_matches";
    
    @Autowired
    private Firestore firestore;

    @Override
    public UserMatches createUserMatches(UserMatches userMatches) {
        try {
            userMatches.setCreatedAt(Timestamp.now());
            userMatches.setUpdatedAt(Timestamp.now());
            userMatches.setVersion(1);
            
            if (userMatches.getMatches() == null) {
                userMatches.setMatches(new ArrayList<>());
            }
            if (userMatches.getTotalMatches() == null) {
                userMatches.setTotalMatches(0);
            }
            if (userMatches.getActiveMatches() == null) {
                userMatches.setActiveMatches(0);
            }
            if (userMatches.getNewMatches() == null) {
                userMatches.setNewMatches(0);
            }
            if (userMatches.getConversationsStarted() == null) {
                userMatches.setConversationsStarted(0);
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userMatches.getConnectId());
            docRef.set(convertToMap(userMatches)).get();
            
            return userMatches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create user matches", e);
        }
    }

    @Override
    public Optional<UserMatches> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToUserMatches(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user matches by connectId", e);
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
            throw new RuntimeException("Failed to check if user matches exists", e);
        }
    }

    @Override
    public UserMatches addMatch(String connectId, UserMatches.Match match) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("matches", FieldValue.arrayUnion(convertMatchToMap(match)));
            updates.put("totalMatches", FieldValue.increment(1));
            updates.put("activeMatches", FieldValue.increment(1));
            updates.put("newMatches", FieldValue.increment(1));
            updates.put("lastMatchAt", match.getMatchedAt());
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add match", e);
        }
    }

    @Override
    public UserMatches updateMatch(String connectId, String matchId, UserMatches.Match updatedMatch) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> matchId.equals(match.getConnectId()) ? updatedMatch : match)
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match", e);
        }
    }

    @Override
    public UserMatches updateMatchStatus(String connectId, String matchId, String status) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> {
                            if (matchId.equals(match.getConnectId())) {
                                match.setStatus(status);
                                match.setLastActivityAt(Timestamp.now());
                            }
                            return match;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match status", e);
        }
    }

    @Override
    public UserMatches markMatchAsRead(String connectId, String matchId, Timestamp readAt) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> {
                            if (matchId.equals(match.getConnectId())) {
                                match.setMyLastRead(readAt);
                                match.setUnreadCount(0);
                            }
                            return match;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to mark match as read", e);
        }
    }

    @Override
    public UserMatches updateLastActivity(String connectId, String matchId, Timestamp lastActivity) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> {
                            if (matchId.equals(match.getConnectId())) {
                                match.setLastActivityAt(lastActivity);
                            }
                            return match;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last activity", e);
        }
    }

    @Override
    public UserMatches updateMatchMessage(String connectId, String matchId, String lastMessage, Timestamp lastMessageAt) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> {
                            if (matchId.equals(match.getConnectId())) {
                                match.setLastMessageText(lastMessage);
                                match.setLastMessageAt(lastMessageAt);
                                match.setHasMessaged(true);
                                match.setLastActivityAt(lastMessageAt);
                                if (match.getUnreadCount() == null) {
                                    match.setUnreadCount(1);
                                } else {
                                    match.setUnreadCount(match.getUnreadCount() + 1);
                                }
                            }
                            return match;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match message", e);
        }
    }

    @Override
    public UserMatches reportMatch(String connectId, String matchId, String reportedBy, Timestamp reportedAt, String adminNotes) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .map(match -> {
                            if (matchId.equals(match.getConnectId())) {
                                match.setReportedBy(reportedBy);
                                match.setReportedAt(reportedAt);
                                match.setAdminNotes(adminNotes);
                                match.setStatus("reported");
                            }
                            return match;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to report match", e);
        }
    }

    @Override
    public UserMatches updateMatchCounts(String connectId, Integer totalMatches, Integer activeMatches, Integer newMatches) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            if (totalMatches != null) updates.put("totalMatches", totalMatches);
            if (activeMatches != null) updates.put("activeMatches", activeMatches);
            if (newMatches != null) updates.put("newMatches", newMatches);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match counts", e);
        }
    }

    @Override
    public UserMatches incrementConversationCount(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("conversationsStarted", FieldValue.increment(1));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            updates.put("version", FieldValue.increment(1));
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to increment conversation count", e);
        }
    }

    @Override
    public UserMatches updateLastMatchTime(String connectId, Timestamp lastMatchAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMatchAt", lastMatchAt);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last match time", e);
        }
    }

    @Override
    public UserMatches removeMatch(String connectId, String matchId) {
        try {
            Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
            if (!userMatchesOpt.isPresent()) {
                throw new RuntimeException("User matches not found");
            }
            
            UserMatches userMatches = userMatchesOpt.get();
            if (userMatches.getMatches() != null) {
                List<UserMatches.Match> updatedMatches = userMatches.getMatches().stream()
                        .filter(match -> !matchId.equals(match.getConnectId()))
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("matches", updatedMatches.stream()
                        .map(this::convertMatchToMap)
                        .collect(Collectors.toList()));
                updates.put("totalMatches", FieldValue.increment(-1));
                updates.put("activeMatches", FieldValue.increment(-1));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                updates.put("version", FieldValue.increment(1));
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User matches not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to remove match", e);
        }
    }

    @Override
    public void deleteUserMatches(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete user matches", e);
        }
    }

    @Override
    public List<UserMatches> findUserMatchesByConnectIds(List<String> connectIds) {
        try {
            List<UserMatches> userMatchesList = new ArrayList<>();
            
            List<List<String>> batches = new ArrayList<>();
            for (int i = 0; i < connectIds.size(); i += 10) {
                batches.add(connectIds.subList(i, Math.min(i + 10, connectIds.size())));
            }
            
            for (List<String> batch : batches) {
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .get();
                        
                userMatchesList.addAll(querySnapshot.getDocuments().stream()
                        .map(this::convertToUserMatches)
                        .collect(Collectors.toList()));
            }
            
            return userMatchesList;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user matches by connectIds", e);
        }
    }

    @Override
    public Map<String, UserMatches> findUserMatchesMapByConnectIds(List<String> connectIds) {
        List<UserMatches> userMatchesList = findUserMatchesByConnectIds(connectIds);
        return userMatchesList.stream()
                .collect(Collectors.toMap(UserMatches::getConnectId, userMatches -> userMatches));
    }

    @Override
    public List<UserMatches.Match> findActiveMatchesForUser(String connectId) {
        Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
        if (!userMatchesOpt.isPresent() || userMatchesOpt.get().getMatches() == null) {
            return new ArrayList<>();
        }
        
        return userMatchesOpt.get().getMatches().stream()
                .filter(match -> "active".equals(match.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserMatches.Match> findNewMatchesForUser(String connectId) {
        Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
        if (!userMatchesOpt.isPresent() || userMatchesOpt.get().getMatches() == null) {
            return new ArrayList<>();
        }
        
        return userMatchesOpt.get().getMatches().stream()
                .filter(match -> "new".equals(match.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserMatches.Match> findUnreadMatchesForUser(String connectId) {
        Optional<UserMatches> userMatchesOpt = findByConnectId(connectId);
        if (!userMatchesOpt.isPresent() || userMatchesOpt.get().getMatches() == null) {
            return new ArrayList<>();
        }
        
        return userMatchesOpt.get().getMatches().stream()
                .filter(match -> match.getUnreadCount() != null && match.getUnreadCount() > 0)
                .collect(Collectors.toList());
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(UserMatches userMatches) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", userMatches.getConnectId());
        map.put("userId", userMatches.getUserId());
        map.put("totalMatches", userMatches.getTotalMatches());
        map.put("activeMatches", userMatches.getActiveMatches());
        map.put("newMatches", userMatches.getNewMatches());
        map.put("conversationsStarted", userMatches.getConversationsStarted());
        map.put("lastMatchAt", userMatches.getLastMatchAt());
        map.put("createdAt", userMatches.getCreatedAt());
        map.put("updatedAt", userMatches.getUpdatedAt());
        map.put("version", userMatches.getVersion());
        
        if (userMatches.getMatches() != null) {
            map.put("matches", userMatches.getMatches().stream()
                    .map(this::convertMatchToMap)
                    .collect(Collectors.toList()));
        }
        
        return map;
    }

    private UserMatches convertToUserMatches(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return UserMatches.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .matches(convertToMatchList((List<Map<String, Object>>) data.get("matches")))
                .totalMatches(safeToInteger(data.get("totalMatches")))
                .activeMatches(safeToInteger(data.get("activeMatches")))
                .newMatches(safeToInteger(data.get("newMatches")))
                .conversationsStarted(safeToInteger(data.get("conversationsStarted")))
                .lastMatchAt((Timestamp) data.get("lastMatchAt"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .version(safeToInteger(data.get("version")))
                .build();
    }

    private Map<String, Object> convertMatchToMap(UserMatches.Match match) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", match.getConnectId());
        map.put("otherUserId", match.getOtherUserId());
        map.put("otherUserName", match.getOtherUserName());
        map.put("otherUserPhoto", match.getOtherUserPhoto());
        map.put("matchedAt", match.getMatchedAt());
        map.put("myActionAt", match.getMyActionAt());
        map.put("theirActionAt", match.getTheirActionAt());
        map.put("status", match.getStatus());
        map.put("lastActivityAt", match.getLastActivityAt());
        map.put("matchSource", match.getMatchSource());
        map.put("matchSetId", match.getMatchSetId());
        map.put("hasMessaged", match.getHasMessaged());
        map.put("lastMessageAt", match.getLastMessageAt());
        map.put("lastMessageText", match.getLastMessageText());
        map.put("myLastRead", match.getMyLastRead());
        map.put("unreadCount", match.getUnreadCount());
        map.put("compatibilityScore", match.getCompatibilityScore());
        map.put("commonInterests", match.getCommonInterests());
        map.put("distance", match.getDistance());
        map.put("reportedBy", match.getReportedBy());
        map.put("reportedAt", match.getReportedAt());
        map.put("adminNotes", match.getAdminNotes());
        return map;
    }

    private UserMatches.Match convertToMatch(Map<String, Object> matchMap) {
        if (matchMap == null) return null;
        
        return UserMatches.Match.builder()
                .connectId((String) matchMap.get("connectId"))
                .otherUserId((String) matchMap.get("otherUserId"))
                .otherUserName((String) matchMap.get("otherUserName"))
                .otherUserPhoto((String) matchMap.get("otherUserPhoto"))
                .matchedAt((Timestamp) matchMap.get("matchedAt"))
                .myActionAt((Timestamp) matchMap.get("myActionAt"))
                .theirActionAt((Timestamp) matchMap.get("theirActionAt"))
                .status((String) matchMap.get("status"))
                .lastActivityAt((Timestamp) matchMap.get("lastActivityAt"))
                .matchSource((String) matchMap.get("matchSource"))
                .matchSetId((String) matchMap.get("matchSetId"))
                .hasMessaged((Boolean) matchMap.get("hasMessaged"))
                .lastMessageAt((Timestamp) matchMap.get("lastMessageAt"))
                .lastMessageText((String) matchMap.get("lastMessageText"))
                .myLastRead((Timestamp) matchMap.get("myLastRead"))
                .unreadCount(safeToInteger(matchMap.get("unreadCount")))
                .compatibilityScore((Double) matchMap.get("compatibilityScore"))
                .commonInterests((List<String>) matchMap.get("commonInterests"))
                .distance((Double) matchMap.get("distance"))
                .reportedBy((String) matchMap.get("reportedBy"))
                .reportedAt((Timestamp) matchMap.get("reportedAt"))
                .adminNotes((String) matchMap.get("adminNotes"))
                .build();
    }

    private List<UserMatches.Match> convertToMatchList(List<Map<String, Object>> matchMaps) {
        if (matchMaps == null) return new ArrayList<>();
        
        return matchMaps.stream()
                .map(this::convertToMatch)
                .collect(Collectors.toList());
    }
    
    private Integer safeToInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}