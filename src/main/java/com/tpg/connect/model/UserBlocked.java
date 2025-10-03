package com.tpg.connect.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;

import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlocked {
    private String connectId;
    private List<String> directBlocks;
    private List<SafetyBlockRule> safetyBlocks;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SafetyBlockRule {
        private String id;
        private String type;           // NAME, LOCATION, EMAIL, PHONE, KEYWORD, COMPANY, UNIVERSITY, JOB, HOMETOWN
        private String pattern;        // The pattern to match against
        private Boolean caseSensitive;
        private Boolean enabled;
        private String description;
        private Timestamp createdAt;
        private Timestamp updatedAt;
        private Integer matchCount;    // How many times this rule has blocked someone
    }
    
    // Initialize empty lists if null
    public List<String> getDirectBlocks() {
        if (directBlocks == null) {
            directBlocks = new ArrayList<>();
        }
        return directBlocks;
    }
    
    public List<SafetyBlockRule> getSafetyBlocks() {
        if (safetyBlocks == null) {
            safetyBlocks = new ArrayList<>();
        }
        return safetyBlocks;
    }
    
    // Helper methods for managing blocks
    public void addDirectBlock(String userId) {
        getDirectBlocks().add(userId);
        setUpdatedAt(Timestamp.now());
    }
    
    public void removeDirectBlock(String userId) {
        getDirectBlocks().remove(userId);
        setUpdatedAt(Timestamp.now());
    }
    
    public void addSafetyBlock(SafetyBlockRule rule) {
        getSafetyBlocks().add(rule);
        setUpdatedAt(Timestamp.now());
    }
    
    public void removeSafetyBlock(String ruleId) {
        getSafetyBlocks().removeIf(rule -> ruleId.equals(rule.getId()));
        setUpdatedAt(Timestamp.now());
    }
    
    public boolean isUserDirectlyBlocked(String userId) {
        return getDirectBlocks().contains(userId);
    }
    
    public SafetyBlockRule getSafetyBlockById(String ruleId) {
        return getSafetyBlocks().stream()
                .filter(rule -> ruleId.equals(rule.getId()))
                .findFirst()
                .orElse(null);
    }
    
    public List<SafetyBlockRule> getEnabledSafetyBlocks() {
        return getSafetyBlocks().stream()
                .filter(rule -> rule.getEnabled() != null && rule.getEnabled())
                .toList();
    }
    
    public List<SafetyBlockRule> getSafetyBlocksByType(String type) {
        return getSafetyBlocks().stream()
                .filter(rule -> type.equals(rule.getType()))
                .toList();
    }
}