package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor 
@AllArgsConstructor
public class DetailedProfile {
    private String pronouns;              // "he/him", "she/her", "they/them"
    private String gender;                // "Man", "Woman", "Non-binary", etc.
    private String sexuality;             // "Straight", "Gay", "Bisexual", etc.
    private String interestedIn;          // "Men", "Women", "Everyone"
    private String jobTitle;              // Professional title
    private String company;               // Company name
    private String university;            // University name
    private String educationLevel;        // Education level
    private String religiousBeliefs;      // Religious preference
    private String hometown;              // Hometown
    private String politics;              // Political leaning
    private List<String> languages;       // Spoken languages
    private String datingIntentions;      // "Casual", "Serious", "Marriage"
    private String relationshipType;      // "Monogamous", "Non-monogamous"
    private String height;                // Height string
    private String ethnicity;             // Ethnic background
    private String children;              // "No kids", "Have kids", etc.
    private String familyPlans;           // "Want kids", "Don't want kids"
    private String pets;                  // "Dog person", "Cat person", etc.
    private String zodiacSign;            // Astrological sign
}