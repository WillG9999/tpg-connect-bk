package com.tpg.connect.client.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing the response from Apple Push Notification service (APNs)
 * 
 * Handles both successful delivery confirmations and error responses
 * with detailed error information for debugging and monitoring.
 */
public class ApnsResponse {
    
    /**
     * Device token that the notification was sent to
     */
    private String deviceToken;
    
    /**
     * Whether the notification was successfully sent
     */
    private boolean success;
    
    /**
     * APNs ID returned by Apple (for successful sends)
     */
    private String apnsId;
    
    /**
     * HTTP status code from APNs
     */
    private int statusCode;
    
    /**
     * Error code if the send failed
     */
    private String errorCode;
    
    /**
     * Human-readable error reason
     */
    private String errorReason;
    
    /**
     * Additional error details from APNs
     */
    private String errorDetails;
    
    /**
     * Timestamp when the response was received
     */
    private LocalDateTime timestamp;
    
    /**
     * Whether this token should be considered invalid
     */
    private boolean invalidToken;
    
    /**
     * Whether this is a temporary error that could be retried
     */
    private boolean retryable;
    
    // Constructors
    
    public ApnsResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApnsResponse(String deviceToken, boolean success) {
        this();
        this.deviceToken = deviceToken;
        this.success = success;
    }
    
    // Factory methods for common response types
    
    public static ApnsResponse success(String deviceToken, String apnsId) {
        ApnsResponse response = new ApnsResponse(deviceToken, true);
        response.apnsId = apnsId;
        response.statusCode = 200;
        return response;
    }
    
    public static ApnsResponse error(String deviceToken, String errorCode, String errorReason) {
        ApnsResponse response = new ApnsResponse(deviceToken, false);
        response.errorCode = errorCode;
        response.errorReason = errorReason;
        response.determineErrorType();
        return response;
    }
    
    public static ApnsResponse httpError(String deviceToken, int statusCode, String errorDetails) {
        ApnsResponse response = new ApnsResponse(deviceToken, false);
        response.statusCode = statusCode;
        response.errorCode = "HTTP_" + statusCode;
        response.errorDetails = errorDetails;
        response.mapHttpStatusToError(statusCode);
        return response;
    }
    
    public static ApnsResponse invalidToken(String deviceToken, String reason) {
        ApnsResponse response = error(deviceToken, "INVALID_TOKEN", reason);
        response.invalidToken = true;
        response.retryable = false;
        return response;
    }
    
    public static ApnsResponse temporaryError(String deviceToken, String reason) {
        ApnsResponse response = error(deviceToken, "TEMPORARY_ERROR", reason);
        response.retryable = true;
        return response;
    }
    
    // Getters and Setters
    
    public String getDeviceToken() {
        return deviceToken;
    }
    
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getApnsId() {
        return apnsId;
    }
    
    public void setApnsId(String apnsId) {
        this.apnsId = apnsId;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorReason() {
        return errorReason;
    }
    
    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isInvalidToken() {
        return invalidToken;
    }
    
    public void setInvalidToken(boolean invalidToken) {
        this.invalidToken = invalidToken;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
    
    // Utility methods
    
    /**
     * Determine error type based on error code
     */
    private void determineErrorType() {
        if (errorCode == null) return;
        
        switch (errorCode) {
            case "BadDeviceToken":
            case "Unregistered":
            case "DeviceTokenNotForTopic":
                invalidToken = true;
                retryable = false;
                break;
                
            case "InternalServerError":
            case "ServiceUnavailable":
            case "TooManyRequests":
                invalidToken = false;
                retryable = true;
                break;
                
            case "BadMessageId":
            case "BadExpirationDate":
            case "BadTopic":
            case "BadCertificate":
            case "BadCertificateEnvironment":
            case "ExpiredProviderToken":
            case "Forbidden":
            case "InvalidProviderToken":
            case "MissingProviderToken":
            case "BadPath":
            case "MethodNotAllowed":
                invalidToken = false;
                retryable = false;
                break;
                
            default:
                invalidToken = false;
                retryable = false;
        }
    }
    
    /**
     * Map HTTP status codes to error information
     */
    private void mapHttpStatusToError(int statusCode) {
        switch (statusCode) {
            case 400:
                errorReason = "Bad request - malformed JSON or missing required fields";
                retryable = false;
                break;
            case 403:
                errorReason = "Authentication error - invalid certificate or token";
                retryable = false;
                break;
            case 405:
                errorReason = "Method not allowed - must use POST";
                retryable = false;
                break;
            case 410:
                errorReason = "Device token is no longer active";
                invalidToken = true;
                retryable = false;
                break;
            case 413:
                errorReason = "Payload too large - maximum size is 4KB";
                retryable = false;
                break;
            case 429:
                errorReason = "Too many requests - rate limited";
                retryable = true;
                break;
            case 500:
                errorReason = "Internal server error";
                retryable = true;
                break;
            case 502:
                errorReason = "Bad gateway";
                retryable = true;
                break;
            case 503:
                errorReason = "Service unavailable";
                retryable = true;
                break;
            default:
                errorReason = "Unknown error: HTTP " + statusCode;
                retryable = false;
        }
    }
    
    /**
     * Get a short summary of the response
     */
    public String getSummary() {
        if (success) {
            return "SUCCESS: " + (apnsId != null ? apnsId : "sent");
        } else {
            return "ERROR: " + errorCode + " - " + errorReason;
        }
    }
    
    /**
     * Check if this response indicates the device token should be removed
     */
    public boolean shouldRemoveToken() {
        return invalidToken || statusCode == 410;
    }
    
    /**
     * Check if this error could be retried later
     */
    public boolean canRetry() {
        return !success && retryable;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApnsResponse that = (ApnsResponse) o;
        return success == that.success &&
               statusCode == that.statusCode &&
               invalidToken == that.invalidToken &&
               retryable == that.retryable &&
               Objects.equals(deviceToken, that.deviceToken) &&
               Objects.equals(apnsId, that.apnsId) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(errorReason, that.errorReason);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceToken, success, apnsId, statusCode, errorCode, 
                          errorReason, invalidToken, retryable);
    }
    
    @Override
    public String toString() {
        return "ApnsResponse{" +
                "deviceToken='" + maskToken(deviceToken) + '\'' +
                ", success=" + success +
                ", apnsId='" + apnsId + '\'' +
                ", statusCode=" + statusCode +
                ", errorCode='" + errorCode + '\'' +
                ", errorReason='" + errorReason + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", timestamp=" + timestamp +
                ", invalidToken=" + invalidToken +
                ", retryable=" + retryable +
                '}';
    }
    
    /**
     * Mask device token for safe logging
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}