package com.tpg.connect.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    @Size(min = 2, max = 50)
    private String name;
    
    @Size(max = 500)
    private String bio;
    
    @Size(max = 100)
    private String location;
    
    @Size(max = 10)
    private List<String> interests;
    
    private List<String> languages;
    private String jobTitle;
    private String company;
    
    // Identity fields
    private String pronouns;
    private String gender;
    private String sexuality;
    private String interestedIn;
    
    // Professional fields
    private String university;
    private String educationLevel;
    
    // Personal fields
    private String religiousBeliefs;
    private String hometown;
    private String politics;
    private String datingIntentions;
    private String relationshipType;
    
    // Physical/lifestyle fields
    private String height;
    private String ethnicity;
    private String children;
    private String familyPlans;
    private String pets;
    private String zodiacSign;
    
    // Prompt fields
    private Map<String, Map<String, String>> photoPrompts;
    private List<Map<String, String>> writtenPrompts;
    private List<Map<String, Object>> pollPrompts;
}