package com.tpg.connect.model.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    // Basic info
    @Size(min = 2, max = 50)
    private String name;
    
    @Min(18)
    @Max(100)
    private Integer age;
    
    @Size(max = 500)
    private String bio;
    
    @Size(max = 100)
    private String location;
    
    @Size(max = 10)
    private List<String> interests;

    // Identity
    private String pronouns;
    private String gender;
    private String sexuality;
    private String interestedIn;
    
    // Virtues  
    private String jobTitle;
    private String company;
    private String university;
    private String educationLevel;
    private String religiousBeliefs;
    private String hometown;
    private String politics;
    private List<String> languages;
    private String datingIntentions;
    private String relationshipType;
    
    // Vitals
    private String height;
    private String ethnicity;
    private String children;
    private String familyPlans;
    private String pets;
    private String zodiacSign;
    
    // Photos (list of URLs)
    @Size(min = 1, max = 6)
    private List<String> photos;
    
    // Written prompts
    private List<Map<String, String>> writtenPrompts;
    
    // Poll prompts
    private List<Map<String, Object>> pollPrompts;
    
    // Field visibility settings
    private Map<String, Boolean> fieldVisibility;
    
    // User preferences
    private Map<String, Object> preferences;
}