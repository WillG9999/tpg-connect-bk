package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String connectId;
    private String userId;
    
    // Basic Profile Info
    private String firstName;
    private String lastName;
    private String name;
    private Integer age;
    private Timestamp dateOfBirth;
    private String location;
    
    // Email Verification
    private Boolean emailVerified;
    private Timestamp emailVerifiedAt;
    
    // Photos
    private List<Photo> photos;
    
    // Interests
    private List<String> interests;
    
    // Detailed Profile
    private Profile profile;
    
    // Prompts
    private List<WrittenPrompt> writtenPrompts;
    private List<PollPrompt> pollPrompts;
    
    // Visibility
    private FieldVisibility fieldVisibility;
    
    // Preferences
    private Preferences preferences;
    
    // Notification Settings
    private NotificationSettings notificationSettings;
    
    // Metadata
    private Boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastActive;
    private Integer version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Photo {
        private String id;
        private String url;
        private Boolean isPrimary;
        private Integer order;
        private List<PhotoPrompt> prompts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhotoPrompt {
        private String id;
        private String text;
        private Position position;
        private Style style;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private Double x;
        private Double y;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Style {
        private String backgroundColor;
        private String textColor;
        private Integer fontSize;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WrittenPrompt {
        private String question;
        private String answer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PollPrompt {
        private String question;
        private String description;
        private List<String> options;
        private String selectedOption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldVisibility {
        private Boolean jobTitle;
        private Boolean university;
        private Boolean religiousBeliefs;
        private Boolean politics;
        private Boolean height;
        private Boolean ethnicity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Preferences {
        private String preferredGender;
        private Integer minAge;
        private Integer maxAge;
        private Integer maxDistance;
        private Integer minHeight;
        private Integer maxHeight;
        private String datingIntention;
        private String drinkingPreference;
        private String smokingPreference;
        private String religionImportance;
        private Boolean wantsChildren;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettings {
        private Boolean pushEnabled;
        private Boolean newMatches;
        private Boolean messages;
        private Boolean profileViews;
        private Boolean matchReminders;
        private QuietHours quietHours;
        private Boolean marketing;
        private Boolean safety;
        private Boolean systemUpdates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuietHours {
        private Boolean enabled;
        private String start;
        private String end;
    }
}