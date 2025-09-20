package com.tpg.connect.model.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    private String id;
    private String matchId;
    private List<String> participantIds;
    private List<Message> messages;
    private Message lastMessage;
    private int unreadCount;
    private LocalDateTime matchedAt;
    private LocalDateTime updatedAt;
    private ConversationStatus status;
    
    public enum ConversationStatus {
        ACTIVE,
        UNMATCHED,
        BLOCKED
    }
}