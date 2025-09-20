package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockUserRequest {
    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
    
    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;
}