package com.tpg.connect.model.api;

import com.tpg.connect.model.document.ConversationDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationsResponse {
    // All active conversations
    private List<ConversationSummary> conversations;
    
    // Conversation statistics
    private ConversationStats stats;
    
    // Response metadata
    private LocalDateTime lastUpdated;
    private String nextPageToken;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationSummary {
        private String id;
        private String matchId;
        
        // Other user info
        private UserInfo otherUser;
        
        // Recent messages (last 10)
        private List<ConversationDocument.MessageData> recentMessages;
        
        // Last message for preview
        private ConversationDocument.MessageData lastMessage;
        
        // Conversation metadata
        private int unreadCount;
        private LocalDateTime matchedAt;
        private LocalDateTime lastActivityAt;
        private ConversationDocument.ConversationStatus status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String name;
        private int age;
        private String primaryPhotoUrl;
        private boolean active;
        private LocalDateTime lastSeen;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationStats {
        private int totalConversations;
        private int totalUnreadMessages;
        private int activeConversations;
        private LocalDateTime lastActivityAt;
    }
}