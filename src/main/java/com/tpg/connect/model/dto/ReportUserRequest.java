package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUserRequest {
    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
    
    @NotBlank(message = "User info is required")
    private String userInfo; // from frontend _nameController - user's name or profile info
    
    @NotBlank(message = "Reason is required") 
    private String reason; // from frontend _selectedReason dropdown - single reason
    
    @NotBlank(message = "Location is required")
    private String location; // from frontend _selectedLocation dropdown - where it occurred
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description; // from frontend _descriptionController - additional details
}