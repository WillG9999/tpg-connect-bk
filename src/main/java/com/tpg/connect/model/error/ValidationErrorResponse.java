package com.tpg.connect.model.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Validation error response model for Connect Dating App API
 * 
 * Handles validation errors from @Valid annotations and provides detailed
 * field-level error information while maintaining security.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorResponse {
    
    private String error;
    private String message;
    private Instant timestamp;
    private String requestId;
    private String path;
    private Integer status;
    private List<FieldError> fieldErrors;

    public ValidationErrorResponse() {
        this.timestamp = Instant.now();
        this.requestId = UUID.randomUUID().toString().substring(0, 8);
        this.fieldErrors = new ArrayList<>();
        this.error = ErrorCode.INVALID_REQUEST.getCode();
        this.message = "Validation failed for one or more fields";
    }

    public ValidationErrorResponse(String message) {
        this();
        this.message = message;
    }

    /**
     * Individual field validation error
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
        private String errorCode;

        public FieldError() {}

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        public FieldError(String field, Object rejectedValue, String message, String errorCode) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
            this.errorCode = errorCode;
        }

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Object getRejectedValue() {
            return rejectedValue;
        }

        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        @Override
        public String toString() {
            return String.format("FieldError{field='%s', message='%s'}", field, message);
        }
    }

    // Builder pattern for fluent construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ValidationErrorResponse response = new ValidationErrorResponse();

        public Builder message(String message) {
            response.message = message;
            return this;
        }

        public Builder path(String path) {
            response.path = path;
            return this;
        }

        public Builder status(Integer status) {
            response.status = status;
            return this;
        }

        public Builder addFieldError(String field, Object rejectedValue, String message) {
            response.fieldErrors.add(new FieldError(field, rejectedValue, message));
            return this;
        }

        public Builder addFieldError(String field, Object rejectedValue, String message, String errorCode) {
            response.fieldErrors.add(new FieldError(field, rejectedValue, message, errorCode));
            return this;
        }

        public Builder addFieldError(FieldError fieldError) {
            response.fieldErrors.add(fieldError);
            return this;
        }

        public ValidationErrorResponse build() {
            return response;
        }
    }

    // Convenience methods
    public void addFieldError(String field, Object rejectedValue, String message) {
        this.fieldErrors.add(new FieldError(field, rejectedValue, message));
    }

    public void addFieldError(String field, Object rejectedValue, String message, String errorCode) {
        this.fieldErrors.add(new FieldError(field, rejectedValue, message, errorCode));
    }

    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    public int getFieldErrorCount() {
        return fieldErrors != null ? fieldErrors.size() : 0;
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

    public List<FieldError> getFieldErrors() {
        return fieldErrors;
    }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    @Override
    public String toString() {
        return String.format("ValidationErrorResponse{error='%s', message='%s', fieldErrors=%d, requestId='%s'}", 
            error, message, getFieldErrorCount(), requestId);
    }
}