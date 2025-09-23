package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor 
public class EnhancedPhoto {
    private String id;                    // Photo ID
    private String url;                   // Firebase Storage URL
    private boolean isPrimary;            // Primary profile photo
    private int order;                    // Display order (1-6)
    private List<PhotoPrompt> prompts;    // Photo prompts/captions
}