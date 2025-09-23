package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.notifications.Notification;
import com.tpg.connect.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final String COLLECTION_NAME = "notifications";
    
    @Autowired
    private Firestore firestore;

    @Override
    public Notification save(Notification notification) {
        try {
            if (notification.getId() == null) {
                notification.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(notification.getId());
            docRef.set(convertToMap(notification)).get();
            
            return notification;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save notification", e);
        }
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        try {
            WriteBatch batch = firestore.batch();
            
            for (Notification notification : notifications) {
                if (notification.getId() == null) {
                    notification.setId(UUID.randomUUID().toString());
                }
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(notification.getId());
                batch.set(docRef, convertToMap(notification));
            }
            
            batch.commit().get();
            return notifications;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save notifications", e);
        }
    }

    @Override
    public Optional<Notification> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToNotification(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find notification by id", e);
        }
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find notifications by userId", e);
        }
    }

    @Override
    public List<Notification> findByUserIdAndRead(String userId, boolean read) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", read)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find notifications by userId and read status", e);
        }
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find notifications by userId ordered by createdAt", e);
        }
    }

    @Override
    public List<Notification> findTopByUserIdOrderByCreatedAtDesc(String userId, int page, int size) {
        try {
            Query query = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(size)
                    .offset(page * size);
                    
            QuerySnapshot querySnapshot = query.get().get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find top notifications by userId", e);
        }
    }

    @Override
    public List<Notification> findUnreadByUserId(String userId) {
        return findByUserIdAndRead(userId, false);
    }

    @Override
    public List<Notification> findOlderThan(LocalDateTime cutoffDate) {
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereLessThan("createdAt", cutoffTimestamp)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find notifications older than cutoff date", e);
        }
    }

    @Override
    public List<Notification> findPendingOlderThan(LocalDateTime cutoffDate) {
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereLessThan("createdAt", cutoffTimestamp)
                    .whereEqualTo("status", Notification.NotificationStatus.PENDING.name())
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToNotification)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find pending notifications older than cutoff date", e);
        }
    }

    @Override
    public long countUnreadByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", false)
                    .get()
                    .get();
                    
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count unread notifications", e);
        }
    }

    @Override
    public Notification markAsRead(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("isRead", true);
            updates.put("readAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to mark notification as read", e);
        }
    }

    @Override
    public void markAllAsReadForUser(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", false)
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("isRead", true);
                updates.put("readAt", Timestamp.now());
                
                batch.update(doc.getReference(), updates);
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to mark all notifications as read", e);
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete notification", e);
        }
    }

    @Override
    public void delete(Notification notification) {
        deleteById(notification.getId());
    }

    @Override
    public void deleteAll(List<Notification> notifications) {
        try {
            WriteBatch batch = firestore.batch();
            
            for (Notification notification : notifications) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(notification.getId());
                batch.delete(docRef);
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete notifications", e);
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
            throw new RuntimeException("Failed to delete notifications by userId", e);
        }
    }

    @Override
    public void deleteByUserIdAndCreatedAtBefore(String userId, LocalDateTime cutoffDate) {
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereLessThan("createdAt", cutoffTimestamp)
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.delete(doc.getReference());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete notifications by userId and createdAt", e);
        }
    }

    @Override
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereLessThan("createdAt", cutoffTimestamp)
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.delete(doc.getReference());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete old notifications", e);
        }
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(Notification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notification.getId());
        map.put("userId", notification.getUserId());
        map.put("type", notification.getType() != null ? notification.getType().name() : null);
        map.put("title", notification.getTitle());
        map.put("message", notification.getMessage());
        map.put("data", notification.getData());
        map.put("status", notification.getStatus() != null ? notification.getStatus().name() : null);
        map.put("channel", notification.getChannel() != null ? notification.getChannel().name() : null);
        map.put("priority", notification.getPriority() != null ? notification.getPriority().name() : null);
        map.put("actionUrl", notification.getActionUrl());
        map.put("imageUrl", notification.getImageUrl());
        map.put("isRead", notification.isRead());
        map.put("isSent", notification.isSent());
        
        // Convert LocalDateTime to Timestamp
        if (notification.getCreatedAt() != null) {
            map.put("createdAt", Timestamp.of(Date.from(notification.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (notification.getReadAt() != null) {
            map.put("readAt", Timestamp.of(Date.from(notification.getReadAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (notification.getSentAt() != null) {
            map.put("sentAt", Timestamp.of(Date.from(notification.getSentAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        
        return map;
    }

    private Notification convertToNotification(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        Notification notification = new Notification();
        notification.setId(doc.getId());
        notification.setUserId((String) data.get("userId"));
        
        // Convert enum values
        if (data.get("type") != null) {
            notification.setType(Notification.NotificationType.valueOf((String) data.get("type")));
        }
        if (data.get("status") != null) {
            notification.setStatus(Notification.NotificationStatus.valueOf((String) data.get("status")));
        }
        if (data.get("channel") != null) {
            notification.setChannel(Notification.NotificationChannel.valueOf((String) data.get("channel")));
        }
        if (data.get("priority") != null) {
            notification.setPriority(Notification.NotificationPriority.valueOf((String) data.get("priority")));
        }
        
        notification.setTitle((String) data.get("title"));
        notification.setMessage((String) data.get("message"));
        notification.setData((Map<String, Object>) data.get("data"));
        notification.setActionUrl((String) data.get("actionUrl"));
        notification.setImageUrl((String) data.get("imageUrl"));
        notification.setRead((Boolean) data.get("isRead"));
        notification.setSent((Boolean) data.get("isSent"));
        
        // Convert Timestamp to LocalDateTime
        if (data.get("createdAt") instanceof Timestamp) {
            notification.setCreatedAt(((Timestamp) data.get("createdAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("readAt") instanceof Timestamp) {
            notification.setReadAt(((Timestamp) data.get("readAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("sentAt") instanceof Timestamp) {
            notification.setSentAt(((Timestamp) data.get("sentAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return notification;
    }
}