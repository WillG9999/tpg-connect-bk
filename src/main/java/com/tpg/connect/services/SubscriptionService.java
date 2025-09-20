package com.tpg.connect.services;

import com.tpg.connect.repository.SubscriptionRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.model.dto.SubscriptionRequest;
import com.tpg.connect.model.premium.Subscription;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentService paymentService;

    public Map<String, Object> getAvailablePlans() {
        Map<String, Object> plans = new HashMap<>();
        
        for (Subscription.SubscriptionPlan plan : Subscription.SubscriptionPlan.values()) {
            Map<String, Object> planDetails = new HashMap<>();
            planDetails.put("duration", plan.getMonths());
            planDetails.put("monthlyPrice", plan.getMonthlyPrice());
            planDetails.put("totalPrice", plan.getTotalPrice());
            planDetails.put("savings", calculateSavings(plan.getMonths()));
            planDetails.put("currency", "GBP");
            planDetails.put("displayName", getPlanDisplayName(plan));
            
            plans.put(plan.name(), planDetails);
        }
        
        return Map.of("plans", plans, "currency", "GBP", "monthlyPrice", "14.99");
    }

    @Cacheable(value = "currentSubscription", key = "#userId")
    public Subscription getCurrentSubscription(String userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, Subscription.SubscriptionStatus.ACTIVE).stream().findFirst().orElse(null);
    }

    public boolean hasActiveSubscription(String userId) {
        Subscription subscription = getCurrentSubscription(userId);
        return subscription != null && subscription.hasAccess();
    }

    @CacheEvict(value = "currentSubscription", key = "#userId")
    public Map<String, Object> createSubscription(String userId, SubscriptionRequest request) {
        // Validate user exists
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Check if user already has active subscription
        Subscription existingSubscription = getCurrentSubscription(userId);
        if (existingSubscription != null && existingSubscription.hasAccess()) {
            throw new IllegalArgumentException("User already has an active subscription");
        }

        // Validate subscription plan
        Subscription.SubscriptionPlan plan;
        try {
            plan = Subscription.SubscriptionPlan.valueOf(request.getPlan());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subscription plan: " + request.getPlan());
        }

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID().toString());
        subscription.setUserId(userId);
        subscription.setPlan(plan);
        subscription.setStatus(Subscription.SubscriptionStatus.PENDING);
        subscription.setMonthlyPrice(plan.getMonthlyPrice());
        subscription.setDurationMonths(plan.getMonths());
        subscription.setTotalAmount(plan.getTotalPrice());
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setAutoRenew(request.getAutoRenew());

        // Process payment based on method
        Map<String, Object> paymentResult;
        switch (request.getPaymentMethod()) {
            case "stripe":
                paymentResult = paymentService.processStripePayment(subscription, request.getPaymentMethodId());
                break;
            case "apple_pay":
                paymentResult = paymentService.processApplePayment(subscription, request.getReceiptData());
                break;
            case "google_pay":
                paymentResult = paymentService.processGooglePayment(subscription, request.getReceiptData());
                break;
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + request.getPaymentMethod());
        }

        if ((Boolean) paymentResult.get("success")) {
            // Payment successful - activate subscription
            subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setEndDate(LocalDateTime.now().plusMonths(plan.getMonths()));
            subscription.setExternalSubscriptionId((String) paymentResult.get("externalId"));
            
            subscriptionRepository.save(subscription);

            // Send confirmation email
            emailService.sendSubscriptionConfirmation(user.getEmail(), subscription);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Subscription created successfully");
            result.put("subscription", subscription);
            result.put("accessGranted", true);
            
            return result;
        } else {
            // Payment failed
            subscription.setStatus(Subscription.SubscriptionStatus.PENDING);
            subscriptionRepository.save(subscription);
            
            throw new IllegalArgumentException("Payment failed: " + paymentResult.get("error"));
        }
    }

    @CacheEvict(value = "currentSubscription", key = "#userId")
    public void cancelSubscription(String userId, String reason) {
        Subscription subscription = getCurrentSubscription(userId);
        if (subscription == null || !subscription.hasAccess()) {
            throw new IllegalArgumentException("No active subscription to cancel");
        }

        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscription.setAutoRenew(false);

        subscriptionRepository.save(subscription);

        // Cancel external subscription
        paymentService.cancelExternalSubscription(subscription.getExternalSubscriptionId(), 
                                                 subscription.getPaymentMethod());

        // Send cancellation confirmation
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            emailService.sendSubscriptionCancellation(user.getEmail(), subscription);
        }
    }

    public List<Subscription> getSubscriptionHistory(String userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Object> getPremiumStatus(String userId) {
        Subscription subscription = getCurrentSubscription(userId);
        boolean hasAccess = subscription != null && subscription.hasAccess();
        
        Map<String, Object> status = new HashMap<>();
        status.put("hasActiveSubscription", hasAccess);
        status.put("subscriptionStatus", hasAccess ? "ACTIVE" : "NONE");
        
        if (hasAccess) {
            status.put("plan", subscription.getPlan().name());
            status.put("startDate", subscription.getStartDate());
            status.put("endDate", subscription.getEndDate());
            status.put("daysRemaining", calculateDaysRemaining(subscription.getEndDate()));
            status.put("autoRenew", subscription.isAutoRenew());
        }
        
        return status;
    }

    public Map<String, Object> restoreSubscription(String userId, String receiptData) {
        // Validate receipt with app store
        Map<String, Object> validationResult = paymentService.validateAppStoreReceipt(receiptData);
        
        if (!(Boolean) validationResult.get("valid")) {
            throw new IllegalArgumentException("Invalid receipt data");
        }

        // Find or create subscription based on receipt
        String externalId = (String) validationResult.get("transactionId");
        Subscription existingSubscription = subscriptionRepository.findByExternalSubscriptionId(externalId).orElse(null);
        
        if (existingSubscription != null) {
            // Update existing subscription
            existingSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
            existingSubscription.setUpdatedAt(LocalDateTime.now());
            subscriptionRepository.save(existingSubscription);
            
            // Clear cache
            clearSubscriptionCache(userId);
            
            return Map.of("success", true, "message", "Subscription restored successfully", 
                         "subscription", existingSubscription);
        } else {
            throw new IllegalArgumentException("No subscription found for this receipt");
        }
    }

    public void processPaymentWebhook(String payload, String signature) {
        // Validate webhook signature
        if (!paymentService.validateWebhookSignature(payload, signature)) {
            throw new IllegalArgumentException("Invalid webhook signature");
        }

        // Process webhook based on payment provider
        Map<String, Object> webhookData = paymentService.parseWebhookPayload(payload);
        String eventType = (String) webhookData.get("type");
        String subscriptionId = (String) webhookData.get("subscriptionId");

        Subscription subscription = subscriptionRepository.findByExternalSubscriptionId(subscriptionId).orElse(null);
        if (subscription == null) {
            return; // Subscription not found, ignore
        }

        switch (eventType) {
            case "subscription_payment_succeeded":
                handleSuccessfulPayment(subscription);
                break;
            case "subscription_payment_failed":
                handleFailedPayment(subscription);
                break;
            case "subscription_cancelled":
                handleSubscriptionCancelled(subscription);
                break;
            case "subscription_expired":
                handleSubscriptionExpired(subscription);
                break;
        }
    }

    private void handleSuccessfulPayment(Subscription subscription) {
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        clearSubscriptionCache(subscription.getUserId());
    }

    private void handleFailedPayment(Subscription subscription) {
        subscription.setStatus(Subscription.SubscriptionStatus.SUSPENDED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        clearSubscriptionCache(subscription.getUserId());
        
        // Send payment failed email
        User user = userRepository.findById(subscription.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendPaymentFailedNotification(user.getEmail(), subscription);
        }
    }

    private void handleSubscriptionCancelled(Subscription subscription) {
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        clearSubscriptionCache(subscription.getUserId());
    }

    private void handleSubscriptionExpired(Subscription subscription) {
        subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
        
        clearSubscriptionCache(subscription.getUserId());
        
        // Send subscription expired email
        User user = userRepository.findById(subscription.getUserId()).orElse(null);
        if (user != null) {
            emailService.sendSubscriptionExpired(user.getEmail(), subscription);
        }
    }

    private BigDecimal calculateSavings(int months) {
        // No savings - same monthly price for all plans
        return BigDecimal.ZERO;
    }

    private String getPlanDisplayName(Subscription.SubscriptionPlan plan) {
        switch (plan) {
            case PREMIUM_4_MONTHS:
                return "4-Month Premium";
            case PREMIUM_6_MONTHS:
                return "6-Month Premium";
            case PREMIUM_12_MONTHS:
                return "12-Month Premium";
            default:
                return plan.name();
        }
    }

    private long calculateDaysRemaining(LocalDateTime endDate) {
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }

    @CacheEvict(value = "currentSubscription", key = "#userId")
    private void clearSubscriptionCache(String userId) {
        // Cache cleared by annotation
    }
}