package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    @NotBlank(message = "Subscription plan is required")
    @Pattern(regexp = "^(PREMIUM_4_MONTHS|PREMIUM_6_MONTHS|PREMIUM_12_MONTHS)$", 
             message = "Plan must be PREMIUM_4_MONTHS, PREMIUM_6_MONTHS, or PREMIUM_12_MONTHS")
    private String plan;
    
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(stripe|apple_pay|google_pay)$", 
             message = "Payment method must be stripe, apple_pay, or google_pay")
    private String paymentMethod;
    
    @NotNull(message = "Auto-renew preference is required")
    private Boolean autoRenew;
    
    // For Stripe payments
    private String paymentMethodId;
    
    // For mobile payments
    private String receiptData;
    
    // Optional promo code
    private String promoCode;
}