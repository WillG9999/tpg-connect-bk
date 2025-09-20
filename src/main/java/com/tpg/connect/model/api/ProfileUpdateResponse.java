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
public class ProfileUpdateResponse {
    private boolean success;
    private String message;
    private Object profile; // Can be CompleteUserProfile or UserProfileDTO
    private LocalDateTime updatedAt;
    
    public ProfileUpdateResponse(boolean success, String message, Object profile) {
        this.success = success;
        this.message = message;
        this.profile = profile;
        this.updatedAt = LocalDateTime.now();
    }
}