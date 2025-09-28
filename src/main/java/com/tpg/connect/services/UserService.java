package com.tpg.connect.services;

import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.User;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.SafetyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class UserService {
    
    // TODO: Implement premium subscription validation
    // TODO: Add payment gateway integration (Stripe/Apple Pay/Google Pay)
    // TODO: Handle subscription status and expiry dates

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SafetyService safetyService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return rawPassword.equals(encodedPassword);
    }

    public User createUser(String username, String email, String password, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        String userId = String.valueOf(System.currentTimeMillis());
        User user = User.builder()
                .connectId(userId)
                .username(username)
                .email(email)
                .passwordHash(password)
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (existingUser != null) {
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }

    public boolean deactivateUserByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setActive(false);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean activateUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    
    public Map<String, Object> blockUser(String userId, String targetUserId) {
        try {
            // Delegate to safety service
            safetyService.blockUser(userId, targetUserId, "Blocked by user");
            return Map.of("success", true, "message", "User blocked successfully", "blockedUserId", targetUserId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to block user: " + e.getMessage());
        }
    }
    
    public Map<String, Object> reportUser(String userId, String targetUserId, String reason) {
        try {
            // Create a basic report request
            com.tpg.connect.model.dto.ReportUserRequest request = new com.tpg.connect.model.dto.ReportUserRequest();
            request.setTargetUserId(targetUserId);
            request.setReasons(java.util.List.of(reason));
            
            String reportId = safetyService.reportUser(userId, request);
            return Map.of(
                "success", true, 
                "message", "User reported successfully",
                "reportId", reportId,
                "reportedUserId", targetUserId
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to report user: " + e.getMessage());
        }
    }
    
    // Deactivate user method
    public boolean deactivateUser(String userId) {
        try {
            // Find user by ID and deactivate
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setActive(false);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage());
        }
    }
    
    public CompleteUserProfile getUserProfile(String userId) {
        return userProfileRepository.findByUserId(userId);
    }
}