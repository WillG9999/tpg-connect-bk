package com.tpg.connect.services;

import com.tpg.connect.client.database.UserProfileRepository;
import com.tpg.connect.model.user.CompleteUserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Cacheable(value = "userProfiles", key = "'user_profile_' + #userId", unless = "#result == null")
    public CompleteUserProfile getCurrentUserProfile(String userId, boolean includePreferences) {
        CompleteUserProfile profile = userProfileRepository.findByUserId(userId);
        
        if (profile == null) {
            return null;
        }

        if (!includePreferences) {
            profile.setPreferences(null);
        }

        return profile;
    }

    public CompleteUserProfile updateUserProfile(CompleteUserProfile profile) {
        return userProfileRepository.save(profile);
    }

    public boolean existsByUserId(String userId) {
        return userProfileRepository.existsByUserId(userId);
    }

    public void deleteUserProfile(String userId) {
        userProfileRepository.deleteByUserId(userId);
    }

    public boolean isValidAge(int age) {
        return age >= 18 && age <= 100;
    }

    public boolean isValidPhotosCount(int photoCount) {
        return photoCount >= 1 && photoCount <= 6;
    }
}