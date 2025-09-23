package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class MatchRepositoryImpl implements MatchRepository {

    private static final String COLLECTION_NAME = "matches";
    
    @Autowired
    private Firestore firestore;

    @Override
    public Match save(Match match) {
        try {
            if (match.getId() == null) {
                match.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(match.getId());
            docRef.set(convertToMap(match)).get();
            
            return match;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save match", e);
        }
    }

    @Override
    public Optional<Match> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToMatch(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match by id", e);
        }
    }

    @Override
    public Optional<Match> findByUserIds(String userId1, String userId2) {
        try {
            // Try both combinations since either user could be user1 or user2
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId1)
                    .whereEqualTo("user2Id", userId2)
                    .get()
                    .get();
                    
            if (!querySnapshot.getDocuments().isEmpty()) {
                return Optional.of(convertToMatch(querySnapshot.getDocuments().get(0)));
            }
            
            // Try the reverse combination
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId2)
                    .whereEqualTo("user2Id", userId1)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToMatch(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match by userIds", e);
        }
    }

    @Override
    public Optional<Match> findByUserIdsAndStatus(String userId1, String userId2, Object status) {
        try {
            String statusStr = status instanceof Match.MatchStatus ? 
                    ((Match.MatchStatus) status).name() : status.toString();
            
            // Try both combinations
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId1)
                    .whereEqualTo("user2Id", userId2)
                    .whereEqualTo("status", statusStr)
                    .get()
                    .get();
                    
            if (!querySnapshot.getDocuments().isEmpty()) {
                return Optional.of(convertToMatch(querySnapshot.getDocuments().get(0)));
            }
            
            // Try the reverse combination
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId2)
                    .whereEqualTo("user2Id", userId1)
                    .whereEqualTo("status", statusStr)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToMatch(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find match by userIds and status", e);
        }
    }

    @Override
    public List<Match> findByUserId(String userId) {
        try {
            List<Match> matches = new ArrayList<>();
            
            // Find matches where user is user1
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            // Find matches where user is user2
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user2Id", userId)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            return matches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find matches by userId", e);
        }
    }

    @Override
    public List<Match> findByUserIdAndStatus(String userId, Object status) {
        try {
            String statusStr = status instanceof Match.MatchStatus ? 
                    ((Match.MatchStatus) status).name() : status.toString();
            
            List<Match> matches = new ArrayList<>();
            
            // Find matches where user is user1
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId)
                    .whereEqualTo("status", statusStr)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            // Find matches where user is user2
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user2Id", userId)
                    .whereEqualTo("status", statusStr)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            return matches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find matches by userId and status", e);
        }
    }

    @Override
    public List<Match> findActiveMatchesByUserId(String userId) {
        return findByUserIdAndStatus(userId, Match.MatchStatus.ACTIVE);
    }

    @Override
    public List<Match> findRecentMatchesByUserId(String userId, int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            List<Match> matches = new ArrayList<>();
            
            // Find matches where user is user1
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId)
                    .whereGreaterThan("matchedAt", cutoffTimestamp)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            // Find matches where user is user2
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user2Id", userId)
                    .whereGreaterThan("matchedAt", cutoffTimestamp)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            return matches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find recent matches by userId", e);
        }
    }

    @Override
    public List<Match> findRecentByUserId(String userId, Object status, int days) {
        try {
            String statusStr = status instanceof Match.MatchStatus ? 
                    ((Match.MatchStatus) status).name() : status.toString();
            
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            List<Match> matches = new ArrayList<>();
            
            // Find matches where user is user1
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user1Id", userId)
                    .whereEqualTo("status", statusStr)
                    .whereGreaterThan("matchedAt", cutoffTimestamp)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            // Find matches where user is user2
            querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user2Id", userId)
                    .whereEqualTo("status", statusStr)
                    .whereGreaterThan("matchedAt", cutoffTimestamp)
                    .get()
                    .get();
            
            matches.addAll(querySnapshot.getDocuments().stream()
                    .map(this::convertToMatch)
                    .collect(Collectors.toList()));
            
            return matches;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find recent matches by userId and status", e);
        }
    }

    @Override
    public long countMatchesByUserId(String userId) {
        return findByUserId(userId).size();
    }

    @Override
    public long countByUserIdAndStatus(String userId, Object status) {
        return findByUserIdAndStatus(userId, status).size();
    }

    @Override
    public Match updateLastActivity(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastActivityAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last activity", e);
        }
    }

    @Override
    public Match updateMatchStatus(String id, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update match status", e);
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete match", e);
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        try {
            List<Match> userMatches = findByUserId(userId);
            
            WriteBatch batch = firestore.batch();
            
            for (Match match : userMatches) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(match.getId());
                batch.delete(docRef);
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete matches by userId", e);
        }
    }

    @Override
    public void unmatchUsers(String userId1, String userId2) {
        try {
            Optional<Match> matchOpt = findByUserIds(userId1, userId2);
            if (matchOpt.isPresent()) {
                Match match = matchOpt.get();
                match.setStatus(Match.MatchStatus.UNMATCHED);
                save(match);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to unmatch users", e);
        }
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(Match match) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", match.getId());
        map.put("user1Id", match.getUser1Id());
        map.put("user2Id", match.getUser2Id());
        map.put("conversationId", match.getConversationId());
        map.put("status", match.getStatus() != null ? match.getStatus().name() : null);
        
        // Convert LocalDateTime to Timestamp
        if (match.getMatchedAt() != null) {
            map.put("matchedAt", Timestamp.of(Date.from(match.getMatchedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (match.getLastActivityAt() != null) {
            map.put("lastActivityAt", Timestamp.of(Date.from(match.getLastActivityAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        
        return map;
    }

    private Match convertToMatch(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        Match match = new Match();
        match.setId(doc.getId());
        match.setUser1Id((String) data.get("user1Id"));
        match.setUser2Id((String) data.get("user2Id"));
        match.setConversationId((String) data.get("conversationId"));
        
        // Convert enum values
        if (data.get("status") != null) {
            match.setStatus(Match.MatchStatus.valueOf((String) data.get("status")));
        }
        
        // Convert Timestamp to LocalDateTime
        if (data.get("matchedAt") instanceof Timestamp) {
            match.setMatchedAt(((Timestamp) data.get("matchedAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("lastActivityAt") instanceof Timestamp) {
            match.setLastActivityAt(((Timestamp) data.get("lastActivityAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return match;
    }
}