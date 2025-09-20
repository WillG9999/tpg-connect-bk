package com.tpg.connect.controllers.premium;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.dto.SubscriptionRequest;
import com.tpg.connect.model.premium.Subscription;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.services.SubscriptionService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/premium/subscription")
public class SubscriptionController extends BaseController implements SubscriptionControllerApi {

    @Autowired
    private AuthService authService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Map<String, Object>> getSubscriptionPlans() {
        try {
            Map<String, Object> plans = subscriptionService.getAvailablePlans();
            return successResponse(plans);
        } catch (Exception e) {
            return errorResponse("Failed to get subscription plans: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Subscription> getCurrentSubscription(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Subscription subscription = subscriptionService.getCurrentSubscription(userId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> createSubscription(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SubscriptionRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> result = subscriptionService.createSubscription(userId, request);
            return successResponse(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to create subscription: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String reason) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            subscriptionService.cancelSubscription(userId, reason);
            return successResponse(Map.of(
                "message", "Subscription cancelled successfully",
                "cancellationEffective", "End of current billing period"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to cancel subscription: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<List<Subscription>> getSubscriptionHistory(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            List<Subscription> history = subscriptionService.getSubscriptionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getPremiumStatus(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> status = subscriptionService.getPremiumStatus(userId);
            return successResponse(status);
        } catch (Exception e) {
            return errorResponse("Failed to get premium status: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> restoreSubscription(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String receiptData) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> result = subscriptionService.restoreSubscription(userId, receiptData);
            return successResponse(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Failed to restore subscription: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> processWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        try {
            subscriptionService.processPaymentWebhook(payload, signature);
            return ResponseEntity.ok(Map.of("received", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Webhook processing failed"));
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.validateToken(token)) {
            return null;
        }

        String username = authService.extractUsername(token);
        return getUserIdFromUsername(username);
    }

    private String getUserIdFromUsername(String username) {
        switch (username) {
            case "admin":
                return "1";
            case "user":
                return "2";
            case "alex":
                return "user_123";
            default:
                return null;
        }
    }

    protected ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", message));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("success", false, "message", message));
    }
}