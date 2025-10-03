package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.repository.UserActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserActionRepositoryImpl implements UserActionRepository {

    private static final String COLLECTION_NAME = "user_actions";
    
    @Autowired
    private Firestore firestore;

    @Override
    public UserAction save(UserAction userAction) {
        try {
            if (userAction.getId() == null) {
                userAction.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userAction.getId());
            docRef.set(convertToMap(userAction)).get();
            
            return userAction;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save user action", e);
        }
    }

    @Override
    public Optional<UserAction> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToUserAction(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user action by id", e);
        }
    }

    @Override
    public Optional<UserAction> findByUserIdAndTargetUserId(String userId, String targetUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("targetUserId", targetUserId)
                    .limit(1)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToUserAction(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user action by userId and targetUserId", e);
        }
    }

    @Override
    public List<UserAction> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserAction)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user actions by userId", e);
        }
    }

    @Override
    public List<UserAction> findByUserIdAndAction(String userId, String action) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("action", action)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserAction)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user actions by userId and action", e);
        }
    }

    @Override
    public List<UserAction> findByTargetUserId(String targetUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("targetUserId", targetUserId)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserAction)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user actions by targetUserId", e);
        }
    }

    @Override
    public List<UserAction> findLikesByUserId(String userId) {
        return findByUserIdAndAction(userId, UserAction.ActionType.LIKE.name());
    }

    @Override
    public List<UserAction> findPassesByUserId(String userId) {
        return findByUserIdAndAction(userId, UserAction.ActionType.PASS.name());
    }

    @Override
    public boolean hasUserActedOnTarget(String userId, String targetUserId) {
        return findByUserIdAndTargetUserId(userId, targetUserId).isPresent();
    }

    @Override
    public boolean existsByUserIdAndTargetUserIdAndAction(String userId, String targetUserId, Object action) {
        try {
            String actionStr = action instanceof UserAction.ActionType ? 
                    ((UserAction.ActionType) action).name() : action.toString();
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("targetUserId", targetUserId)
                    .whereEqualTo("action", actionStr)
                    .limit(1)
                    .get()
                    .get();
                    
            return !querySnapshot.getDocuments().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if user action exists", e);
        }
    }

    @Override
    public boolean existsByUserIdAndTargetUserId(String userId, String targetUserId) {
        return hasUserActedOnTarget(userId, targetUserId);
    }

    @Override
    public List<UserAction> findByUserIdAndTimestampAfter(String userId, LocalDateTime cutoffDate) {
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereGreaterThan("timestamp", cutoffTimestamp)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUserAction)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user actions by userId and timestamp after", e);
        }
    }

    @Override
    public UserAction updateAction(String id, String newAction) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("action", newAction);
            updates.put("timestamp", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update user action", e);
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete user action", e);
        }
    }

    @Override
    public void deleteByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.delete(doc.getReference());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete user actions by userId", e);
        }
    }

    @Override
    public void deleteByUserIdAndTargetUserId(String userId, String targetUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("targetUserId", targetUserId)
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.delete(doc.getReference());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete user actions by userId and targetUserId", e);
        }
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(UserAction userAction) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", userAction.getId());
        map.put("userId", userAction.getUserId());
        map.put("targetUserId", userAction.getTargetUserId());
        map.put("action", userAction.getAction() != null ? userAction.getAction().name() : null);
        map.put("batchDate", userAction.getBatchDate());
        map.put("processed", userAction.isProcessed());
        
        // Convert LocalDateTime to Timestamp
        if (userAction.getTimestamp() != null) {
            map.put("timestamp", Timestamp.of(Date.from(userAction.getTimestamp().atZone(ZoneId.systemDefault()).toInstant())));
        }
        
        return map;
    }

    private UserAction convertToUserAction(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        UserAction userAction = new UserAction();
        userAction.setId(doc.getId());
        userAction.setUserId((String) data.get("userId"));
        userAction.setTargetUserId((String) data.get("targetUserId"));
        userAction.setBatchDate((String) data.get("batchDate"));
        userAction.setProcessed((Boolean) data.get("processed"));
        
        // Convert enum values
        if (data.get("action") != null) {
            userAction.setAction(UserAction.ActionType.valueOf((String) data.get("action")));
        }
        
        // Convert Timestamp to LocalDateTime
        if (data.get("timestamp") instanceof Timestamp) {
            userAction.setTimestamp(((Timestamp) data.get("timestamp")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return userAction;
    }

    @Override
    public long countByUserId(String userId) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection.whereEqualTo("userId", userId);
            
            QuerySnapshot querySnapshot = query.get().get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count user actions by userId", e);
        }
    }

    @Override
    public List<UserAction> findRecentByUserId(String userId, int limit) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit);
            
            QuerySnapshot querySnapshot = query.get().get();
            List<UserAction> actions = new ArrayList<>();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                actions.add(convertToUserAction(doc));
            }
            
            return actions;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find recent user actions by userId", e);
        }
    }

    @Override
    public long countByUserIdAndAction(String userId, String action) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("action", action);
            
            QuerySnapshot querySnapshot = query.get().get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count user actions by userId and action", e);
        }
    }
}