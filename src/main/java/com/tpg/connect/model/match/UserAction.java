package com.tpg.connect.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {
    private String id;
    private String userId;
    private String targetUserId;
    private ActionType action;
    private String batchDate;
    private LocalDateTime timestamp;
    private boolean processed;
    
    public enum ActionType {
        LIKE,
        PASS,
        SUPER_LIKE,
        DISLIKE
    }
}