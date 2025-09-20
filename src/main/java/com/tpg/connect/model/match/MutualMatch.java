package com.tpg.connect.model.match;

import com.tpg.connect.model.conversation.UserSummary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MutualMatch {
    private String matchId;
    private UserSummary user;
    private LocalDateTime matchedAt;
    private String conversationId;
}