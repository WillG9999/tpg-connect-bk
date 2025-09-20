package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscoverySettingsRequest {
    
    @Min(18)
    @Max(100)
    private Integer minAge;
    
    @Min(18)
    @Max(100)
    private Integer maxAge;
    
    @Min(1)
    @Max(100)
    private Integer maxDistance; // in kilometers
    
    private String preferredGender; // "men", "women", "everyone"
    
    private Boolean showMeToMen;
    
    private Boolean showMeToWomen;
    
    private Boolean showMeToNonBinary;
    
    // Location preferences
    private Double latitude;
    private Double longitude;
    private String city;
    private String region;
    
    // Additional filters
    private String relationshipType; // "serious", "casual", "open_to_anything"
    private Boolean smokerPreference;
    private Boolean drinkerPreference;
    private String educationLevelPreference;
}