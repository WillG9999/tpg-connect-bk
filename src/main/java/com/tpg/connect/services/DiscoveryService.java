package com.tpg.connect.services;

import com.tpg.connect.repository.UserProfileRepository;
import com.tpg.connect.model.api.LikeResponse;
import com.tpg.connect.model.dto.DiscoveryRequest;
import com.tpg.connect.model.dto.DiscoverySettingsRequest;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.model.match.UserAction;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.user.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiscoveryService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Mock dailyBatchService
    private final DailyBatchService dailyBatchService = new DailyBatchService();

    @Autowired
    private MatchService matchService;

    @Autowired
    private PotentialMatchesService potentialMatchesService;

    private static final int DEFAULT_DISCOVERY_LIMIT = 10;
    private static final int MAX_DISCOVERY_LIMIT = 50;

    @Cacheable(value = "potentialMatches", key = "'discovery_' + #userId + '_' + #request.hashCode()", unless = "#result.isEmpty()")
    public List<CompleteUserProfile> getPotentialMatches(String userId, DiscoveryRequest request) {
        CompleteUserProfile currentUser = userProfileRepository.findByUserId(userId);
        if (currentUser == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        // Get user's discovery preferences
        UserPreferences preferences = currentUser.getPreferences();
        if (preferences == null) {
            preferences = createDefaultPreferences();
        }

        // Apply filters and get potential matches
        List<CompleteUserProfile> candidates = findCandidates(currentUser, preferences, request);
        
        // Apply matching algorithm
        List<CompleteUserProfile> rankedMatches = rankMatches(currentUser, candidates);
        
        // Limit results
        int limit = Math.min(request.getCount() > 0 ? request.getCount() : DEFAULT_DISCOVERY_LIMIT, MAX_DISCOVERY_LIMIT);
        return rankedMatches.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public LikeResponse likeUser(String userId, String targetUserId) {
        // Validate users exist
        CompleteUserProfile currentUser = userProfileRepository.findByUserId(userId);
        CompleteUserProfile targetUser = userProfileRepository.findByUserId(targetUserId);
        
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user not found: " + userId);
        }
        if (targetUser == null) {
            throw new IllegalArgumentException("Target user not found: " + targetUserId);
        }

        // Record the like action
        UserAction likeAction = new UserAction();
        likeAction.setId(UUID.randomUUID().toString());
        likeAction.setUserId(userId);
        likeAction.setTargetUserId(targetUserId);
        likeAction.setAction(UserAction.ActionType.LIKE);
        likeAction.setTimestamp(LocalDateTime.now());

        // Check if it's a mutual match
        boolean isMutualMatch = matchService.checkForMutualLike(userId, targetUserId);
        
        if (isMutualMatch) {
            // Create match
            Match match = matchService.createMatch(userId, targetUserId);
            return new LikeResponse(true, "It's a match!", true, match);
        } else {
            // Just a like, no match yet
            return new LikeResponse(true, "User liked successfully", false, null);
        }
    }

    public void dislikeUser(String userId, String targetUserId) {
        // Validate users exist
        CompleteUserProfile currentUser = userProfileRepository.findByUserId(userId);
        CompleteUserProfile targetUser = userProfileRepository.findByUserId(targetUserId);
        
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user not found: " + userId);
        }
        if (targetUser == null) {
            throw new IllegalArgumentException("Target user not found: " + targetUserId);
        }

        // Record the dislike action
        UserAction dislikeAction = new UserAction();
        dislikeAction.setId(UUID.randomUUID().toString());
        dislikeAction.setUserId(userId);
        dislikeAction.setTargetUserId(targetUserId);
        dislikeAction.setAction(UserAction.ActionType.PASS);
        dislikeAction.setTimestamp(LocalDateTime.now());

        // Store the action (implementation would persist this)
        // For now, we'll just log it
        System.out.println("User " + userId + " disliked " + targetUserId);
    }

    public Map<String, Object> getDiscoverySettings(String userId) {
        CompleteUserProfile user = userProfileRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = createDefaultPreferences();
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put("minAge", preferences.getMinAge());
        settings.put("maxAge", preferences.getMaxAge());
        settings.put("maxDistance", preferences.getMaxDistance());
        settings.put("preferredGender", preferences.getPreferredGender());
        settings.put("datingIntention", preferences.getDatingIntention());
        settings.put("drinkingPreference", preferences.getDrinkingPreference());
        settings.put("smokingPreference", preferences.getSmokingPreference());

        return settings;
    }

    public Map<String, Object> updateDiscoverySettings(String userId, DiscoverySettingsRequest request) {
        CompleteUserProfile user = userProfileRepository.findByUserId(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        UserPreferences preferences = user.getPreferences();
        if (preferences == null) {
            preferences = createDefaultPreferences();
            user.setPreferences(preferences);
        }

        // Update preferences
        if (request.getMinAge() != null) preferences.setMinAge(request.getMinAge());
        if (request.getMaxAge() != null) preferences.setMaxAge(request.getMaxAge());
        if (request.getMaxDistance() != null) preferences.setMaxDistance(request.getMaxDistance());
        if (request.getPreferredGender() != null) preferences.setPreferredGender(request.getPreferredGender());
        if (request.getRelationshipType() != null) preferences.setDatingIntention(request.getRelationshipType());

        // Update location if provided
        if (request.getLatitude() != null && request.getLongitude() != null) {
            // Update user location (would be stored in user profile)
        }

        // Save updated preferences
        user.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(user);

        return getDiscoverySettings(userId);
    }

    public String getCurrentBatchId(String userId) {
        return dailyBatchService.getCurrentBatchId(userId);
    }

    private List<CompleteUserProfile> findCandidates(CompleteUserProfile currentUser, UserPreferences preferences, DiscoveryRequest request) {
        // Get all users (in real implementation, this would be optimized with database queries)
        List<CompleteUserProfile> allUsers = userProfileRepository.findAll();
        
        return allUsers.stream()
                .filter(user -> !user.getUserId().equals(currentUser.getUserId())) // Exclude self
                .filter(user -> isAgeInRange(user, preferences))
                .filter(user -> isGenderMatch(user, preferences))
                .filter(user -> isLocationInRange(user, currentUser, preferences, request))
                .filter(user -> !hasUserBeenActedOn(currentUser.getUserId(), user.getUserId())) // Not already liked/disliked
                .collect(Collectors.toList());
    }

    private List<CompleteUserProfile> rankMatches(CompleteUserProfile currentUser, List<CompleteUserProfile> candidates) {
        // Simple ranking algorithm - can be enhanced with ML
        return candidates.stream()
                .sorted((u1, u2) -> {
                    int score1 = calculateCompatibilityScore(currentUser, u1);
                    int score2 = calculateCompatibilityScore(currentUser, u2);
                    return Integer.compare(score2, score1); // Descending order
                })
                .collect(Collectors.toList());
    }

    private int calculateCompatibilityScore(CompleteUserProfile user1, CompleteUserProfile user2) {
        int score = 0;
        
        // Interest overlap
        if (user1.getInterests() != null && user2.getInterests() != null) {
            long commonInterests = user1.getInterests().stream()
                    .filter(user2.getInterests()::contains)
                    .count();
            score += (int) (commonInterests * 10);
        }
        
        // Age compatibility
        int ageDiff = Math.abs(user1.getAge() - user2.getAge());
        score += Math.max(0, 50 - ageDiff * 2); // Closer age = higher score
        
        // Location proximity (simplified)
        if (user1.getLocation() != null && user2.getLocation() != null && 
            user1.getLocation().equals(user2.getLocation())) {
            score += 20;
        }
        
        return score;
    }

    private boolean isAgeInRange(CompleteUserProfile user, UserPreferences preferences) {
        return user.getAge() >= preferences.getMinAge() && user.getAge() <= preferences.getMaxAge();
    }

    private boolean isGenderMatch(CompleteUserProfile user, UserPreferences preferences) {
        if ("everyone".equalsIgnoreCase(preferences.getPreferredGender())) {
            return true;
        }
        
        String userGender = user.getProfile() != null ? user.getProfile().getGender() : null;
        return preferences.getPreferredGender().equalsIgnoreCase(userGender);
    }

    private boolean isLocationInRange(CompleteUserProfile user, CompleteUserProfile currentUser, 
                                     UserPreferences preferences, DiscoveryRequest request) {
        // Simplified location check - in real implementation would use actual distance calculation
        if (request.getLatitude() != null && request.getLongitude() != null) {
            // Would calculate actual distance here
            return true; // Placeholder
        }
        
        // Fallback to city/region matching
        return user.getLocation() != null && currentUser.getLocation() != null;
    }

    private boolean hasUserBeenActedOn(String userId, String targetUserId) {
        // In real implementation, would check UserAction repository
        // For now, return false (no previous actions)
        return false;
    }

    private UserPreferences createDefaultPreferences() {
        UserPreferences preferences = new UserPreferences();
        preferences.setMinAge(18);
        preferences.setMaxAge(35);
        preferences.setMaxDistance(25);
        preferences.setPreferredGender("everyone");
        preferences.setDatingIntention("open_to_anything");
        preferences.setDrinkingPreference("no_preference");
        preferences.setSmokingPreference("no_preference");
        return preferences;
    }
}