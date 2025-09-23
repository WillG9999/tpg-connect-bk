package com.tpg.connect.repository;

import com.tpg.connect.model.notifications.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    
    // Create Operations
    Notification save(Notification notification);
    List<Notification> saveAll(List<Notification> notifications);
    
    // Read Operations
    Optional<Notification> findById(String id);
    List<Notification> findByUserId(String userId);
    List<Notification> findByUserIdAndRead(String userId, boolean read);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findTopByUserIdOrderByCreatedAtDesc(String userId, int page, int size);
    List<Notification> findUnreadByUserId(String userId);
    List<Notification> findOlderThan(LocalDateTime cutoffDate);
    List<Notification> findPendingOlderThan(LocalDateTime cutoffDate);
    long countUnreadByUserId(String userId);
    
    // Update Operations
    Notification markAsRead(String id);
    void markAllAsReadForUser(String userId);
    
    // Delete Operations
    void deleteById(String id);
    void delete(Notification notification);
    void deleteAll(List<Notification> notifications);
    void deleteByUserId(String userId);
    void deleteByUserIdAndCreatedAtBefore(String userId, LocalDateTime cutoffDate);
    void deleteOldNotifications(int daysOld);
}