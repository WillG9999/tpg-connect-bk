package com.tpg.connect.repository;

import com.tpg.connect.model.premium.Subscription;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository {
    
    // Create Operations
    Subscription save(Subscription subscription);
    
    // Read Operations
    Optional<Subscription> findById(String id);
    Optional<Subscription> findByUserId(String userId);
    Optional<Subscription> findActiveByUserId(String userId);
    List<Subscription> findBySubscriptionType(String subscriptionType);
    List<Subscription> findByUserIdAndStatus(String userId, Object status);
    List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Subscription> findByExternalSubscriptionId(String externalId);
    List<Subscription> findActiveSubscriptions();
    List<Subscription> findExpiredSubscriptions();
    List<Subscription> findExpiringSubscriptions(int days);
    
    // Update Operations
    Subscription updateStatus(String id, String status);
    Subscription renewSubscription(String id, int months);
    Subscription cancelSubscription(String id);
    
    // Delete Operations
    void deleteById(String id);
    void deleteByUserId(String userId);
}