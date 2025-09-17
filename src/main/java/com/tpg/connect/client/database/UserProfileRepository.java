package com.tpg.connect.client.database;

import com.tpg.connect.model.user.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserProfileRepository {

    private final Map<String, CompleteUserProfile> userProfiles = new HashMap<>();

    public UserProfileRepository() {
        initializeSampleData();
    }

    public CompleteUserProfile findByUserId(String userId) {
        return userProfiles.get(userId);
    }

    public CompleteUserProfile save(CompleteUserProfile profile) {
        userProfiles.put(profile.getId(), profile);
        return profile;
    }

    public boolean existsByUserId(String userId) {
        return userProfiles.containsKey(userId);
    }

    public void deleteByUserId(String userId) {
        userProfiles.remove(userId);
    }

    private void initializeSampleData() {
        CompleteUserProfile alexProfile = createAlexProfile();
        CompleteUserProfile adminProfile = createAdminProfile();
        CompleteUserProfile userProfile = createUserProfile();

        userProfiles.put("1", adminProfile);
        userProfiles.put("2", userProfile);
        userProfiles.put("user_123", alexProfile);
    }

    private CompleteUserProfile createAlexProfile() {
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setId("user_123");
        profile.setName("Alex Johnson");
        profile.setAge(28);
        profile.setBio("Looking for meaningful connections...");
        profile.setLocation("San Francisco, CA");
        profile.setInterests(Arrays.asList("Music", "Travel", "Food"));

        List<Photo> photos = Arrays.asList(
            new Photo("photo_1", "https://cdn.example.com/photos/1.jpg", true, 1)
        );
        profile.setPhotos(photos);

        UserProfile userProfile = new UserProfile();
        userProfile.setPronouns("they/them");
        userProfile.setGender("Non-binary");
        userProfile.setSexuality("Pansexual");
        userProfile.setInterestedIn("Everyone");
        userProfile.setJobTitle("Product Manager");
        userProfile.setCompany("Tech Startup");
        userProfile.setUniversity("UC Berkeley");
        userProfile.setEducationLevel("Master's Degree");
        userProfile.setReligiousBeliefs("Spiritual");
        userProfile.setHometown("Los Angeles, CA");
        userProfile.setPolitics("Progressive");
        userProfile.setLanguages(Arrays.asList("English", "Spanish"));
        userProfile.setDatingIntentions("Serious relationship");
        userProfile.setRelationshipType("Monogamous");
        userProfile.setHeight("5'8\"");
        userProfile.setEthnicity("Mixed/Multiracial");
        userProfile.setChildren("No kids");
        userProfile.setFamilyPlans("Open to kids");
        userProfile.setPets("Cat person");
        userProfile.setZodiacSign("Sagittarius");
        profile.setProfile(userProfile);

        List<WrittenPrompt> writtenPrompts = Arrays.asList(
            new WrittenPrompt("My simple pleasures", "Weekend farmers market visits...")
        );
        profile.setWrittenPrompts(writtenPrompts);

        List<PollPrompt> pollPrompts = Arrays.asList(
            new PollPrompt("Best first date idea", "What sounds perfect?", 
                Arrays.asList("Coffee", "Museum", "Food trucks", "Hiking"))
        );
        profile.setPollPrompts(pollPrompts);

        FieldVisibility fieldVisibility = new FieldVisibility();
        fieldVisibility.setJobTitle(true);
        fieldVisibility.setReligiousBeliefs(false);
        profile.setFieldVisibility(fieldVisibility);

        UserPreferences preferences = new UserPreferences();
        preferences.setPreferredGender("both");
        preferences.setMinAge(22);
        preferences.setMaxAge(35);
        preferences.setMinHeight(60);
        preferences.setMaxHeight(84);
        preferences.setDatingIntention("serious");
        preferences.setDrinkingPreference("sometimes");
        preferences.setSmokingPreference("never");
        profile.setPreferences(preferences);

        return profile;
    }

    private CompleteUserProfile createAdminProfile() {
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setId("1");
        profile.setName("Admin User");
        profile.setAge(30);
        profile.setBio("System administrator");
        profile.setLocation("New York, NY");
        profile.setInterests(Arrays.asList("Technology", "Programming"));

        List<Photo> photos = Arrays.asList(
            new Photo("admin_photo_1", "https://cdn.example.com/admin/1.jpg", true, 1)
        );
        profile.setPhotos(photos);

        UserProfile userProfile = new UserProfile();
        userProfile.setJobTitle("System Administrator");
        userProfile.setCompany("Tech Corp");
        profile.setProfile(userProfile);

        profile.setWrittenPrompts(new ArrayList<>());
        profile.setPollPrompts(new ArrayList<>());
        profile.setFieldVisibility(new FieldVisibility());
        profile.setPreferences(new UserPreferences());

        return profile;
    }

    private CompleteUserProfile createUserProfile() {
        CompleteUserProfile profile = new CompleteUserProfile();
        profile.setId("2");
        profile.setName("Regular User");
        profile.setAge(25);
        profile.setBio("Just a regular user");
        profile.setLocation("Austin, TX");
        profile.setInterests(Arrays.asList("Movies", "Books", "Sports"));

        List<Photo> photos = Arrays.asList(
            new Photo("user_photo_1", "https://cdn.example.com/user/1.jpg", true, 1)
        );
        profile.setPhotos(photos);

        UserProfile userProfile = new UserProfile();
        userProfile.setJobTitle("Software Developer");
        userProfile.setCompany("Startup Inc");
        profile.setProfile(userProfile);

        profile.setWrittenPrompts(new ArrayList<>());
        profile.setPollPrompts(new ArrayList<>());
        profile.setFieldVisibility(new FieldVisibility());
        profile.setPreferences(new UserPreferences());

        return profile;
    }
}