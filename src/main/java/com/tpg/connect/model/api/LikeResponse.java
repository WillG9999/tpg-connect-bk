package com.tpg.connect.model.api;

import com.tpg.connect.model.match.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {
    private boolean success;
    private String message;
    private boolean isMatch;
    private Match match;
    private LocalDateTime timestamp;
    
    public LikeResponse(boolean success, String message, boolean isMatch, Match match) {
        this.success = success;
        this.message = message;
        this.isMatch = isMatch;
        this.match = match;
        this.timestamp = LocalDateTime.now();
    }
}