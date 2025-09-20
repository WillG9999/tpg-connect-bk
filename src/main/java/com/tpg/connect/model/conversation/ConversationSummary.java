package com.tpg.connect.model.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummary {
    private String id;
    private String matchId;
    private UserSummary otherUser;
    private Message lastMessage;
    private int unreadCount;
    private LocalDateTime matchedAt;
    private LocalDateTime updatedAt;
}