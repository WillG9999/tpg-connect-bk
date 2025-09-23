package com.tpg.connect.controllers.auth;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.api.LoginResponse;
import com.tpg.connect.model.api.RegisterResponse;
import com.tpg.connect.model.dto.ChangePasswordRequest;
import com.tpg.connect.model.dto.ForgotPasswordRequest;
import com.tpg.connect.model.dto.LoginRequest;
import com.tpg.connect.model.dto.RegisterRequest;
import com.tpg.connect.model.dto.ResetPasswordRequest;
import com.tpg.connect.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController implements AuthControllerApi {

    @Autowired
    private AuthenticationService authenticationService;
    
    @Value("${app.dev.expose-reset-tokens:false}")
    private boolean exposeResetTokens;

    @Override
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            RegisterResponse response = authenticationService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new RegisterResponse(false, e.getMessage(), null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RegisterResponse(false, "Registration failed: " + e.getMessage(), null, null));
        }
    }

    @Override
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, e.getMessage(), null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Login failed: " + e.getMessage(), null, null, null));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> logout(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            authenticationService.logoutUser(authHeader);
            return successResponse(Map.of("message", "Logged out successfully"));
        } catch (Exception e) {
            return errorResponse("Logout failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            LoginResponse response = authenticationService.refreshToken(authHeader);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse(false, e.getMessage(), null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new LoginResponse(false, "Token refresh failed: " + e.getMessage(), null, null, null));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String resetToken = authenticationService.initiatePasswordReset(request.getEmail());
            Map<String, Object> response = Map.of(
                "message", "Password reset email sent if account exists",
                "email", request.getEmail()
            );
            
            // In development mode, include the reset token for testing
            if (resetToken != null && isDevelopmentMode()) {
                Map<String, Object> devResponse = new HashMap<>(response);
                devResponse.put("resetToken", resetToken);
                return successResponse(devResponse);
            }
            
            return successResponse(response);
        } catch (Exception e) {
            // Always return success for security (don't reveal if email exists)
            return successResponse(Map.of(
                "message", "Password reset email sent if account exists",
                "email", request.getEmail()
            ));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authenticationService.resetPassword(request);
            return successResponse(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Password reset failed: " + e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            authenticationService.changePassword(userId, request);
            return successResponse(Map.of("message", "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Password change failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            authenticationService.deleteAccount(userId);
            return successResponse(Map.of("message", "Account deleted successfully"));
        } catch (Exception e) {
            return errorResponse("Account deletion failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        try {
            authenticationService.verifyEmail(token);
            return successResponse(Map.of("message", "Email verified successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return errorResponse("Email verification failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestParam String email) {
        try {
            authenticationService.resendEmailVerification(email);
            return successResponse(Map.of(
                "message", "Verification email sent if account exists",
                "email", email
            ));
        } catch (Exception e) {
            // Always return success for security
            return successResponse(Map.of(
                "message", "Verification email sent if account exists",
                "email", email
            ));
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        try {
            return authenticationService.extractUserIdFromToken(token);
        } catch (Exception e) {
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
    
    private boolean isDevelopmentMode() {
        return exposeResetTokens;
    }
    
    // Test endpoint for JSON deserialization
    @PostMapping("/test-json")
    public ResponseEntity<Map<String, Object>> testJsonDeserialization(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(Map.of(
            "message", "JSON deserialization working",
            "email", request.getEmail() != null ? request.getEmail() : "null",
            "password", request.getPassword() != null ? "[present]" : "null",
            "rememberMe", request.isRememberMe()
        ));
    }
}