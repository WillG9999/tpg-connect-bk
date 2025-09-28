package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CompleteUserProfile {
    private String connectId;  // 12-digit ConnectID - Document ID
    
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String email;
    private List<EnhancedPhoto> photos;
    private String location;
    
    private DetailedProfile profile;
    private List<WrittenPrompt> writtenPrompts;
    private List<PollPrompt> pollPrompts;
    private FieldVisibility fieldVisibility;
    private UserPreferences preferences;
    private NotificationSettings notifications;
    
    // Additional profile fields from Firebase schema
    private boolean active = true;
    private boolean isOnline = false;
    private boolean isPremium = false;
    private boolean isVerified = false;
    private String subscriptionType;
    
    // NoSQL metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActive;
    private int version = 1;
    
    // Computed/Helper methods for backward compatibility
    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return firstName != null ? firstName : (lastName != null ? lastName : "");
    }
    
    public void setName(String name) {
        // For backward compatibility - split name into first/last
        if (name != null && !name.trim().isEmpty()) {
            String[] parts = name.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        }
    }
    
    public int getAge() {
        if (dateOfBirth != null) {
            return java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
        }
        return 0;
    }
    
    public void setAge(Integer age) {
        // For backward compatibility - estimate birth year
        if (age != null && age > 0) {
            this.dateOfBirth = java.time.LocalDate.now().minusYears(age);
        }
    }
    
    public String getId() {
        return connectId;
    }
    
    public void setId(String id) {
        this.connectId = id;
    }
    
    public String getUserId() {
        return connectId;
    }
    
    public void setUserId(String userId) {
        this.connectId = userId;
    }
    
    public List<String> getInterests() {
        // Extract interests from detailed profile or return empty list
        if (profile != null && profile.getLanguages() != null) {
            return profile.getLanguages(); // Using languages as placeholder for interests
        }
        return new java.util.ArrayList<>();
    }
    
    public void setInterests(List<String> interests) {
        // Store interests in detailed profile
        if (profile == null) {
            profile = new DetailedProfile();
        }
        profile.setLanguages(interests); // Using languages as placeholder for interests
    }
    
    // Additional getter methods for boolean fields
    public boolean getActive() {
        return active;
    }
    
    public boolean getOnline() {
        return isOnline;
    }
    
    public boolean getPremium() {
        return isPremium;
    }
    
    public boolean getVerified() {
        return isVerified;
    }
    
    // Getter for interestedIn from nested profile
    public String getInterestedIn() {
        return profile != null ? profile.getInterestedIn() : null;
    }
    
    // Setter for interestedIn in nested profile
    public void setInterestedIn(String interestedIn) {
        if (profile == null) {
            profile = new DetailedProfile();
        }
        profile.setInterestedIn(interestedIn);
    }
    
    // Override getter for gender to prefer profile version over root level
    public String getGender() {
        // Check nested profile first, then fall back to root level field
        if (profile != null && profile.getGender() != null) {
            return profile.getGender();
        }
        return this.gender;
    }
    
    // Override setter for gender to set both locations for compatibility
    public void setGender(String gender) {
        this.gender = gender; // Set root level for backward compatibility
        if (profile == null) {
            profile = new DetailedProfile();
        }
        profile.setGender(gender); // Also set in profile
    }
}