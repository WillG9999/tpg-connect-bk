package com.tpg.connect.model.dto;

import com.tpg.connect.model.user.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    private String id;
    private String name;
    private int age;
    private String bio;
    private List<String> photos; // Frontend expects string array
    private String location;
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
        dto.setId(profile.getId());
        dto.setName(profile.getName() != null ? profile.getName() : "");
        dto.setAge(profile.getAge());
        dto.setBio(profile.getBio() != null ? profile.getBio() : "");
        dto.setLocation(profile.getLocation() != null ? profile.getLocation() : "");
        dto.setInterests(profile.getInterests() != null ? profile.getInterests() : List.of());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        
        // Convert Photo objects to string array
        if (profile.getPhotos() != null) {
            dto.setPhotos(profile.getPhotos().stream()
                .sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder()))
                .map(photo -> photo.getUrl() != null ? photo.getUrl() : "")
                .filter(url -> !url.isEmpty()) // Remove empty URLs
                .collect(Collectors.toList()));
        } else {
            dto.setPhotos(List.of()); // Frontend expects empty list, not null
        }
        
        // User profile fields
        if (profile.getProfile() != null) {
            UserProfile userProfile = profile.getProfile();
            dto.setPronouns(userProfile.getPronouns());
            dto.setGender(userProfile.getGender());
            dto.setSexuality(userProfile.getSexuality());
            dto.setInterestedIn(userProfile.getInterestedIn());
            dto.setJobTitle(userProfile.getJobTitle());
            dto.setCompany(userProfile.getCompany());
            dto.setUniversity(userProfile.getUniversity());
            dto.setEducationLevel(userProfile.getEducationLevel());
            dto.setReligiousBeliefs(userProfile.getReligiousBeliefs());
            dto.setHometown(userProfile.getHometown());
            dto.setPolitics(userProfile.getPolitics());
            dto.setLanguages(userProfile.getLanguages());
            dto.setDatingIntentions(userProfile.getDatingIntentions());
            dto.setRelationshipType(userProfile.getRelationshipType());
            dto.setHeight(userProfile.getHeight());
            dto.setEthnicity(userProfile.getEthnicity());
            dto.setChildren(userProfile.getChildren());
            dto.setFamilyPlans(userProfile.getFamilyPlans());
            dto.setPets(userProfile.getPets());
            dto.setZodiacSign(userProfile.getZodiacSign());
        }
        
        // Convert written prompts to simple map format
        if (profile.getWrittenPrompts() != null) {
            dto.setWrittenPrompts(profile.getWrittenPrompts().stream()
                .map(wp -> Map.of(
                    "prompt", wp.getPrompt() != null ? wp.getPrompt() : "",
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
                    "prompt", pp.getPrompt() != null ? pp.getPrompt() : "",
                    "question", pp.getQuestion() != null ? pp.getQuestion() : "",
                    "options", pp.getOptions() != null ? pp.getOptions() : List.of()
                ))
                .collect(Collectors.toList()));
        } else {
            dto.setPollPrompts(List.of()); // Frontend expects empty list, not null
        }
        
        // Convert field visibility to Map format
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
        
        return dto;
    }
}