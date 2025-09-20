package com.tpg.connect.repository.impl;

import com.tpg.connect.model.BlockedUser;
import com.tpg.connect.repository.BlockedUserRepository;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class BlockedUserRepositoryImpl implements BlockedUserRepository {

    private static final String COLLECTION_NAME = "blocked_users";
    
    @Autowired
    private Firestore firestore;

    @Override
    public BlockedUser createBlock(BlockedUser blockedUser) {
        try {
            if (blockedUser.getBlockedAt() == null) {
                blockedUser.setBlockedAt(Timestamp.now());
            }
            if (blockedUser.getStatus() == null) {
                blockedUser.setStatus("ACTIVE");
            }
            if (blockedUser.getBlockType() == null) {
                blockedUser.setBlockType("USER_INITIATED");
            }
            if (blockedUser.getSeverity() == null) {
                blockedUser.setSeverity("MEDIUM");
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(blockedUser.getConnectId());
            docRef.set(convertToMap(blockedUser)).get();
            
            return blockedUser;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create block", e);
        }
    }

    @Override
    public Optional<BlockedUser> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToBlockedUser(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocked user by connectId", e);
        }
    }

    @Override
    public List<BlockedUser> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by userId", e);
        }
    }

    @Override
    public List<BlockedUser> findByBlockedUserId(String blockedUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockedUserId", blockedUserId)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by blockedUserId", e);
        }
    }

    @Override
    public boolean isUserBlocked(String userId, String blockedUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("blockedUserId", blockedUserId)
                    .whereEqualTo("status", "ACTIVE")
                    .limit(1)
                    .get()
                    .get();
                    
            return !querySnapshot.getDocuments().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if user is blocked", e);
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
            throw new RuntimeException("Failed to check if blocked user exists", e);
        }
    }

    @Override
    public BlockedUser save(BlockedUser blockedUser) {
        return updateBlock(blockedUser);
    }

    @Override
    public BlockedUser findByUserIdAndBlockedUserId(String userId, String blockedUserId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("blockedUserId", blockedUserId)
                    .limit(1)
                    .get()
                    .get();
                    
            if (querySnapshot.getDocuments().isEmpty()) {
                return null;
            }
            
            return convertToBlockedUser(querySnapshot.getDocuments().get(0));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find block by userId and blockedUserId", e);
        }
    }

    @Override
    public List<BlockedUser> findByUserIdAndStatus(String userId, Object status) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", status.toString())
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by userId and status", e);
        }
    }

    @Override
    public BlockedUser updateBlock(BlockedUser blockedUser) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(blockedUser.getConnectId());
            docRef.set(convertToMap(blockedUser)).get();
            
            return blockedUser;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update block", e);
        }
    }

    @Override
    public BlockedUser updateStatus(String connectId, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update block status", e);
        }
    }

    @Override
    public BlockedUser addReview(String connectId, String reviewedBy, Timestamp reviewedAt, String adminNotes) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("reviewedBy", reviewedBy);
            updates.put("reviewedAt", reviewedAt);
            updates.put("adminNotes", adminNotes);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add review to block", e);
        }
    }

    @Override
    public BlockedUser unblockUser(String connectId, String unblockReason, String unblockedBy, Timestamp unblockedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "REMOVED");
            updates.put("unblockReason", unblockReason);
            updates.put("unblockedBy", unblockedBy);
            updates.put("unblockedAt", unblockedAt);
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("Block not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to unblock user", e);
        }
    }

    @Override
    public void deleteBlock(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete block", e);
        }
    }

    @Override
    public List<BlockedUser> findActiveBlocks(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", "ACTIVE")
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active blocks", e);
        }
    }

    @Override
    public List<BlockedUser> findBlocksByType(String blockType) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("blockType", blockType)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by type", e);
        }
    }

    @Override
    public List<BlockedUser> findBlocksBySeverity(String severity) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("severity", severity)
                    .orderBy("blockedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks by severity", e);
        }
    }

    @Override
    public List<BlockedUser> findBlocksNeedingReview() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", "ACTIVE")
                    .whereEqualTo("reviewedBy", null)
                    .orderBy("blockedAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToBlockedUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find blocks needing review", e);
        }
    }

    // Helper conversion methods
    private Map<String, Object> convertToMap(BlockedUser blockedUser) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", blockedUser.getConnectId());
        map.put("userId", blockedUser.getUserId());
        map.put("blockedUserId", blockedUser.getBlockedUserId());
        map.put("reason", blockedUser.getReason());
        map.put("blockedAt", blockedUser.getBlockedAt());
        map.put("status", blockedUser.getStatus());
        map.put("source", blockedUser.getSource());
        map.put("matchId", blockedUser.getMatchId());
        map.put("blockType", blockedUser.getBlockType());
        map.put("severity", blockedUser.getSeverity());
        map.put("reviewedBy", blockedUser.getReviewedBy());
        map.put("reviewedAt", blockedUser.getReviewedAt());
        map.put("adminNotes", blockedUser.getAdminNotes());
        map.put("unblockedAt", blockedUser.getUnblockedAt());
        map.put("unblockReason", blockedUser.getUnblockReason());
        map.put("unblockedBy", blockedUser.getUnblockedBy());
        return map;
    }

    private BlockedUser convertToBlockedUser(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return BlockedUser.builder()
                .connectId(doc.getId())
                .userId((String) data.get("userId"))
                .blockedUserId((String) data.get("blockedUserId"))
                .reason((String) data.get("reason"))
                .blockedAt((Timestamp) data.get("blockedAt"))
                .status((String) data.get("status"))
                .source((String) data.get("source"))
                .matchId((String) data.get("matchId"))
                .blockType((String) data.get("blockType"))
                .severity((String) data.get("severity"))
                .reviewedBy((String) data.get("reviewedBy"))
                .reviewedAt((Timestamp) data.get("reviewedAt"))
                .adminNotes((String) data.get("adminNotes"))
                .unblockedAt((Timestamp) data.get("unblockedAt"))
                .unblockReason((String) data.get("unblockReason"))
                .unblockedBy((String) data.get("unblockedBy"))
                .build();
    }
}