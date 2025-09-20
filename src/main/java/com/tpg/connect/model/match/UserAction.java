package com.tpg.connect.model.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_actions")
public class UserAction {
    @Id
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