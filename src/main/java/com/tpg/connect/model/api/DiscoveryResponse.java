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
public class DiscoveryResponse {
    private boolean success;
    private String message;
    private List<CompleteUserProfile> users;
    private String batchId;
    private LocalDateTime timestamp;
    
    public DiscoveryResponse(boolean success, String message, List<CompleteUserProfile> users, String batchId) {
        this.success = success;
        this.message = message;
        this.users = users;
        this.batchId = batchId;
        this.timestamp = LocalDateTime.now();
    }
}