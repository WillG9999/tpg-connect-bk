package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUserRequest {
    @NotBlank(message = "Target user ID is required")
    private String targetUserId;
    
    @NotEmpty(message = "At least one reason is required")
    private List<String> reasons; // e.g., ["harassment", "inappropriate_photos", "spam"]
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    private List<String> evidenceUrls; // Screenshots or other evidence
    
    private String context; // "profile", "message", "match"
    
    private String conversationId; // If reporting from a conversation
}