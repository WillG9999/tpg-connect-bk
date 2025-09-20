package com.tpg.connect.model.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private ErrorDetails error;
    private LocalDateTime timestamp;
    private String requestId;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Map<String, Object> details;
    }
    
    public static ApiError validationError(String field, String reason) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setCode("VALIDATION_ERROR");
        errorDetails.setMessage("Invalid request data");
        errorDetails.setDetails(Map.of("field", field, "reason", reason));
        
        return new ApiError(errorDetails, LocalDateTime.now(), null);
    }
    
    public static ApiError unauthorizedError() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setCode("UNAUTHORIZED");
        errorDetails.setMessage("Invalid or expired token");
        
        return new ApiError(errorDetails, LocalDateTime.now(), null);
    }
    
    public static ApiError batchNotReadyError() {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setCode("BATCH_NOT_READY");
        errorDetails.setMessage("Daily batch not yet delivered");
        
        return new ApiError(errorDetails, LocalDateTime.now(), null);
    }
}