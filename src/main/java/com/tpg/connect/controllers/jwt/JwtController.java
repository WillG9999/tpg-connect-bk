package com.tpg.connect.controllers.jwt;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.jwt.LoginRequest;
import com.tpg.connect.model.jwt.AuthResponse;
import com.tpg.connect.model.jwt.RefreshTokenRequest;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JwtController extends BaseController implements JwtControllerApi {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public ResponseEntity<Map<String, Object>> login(LoginRequest loginRequest) {
        AuthResponse authResponse = authService.authenticate(loginRequest);
        
        if (authResponse != null) {
            return successResponse(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid credentials"));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> refresh(RefreshTokenRequest refreshRequest) {
        AuthResponse authResponse = authService.refreshToken(refreshRequest.getRefreshToken());
        
        if (authResponse != null) {
            return successResponse(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid refresh token"));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
            boolean success = authService.logout(token);
            
            if (success) {
                return successResponse(Map.of("message", "Logout successful"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Invalid token"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Invalid authorization header"));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> generateToken(String username) {
        String token = jwtUtil.generateToken(username);
        Map<String, String> tokenData = Map.of("token", token, "username", username);
        return successResponse(tokenData);
    }

    @Override
    public ResponseEntity<Map<String, Object>> generateTokenWithRole(String username, String role) {
        String token = jwtUtil.generateTokenWithClaims(username, role);
        Map<String, String> tokenData = Map.of("token", token, "username", username, "role", role);
        return successResponse(tokenData);
    }
}