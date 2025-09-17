package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserProfile {
    private String pronouns;
    private String gender;
    private String sexuality;
    private String interestedIn;
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
    private String height;
    private String ethnicity;
    private String children;
    private String familyPlans;
    private String pets;
    private String zodiacSign;
}