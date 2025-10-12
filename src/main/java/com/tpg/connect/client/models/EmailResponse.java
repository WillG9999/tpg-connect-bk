package com.tpg.connect.client.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model representing the response from email sending
 * 
 * Handles both successful delivery confirmations and error responses
 * with detailed error information for debugging and monitoring.
 */
public class EmailResponse {
    
    /**
     * Recipient email address
     */
    private String recipientEmail;
    
    /**
     * Whether the email was successfully sent
     */
    private boolean success;
    
    /**
     * Message ID returned by email provider
     */
    private String messageId;
    
    /**
     * Email provider response code
     */
    private String responseCode;
    
    /**
     * Error code if the send failed
     */
    private String errorCode;
    
    /**
     * Human-readable error reason
     */
    private String errorReason;
    
    /**
     * Additional error details from provider
     */
    private String errorDetails;
    
    /**
     * Timestamp when the response was received
     */
    private LocalDateTime timestamp;
    
    /**
     * Whether this email address should be considered invalid
     */
    private boolean invalidEmail;
    
    /**
     * Whether this is a temporary error that could be retried
     */
    private boolean retryable;
    
    /**
     * Delivery status (queued, delivered, bounced, etc.)
     */
    private String deliveryStatus;
    
    /**
     * Provider-specific metadata
     */
    private String providerMetadata;
    
    /**
     * Number of retry attempts made
     */
    private int retryCount;
    
    /**
     * Bounce reason (if bounced)
     */
    private String bounceReason;
    
    /**
     * Bounce type (hard, soft, complaint)
     */
    private String bounceType;
    
    // Constructors
    
    public EmailResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public EmailResponse(String recipientEmail, boolean success) {
        this();
        this.recipientEmail = recipientEmail;
        this.success = success;
    }
    
    // Factory methods for common response types
    
    public static EmailResponse success(String recipientEmail, String messageId) {
        EmailResponse response = new EmailResponse(recipientEmail, true);
        response.messageId = messageId;
        response.deliveryStatus = "sent";
        response.responseCode = "200";
        return response;
    }
    
    public static EmailResponse queued(String recipientEmail, String messageId) {
        EmailResponse response = new EmailResponse(recipientEmail, true);
        response.messageId = messageId;
        response.deliveryStatus = "queued";
        response.responseCode = "202";
        return response;
    }
    
    public static EmailResponse error(String recipientEmail, String errorCode, String errorReason) {
        EmailResponse response = new EmailResponse(recipientEmail, false);
        response.errorCode = errorCode;
        response.errorReason = errorReason;
        response.deliveryStatus = "failed";
        response.determineErrorType();
        return response;
    }
    
    public static EmailResponse invalidEmail(String recipientEmail, String reason) {
        EmailResponse response = error(recipientEmail, "INVALID_EMAIL", reason);
        response.invalidEmail = true;
        response.retryable = false;
        response.bounceType = "hard";
        return response;
    }
    
    public static EmailResponse temporaryError(String recipientEmail, String reason) {
        EmailResponse response = error(recipientEmail, "TEMPORARY_ERROR", reason);
        response.retryable = true;
        response.bounceType = "soft";
        return response;
    }
    
    public static EmailResponse bounced(String recipientEmail, String bounceReason, String bounceType) {
        EmailResponse response = error(recipientEmail, "BOUNCED", bounceReason);
        response.bounceReason = bounceReason;
        response.bounceType = bounceType;
        response.deliveryStatus = "bounced";
        response.invalidEmail = "hard".equals(bounceType);
        response.retryable = "soft".equals(bounceType);
        return response;
    }
    
    public static EmailResponse blocked(String recipientEmail, String reason) {
        EmailResponse response = error(recipientEmail, "BLOCKED", reason);
        response.deliveryStatus = "blocked";
        response.retryable = false;
        return response;
    }
    
    // Getters and Setters
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getResponseCode() {
        return responseCode;
    }
    
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
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
    
    public boolean isInvalidEmail() {
        return invalidEmail;
    }
    
    public void setInvalidEmail(boolean invalidEmail) {
        this.invalidEmail = invalidEmail;
    }
    
    public boolean isRetryable() {
        return retryable;
    }
    
    public void setRetryable(boolean retryable) {
        this.retryable = retryable;
    }
    
    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    
    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
    
    public String getProviderMetadata() {
        return providerMetadata;
    }
    
