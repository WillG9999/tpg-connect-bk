package com.tpg.connect.model.document;

import com.tpg.connect.model.conversation.Message;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ConversationDocument {
    private String id;
    
    private String matchId;
    private List<String> participantIds;
    
    // Embedded messages for better performance
    private List<MessageData> messages;
    
    // Last message for quick access
    private MessageData lastMessage;
    
    // Unread counts per user
    private Map<String, Integer> unreadCounts;
    
    // Conversation metadata
    private ConversationMetadata metadata;
    
    @Data
    @NoArgsConstructor
    public static class MessageData {
        private String id;
        private String senderId;
        private String content;
        private LocalDateTime sentAt;
        private MessageStatus status;
        private String messageType; // TEXT, IMAGE, GIF
    }
    
    @Data
    @NoArgsConstructor
    public static class ConversationMetadata {
        private LocalDateTime matchedAt;
        private LocalDateTime updatedAt;
        private LocalDateTime lastActivityAt;
        private ConversationStatus status;
        private boolean archived;
        private List<String> tags;
    }
    
    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
    
    public enum ConversationStatus {
        ACTIVE, UNMATCHED, BLOCKED, REPORTED
    }
}