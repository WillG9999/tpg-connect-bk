package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyBlockRuleRequest {
    @NotBlank(message = "Block type is required")
    private String type; // NAME, LOCATION, EMAIL, PHONE, KEYWORD, COMPANY, UNIVERSITY, JOB, HOMETOWN (case-insensitive)
    
    @NotBlank(message = "Pattern is required")
    @Size(min = 1, max = 200, message = "Pattern must be between 1 and 200 characters")
    private String pattern; // The pattern to match against
    
    @NotNull(message = "Case sensitive flag is required")
    private Boolean caseSensitive;
    
    @NotNull(message = "Enabled flag is required")
    private Boolean enabled;
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description; // Optional description of why this block exists
}