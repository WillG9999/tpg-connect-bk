package com.tpg.connect.model.premium;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subscriptions")
public class Subscription {
    @Id
    private String id;
    private String userId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private BigDecimal monthlyPrice; // £14.99
    private int durationMonths; // 4, 6, or 12 only
    private BigDecimal totalAmount; // monthlyPrice * durationMonths
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentMethod; // "stripe", "apple_pay", "google_pay"
    private String externalSubscriptionId; // Stripe subscription ID
    private boolean autoRenew;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    public enum SubscriptionPlan {
        PREMIUM_4_MONTHS(4, new BigDecimal("14.99")),   // £59.96 total
        PREMIUM_6_MONTHS(6, new BigDecimal("14.99")),   // £89.94 total
        PREMIUM_12_MONTHS(12, new BigDecimal("14.99")); // £179.88 total
        
        private final int months;
        private final BigDecimal monthlyPrice;
        
        SubscriptionPlan(int months, BigDecimal monthlyPrice) {
            this.months = months;
            this.monthlyPrice = monthlyPrice;
        }
        
        public int getMonths() { return months; }
        public BigDecimal getMonthlyPrice() { return monthlyPrice; }
        public BigDecimal getTotalPrice() { return monthlyPrice.multiply(new BigDecimal(months)); }
    }
    
    public enum SubscriptionStatus {
        PENDING,     // Payment pending
        ACTIVE,      // Currently active - user can use the app
        EXPIRED,     // Subscription ended - user loses access
        CANCELLED,   // User cancelled - effective at end date
        SUSPENDED,   // Admin suspended
        REFUNDED     // Payment refunded
    }
    
    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE && 
               LocalDateTime.now().isBefore(endDate);
    }
    
    public boolean hasAccess() {
        return isActive();
    }
}