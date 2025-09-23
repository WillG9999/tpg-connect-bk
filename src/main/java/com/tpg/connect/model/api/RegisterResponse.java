package com.tpg.connect.model.api;

import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.dto.UserProfileDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("user")
    private Object user; // Can be CompleteUserProfile or UserProfileDTO
    
    @JsonProperty("emailVerificationRequired")
    private boolean emailVerificationRequired;
    
    @JsonProperty("registeredAt")
    private String registeredAt;
    
    @JsonProperty("verificationToken")
    private String verificationToken; // Only included in development mode
    
    public RegisterResponse(boolean success, String message, String accessToken, Object user) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.user = user;
        this.emailVerificationRequired = true;
        this.registeredAt = java.time.Instant.now().toString();
    }
    
    // Constructor with verification token for development mode
    public RegisterResponse(boolean success, String message, String accessToken, Object user, String verificationToken) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.user = user;
        this.emailVerificationRequired = true;
        this.registeredAt = java.time.Instant.now().toString();
        this.verificationToken = verificationToken;
    }
}