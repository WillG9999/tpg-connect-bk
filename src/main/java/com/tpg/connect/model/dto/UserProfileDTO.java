package com.tpg.connect.model.dto;

import com.tpg.connect.model.user.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class UserProfileDTO {
    
    // Firebase user_profiles collection structure
    @JsonProperty("connectId")
    private String connectId;
    
    @JsonProperty("userId") 
    private String userId;
    
    // Basic Profile Info
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("age")
    private int age;
    
    @JsonProperty("dateOfBirth")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    @JsonProperty("location")
    private String location;
    
    // Email Verification Status
    @JsonProperty("emailVerified")
    private boolean emailVerified;
    
    @JsonProperty("emailVerifiedAt")
    private LocalDateTime emailVerifiedAt;
    
    // Photos with Prompts (Array of Objects matching Firebase schema)
    @JsonProperty("photos")
    private List<PhotoDTO> photos = new ArrayList<>();
    
    // Photo Prompts (Flattened for frontend compatibility)
    @JsonProperty("photoPrompts")
    private Map<String, Map<String, String>> photoPrompts = new HashMap<>();
    
    // Interests & Lifestyle
    @JsonProperty("interests")
    private List<String> interests = new ArrayList<>();
    
    // Detailed Profile (Nested Object) - matches Firebase 'profile' field
    @JsonProperty("profile")
    private DetailedProfileDTO profile;
    
    // Written Prompts (Array of Objects)
    @JsonProperty("writtenPrompts")
    private List<WrittenPromptDTO> writtenPrompts = new ArrayList<>();
    
    // Poll Prompts (Array of Objects)
    @JsonProperty("pollPrompts")
    private List<PollPromptDTO> pollPrompts = new ArrayList<>();
    
    // Field Visibility Settings (Nested Object)
    @JsonProperty("fieldVisibility")
    private Map<String, Boolean> fieldVisibility = new HashMap<>();
    
    // User Preferences (Nested Object)
    @JsonProperty("preferences")
    private PreferencesDTO preferences;
    
    // Notification Settings (Nested Object)
    @JsonProperty("notificationSettings")
    private NotificationSettingsDTO notificationSettings;
    
    // Metadata
    @JsonProperty("active")
    private boolean active = true;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("lastActive")
    private LocalDateTime lastActive;
    
    @JsonProperty("version")
    private int version = 1;
    
    // Firebase Photo DTO matching schema
    @Data
    @NoArgsConstructor
    public static class PhotoDTO {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("url")
        private String url;
        
        @JsonProperty("isPrimary")
        private boolean isPrimary = false;
        
        @JsonProperty("order")
        private int order;
        
        @JsonProperty("prompts")
        private List<PhotoPromptDTO> prompts = new ArrayList<>();
    }
    
    // Photo Prompt DTO
    @Data
    @NoArgsConstructor  
    public static class PhotoPromptDTO {
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("text")
        private String text;
        
        @JsonProperty("position")
        private PositionDTO position;
        
        @JsonProperty("style")
        private StyleDTO style;
    }
    
    // Position DTO for photo prompts
    @Data
    @NoArgsConstructor
    public static class PositionDTO {
        @JsonProperty("x")
        private double x = 0.5; // 0-1 range
        
        @JsonProperty("y")
        private double y = 0.8; // 0-1 range
    }
    
    // Style DTO for photo prompts
    @Data
    @NoArgsConstructor
    public static class StyleDTO {
        @JsonProperty("backgroundColor")
        private String backgroundColor = "rgba(0,0,0,0.7)";
        
        @JsonProperty("textColor")
        private String textColor = "#FFFFFF";
        
        @JsonProperty("fontSize")
        private int fontSize = 14;
    }
    
    // Detailed Profile DTO matching Firebase 'profile' nested object
    @Data
    @NoArgsConstructor
    public static class DetailedProfileDTO {
        @JsonProperty("pronouns")
        private String pronouns = "";
        
        @JsonProperty("gender")
        private String gender = "";
        
        @JsonProperty("sexuality")
        private String sexuality = "";
        
        @JsonProperty("interestedIn")
        private String interestedIn = "";
        
        @JsonProperty("jobTitle")
        private String jobTitle = "";
        
        @JsonProperty("company")
        private String company = "";
        
        @JsonProperty("university")
        private String university = "";
        
        @JsonProperty("educationLevel")
        private String educationLevel = "";
        
        @JsonProperty("religiousBeliefs")
        private String religiousBeliefs = "";
        
        @JsonProperty("hometown")
        private String hometown = "";
        
        @JsonProperty("politics")
        private String politics = "";
        
        @JsonProperty("languages")
        private List<String> languages = new ArrayList<>();
        
        @JsonProperty("datingIntentions")
        private String datingIntentions = "";
        
        @JsonProperty("relationshipType")
        private String relationshipType = "";
        
        @JsonProperty("height")
        private String height = "";
        
        @JsonProperty("ethnicity")
        private String ethnicity = "";
        
        @JsonProperty("children")
        private String children = "";
        
        @JsonProperty("familyPlans")
        private String familyPlans = "";
        
        @JsonProperty("pets")
        private String pets = "";
        
        @JsonProperty("zodiacSign")
        private String zodiacSign = "";
    }
    
    // Written Prompts DTO
    @Data
    @NoArgsConstructor
    public static class WrittenPromptDTO {
        @JsonProperty("question")
        private String question = "";
        
        @JsonProperty("answer")
        private String answer = "";
    }
    
    // Poll Prompts DTO
    @Data
    @NoArgsConstructor
    public static class PollPromptDTO {
        @JsonProperty("question")
        private String question = "";
        
        @JsonProperty("description")
        private String description = "";
        
        @JsonProperty("options")
        private List<String> options = new ArrayList<>();
        
        @JsonProperty("selectedOption")
        private String selectedOption = "";
    }
    
    // Preferences DTO matching Firebase 'preferences' nested object
    @Data
    @NoArgsConstructor
    public static class PreferencesDTO {
        @JsonProperty("preferredGender")
        private String preferredGender = "both";
        
        @JsonProperty("minAge")
        private int minAge = 18;
        
        @JsonProperty("maxAge")
        private int maxAge = 99;
        
        @JsonProperty("maxDistance")
        private int maxDistance = 25;
        
        @JsonProperty("minHeight")
        private int minHeight = 48; // inches
        
        @JsonProperty("maxHeight")
        private int maxHeight = 96; // inches
        
        @JsonProperty("datingIntention")
        private String datingIntention = "";
        
        @JsonProperty("drinkingPreference")
        private String drinkingPreference = "";
        
        @JsonProperty("smokingPreference")
        private String smokingPreference = "";
        
        @JsonProperty("religionImportance")
        private String religionImportance = "Low";
        
        @JsonProperty("wantsChildren")
        private boolean wantsChildren = false;
    }
    
    // Notification Settings DTO matching Firebase 'notificationSettings' nested object
    @Data
    @NoArgsConstructor
    public static class NotificationSettingsDTO {
        @JsonProperty("pushEnabled")
        private boolean pushEnabled = true;
        
        @JsonProperty("newMatches")
        private boolean newMatches = true;
        
        @JsonProperty("messages")
        private boolean messages = true;
        
        @JsonProperty("profileViews")
        private boolean profileViews = false;
        
        @JsonProperty("matchReminders")
        private boolean matchReminders = true;
        
        @JsonProperty("quietHours")
        private QuietHoursDTO quietHours;
        
        @JsonProperty("marketing")
        private boolean marketing = false;
        
        @JsonProperty("safety")
        private boolean safety = true;
        
        @JsonProperty("systemUpdates")
        private boolean systemUpdates = true;
    }
    
    // Quiet Hours DTO
    @Data
    @NoArgsConstructor
    public static class QuietHoursDTO {
        @JsonProperty("enabled")
        private boolean enabled = false;
        
        @JsonProperty("start")
        private String start = "22:00";
        
        @JsonProperty("end") 
        private String end = "08:00";
    }
    
    // Convert from CompleteUserProfile to Firebase-compatible DTO
    public static UserProfileDTO fromCompleteUserProfile(CompleteUserProfile profile) {
        UserProfileDTO dto = new UserProfileDTO();
        
        // Basic info with null safety
        dto.setConnectId(profile.getConnectId() != null ? profile.getConnectId() : "");
        dto.setUserId(profile.getConnectId() != null ? profile.getConnectId() : ""); // Same as connectId
        dto.setFirstName(profile.getFirstName() != null ? profile.getFirstName() : "");
        dto.setLastName(profile.getLastName() != null ? profile.getLastName() : "");
        dto.setName(profile.getFirstName() != null ? profile.getFirstName() : ""); // Only first name as requested
        
        // Calculate age from date of birth
        if (profile.getDateOfBirth() != null) {
            int calculatedAge = java.time.Period.between(profile.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            dto.setAge(calculatedAge);
            dto.setDateOfBirth(profile.getDateOfBirth());
        }
        
        dto.setLocation(profile.getLocation() != null ? profile.getLocation() : "");
        dto.setEmailVerified(profile.getVerified());
        dto.setEmailVerifiedAt(profile.getUpdatedAt()); // Use profile update time as proxy
        
        // Convert EnhancedPhoto objects to Firebase PhotoDTO structure
        if (profile.getPhotos() != null) {
            try {
                List<PhotoDTO> photoDTOs = profile.getPhotos().stream()
                    .sorted((p1, p2) -> Integer.compare(p1.getOrder(), p2.getOrder()))
                    .map(photo -> {
                        PhotoDTO photoDTO = new PhotoDTO();
                        photoDTO.setId(photo.getId() != null ? photo.getId() : "photo_" + System.currentTimeMillis());
                        photoDTO.setUrl(photo.getUrl() != null ? photo.getUrl() : "");
                        photoDTO.setPrimary(photo.isPrimary());
                        photoDTO.setOrder(photo.getOrder());
                        
                        // Convert photo prompts if they exist
                        if (photo.getPrompts() != null) {
                            List<PhotoPromptDTO> promptDTOs = photo.getPrompts().stream()
                                .map(prompt -> {
                                    PhotoPromptDTO promptDTO = new PhotoPromptDTO();
                                    promptDTO.setId(prompt.getId() != null ? prompt.getId() : "prompt_" + System.currentTimeMillis());
                                    promptDTO.setText(prompt.getText() != null ? prompt.getText() : "");
                                    
                                    PositionDTO position = new PositionDTO();
                                    if (prompt.getPosition() != null) {
                                        position.setX(prompt.getPosition().getX());
                                        position.setY(prompt.getPosition().getY());
                                    }
                                    promptDTO.setPosition(position);
                                    
                                    StyleDTO style = new StyleDTO();
                                    if (prompt.getStyle() != null) {
                                        style.setBackgroundColor(prompt.getStyle().getBackgroundColor() != null ? 
                                            prompt.getStyle().getBackgroundColor() : "rgba(0,0,0,0.7)");
                                        style.setTextColor(prompt.getStyle().getTextColor() != null ? 
                                            prompt.getStyle().getTextColor() : "#FFFFFF");
                                        style.setFontSize(prompt.getStyle().getFontSize());
                                    }
                                    promptDTO.setStyle(style);
                                    
                                    return promptDTO;
                                })
                                .collect(Collectors.toList());
                            photoDTO.setPrompts(promptDTOs);
                        }
                        
                        return photoDTO;
                    })
                    .filter(photoDTO -> !photoDTO.getUrl().isEmpty())
                    .collect(Collectors.toList());
                dto.setPhotos(photoDTOs);
                
                // Create flattened photo prompts map for frontend compatibility
                Map<String, Map<String, String>> flattenedPrompts = new HashMap<>();
                for (int i = 0; i < photoDTOs.size(); i++) {
                    PhotoDTO photoDTO = photoDTOs.get(i);
                    if (photoDTO.getPrompts() != null && !photoDTO.getPrompts().isEmpty()) {
                        // Take the first prompt for each photo (simple implementation)
                        PhotoPromptDTO firstPrompt = photoDTO.getPrompts().get(0);
                        Map<String, String> promptData = new HashMap<>();
                        promptData.put("prompt", firstPrompt.getText() != null ? firstPrompt.getText() : "");
                        promptData.put("caption", firstPrompt.getText() != null ? firstPrompt.getText() : "");
                        flattenedPrompts.put(String.valueOf(i), promptData);
                    }
                }
                dto.setPhotoPrompts(flattenedPrompts);
                
            } catch (Exception e) {
                dto.setPhotos(new ArrayList<>());
                dto.setPhotoPrompts(new HashMap<>());
            }
        }
        
        // Set interests
        dto.setInterests(profile.getInterests() != null ? profile.getInterests() : new ArrayList<>());
        
        // Convert DetailedProfile to nested 'profile' object
        if (profile.getProfile() != null) {
            DetailedProfile detailedProfile = profile.getProfile();
            DetailedProfileDTO profileDTO = new DetailedProfileDTO();
            
            profileDTO.setPronouns(detailedProfile.getPronouns() != null ? detailedProfile.getPronouns() : "");
            profileDTO.setGender(detailedProfile.getGender() != null ? detailedProfile.getGender() : "");
            profileDTO.setSexuality(detailedProfile.getSexuality() != null ? detailedProfile.getSexuality() : "");
            profileDTO.setInterestedIn(detailedProfile.getInterestedIn() != null ? detailedProfile.getInterestedIn() : "");
            profileDTO.setJobTitle(detailedProfile.getJobTitle() != null ? detailedProfile.getJobTitle() : "");
            profileDTO.setCompany(detailedProfile.getCompany() != null ? detailedProfile.getCompany() : "");
            profileDTO.setUniversity(detailedProfile.getUniversity() != null ? detailedProfile.getUniversity() : "");
            profileDTO.setEducationLevel(detailedProfile.getEducationLevel() != null ? detailedProfile.getEducationLevel() : "");
            profileDTO.setReligiousBeliefs(detailedProfile.getReligiousBeliefs() != null ? detailedProfile.getReligiousBeliefs() : "");
            profileDTO.setHometown(detailedProfile.getHometown() != null ? detailedProfile.getHometown() : "");
            profileDTO.setPolitics(detailedProfile.getPolitics() != null ? detailedProfile.getPolitics() : "");
            profileDTO.setLanguages(detailedProfile.getLanguages() != null ? detailedProfile.getLanguages() : new ArrayList<>());
            profileDTO.setDatingIntentions(detailedProfile.getDatingIntentions() != null ? detailedProfile.getDatingIntentions() : "");
            profileDTO.setRelationshipType(detailedProfile.getRelationshipType() != null ? detailedProfile.getRelationshipType() : "");
            profileDTO.setHeight(detailedProfile.getHeight() != null ? detailedProfile.getHeight() : "");
            profileDTO.setEthnicity(detailedProfile.getEthnicity() != null ? detailedProfile.getEthnicity() : "");
            profileDTO.setChildren(detailedProfile.getChildren() != null ? detailedProfile.getChildren() : "");
            profileDTO.setFamilyPlans(detailedProfile.getFamilyPlans() != null ? detailedProfile.getFamilyPlans() : "");
            profileDTO.setPets(detailedProfile.getPets() != null ? detailedProfile.getPets() : "");
            profileDTO.setZodiacSign(detailedProfile.getZodiacSign() != null ? detailedProfile.getZodiacSign() : "");
            
            dto.setProfile(profileDTO);
        } else {
            dto.setProfile(new DetailedProfileDTO()); // Default empty profile
        }
        
        // Convert written prompts
        if (profile.getWrittenPrompts() != null) {
            List<WrittenPromptDTO> writtenPromptDTOs = profile.getWrittenPrompts().stream()
                .map(wp -> {
                    WrittenPromptDTO promptDTO = new WrittenPromptDTO();
                    promptDTO.setQuestion(wp.getQuestion() != null ? wp.getQuestion() : "");
                    promptDTO.setAnswer(wp.getAnswer() != null ? wp.getAnswer() : "");
                    return promptDTO;
                })
                .collect(Collectors.toList());
            dto.setWrittenPrompts(writtenPromptDTOs);
        }
        
        // Convert poll prompts
        if (profile.getPollPrompts() != null) {
            List<PollPromptDTO> pollPromptDTOs = profile.getPollPrompts().stream()
                .map(pp -> {
                    PollPromptDTO promptDTO = new PollPromptDTO();
                    promptDTO.setQuestion(pp.getQuestion() != null ? pp.getQuestion() : "");
                    promptDTO.setDescription(pp.getDescription() != null ? pp.getDescription() : "");
                    promptDTO.setOptions(pp.getOptions() != null ? pp.getOptions() : new ArrayList<>());
                    promptDTO.setSelectedOption(pp.getSelectedOption() != null ? pp.getSelectedOption() : "");
                    return promptDTO;
                })
                .collect(Collectors.toList());
            dto.setPollPrompts(pollPromptDTOs);
        }
        
        // Set field visibility
        if (profile.getFieldVisibility() != null) {
            FieldVisibility fv = profile.getFieldVisibility();
            Map<String, Boolean> visibility = new HashMap<>();
            visibility.put("jobTitle", fv.isJobTitle());
            visibility.put("company", fv.isCompany());
            visibility.put("university", fv.isUniversity());
            visibility.put("religiousBeliefs", fv.isReligiousBeliefs());
            visibility.put("politics", fv.isPolitics());
            visibility.put("hometown", fv.isHometown());
            visibility.put("height", fv.isHeight());
            visibility.put("ethnicity", fv.isEthnicity());
            dto.setFieldVisibility(visibility);
        } else {
            // Default field visibility - all visible
            Map<String, Boolean> defaultVisibility = new HashMap<>();
            defaultVisibility.put("jobTitle", true);
            defaultVisibility.put("company", true);
            defaultVisibility.put("university", true);
            defaultVisibility.put("religiousBeliefs", true);
            defaultVisibility.put("politics", true);
            defaultVisibility.put("hometown", true);
            defaultVisibility.put("height", true);
            defaultVisibility.put("ethnicity", true);
            dto.setFieldVisibility(defaultVisibility);
        }
        
        // Set preferences (create default if not exists)
        if (profile.getPreferences() != null) {
            UserPreferences prefs = profile.getPreferences();
            PreferencesDTO prefsDTO = new PreferencesDTO();
            prefsDTO.setPreferredGender(prefs.getPreferredGender() != null ? prefs.getPreferredGender() : "both");
            prefsDTO.setMinAge(prefs.getMinAge());
            prefsDTO.setMaxAge(prefs.getMaxAge());
            prefsDTO.setMaxDistance(prefs.getMaxDistance());
            prefsDTO.setMinHeight(prefs.getMinHeight());
            prefsDTO.setMaxHeight(prefs.getMaxHeight());
            prefsDTO.setDatingIntention(prefs.getDatingIntention() != null ? prefs.getDatingIntention() : "");
            prefsDTO.setDrinkingPreference(prefs.getDrinkingPreference() != null ? prefs.getDrinkingPreference() : "");
            prefsDTO.setSmokingPreference(prefs.getSmokingPreference() != null ? prefs.getSmokingPreference() : "");
            prefsDTO.setReligionImportance("Low"); // Default value
            prefsDTO.setWantsChildren(false); // Default value
            dto.setPreferences(prefsDTO);
        } else {
            dto.setPreferences(new PreferencesDTO()); // Default preferences
        }
        
        // Set notification settings (create default if not exists)
        if (profile.getNotifications() != null) {
            NotificationSettings settings = profile.getNotifications();
            NotificationSettingsDTO settingsDTO = new NotificationSettingsDTO();
            settingsDTO.setPushEnabled(true); // Default to enabled
            settingsDTO.setNewMatches(settings.isNewMatches());
            settingsDTO.setMessages(settings.isMessages());
            settingsDTO.setProfileViews(false); // Default to false
            settingsDTO.setMatchReminders(true); // Default to true
            settingsDTO.setMarketing(settings.isPromotions());
            settingsDTO.setSafety(true); // Default to true
            settingsDTO.setSystemUpdates(settings.isEmailUpdates());
            
            // Set quiet hours (default disabled)
            QuietHoursDTO quietHoursDTO = new QuietHoursDTO();
            settingsDTO.setQuietHours(quietHoursDTO);
            
            dto.setNotificationSettings(settingsDTO);
        } else {
            NotificationSettingsDTO defaultSettings = new NotificationSettingsDTO();
            defaultSettings.setQuietHours(new QuietHoursDTO()); // Default quiet hours
            dto.setNotificationSettings(defaultSettings);
        }
        
        // Set metadata
        dto.setActive(profile.getActive());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        dto.setLastActive(profile.getLastActive());
        dto.setVersion(profile.getVersion());
        
        return dto;
    }
}