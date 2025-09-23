package com.tpg.connect.model.dto;

import com.tpg.connect.model.user.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("age")
    private int age;
    
    @JsonProperty("bio")
    private String bio;
    
    @JsonProperty("photos")
    private List<String> photos; // Frontend expects string array
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("interests")
    private List<String> interests;
    
    // Identity fields
    private String pronouns;
    private String gender;
    private String sexuality;
    private String interestedIn;
    
    // Professional fields
    private String jobTitle;
    private String company;
    private String university;
    private String educationLevel;
    
    // Personal fields
    private String religiousBeliefs;
    private String hometown;
    private String politics;
    private List<String> languages;
    private String datingIntentions;
    private String relationshipType;
    private String height;
    private String ethnicity;
    private String children;
    private String familyPlans;
    private String pets;
    private String zodiacSign;
    
    // Prompts as simple maps (frontend format)
    private List<Map<String, String>> writtenPrompts;
    private List<Map<String, Object>> pollPrompts;
    
    // Field visibility as Map<String, Boolean> (frontend format)
    private Map<String, Boolean> fieldVisibility;
    
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime updatedAt;
    
    // Convert from CompleteUserProfile to DTO
    public static UserProfileDTO fromCompleteUserProfile(CompleteUserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        
        // Basic info with null safety
        dto.setId(profile.getConnectId());
        dto.setName(profile.getFirstName() != null ? profile.getFirstName() : "");
        
        // Calculate age from date of birth
        if (profile.getDateOfBirth() != null) {
            int calculatedAge = java.time.Period.between(profile.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            dto.setAge(calculatedAge);
        }
        
        dto.setLocation(profile.getLocation() != null ? profile.getLocation() : "");
        dto.setGender(profile.getGender() != null ? profile.getGender() : "");
        
        // Convert EnhancedPhoto objects to string array
        if (profile.getPhotos() != null) {
            try {
                dto.setPhotos(profile.getPhotos().stream()
                    .sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder()))
                    .map(photo -> photo.getUrl() != null ? photo.getUrl() : "")
                    .filter(url -> !url.isEmpty()) // Remove empty URLs
                    .collect(Collectors.toList()));
            } catch (Exception e) {
                // Fallback to empty list if photo processing fails
                dto.setPhotos(List.of());
            }
        } else {
            dto.setPhotos(List.of()); // Frontend expects empty list, not null
        }
        
        // User profile fields from DetailedProfile
        if (profile.getProfile() != null) {
            DetailedProfile detailedProfile = profile.getProfile();
            dto.setPronouns(detailedProfile.getPronouns());
            dto.setSexuality(detailedProfile.getSexuality());
            dto.setInterestedIn(detailedProfile.getInterestedIn());
            dto.setJobTitle(detailedProfile.getJobTitle());
            dto.setCompany(detailedProfile.getCompany());
            dto.setUniversity(detailedProfile.getUniversity());
            dto.setEducationLevel(detailedProfile.getEducationLevel());
            dto.setReligiousBeliefs(detailedProfile.getReligiousBeliefs());
            dto.setHometown(detailedProfile.getHometown());
            dto.setPolitics(detailedProfile.getPolitics());
            dto.setLanguages(detailedProfile.getLanguages());
            dto.setDatingIntentions(detailedProfile.getDatingIntentions());
            dto.setRelationshipType(detailedProfile.getRelationshipType());
            dto.setHeight(detailedProfile.getHeight());
            dto.setEthnicity(detailedProfile.getEthnicity());
            dto.setChildren(detailedProfile.getChildren());
            dto.setFamilyPlans(detailedProfile.getFamilyPlans());
            dto.setPets(detailedProfile.getPets());
            dto.setZodiacSign(detailedProfile.getZodiacSign());
        }
        
        // Convert written prompts to simple map format
        if (profile.getWrittenPrompts() != null) {
            dto.setWrittenPrompts(profile.getWrittenPrompts().stream()
                .map(wp -> Map.of(
                    "question", wp.getQuestion() != null ? wp.getQuestion() : "",
                    "answer", wp.getAnswer() != null ? wp.getAnswer() : ""
                ))
                .collect(Collectors.toList()));
        } else {
            dto.setWrittenPrompts(List.of()); // Frontend expects empty list, not null
        }
        
        // Convert poll prompts to map format
        if (profile.getPollPrompts() != null) {
            dto.setPollPrompts(profile.getPollPrompts().stream()
                .map(pp -> Map.<String, Object>of(
                    "question", pp.getQuestion() != null ? pp.getQuestion() : "",
                    "description", pp.getDescription() != null ? pp.getDescription() : "",
                    "options", pp.getOptions() != null ? pp.getOptions() : List.of(),
                    "selectedOption", pp.getSelectedOption() != null ? pp.getSelectedOption() : ""
                ))
                .collect(Collectors.toList()));
        } else {
            dto.setPollPrompts(List.of()); // Frontend expects empty list, not null
        }
        
        // Convert field visibility to Map format
        try {
            if (profile.getFieldVisibility() != null) {
                FieldVisibility fv = profile.getFieldVisibility();
                dto.setFieldVisibility(Map.of(
                    "jobTitle", fv.isJobTitle(),
                    "company", fv.isCompany(),
                    "university", fv.isUniversity(),
                    "religiousBeliefs", fv.isReligiousBeliefs(),
                    "politics", fv.isPolitics(),
                    "hometown", fv.isHometown(),
                    "height", fv.isHeight(),
                    "ethnicity", fv.isEthnicity()
                ));
            } else {
                // Default field visibility - all fields visible
                dto.setFieldVisibility(Map.of(
                    "jobTitle", true,
                    "company", true,
                    "university", true,
                    "religiousBeliefs", true,
                    "politics", true,
                    "hometown", true,
                    "height", true,
                    "ethnicity", true
                ));
            }
        } catch (Exception e) {
            // Fallback to default visibility settings
            dto.setFieldVisibility(Map.of(
                "jobTitle", true,
                "company", true,
                "university", true,
                "religiousBeliefs", true,
                "politics", true,
                "hometown", true,
                "height", true,
                "ethnicity", true
            ));
        }
        
        return dto;
    }
}