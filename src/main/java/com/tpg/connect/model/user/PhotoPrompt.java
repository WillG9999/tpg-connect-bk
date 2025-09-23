package com.tpg.connect.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoPrompt {
    private String id;                    // Prompt ID
    private String text;                  // Prompt text
    private PhotoPosition position;       // Position on photo (0-1)
    private PhotoStyle style;             // Styling options
    
    @Data
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class PhotoPosition {
        private double x;                 // X position (0-1)
        private double y;                 // Y position (0-1)
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor 
    public static class PhotoStyle {
        private String backgroundColor;    // Background color
        private String textColor;         // Text color
        private int fontSize;              // Font size
    }
}