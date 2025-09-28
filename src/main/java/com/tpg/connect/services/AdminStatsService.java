package com.tpg.connect.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service for generating admin statistics and analytics
 */
@Service
public class AdminStatsService {

    private static final Logger logger = LoggerFactory.getLogger(AdminStatsService.class);

    @Autowired
    private Firestore firestore;

    /**
     * Get demographics statistics including gender and interest distribution
     */
    public Map<String, Object> getDemographicsStatistics() throws ExecutionException, InterruptedException {
        logger.info("📊 Calculating demographics statistics from user profiles");

        // Get all user profiles
        QuerySnapshot querySnapshot = firestore.collection("user_profiles")
                .get()
                .get();

        // Initialize counters
        Map<String, Integer> genderCounts = new HashMap<>();
        Map<String, Integer> interestedInCounts = new HashMap<>();
        Map<String, Integer> crossTabulation = new HashMap<>();
        int totalUsers = 0;

        // Process each user profile
        for (QueryDocumentSnapshot document : querySnapshot) {
            Map<String, Object> data = document.getData();
            totalUsers++;

            // Extract gender
            String gender = (String) data.get("gender");
            if (gender != null) {
                gender = normalizeGender(gender);
                genderCounts.put(gender, genderCounts.getOrDefault(gender, 0) + 1);
            }

            // Extract interestedIn from nested profile object
            String interestedIn = null;
            Object profileObj = data.get("profile");
            if (profileObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) profileObj;
                interestedIn = (String) profile.get("interestedIn");
            }

            if (interestedIn != null) {
                interestedIn = normalizeInterestedIn(interestedIn);
                interestedInCounts.put(interestedIn, interestedInCounts.getOrDefault(interestedIn, 0) + 1);

                // Cross-tabulation: gender × interestedIn
                if (gender != null) {
                    String crossKey = gender + " → " + interestedIn;
                    crossTabulation.put(crossKey, crossTabulation.getOrDefault(crossKey, 0) + 1);
                }
            }
        }

        logger.info("📊 Processed {} user profiles for demographics", totalUsers);
        
        // Build response
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("genderDistribution", buildGenderStats(genderCounts, totalUsers));
        stats.put("interestDistribution", buildInterestStats(interestedInCounts, totalUsers));
        stats.put("crossTabulation", buildCrossTabStats(crossTabulation, totalUsers));
        stats.put("generatedAt", java.time.Instant.now().toString());

        return stats;
    }

    private String normalizeGender(String gender) {
        if (gender == null) return "Unknown";
        
        switch (gender.toLowerCase().trim()) {
            case "male":
            case "man":
                return "Male";
            case "female":
            case "woman":
                return "Female";
            case "non-binary":
            case "nonbinary":
            case "non_binary":
                return "Non-binary";
            default:
                return "Other";
        }
    }

    private String normalizeInterestedIn(String interestedIn) {
        if (interestedIn == null) return "Unknown";
        
        switch (interestedIn.toLowerCase().trim()) {
            case "male":
            case "man":
            case "men":
                return "Men";
            case "female":
            case "woman":
            case "women":
                return "Women";
            case "everyone":
            case "all":
            case "both":
                return "Everyone";
            default:
                return "Other";
        }
    }

    private Map<String, Object> buildGenderStats(Map<String, Integer> genderCounts, int total) {
        Map<String, Object> stats = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : genderCounts.entrySet()) {
            Map<String, Object> genderStat = new HashMap<>();
            int count = entry.getValue();
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            
            genderStat.put("count", count);
            genderStat.put("percentage", Math.round(percentage * 10) / 10.0); // Round to 1 decimal
            
            stats.put(entry.getKey(), genderStat);
        }
        
        return stats;
    }

    private Map<String, Object> buildInterestStats(Map<String, Integer> interestedInCounts, int total) {
        Map<String, Object> stats = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : interestedInCounts.entrySet()) {
            Map<String, Object> interestStat = new HashMap<>();
            int count = entry.getValue();
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            
            interestStat.put("count", count);
            interestStat.put("percentage", Math.round(percentage * 10) / 10.0); // Round to 1 decimal
            
            stats.put(entry.getKey(), interestStat);
        }
        
        return stats;
    }

    private Map<String, Object> buildCrossTabStats(Map<String, Integer> crossTabulation, int total) {
        Map<String, Object> stats = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : crossTabulation.entrySet()) {
            Map<String, Object> crossStat = new HashMap<>();
            int count = entry.getValue();
            double percentage = total > 0 ? (count * 100.0 / total) : 0.0;
            
            crossStat.put("count", count);
            crossStat.put("percentage", Math.round(percentage * 10) / 10.0); // Round to 1 decimal
            
            stats.put(entry.getKey(), crossStat);
        }
        
        return stats;
    }
}