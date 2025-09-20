package com.tpg.connect.model.api;

import com.tpg.connect.model.user.CompleteUserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialMatchesResponse {
    private boolean success;
    private String message;
    private List<CompleteUserProfile> users;
    private String matchSetId;
    private int totalUsers;
    private boolean completed;
    private LocalDateTime generatedAt;
    
    public PotentialMatchesResponse(boolean success, String message, List<CompleteUserProfile> users, 
                                 String matchSetId, int totalUsers, boolean completed) {
        this.success = success;
        this.message = message;
        this.users = users;
        this.matchSetId = matchSetId;
        this.totalUsers = totalUsers;
        this.completed = completed;
        this.generatedAt = LocalDateTime.now();
    }
}