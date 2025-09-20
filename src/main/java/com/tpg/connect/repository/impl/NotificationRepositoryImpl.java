package com.tpg.connect.repository.impl;

import com.tpg.connect.model.notifications.Notification;
import com.tpg.connect.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {

    private static final String COLLECTION_NAME = "notifications";
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Notification save(Notification notification) {
        return mongoTemplate.save(notification, COLLECTION_NAME);
    }

    @Override
    public List<Notification> saveAll(List<Notification> notifications) {
        return (List<Notification>) mongoTemplate.insertAll(notifications);
    }

    @Override
    public Optional<Notification> findById(String id) {
        Notification notification = mongoTemplate.findById(id, Notification.class, COLLECTION_NAME);
        return Optional.ofNullable(notification);
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findByUserIdAndRead(String userId, boolean read) {
        Query query = new Query(Criteria.where("userId").is(userId).and("isRead").is(read));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findByUserIdOrderByCreatedAtDesc(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findTopByUserIdOrderByCreatedAtDesc(String userId, PageRequest pageRequest) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .with(pageRequest);
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findUnreadByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("isRead").is(false));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findOlderThan(LocalDateTime cutoffDate) {
        Query query = new Query(Criteria.where("createdAt").lt(cutoffDate));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public List<Notification> findPendingOlderThan(LocalDateTime cutoffDate) {
        Query query = new Query(Criteria.where("createdAt").lt(cutoffDate)
                .and("status").is(Notification.NotificationStatus.PENDING));
        return mongoTemplate.find(query, Notification.class, COLLECTION_NAME);
    }

    @Override
    public long countUnreadByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("isRead").is(false));
        return mongoTemplate.count(query, COLLECTION_NAME);
    }

    @Override
    public Notification markAsRead(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update()
                .set("isRead", true)
                .set("readAt", LocalDateTime.now());
        
        mongoTemplate.updateFirst(query, update, COLLECTION_NAME);
        return findById(id).orElse(null);
    }

    @Override
    public void markAllAsReadForUser(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId).and("isRead").is(false));
        Update update = new Update()
                .set("isRead", true)
                .set("readAt", LocalDateTime.now());
        
        mongoTemplate.updateMulti(query, update, COLLECTION_NAME);
    }

    @Override
    public void deleteById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, COLLECTION_NAME);
    }

    @Override
    public void delete(Notification notification) {
        mongoTemplate.remove(notification, COLLECTION_NAME);
    }

    @Override
    public void deleteAll(List<Notification> notifications) {
        notifications.forEach(notification -> mongoTemplate.remove(notification, COLLECTION_NAME));
    }

    @Override
    public void deleteByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        mongoTemplate.remove(query, COLLECTION_NAME);
    }

    @Override
    public void deleteByUserIdAndCreatedAtBefore(String userId, LocalDateTime cutoffDate) {
        Query query = new Query(Criteria.where("userId").is(userId)
                .and("createdAt").lt(cutoffDate));
        mongoTemplate.remove(query, COLLECTION_NAME);
    }

    @Override
    public void deleteOldNotifications(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        Query query = new Query(Criteria.where("createdAt").lt(cutoffDate));
        mongoTemplate.remove(query, COLLECTION_NAME);
    }
}