    public void setProviderMetadata(String providerMetadata) {
        this.providerMetadata = providerMetadata;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public String getBounceReason() {
        return bounceReason;
    }
    
    public void setBounceReason(String bounceReason) {
        this.bounceReason = bounceReason;
    }
    
    public String getBounceType() {
        return bounceType;
    }
    
    public void setBounceType(String bounceType) {
        this.bounceType = bounceType;
    }
    
    // Utility methods
    
    /**
     * Determine error type based on error code
     */
    private void determineErrorType() {
        if (errorCode == null) return;
        
        switch (errorCode.toUpperCase()) {
            case "INVALID_EMAIL":
            case "MAILBOX_FULL":
            case "USER_UNKNOWN":
            case "DOMAIN_NOT_FOUND":
            case "PERMANENT_FAILURE":
                invalidEmail = true;
                retryable = false;
                bounceType = "hard";
                break;
                
            case "TEMPORARY_ERROR":
            case "RATE_LIMITED":
            case "SERVICE_UNAVAILABLE":
            case "CONNECTION_TIMEOUT":
            case "TEMPORARY_FAILURE":
                invalidEmail = false;
                retryable = true;
                bounceType = "soft";
                break;
                
            case "BLOCKED":
            case "BLACKLISTED":
            case "SPAM_DETECTED":
            case "POLICY_VIOLATION":
                invalidEmail = false;
                retryable = false;
                break;
                
            case "AUTHENTICATION_FAILED":
            case "INSUFFICIENT_CREDITS":
            case "INVALID_API_KEY":
                invalidEmail = false;
                retryable = false;
                break;
                
            default:
                invalidEmail = false;
                retryable = false;
        }
    }
    
    /**
     * Get a short summary of the response
     */
    public String getSummary() {
        if (success) {
            return "SUCCESS: " + (deliveryStatus != null ? deliveryStatus.toUpperCase() : "SENT") +
                   (messageId != null ? " (ID: " + messageId + ")" : "");
        } else {
            return "ERROR: " + errorCode + " - " + errorReason;
        }
    }
    
    /**
     * Check if this response indicates the email address should be removed
     */
    public boolean shouldRemoveEmail() {
        return invalidEmail || "hard".equals(bounceType);
    }
    
    /**
     * Check if this error could be retried later
     */
    public boolean canRetry() {
        return !success && retryable;
    }
    
    /**
     * Check if email was delivered successfully
     */
    public boolean isDelivered() {
        return success && ("sent".equals(deliveryStatus) || "delivered".equals(deliveryStatus));
    }
    
    /**
     * Check if email is queued for delivery
     */
    public boolean isQueued() {
        return success && "queued".equals(deliveryStatus);
    }
    
    /**
     * Check if email bounced
     */
    public boolean isBounced() {
        return !success && "bounced".equals(deliveryStatus);
    }
    
    /**
     * Check if email was blocked
     */
    public boolean isBlocked() {
        return !success && "blocked".equals(deliveryStatus);
    }
    
    /**
     * Increment retry count
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    /**
     * Check if this is a hard bounce
     */
    public boolean isHardBounce() {
        return "hard".equals(bounceType);
    }
    
    /**
     * Check if this is a soft bounce
     */
    public boolean isSoftBounce() {
        return "soft".equals(bounceType);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailResponse that = (EmailResponse) o;
        return success == that.success &&
               invalidEmail == that.invalidEmail &&
               retryable == that.retryable &&
               retryCount == that.retryCount &&
               Objects.equals(recipientEmail, that.recipientEmail) &&
               Objects.equals(messageId, that.messageId) &&
               Objects.equals(responseCode, that.responseCode) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(deliveryStatus, that.deliveryStatus);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(recipientEmail, success, messageId, responseCode, errorCode,
                          deliveryStatus, invalidEmail, retryable, retryCount);
    }
    
    @Override
    public String toString() {
        return "EmailResponse{" +
                "recipientEmail='" + recipientEmail + '\'' +
                ", success=" + success +
                ", messageId='" + messageId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", errorReason='" + errorReason + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                ", timestamp=" + timestamp +
                ", invalidEmail=" + invalidEmail +
                ", retryable=" + retryable +
                ", deliveryStatus='" + deliveryStatus + '\'' +
                ", providerMetadata='" + providerMetadata + '\'' +
                ", retryCount=" + retryCount +
                ", bounceReason='" + bounceReason + '\'' +
                ", bounceType='" + bounceType + '\'' +
                '}';
    }
}