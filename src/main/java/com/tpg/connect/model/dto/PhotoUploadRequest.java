package com.tpg.connect.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {
    @NotBlank
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif)$", message = "Must be a valid image URL")
    private String photoUrl;
    
    private boolean isPrimary;
    
    @Min(1)
    @Max(6)
    private int order;
}