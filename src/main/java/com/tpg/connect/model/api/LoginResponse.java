package com.tpg.connect.model.api;

import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.dto.UserProfileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Object user; // Can be CompleteUserProfile or UserProfileDTO
    private LocalDateTime expiresAt;
    private String tokenType;
    
    public LoginResponse(boolean success, String message, String accessToken, String refreshToken, Object user) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
        this.tokenType = "Bearer";
        this.expiresAt = null; // Let Jackson handle this or set as string
    }
}