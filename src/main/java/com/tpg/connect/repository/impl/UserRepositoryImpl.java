package com.tpg.connect.repository.impl;

import com.tpg.connect.model.User;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.util.ConnectIdGenerator;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private static final String COLLECTION_NAME = "userAuth";
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private ConnectIdGenerator connectIdGenerator;

    @Override
    public User createUser(User user) {
        String connectId = connectIdGenerator.generateUniqueConnectId(this);
        return createUserWithConnectId(user, connectId);
    }

    @Override
    public User createUserWithConnectId(User user, String connectId) {
        try {
            user.setConnectId(connectId);
            user.setCreatedAt(Timestamp.now());
            user.setUpdatedAt(Timestamp.now());
            user.setActive(true);
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            docRef.set(convertToMap(user)).get();
            
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    @Override
    public Optional<User> findByConnectId(String connectId) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(connectId)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToUser(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user by connectId", e);
        }
    }

    @Override
    public Optional<User> findById(String id) {
        // For backwards compatibility, map findById to findByConnectId
        return findByConnectId(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .whereEqualTo("active", true)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                Optional.empty() : 
                Optional.of(convertToUser(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("username", username)
                    .whereEqualTo("active", true)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                Optional.empty() : 
                Optional.of(convertToUser(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find user by username", e);
        }
    }

    @Override
    public List<User> findActiveUsers() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("active", true)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToUser)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active users", e);
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
            throw new RuntimeException("Failed to check if user exists by connectId", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get()
                    .get();
            return !querySnapshot.getDocuments().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if user exists by email", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .get();
            return !querySnapshot.getDocuments().isEmpty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to check if user exists by username", e);
        }
    }

    @Override
    public User updateUser(User user) {
        try {
            user.setUpdatedAt(Timestamp.now());
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(user.getConnectId());
            docRef.set(convertToMap(user)).get();
            return user;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    @Override
    public User save(User user) {
        if (user.getConnectId() != null && existsByConnectId(user.getConnectId())) {
            return updateUser(user);
        } else {
            return createUser(user);
        }
    }

    @Override
    public User updateEmailVerificationStatus(String connectId, boolean verified, Timestamp verifiedAt) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("emailVerified", verified);
            updates.put("emailVerifiedAt", verifiedAt);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update email verification", e);
        }
    }

    @Override
    public User updateLastLogin(String connectId, Timestamp loginTime, String deviceType) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastLoginAt", loginTime);
            updates.put("lastLoginDevice", deviceType);
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last login", e);
        }
    }

    @Override
    public User addFcmToken(String connectId, User.FcmToken token) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmTokens", FieldValue.arrayUnion(convertTokenToMap(token)));
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to add FCM token", e);
        }
    }

    @Override
    public User removeFcmToken(String connectId, String deviceId) {
        try {
            // First get the current user to find the token to remove
            Optional<User> userOpt = findByConnectId(connectId);
            if (!userOpt.isPresent()) {
                throw new RuntimeException("User not found");
            }
            
            User user = userOpt.get();
            if (user.getFcmTokens() != null) {
                // Find and remove the token with matching deviceId
                List<User.FcmToken> updatedTokens = user.getFcmTokens().stream()
                        .filter(token -> !deviceId.equals(token.getDeviceId()))
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("fcmTokens", updatedTokens.stream()
                        .map(this::convertTokenToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to remove FCM token", e);
        }
    }

    @Override
    public User updateFcmTokenLastUsed(String connectId, String deviceId, Timestamp lastUsed) {
        try {
            // First get the current user
            Optional<User> userOpt = findByConnectId(connectId);
            if (!userOpt.isPresent()) {
                throw new RuntimeException("User not found");
            }
            
            User user = userOpt.get();
            if (user.getFcmTokens() != null) {
                // Update the specific token's lastUsed timestamp
                List<User.FcmToken> updatedTokens = user.getFcmTokens().stream()
                        .map(token -> {
                            if (deviceId.equals(token.getDeviceId())) {
                                token.setLastUsed(lastUsed);
                            }
                            return token;
                        })
                        .collect(Collectors.toList());
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
                Map<String, Object> updates = new HashMap<>();
                updates.put("fcmTokens", updatedTokens.stream()
                        .map(this::convertTokenToMap)
                        .collect(Collectors.toList()));
                updates.put("updatedAt", FieldValue.serverTimestamp());
                
                docRef.update(updates).get();
            }
            
            return findByConnectId(connectId).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update FCM token last used", e);
        }
    }

    @Override
    public void softDeleteUser(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("active", false);
            updates.put("deletedAt", FieldValue.serverTimestamp());
            updates.put("updatedAt", FieldValue.serverTimestamp());
            
            docRef.update(updates).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to soft delete user", e);
        }
    }

    @Override
    public void hardDeleteUser(String connectId) {
        try {
            firestore.collection(COLLECTION_NAME).document(connectId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to hard delete user", e);
        }
    }

    @Override
    public List<User> findUsersByConnectIds(List<String> connectIds) {
        try {
            List<User> users = new ArrayList<>();
            
            // Firestore has a limit of 10 documents in whereIn queries
            // So we need to batch the requests
            List<List<String>> batches = new ArrayList<>();
            for (int i = 0; i < connectIds.size(); i += 10) {
                batches.add(connectIds.subList(i, Math.min(i + 10, connectIds.size())));
            }
            
            for (List<String> batch : batches) {
                QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                        .whereIn(FieldPath.documentId(), batch)
                        .get()
                        .get();
                        
                users.addAll(querySnapshot.getDocuments().stream()
                        .map(this::convertToUser)
                        .collect(Collectors.toList()));
            }
            
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find users by connectIds", e);
        }
    }

    @Override
    public Map<String, User> findUserMapByConnectIds(List<String> connectIds) {
        List<User> users = findUsersByConnectIds(connectIds);
        return users.stream()
                .collect(Collectors.toMap(User::getConnectId, user -> user));
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("connectId", user.getConnectId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("passwordHash", user.getPasswordHash());
        map.put("role", user.getRole());
        map.put("active", user.getActive());
        map.put("emailVerified", user.getEmailVerified());
        map.put("createdAt", user.getCreatedAt());
        map.put("updatedAt", user.getUpdatedAt());
        map.put("lastLoginAt", user.getLastLoginAt());
        map.put("emailVerifiedAt", user.getEmailVerifiedAt());
        map.put("deletedAt", user.getDeletedAt());
        map.put("lastLoginDevice", user.getLastLoginDevice());
        
        if (user.getFcmTokens() != null) {
            map.put("fcmTokens", user.getFcmTokens().stream()
                    .map(this::convertTokenToMap)
                    .collect(Collectors.toList()));
        }
        
        return map;
    }

    private User convertToUser(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return User.builder()
                .connectId(doc.getId())
                .username((String) data.get("username"))
                .email((String) data.get("email"))
                .passwordHash((String) data.get("passwordHash"))
                .role((String) data.get("role"))
                .active((Boolean) data.get("active"))
                .emailVerified((Boolean) data.get("emailVerified"))
                .createdAt((Timestamp) data.get("createdAt"))
                .updatedAt((Timestamp) data.get("updatedAt"))
                .lastLoginAt((Timestamp) data.get("lastLoginAt"))
                .emailVerifiedAt((Timestamp) data.get("emailVerifiedAt"))
                .deletedAt((Timestamp) data.get("deletedAt"))
                .lastLoginDevice((String) data.get("lastLoginDevice"))
                .fcmTokens(convertToTokenList((List<Map<String, Object>>) data.get("fcmTokens")))
                .build();
    }

    private Map<String, Object> convertTokenToMap(User.FcmToken token) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", token.getToken());
        map.put("deviceType", token.getDeviceType());
        map.put("deviceId", token.getDeviceId());
        map.put("addedAt", token.getAddedAt());
        map.put("lastUsed", token.getLastUsed());
        map.put("isActive", token.getIsActive());
        return map;
    }

    private List<User.FcmToken> convertToTokenList(List<Map<String, Object>> tokenMaps) {
        if (tokenMaps == null) return new ArrayList<>();
        
        return tokenMaps.stream()
                .map(this::convertToToken)
                .collect(Collectors.toList());
    }

    private User.FcmToken convertToToken(Map<String, Object> tokenMap) {
        return User.FcmToken.builder()
                .token((String) tokenMap.get("token"))
                .deviceType((String) tokenMap.get("deviceType"))
                .deviceId((String) tokenMap.get("deviceId"))
                .addedAt((Timestamp) tokenMap.get("addedAt"))
                .lastUsed((Timestamp) tokenMap.get("lastUsed"))
                .isActive((Boolean) tokenMap.get("isActive"))
                .build();
    }

    @Override
    public List<User> findAllForAdmin(int page, int size, String search, String status, String sortBy, String sortDirection) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection;
            
            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                // Note: Firestore doesn't support full-text search, so this is a simple contains check
                // In production, you might want to use a search service like Algolia
                query = query.whereGreaterThanOrEqualTo("email", search)
                           .whereLessThan("email", search + "\uf8ff");
            }
            
            // Apply status filter - applicationStatus should match the provided status
            if (status != null && !status.trim().isEmpty()) {
                // Convert status to ApplicationStatus enum value for filtering
                try {
                    ApplicationStatus statusEnum = ApplicationStatus.valueOf(status);
                    query = query.whereEqualTo("applicationStatus", statusEnum.toString());
                } catch (IllegalArgumentException e) {
                    // If invalid status provided, don't apply filter
                    log.warn("Invalid status filter provided: {}", status);
                }
            }
            
            // Apply sorting (Firestore has limited sorting capabilities)
            if (sortBy != null) {
                if ("desc".equals(sortDirection)) {
                    query = query.orderBy(sortBy, Query.Direction.DESCENDING);
                } else {
                    query = query.orderBy(sortBy, Query.Direction.ASCENDING);
                }
            }
            
            // Apply pagination
            query = query.offset(page * size).limit(size);
            
            QuerySnapshot querySnapshot = query.get().get();
            List<User> users = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                users.add(convertToUser(doc));
            }
            return users;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find users for admin", e);
        }
    }

    @Override
    public long countForAdmin(String search, String status) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection;
            
            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                query = query.whereGreaterThanOrEqualTo("email", search)
                           .whereLessThan("email", search + "\uf8ff");
            }
            
            // Apply status filter - applicationStatus should match the provided status
            if (status != null && !status.trim().isEmpty()) {
                // Convert status to ApplicationStatus enum value for filtering
                try {
                    ApplicationStatus statusEnum = ApplicationStatus.valueOf(status);
                    query = query.whereEqualTo("applicationStatus", statusEnum.toString());
                } catch (IllegalArgumentException e) {
                    // If invalid status provided, don't apply filter
                    log.warn("Invalid status filter provided: {}", status);
                }
            }
            
            QuerySnapshot querySnapshot = query.get().get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count users for admin", e);
        }
    }
}