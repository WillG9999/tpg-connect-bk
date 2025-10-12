package com.tpg.connect.client;

import com.tpg.connect.client.config.ApnsClientConfig;
import com.tpg.connect.client.models.ApnsNotification;
import com.tpg.connect.client.models.ApnsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * iOS Push Notification Client
 * 
 * Handles direct communication with Apple Push Notification service (APNs)
 * for sending push notifications to iOS devices.
 */
@Component
public class IosClient {
    
    private static final Logger logger = LoggerFactory.getLogger(IosClient.class);
    
    private final ApnsClientConfig config;
    private final HttpClient httpClient;
    
    @Autowired
    public IosClient(ApnsClientConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(config.getConnectionTimeout()))
            .build();
        
        logger.info("🍎 iOS Client initialized for environment: {}", config.getEnvironment());
    }
    
    /**
     * Send a push notification to a single iOS device
     * 
     * @param deviceToken The iOS device token
     * @param notification The notification to send
     * @return ApnsResponse containing the result
     */
    public CompletableFuture<ApnsResponse> sendNotification(String deviceToken, ApnsNotification notification) {
        logger.debug("🍎 Sending iOS notification to device: {}", maskToken(deviceToken));
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = buildApnsRequest(deviceToken, notification);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                ApnsResponse apnsResponse = processApnsResponse(response, deviceToken);
                logNotificationResult(deviceToken, apnsResponse);
                
                return apnsResponse;
                
            } catch (Exception e) {
                logger.error("🍎 Failed to send iOS notification to device: {}", maskToken(deviceToken), e);
                return ApnsResponse.error(deviceToken, "SEND_FAILED", e.getMessage());
            }
        });
    }
    
    /**
     * Send push notifications to multiple iOS devices
     * 
     * @param deviceTokens List of iOS device tokens
     * @param notification The notification to send
     * @return List of ApnsResponse results
     */
    public CompletableFuture<List<ApnsResponse>> sendBatchNotification(List<String> deviceTokens, ApnsNotification notification) {
        logger.info("🍎 Sending iOS notification batch to {} devices", deviceTokens.size());
        
        List<CompletableFuture<ApnsResponse>> futures = deviceTokens.stream()
            .map(token -> sendNotification(token, notification))
            .toList();
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }
    
    /**
     * Validate an iOS device token format
     * 
     * @param deviceToken The token to validate
     * @return true if token format is valid
     */
    public boolean validateDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            return false;
        }
        
        // Remove any whitespace and check length (64 hex characters)
        String cleanToken = deviceToken.replaceAll("\\s", "");
        return cleanToken.matches("^[0-9a-fA-F]{64}$");
    }
    
    /**
     * Build HTTP request for APNs
     */
    private HttpRequest buildApnsRequest(String deviceToken, ApnsNotification notification) {
        String apnsUrl = config.getApnsUrl() + "/3/device/" + deviceToken;
        
        return HttpRequest.newBuilder()
            .uri(URI.create(apnsUrl))
            .header("Authorization", "bearer " + config.getAuthToken())
            .header("apns-topic", config.getBundleId())
            .header("apns-push-type", notification.getPushType())
            .header("apns-priority", String.valueOf(notification.getPriority()))
            .header("apns-expiration", String.valueOf(notification.getExpiration()))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(notification.toJson()))
            .timeout(Duration.ofSeconds(config.getRequestTimeout()))
            .build();
    }
    
    /**
     * Process APNs HTTP response
     */
    private ApnsResponse processApnsResponse(HttpResponse<String> response, String deviceToken) {
        int statusCode = response.statusCode();
        String responseBody = response.body();
        String apnsId = response.headers().firstValue("apns-id").orElse(null);
        
        if (statusCode == 200) {
            return ApnsResponse.success(deviceToken, apnsId);
        } else {
            return ApnsResponse.error(deviceToken, "HTTP_" + statusCode, responseBody);
        }
    }
    
    /**
     * Log notification result
     */
    private void logNotificationResult(String deviceToken, ApnsResponse response) {
        if (response.isSuccess()) {
            logger.info("🍎✅ iOS notification sent successfully to device: {}, APNs ID: {}", 
                maskToken(deviceToken), response.getApnsId());
        } else {
            logger.warn("🍎❌ iOS notification failed to device: {}, Error: {}, Reason: {}", 
                maskToken(deviceToken), response.getErrorCode(), response.getErrorReason());
        }
    }
    
    /**
     * Mask device token for logging (show first/last 4 characters)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
    
    /**
     * Get client configuration
     */
    public ApnsClientConfig getConfig() {
        return config;
    }
    
    /**
     * Check if client is configured for production environment
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(config.getEnvironment());
    }
}