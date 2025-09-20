package com.tpg.connect.model.safety;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "safety_blocks")
public class SafetyBlock {
    @Id
    private String id;
    private String userId;           // User who created this safety block
    private SafetyBlockType blockType;
    private String pattern;          // The pattern to match against
    private boolean caseSensitive;
    private boolean enabled;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int matchCount;          // How many times this rule has blocked someone
    
    public enum SafetyBlockType {
        NAME,           // Block by name pattern
        LOCATION,       // Block by location pattern
        EMAIL,          // Block by email pattern
        PHONE,          // Block by phone pattern
        KEYWORD,        // Block by bio/prompt keyword
        COMPANY,        // Block by company name
        UNIVERSITY      // Block by university name
    }
}