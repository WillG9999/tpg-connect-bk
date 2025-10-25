package com.tpg.connect.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

/**
 * Standard error response model for Connect Dating App API
 * 
 * Provides consistent, secure error responses that don't expose sensitive
 * internal system details while giving users actionable information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private String error;
    private String message;
    private Instant timestamp;
    private String requestId;
    private String path;
    private Integer status;

    public ErrorResponse() {
        this.timestamp = Instant.now();
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
    }

    public ErrorResponse(ErrorCode errorCode) {
        this();
        this.error = errorCode.getCode();
        this.message = errorCode.getDefaultMessage();
    }

    public ErrorResponse(ErrorCode errorCode, String customMessage) {
        this();
        this.error = errorCode.getCode();
        this.message = customMessage;
    }

    public ErrorResponse(String error, String message) {
        this();
        this.error = error;
        this.message = message;
    }

    // Builder pattern for fluent construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ErrorResponse errorResponse = new ErrorResponse();

        public Builder error(String error) {
            errorResponse.error = error;
            return this;
        }

        public Builder errorCode(ErrorCode errorCode) {
            errorResponse.error = errorCode.getCode();
            if (errorResponse.message == null) {
                errorResponse.message = errorCode.getDefaultMessage();
            }
            return this;
        }

        public Builder message(String message) {
            errorResponse.message = message;
            return this;
        }

        public Builder path(String path) {
            errorResponse.path = path;
            return this;
        }

        public Builder status(Integer status) {
            errorResponse.status = status;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            errorResponse.timestamp = timestamp;
            return this;
        }

        public Builder requestId(String requestId) {
            errorResponse.requestId = requestId;
            return this;
        }

        public ErrorResponse build() {
            return errorResponse;
        }
    }

    // Getters and Setters
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("ErrorResponse{error='%s', message='%s', requestId='%s', timestamp=%s}", 
            error, message, requestId, timestamp);
    }
}