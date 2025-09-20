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
public class RegisterResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private Object user; // Can be CompleteUserProfile or UserProfileDTO
    private boolean emailVerificationRequired;
    private LocalDateTime registeredAt;
    
    public RegisterResponse(boolean success, String message, String accessToken, Object user) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.user = user;
        this.emailVerificationRequired = true;
        this.registeredAt = LocalDateTime.now();
    }
}