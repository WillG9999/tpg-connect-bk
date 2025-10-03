package com.tpg.connect.repository;

import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.conversation.Message;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ConversationRepository {
    
    // Conversation CRUD Operations
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(String conversationId);
    List<Conversation> findByParticipantId(String userId);
    List<Conversation> findByParticipantIdAndStatus(String userId, Object status);
    List<Conversation> findActiveByParticipantId(String userId);
    List<Conversation> findArchivedByParticipantId(String userId);
    void deleteById(String conversationId);
    void deleteByParticipantId(String userId);
    
    // Conversation Updates
    Conversation updateLastMessage(String conversationId, Message lastMessage);
    Conversation updateConversationStatus(String conversationId, String status);
    Conversation updateUnreadCount(String conversationId, String userId, int count);
    Conversation markAsArchived(String conversationId, boolean archived);
    
    // Message Operations
    Message saveMessage(String conversationId, Message message);
    List<Message> findMessagesByConversationId(String conversationId);
    List<Message> findMessagesByConversationId(String conversationId, int page, int limit);
    List<Message> findUnreadMessages(String conversationId, String userId);
    void markMessagesAsRead(String conversationId, String userId);
    void deleteMessage(String conversationId, String messageId);
    
    // Statistics and Queries
    long countUnreadConversations(String userId);
    long countActiveConversations(String userId);
    long countByUserId(String userId);
    long countMessagesByUserId(String userId);
    List<Conversation> findRecentConversations(String userId, int days);
    List<Conversation> findRecentByUserId(String userId, int limit);
    Optional<Conversation> findConversationBetweenUsers(String userId1, String userId2);
    
    // Archive Management
    List<Conversation> findConversationsToArchive(LocalDateTime cutoffDate);
    void autoArchiveInactiveConversations(LocalDateTime cutoffDate);
}