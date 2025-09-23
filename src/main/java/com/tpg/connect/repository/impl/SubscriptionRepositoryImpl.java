package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.premium.Subscription;
import com.tpg.connect.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private static final String COLLECTION_NAME = "subscriptions";
    
    @Autowired
    private Firestore firestore;

    @Override
    public Subscription save(Subscription subscription) {
        try {
            if (subscription.getId() == null) {
                subscription.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(subscription.getId());
            docRef.set(convertToMap(subscription)).get();
            
            return subscription;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save subscription", e);
        }
    }

    @Override
    public Optional<Subscription> findById(String id) {
        try {
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get();
                    
            return doc.exists() ? Optional.of(convertToSubscription(doc)) : Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscription by id", e);
        }
    }

    @Override
    public Optional<Subscription> findByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToSubscription(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscription by userId", e);
        }
    }

    @Override
    public Optional<Subscription> findActiveByUserId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", Subscription.SubscriptionStatus.ACTIVE.name())
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToSubscription(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active subscription by userId", e);
        }
    }

    @Override
    public List<Subscription> findBySubscriptionType(String subscriptionType) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("plan", subscriptionType)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscriptions by type", e);
        }
    }

    @Override
    public List<Subscription> findByUserIdAndStatus(String userId, Object status) {
        try {
            String statusStr = status instanceof Subscription.SubscriptionStatus ? 
                    ((Subscription.SubscriptionStatus) status).name() : status.toString();
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("status", statusStr)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscriptions by userId and status", e);
        }
    }

    @Override
    public List<Subscription> findByUserIdOrderByCreatedAtDesc(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscriptions by userId ordered by createdAt", e);
        }
    }

    @Override
    public Optional<Subscription> findByExternalSubscriptionId(String externalId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("externalSubscriptionId", externalId)
                    .limit(1)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().isEmpty() ? 
                    Optional.empty() : 
                    Optional.of(convertToSubscription(querySnapshot.getDocuments().get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find subscription by external id", e);
        }
    }

    @Override
    public List<Subscription> findActiveSubscriptions() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", Subscription.SubscriptionStatus.ACTIVE.name())
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find active subscriptions", e);
        }
    }

    @Override
    public List<Subscription> findExpiredSubscriptions() {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", Subscription.SubscriptionStatus.EXPIRED.name())
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find expired subscriptions", e);
        }
    }

    @Override
    public List<Subscription> findExpiringSubscriptions(int days) {
        try {
            LocalDateTime expiryDate = LocalDateTime.now().plusDays(days);
            Timestamp expiryTimestamp = Timestamp.of(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("status", Subscription.SubscriptionStatus.ACTIVE.name())
                    .whereLessThan("endDate", expiryTimestamp)
                    .get()
                    .get();
                    
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToSubscription)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find expiring subscriptions", e);
        }
    }

    @Override
    public Subscription updateStatus(String id, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update subscription status", e);
        }
    }

    @Override
    public Subscription renewSubscription(String id, int months) {
        try {
            Optional<Subscription> subscriptionOpt = findById(id);
            if (subscriptionOpt.isPresent()) {
                Subscription subscription = subscriptionOpt.get();
                
                // Extend the end date by the specified months
                LocalDateTime newEndDate = subscription.getEndDate() != null ? 
                        subscription.getEndDate().plusMonths(months) : 
                        LocalDateTime.now().plusMonths(months);
                
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
                
                Map<String, Object> updates = new HashMap<>();
                updates.put("endDate", Timestamp.of(Date.from(newEndDate.atZone(ZoneId.systemDefault()).toInstant())));
                updates.put("status", Subscription.SubscriptionStatus.ACTIVE.name());
                updates.put("updatedAt", Timestamp.now());
                
                docRef.update(updates).get();
                
                return findById(id).orElse(null);
            }
            return null;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to renew subscription", e);
        }
    }

    @Override
    public Subscription cancelSubscription(String id) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", Subscription.SubscriptionStatus.CANCELLED.name());
            updates.put("cancelledAt", Timestamp.now());
            updates.put("updatedAt", Timestamp.now());
            updates.put("autoRenew", false);
            
            docRef.update(updates).get();
            
            return findById(id).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to cancel subscription", e);
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            firestore.collection(COLLECTION_NAME).document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete subscription", e);
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
            throw new RuntimeException("Failed to delete subscriptions by userId", e);
        }
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(Subscription subscription) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", subscription.getId());
        map.put("userId", subscription.getUserId());
        map.put("plan", subscription.getPlan() != null ? subscription.getPlan().name() : null);
        map.put("status", subscription.getStatus() != null ? subscription.getStatus().name() : null);
        map.put("monthlyPrice", subscription.getMonthlyPrice() != null ? subscription.getMonthlyPrice().toString() : null);
        map.put("durationMonths", subscription.getDurationMonths());
        map.put("totalAmount", subscription.getTotalAmount() != null ? subscription.getTotalAmount().toString() : null);
        map.put("paymentMethod", subscription.getPaymentMethod());
        map.put("externalSubscriptionId", subscription.getExternalSubscriptionId());
        map.put("autoRenew", subscription.isAutoRenew());
        map.put("cancellationReason", subscription.getCancellationReason());
        
        // Convert LocalDateTime to Timestamp
        if (subscription.getStartDate() != null) {
            map.put("startDate", Timestamp.of(Date.from(subscription.getStartDate().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (subscription.getEndDate() != null) {
            map.put("endDate", Timestamp.of(Date.from(subscription.getEndDate().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (subscription.getCreatedAt() != null) {
            map.put("createdAt", Timestamp.of(Date.from(subscription.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (subscription.getUpdatedAt() != null) {
            map.put("updatedAt", Timestamp.of(Date.from(subscription.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (subscription.getCancelledAt() != null) {
            map.put("cancelledAt", Timestamp.of(Date.from(subscription.getCancelledAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        
        return map;
    }

    private Subscription convertToSubscription(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        Subscription subscription = new Subscription();
        subscription.setId(doc.getId());
        subscription.setUserId((String) data.get("userId"));
        subscription.setDurationMonths((Integer) data.get("durationMonths"));
        subscription.setPaymentMethod((String) data.get("paymentMethod"));
        subscription.setExternalSubscriptionId((String) data.get("externalSubscriptionId"));
        subscription.setAutoRenew((Boolean) data.get("autoRenew"));
        subscription.setCancellationReason((String) data.get("cancellationReason"));
        
        // Convert enum values
        if (data.get("plan") != null) {
            subscription.setPlan(Subscription.SubscriptionPlan.valueOf((String) data.get("plan")));
        }
        if (data.get("status") != null) {
            subscription.setStatus(Subscription.SubscriptionStatus.valueOf((String) data.get("status")));
        }
        
        // Convert BigDecimal values
        if (data.get("monthlyPrice") != null) {
            subscription.setMonthlyPrice(new BigDecimal((String) data.get("monthlyPrice")));
        }
        if (data.get("totalAmount") != null) {
            subscription.setTotalAmount(new BigDecimal((String) data.get("totalAmount")));
        }
        
        // Convert Timestamp to LocalDateTime
        if (data.get("startDate") instanceof Timestamp) {
            subscription.setStartDate(((Timestamp) data.get("startDate")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("endDate") instanceof Timestamp) {
            subscription.setEndDate(((Timestamp) data.get("endDate")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("createdAt") instanceof Timestamp) {
            subscription.setCreatedAt(((Timestamp) data.get("createdAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("updatedAt") instanceof Timestamp) {
            subscription.setUpdatedAt(((Timestamp) data.get("updatedAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("cancelledAt") instanceof Timestamp) {
            subscription.setCancelledAt(((Timestamp) data.get("cancelledAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return subscription;
    }
}