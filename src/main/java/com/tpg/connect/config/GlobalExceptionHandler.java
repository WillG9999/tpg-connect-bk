package com.tpg.connect.config;

import com.tpg.connect.model.error.ErrorCode;
import com.tpg.connect.model.error.ErrorResponse;
import com.tpg.connect.model.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeoutException;

/**
 * Global Exception Handler for Connect Dating App
 * 
 * Provides centralized exception handling to ensure consistent, secure error responses
 * that don't expose sensitive system information while providing actionable feedback
 * to API consumers.
 * 
 * Security Features:
 * - No stack trace exposure in production
 * - Sanitized error messages
 * - Detailed logging for developers
 * - Request correlation IDs for debugging
 * - Consistent error response format
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== Authentication & Authorization Errors ==========

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        
        logger.warn("Authentication failed for request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.UNAUTHORIZED)
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException e, HttpServletRequest request) {
        
        logger.warn("Invalid credentials attempt for request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_CREDENTIALS)
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class, java.nio.file.AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            Exception e, HttpServletRequest request) {
        
        logger.warn("Access denied for request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.ACCESS_DENIED)
                .path(request.getRequestURI())
                .status(HttpStatus.FORBIDDEN.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    // ========== Validation Errors ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        logger.debug("Validation failed for request {}: {} validation errors", 
            request.getRequestURI(), e.getBindingResult().getErrorCount());
        
        ValidationErrorResponse.Builder responseBuilder = ValidationErrorResponse.builder()
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value());

        // Add field-specific errors
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            String errorCode = mapValidationErrorCode(fieldError.getCode());
            responseBuilder.addFieldError(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage(),
                errorCode
            );
        }

        return ResponseEntity.badRequest().body(responseBuilder.build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ValidationErrorResponse> handleBindException(
            BindException e, HttpServletRequest request) {
        
        logger.debug("Binding failed for request {}: {} errors", 
            request.getRequestURI(), e.getBindingResult().getErrorCount());
        
        ValidationErrorResponse.Builder responseBuilder = ValidationErrorResponse.builder()
                .message("Invalid request data")
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value());

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            responseBuilder.addFieldError(
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage()
            );
        }

        return ResponseEntity.badRequest().body(responseBuilder.build());
    }

    // ========== Request Processing Errors ==========

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        
        logger.debug("Invalid argument for request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_REQUEST)
                .message(sanitizeMessage(e.getMessage()))
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        
        logger.debug("Invalid JSON in request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_FORMAT)
                .message("Invalid request format. Please check your JSON syntax.")
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        logger.debug("Type mismatch for parameter '{}' in request {}: {}", 
            e.getName(), request.getRequestURI(), e.getMessage());
        
        String message = String.format("Invalid value for parameter '%s'", e.getName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_FORMAT)
                .message(message)
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        logger.debug("Missing required parameter '{}' in request {}", e.getParameterName(), request.getRequestURI());
        
        String message = String.format("Missing required parameter: %s", e.getParameterName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.MISSING_REQUIRED_FIELD)
                .message(message)
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(
            DateTimeParseException e, HttpServletRequest request) {
        
        logger.debug("Date parsing error in request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INVALID_DATE)
                .message("Invalid date format. Please use ISO-8601 format (YYYY-MM-DD).")
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    // ========== HTTP Method & Media Type Errors ==========

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        
        logger.debug("Method {} not supported for request {}", e.getMethod(), request.getRequestURI());
        
        String message = String.format("HTTP method %s is not supported for this endpoint", e.getMethod());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("METHOD_NOT_ALLOWED")
                .message(message)
                .path(request.getRequestURI())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException e, HttpServletRequest request) {
        
        logger.debug("Media type {} not supported for request {}", e.getContentType(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("MEDIA_TYPE_NOT_SUPPORTED")
                .message("Content type not supported. Please use application/json.")
                .path(request.getRequestURI())
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException e, HttpServletRequest request) {
        
        logger.debug("No handler found for request {}", request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("ENDPOINT_NOT_FOUND")
                .message("The requested endpoint was not found.")
                .path(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ========== Service & External Errors ==========

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeout(
            TimeoutException e, HttpServletRequest request) {
        
        logger.error("Timeout occurred for request {}: {}", request.getRequestURI(), e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.SERVICE_UNAVAILABLE)
                .message("Request timeout. Please try again.")
                .path(request.getRequestURI())
                .status(HttpStatus.REQUEST_TIMEOUT.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }

    // ========== Generic Exception Handler ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e, HttpServletRequest request) {
        
        // Log the full exception for developers
        logger.error("Unexpected error occurred for request {}: {}", 
            request.getRequestURI(), e.getMessage(), e);
        
        // Return a safe, generic response to users
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR)
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ========== Helper Methods ==========

    /**
     * Maps Spring validation error codes to our custom error codes
     */
    private String mapValidationErrorCode(String springErrorCode) {
        return switch (springErrorCode) {
            case "NotNull", "NotEmpty", "NotBlank" -> ErrorCode.MISSING_REQUIRED_FIELD.getCode();
            case "Email" -> ErrorCode.INVALID_EMAIL.getCode();
            case "Size" -> ErrorCode.VALUE_TOO_LONG.getCode();
            case "Min", "Max" -> ErrorCode.INVALID_FORMAT.getCode();
            case "Pattern" -> ErrorCode.INVALID_FORMAT.getCode();
            case "Past", "Future" -> ErrorCode.INVALID_DATE.getCode();
            default -> ErrorCode.INVALID_REQUEST.getCode();
        };
    }

    /**
     * Sanitizes error messages to prevent information disclosure
     */
    private String sanitizeMessage(String message) {
        if (message == null) {
            return "Invalid request";
        }
        
        // Remove potential sensitive information from error messages
        String sanitized = message
            .replaceAll("(?i)password", "[REDACTED]")
            .replaceAll("(?i)token", "[REDACTED]")
            .replaceAll("(?i)secret", "[REDACTED]")
            .replaceAll("(?i)key", "[REDACTED]")
            .replaceAll("java\\.lang\\.[A-Za-z]*Exception", "error")
            .replaceAll("org\\.[a-zA-Z.]*", "system error");
        
        // Limit message length to prevent verbose error exposure
        if (sanitized.length() > 200) {
            sanitized = sanitized.substring(0, 200) + "...";
        }
        
        return sanitized;
    }
}