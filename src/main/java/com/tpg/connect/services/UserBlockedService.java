package com.tpg.connect.services;

import com.google.cloud.Timestamp;
import com.tpg.connect.model.UserBlocked;
import com.tpg.connect.model.UserBlocked.SafetyBlockRule;
import com.tpg.connect.model.dto.SafetyBlockRuleRequest;
import com.tpg.connect.model.dto.UserBlockedRequest;
import com.tpg.connect.repository.UserBlockedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserBlockedService {

    @Autowired
    private UserBlockedRepository userBlockedRepository;

    // @Cacheable(value = "userBlocked", key = "#userId") // Temporarily disabled for debugging
    public UserBlocked getUserBlockedConfig(String userId) {
        UserBlocked config = userBlockedRepository.findByConnectId(userId);
        if (config == null) {
            config = userBlockedRepository.createEmptyUserBlocked(userId);
        }
        return config;
    }

    public UserBlocked addDirectBlock(String userId, UserBlockedRequest request) {
        UserBlocked config = getUserBlockedConfig(userId);
        
        if (!config.isUserDirectlyBlocked(request.getTargetUserId())) {
            config.addDirectBlock(request.getTargetUserId());
            config = userBlockedRepository.save(config);
            clearUserBlockedCache(userId);
        }
        
        return config;
    }

    public UserBlocked removeDirectBlock(String userId, String targetUserId) {
        UserBlocked config = getUserBlockedConfig(userId);
        
        if (config.isUserDirectlyBlocked(targetUserId)) {
            config.removeDirectBlock(targetUserId);
            config = userBlockedRepository.save(config);
            clearUserBlockedCache(userId);
        }
        
        return config;
    }

    public UserBlocked addSafetyBlock(String userId, SafetyBlockRuleRequest request) {
        validateSafetyBlockRequest(request);
        
        UserBlocked config = getUserBlockedConfig(userId);
        
        SafetyBlockRule rule = SafetyBlockRule.builder()
                .id(UUID.randomUUID().toString())
                .type(request.getType().toUpperCase())
                .pattern(request.getPattern())
                .caseSensitive(request.getCaseSensitive())
                .enabled(request.getEnabled())
                .description(request.getDescription())
                .createdAt(Timestamp.now())
                .updatedAt(Timestamp.now())
                .matchCount(0)
                .build();
        
        config.addSafetyBlock(rule);
        config = userBlockedRepository.save(config);
        clearUserBlockedCache(userId);
        
        return config;
    }

    public UserBlocked updateSafetyBlock(String userId, String ruleId, SafetyBlockRuleRequest request) {
        validateSafetyBlockRequest(request);
        
        UserBlocked config = getUserBlockedConfig(userId);
        SafetyBlockRule existingRule = config.getSafetyBlockById(ruleId);
        
        if (existingRule == null) {
            throw new IllegalArgumentException("Safety block rule not found");
        }
        
        // Remove old rule and add updated one
        config.removeSafetyBlock(ruleId);
        
        SafetyBlockRule updatedRule = SafetyBlockRule.builder()
                .id(ruleId)
                .type(request.getType().toUpperCase())
                .pattern(request.getPattern())
                .caseSensitive(request.getCaseSensitive())
                .enabled(request.getEnabled())
                .description(request.getDescription())
                .createdAt(existingRule.getCreatedAt())
                .updatedAt(Timestamp.now())
                .matchCount(existingRule.getMatchCount())
                .build();
        
        config.addSafetyBlock(updatedRule);
        config = userBlockedRepository.save(config);
        clearUserBlockedCache(userId);
        
        return config;
    }

    public UserBlocked deleteSafetyBlock(String userId, String ruleId) {
        UserBlocked config = getUserBlockedConfig(userId);
        
        if (config.getSafetyBlockById(ruleId) == null) {
            throw new IllegalArgumentException("Safety block rule not found");
        }
        
        config.removeSafetyBlock(ruleId);
        config = userBlockedRepository.save(config);
        clearUserBlockedCache(userId);
        
        return config;
    }


    private void validateSafetyBlockRequest(SafetyBlockRuleRequest request) {
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Block type cannot be empty");
        }

        String blockType = request.getType().toUpperCase();
        if (!isValidBlockType(blockType)) {
            throw new IllegalArgumentException("Invalid block type: " + request.getType());
        }

        if (request.getPattern() == null || request.getPattern().trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern cannot be empty");
        }
    }

    private boolean isValidBlockType(String blockType) {
        if (blockType == null) {
            return false;
        }
        
        // Convert to uppercase for case-insensitive comparison
        String upperBlockType = blockType.toUpperCase();
        
        // Support all frontend block types: Name, Phone, Email, Location, Job, Company, University, Hometown
        // Plus backend types: KEYWORD
        return upperBlockType.equals("NAME") || upperBlockType.equals("LOCATION") || 
               upperBlockType.equals("EMAIL") || upperBlockType.equals("PHONE") ||
               upperBlockType.equals("KEYWORD") || upperBlockType.equals("COMPANY") || 
               upperBlockType.equals("UNIVERSITY") || upperBlockType.equals("JOB") ||
               upperBlockType.equals("HOMETOWN");
    }

    // @CacheEvict(value = "userBlocked", key = "#userId") // Temporarily disabled for debugging
    private void clearUserBlockedCache(String userId) {
        // Cache cleared by annotation
    }
}