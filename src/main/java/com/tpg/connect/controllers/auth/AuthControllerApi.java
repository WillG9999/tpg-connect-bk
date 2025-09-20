package com.tpg.connect.controllers.auth;

import com.tpg.connect.model.api.LoginResponse;
import com.tpg.connect.model.api.RegisterResponse;
import com.tpg.connect.model.dto.ChangePasswordRequest;
import com.tpg.connect.model.dto.ForgotPasswordRequest;
import com.tpg.connect.model.dto.LoginRequest;
import com.tpg.connect.model.dto.RegisterRequest;
import com.tpg.connect.model.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "Authentication", description = "User authentication and account management")
public interface AuthControllerApi {

    @Operation(summary = "Register new user", description = "Create a new user account with email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or email already exists"),
        @ApiResponse(responseCode = "500", description = "Registration failed")
    })
    @PostMapping("/register")
    ResponseEntity<RegisterResponse> register(
        @Parameter(description = "User registration details", required = true)
        @Valid @RequestBody RegisterRequest request
    );

    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "500", description = "Login failed")
    })
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(
        @Parameter(description = "User login credentials", required = true)
        @Valid @RequestBody LoginRequest request
    );

    @Operation(summary = "User logout", description = "Invalidate user session and JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/logout")
    ResponseEntity<Map<String, Object>> logout(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Refresh JWT token", description = "Get a new JWT token using current valid token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "500", description = "Token refresh failed")
    })
    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refreshToken(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Forgot password", description = "Initiate password reset process by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent if account exists"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/forgot-password")
    ResponseEntity<Map<String, Object>> forgotPassword(
        @Parameter(description = "Email for password reset", required = true)
        @Valid @RequestBody ForgotPasswordRequest request
    );

    @Operation(summary = "Reset password", description = "Reset password using reset token from email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired reset token"),
        @ApiResponse(responseCode = "500", description = "Password reset failed")
    })
    @PostMapping("/reset-password")
    ResponseEntity<Map<String, Object>> resetPassword(
        @Parameter(description = "Password reset details", required = true)
        @Valid @RequestBody ResetPasswordRequest request
    );

    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid current password or new password"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/change-password")
    ResponseEntity<Map<String, Object>> changePassword(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader,
        @Parameter(description = "Password change details", required = true)
        @Valid @RequestBody ChangePasswordRequest request
    );

    @Operation(summary = "Delete account", description = "Permanently delete user account and all data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Account deletion failed")
    })
    @DeleteMapping("/delete-account")
    ResponseEntity<Map<String, Object>> deleteAccount(
        @Parameter(description = "Bearer token for authentication", required = true)
        @RequestHeader("Authorization") String authHeader
    );

    @Operation(summary = "Verify email", description = "Verify user email address using verification token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    })
    @GetMapping("/verify-email")
    ResponseEntity<Map<String, Object>> verifyEmail(
        @Parameter(description = "Email verification token", required = true)
        @RequestParam String token
    );

    @Operation(summary = "Resend email verification", description = "Resend email verification link")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent if account exists"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    @PostMapping("/resend-verification")
    ResponseEntity<Map<String, Object>> resendVerification(
        @Parameter(description = "Email address to resend verification", required = true)
        @RequestParam String email
    );
}