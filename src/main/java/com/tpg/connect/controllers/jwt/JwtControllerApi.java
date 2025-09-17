package com.tpg.connect.controllers.jwt;

import com.tpg.connect.model.jwt.LoginRequest;
import com.tpg.connect.model.jwt.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Authentication", description = "JWT authentication endpoints")
@RequestMapping("/api/auth")
public interface JwtControllerApi {

    @Operation(summary = "User login", description = "Authenticates user and returns access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest);

    @Operation(summary = "Refresh access token", description = "Generates new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    ResponseEntity<Map<String, Object>> refresh(@RequestBody RefreshTokenRequest refreshRequest);

    @Operation(summary = "User logout", description = "Invalidates the current session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    ResponseEntity<Map<String, Object>> logout(
            @Parameter(description = "Authorization header with Bearer token", required = true)
            @RequestHeader("Authorization") String authHeader);



    @Operation(summary = "Generate JWT token", description = "Generates a simple JWT token for a given username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated successfully")
    })
    @PostMapping("/generate")
    ResponseEntity<Map<String, Object>> generateToken(
            @Parameter(description = "Username for token generation", required = true)
            @RequestParam String username);





    @Operation(summary = "Generate JWT token with role", description = "Generates a JWT token with username and role claims")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated successfully")
    })
    @PostMapping("/generate-with-role")
    ResponseEntity<Map<String, Object>> generateTokenWithRole(
            @Parameter(description = "Username for token generation", required = true)
            @RequestParam String username,
            @Parameter(description = "User role", required = true)
            @RequestParam String role);
}