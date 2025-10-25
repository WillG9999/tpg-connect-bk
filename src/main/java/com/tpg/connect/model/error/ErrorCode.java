package com.tpg.connect.model.error;

/**
 * Standardized error codes for Connect Dating App API responses
 * 
 * Provides consistent, safe error codes that don't expose internal system details
 * while giving clients actionable information about what went wrong.
 */
public enum ErrorCode {
    
    // Authentication & Authorization Errors (4xx)
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid email or password"),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication required"),
    ACCESS_DENIED("ACCESS_DENIED", "Access denied"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Authentication token has expired"),
    TOKEN_INVALID("TOKEN_INVALID", "Invalid authentication token"),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled"),
    ACCOUNT_NOT_VERIFIED("ACCOUNT_NOT_VERIFIED", "Email verification required"),
    
    // Validation Errors (4xx)
    INVALID_REQUEST("INVALID_REQUEST", "The request contains invalid data"),
    MISSING_REQUIRED_FIELD("MISSING_REQUIRED_FIELD", "Required field is missing"),
    INVALID_FORMAT("INVALID_FORMAT", "Invalid data format"),
    VALUE_TOO_LONG("VALUE_TOO_LONG", "Value exceeds maximum length"),
    VALUE_TOO_SHORT("VALUE_TOO_SHORT", "Value is too short"),
    INVALID_EMAIL("INVALID_EMAIL", "Invalid email format"),
    WEAK_PASSWORD("WEAK_PASSWORD", "Password does not meet security requirements"),
    INVALID_DATE("INVALID_DATE", "Invalid date format or value"),
    INVALID_AGE("INVALID_AGE", "Age must be between 18 and 100"),
    
    // Resource Errors (4xx)
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found"),
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND", "User profile not found"),
    MATCH_NOT_FOUND("MATCH_NOT_FOUND", "Match not found"),
    CONVERSATION_NOT_FOUND("CONVERSATION_NOT_FOUND", "Conversation not found"),
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", "Message not found"),
    PHOTO_NOT_FOUND("PHOTO_NOT_FOUND", "Photo not found"),
    
    // Conflict Errors (4xx)
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", "Email address is already registered"),
    USER_ALREADY_EXISTS("USER_ALREADY_EXISTS", "User already exists"),
    MATCH_ALREADY_EXISTS("MATCH_ALREADY_EXISTS", "Match already exists"),
    ALREADY_BLOCKED("ALREADY_BLOCKED", "User is already blocked"),
    CANNOT_MATCH_SELF("CANNOT_MATCH_SELF", "Cannot match with yourself"),
    
    // Business Logic Errors (4xx)
    INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Insufficient permissions for this action"),
    ACTION_NOT_ALLOWED("ACTION_NOT_ALLOWED", "This action is not allowed"),
    DAILY_LIMIT_EXCEEDED("DAILY_LIMIT_EXCEEDED", "Daily limit exceeded"),
    PREMIUM_REQUIRED("PREMIUM_REQUIRED", "Premium subscription required"),
    USER_BLOCKED("USER_BLOCKED", "User is blocked"),
    PROFILE_INCOMPLETE("PROFILE_INCOMPLETE", "Profile must be completed first"),
    VERIFICATION_REQUIRED("VERIFICATION_REQUIRED", "Account verification required"),
    
    // File Upload Errors (4xx)
    FILE_TOO_LARGE("FILE_TOO_LARGE", "File size exceeds maximum limit"),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Invalid file type"),
    FILE_UPLOAD_FAILED("FILE_UPLOAD_FAILED", "File upload failed"),
    TOO_MANY_FILES("TOO_MANY_FILES", "Maximum number of files exceeded"),
    
    // Rate Limiting (4xx)
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", "Rate limit exceeded, please try again later"),
    TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests"),
    
    // Server Errors (5xx)
    INTERNAL_ERROR("INTERNAL_ERROR", "An unexpected error occurred"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service temporarily unavailable"),
    DATABASE_ERROR("DATABASE_ERROR", "Database operation failed"),
    EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service error"),
    EMAIL_SERVICE_ERROR("EMAIL_SERVICE_ERROR", "Email service is currently unavailable"),
    STORAGE_SERVICE_ERROR("STORAGE_SERVICE_ERROR", "File storage service error");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String toString() {
        return code;
    }
}