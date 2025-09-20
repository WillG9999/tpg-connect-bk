package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    private String id;
    
    @NotBlank
    @Pattern(regexp = "^https?://.*\\.(jpg|jpeg|png|gif)$", message = "Must be a valid image URL")
    private String url;
    
    private boolean isPrimary;
    
    @Min(1)
    @Max(6)
    private int order;
}