package com.tpg.connect.controllers.premium;

import com.tpg.connect.model.dto.SubscriptionRequest;
import com.tpg.connect.model.premium.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "Premium Subscription", description = "Premium subscription management - £14.99/month for 4, 6, or 12 month intervals")
public interface SubscriptionControllerApi {

    @Operation(summary = "Get subscription plans", description = "Get available subscription plans with pricing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription plans retrieved successfully")
    })
    @GetMapping("/plans")
    ResponseEntity<Map<String, Object>> getSubscriptionPlans();

    @Operation(summary = "Get current subscription", description = "Get user's current subscription details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Current subscription retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No active subscription found")
    })
    @GetMapping("/current")
    ResponseEntity<Subscription> getCurrentSubscription(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Create subscription", description = "Create a new premium subscription (4, 6, or 12 months)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid subscription request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/create")
    ResponseEntity<Map<String, Object>> createSubscription(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Subscription details", required = true)
        @Valid @RequestBody SubscriptionRequest request
    );

    @Operation(summary = "Cancel subscription", description = "Cancel current subscription (effective at end of billing period)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "No active subscription to cancel"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/cancel")
    ResponseEntity<Map<String, Object>> cancelSubscription(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Cancellation reason (optional)")
        @RequestParam(required = false) String reason
    );

    @Operation(summary = "Get subscription history", description = "Get user's subscription history")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/history")
    ResponseEntity<List<Subscription>> getSubscriptionHistory(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Get premium status", description = "Check if user has active premium subscription and feature access")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Premium status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/status")
    ResponseEntity<Map<String, Object>> getPremiumStatus(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Restore subscription", description = "Restore subscription from App Store/Google Play receipt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription restored successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid receipt data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/restore")
    ResponseEntity<Map<String, Object>> restoreSubscription(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "App Store/Google Play receipt data", required = true)
        @RequestParam String receiptData
    );

    @Operation(summary = "Process payment webhook", description = "Handle payment provider webhooks (Stripe, Apple, Google)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook data")
    })
    @PostMapping("/webhook")
    ResponseEntity<Map<String, Object>> processWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String signature
    );
}