package com.tpg.connect.services;

import com.tpg.connect.model.jwt.AuthResponse;
import com.tpg.connect.model.jwt.LoginRequest;
import com.tpg.connect.model.user.User;
import com.tpg.connect.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    public AuthResponse authenticate(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        User user = userService.findByUsername(username);
        if (user != null && userService.validatePassword(password, user.getPassword()) && user.isActive()) {
            String accessToken = jwtUtil.generateAccessToken(username, user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(username);

            return new AuthResponse(
                accessToken,
                refreshToken,
                username,
                user.getRole(),
                jwtUtil.getAccessTokenExpiration()
            );
        }
        return null;
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken) && "refresh".equals(jwtUtil.extractTokenType(refreshToken))) {
            String username = jwtUtil.extractUsername(refreshToken);
            User user = userService.findByUsername(username);
            
            if (user != null && user.isActive()) {
                String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
                
                return new AuthResponse(
                    newAccessToken,
                    refreshToken,
                    username,
                    user.getRole(),
                    jwtUtil.getAccessTokenExpiration()
                );
            }
        }
        return null;
    }

    public boolean logout(String token) {
        if (token != null && jwtUtil.validateToken(token)) {
            jwtUtil.blacklistToken(token);
            return true;
        }
        return false;
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    public String extractRole(String token) {
        return jwtUtil.extractRole(token);
    }
}