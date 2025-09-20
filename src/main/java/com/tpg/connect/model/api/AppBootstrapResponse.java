package com.tpg.connect.model.api;

import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.system.AppConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppBootstrapResponse {
    // Complete user profile data
    private CompleteUserProfile user;
    
    // App configuration and limits
    private AppConfig config;
    
    // All available prompts
    private PromptData prompts;
    
    // Constants and enums
    private AppConstants constants;
    
    // Cache metadata
    private CacheMetadata cache;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptData {
        private List<String> writtenPrompts;
        private List<String> picturePrompts;
        private List<String> pollPrompts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppConstants {
        private Map<String, List<String>> genderOptions;
        private Map<String, List<String>> sexualityOptions;
        private Map<String, List<String>> relationshipOptions;
        private Map<String, List<String>> educationOptions;
        private Map<String, List<String>> religionOptions;
        private Map<String, List<String>> politicsOptions;
        private Map<String, List<String>> lifestyleOptions;
        private List<String> interests;
        private List<String> languages;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheMetadata {
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;
        private String version;
        private long ttlSeconds;
    }
}