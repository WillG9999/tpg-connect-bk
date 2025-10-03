package com.tpg.connect.repository.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import com.tpg.connect.model.UserBlocked;
import com.tpg.connect.repository.UserBlockedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Repository
public class UserBlockedRepositoryImpl implements UserBlockedRepository {
    
    private static final String COLLECTION_NAME = "UserBlocked";
    
    @Autowired
    private Firestore firestore;
    
    @Override
    public UserBlocked findByConnectId(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            if (document.exists()) {
                return document.toObject(UserBlocked.class);
            }
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error retrieving user blocked configuration", e);
        }
    }
    
    @Override
    public UserBlocked save(UserBlocked userBlocked) {
        try {
            // Set updated timestamp
            userBlocked.setUpdatedAt(Timestamp.now());
            if (userBlocked.getCreatedAt() == null) {
                userBlocked.setCreatedAt(Timestamp.now());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userBlocked.getConnectId());
            ApiFuture<WriteResult> future = docRef.set(userBlocked);
            future.get(); // Wait for completion
            
            return userBlocked;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error saving user blocked configuration", e);
        }
    }
    
    @Override
    public void delete(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            ApiFuture<WriteResult> future = docRef.delete();
            future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error deleting user blocked configuration", e);
        }
    }
    
    @Override
    public boolean existsByConnectId(String connectId) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(connectId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error checking user blocked configuration existence", e);
        }
    }
    
    @Override
    public UserBlocked createEmptyUserBlocked(String connectId) {
        UserBlocked userBlocked = UserBlocked.builder()
                .connectId(connectId)
                .directBlocks(new ArrayList<>())
                .safetyBlocks(new ArrayList<>())
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .build();
        
        return save(userBlocked);
    }
}