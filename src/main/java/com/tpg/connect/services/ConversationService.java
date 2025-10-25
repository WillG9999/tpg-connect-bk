package com.tpg.connect.services;

import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.conversation.ConversationSummary;
import com.tpg.connect.model.conversation.Message;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.repository.ConversationRepository;
import com.tpg.connect.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.tpg.connect.controllers.websocket.SimpleWebSocketHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConversationService {
    
    // TODO: Implement push notifications for new messages using FCM
    // TODO: Add real-time message delivery status tracking
    // TODO: Implement message encryption for enhanced privacy
    // TODO: Add typing indicators and read receipts
    // TODO: Implement message media support (images, voice notes)

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Autowired(required = false)
    private SimpleWebSocketHandler simpleWebSocketHandler;

    @Cacheable(value = "conversations", key = "'user_conversations_' + #userId")
    public List<Conversation> getUserConversations(String userId, boolean includeArchived) {
        if (includeArchived) {
            return conversationRepository.findByParticipantId(userId);
        } else {
            return conversationRepository.findActiveByParticipantId(userId);
        }
    }

    @Cacheable(value = "conversations", key = "'conversation_' + #conversationId")
    public Optional<Conversation> getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId);
    }

    public Optional<Conversation> getConversationByMatchId(String matchId) {
        // Find the match first to get the conversation ID
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isPresent()) {
            String conversationId = matchOpt.get().getConversationId();
            return conversationRepository.findById(conversationId);
        }
        return Optional.empty();
    }

    @CacheEvict(value = "conversations", allEntries = true)
    public Conversation createConversationFromMatch(String matchId) {
        System.out.println("📞 ConversationService: Creating conversation from match: " + matchId);
        
        // Get the match details
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            System.err.println("❌ ConversationService: Match not found: " + matchId);
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        Match match = matchOpt.get();
        System.out.println("📞 ConversationService: Found match - User1: " + match.getUser1Id() + ", User2: " + match.getUser2Id());
        
        // Check if conversation already exists
        if (match.getConversationId() != null) {
            System.out.println("📞 ConversationService: Match already has conversation ID: " + match.getConversationId());
            Optional<Conversation> existingConv = conversationRepository.findById(match.getConversationId());
            if (existingConv.isPresent()) {
                System.out.println("✅ ConversationService: Returning existing conversation: " + existingConv.get().getId());
                return existingConv.get();
            }
        }

        // Create new conversation with deterministic ID
        String conversationId = generateConversationId(match.getUser1Id(), match.getUser2Id());
        System.out.println("📞 ConversationService: Generated conversation ID: " + conversationId);
        
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setMatchId(matchId);
        conversation.setParticipantIds(List.of(match.getUser1Id(), match.getUser2Id()));
        conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
        conversation.setUnreadCount(0);
        conversation.setMatchedAt(match.getMatchedAt());
        conversation.setUpdatedAt(LocalDateTime.now());

        System.out.println("📞 ConversationService: Saving conversation to Firestore...");
        Conversation savedConversation = conversationRepository.save(conversation);
        System.out.println("✅ ConversationService: Successfully saved conversation: " + savedConversation.getId());

        // Update match with conversation ID if not already set
        if (match.getConversationId() == null) {
            System.out.println("📞 ConversationService: Updating match with conversation ID...");
            match.setConversationId(savedConversation.getId());
            matchRepository.save(match);
            System.out.println("✅ ConversationService: Updated match with conversation ID");
        }

        return savedConversation;
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public Message sendMessage(String conversationId, String senderId, String content) {
        System.out.println("🚀 ConversationService: sendMessage called - conversationId: " + conversationId + ", senderId: " + senderId);
        // Verify conversation exists and user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(senderId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        // Check if conversation is active
        if (conversation.getStatus() != Conversation.ConversationStatus.ACTIVE) {
            throw new IllegalStateException("Cannot send message to inactive conversation");
        }

        // Create message
        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setSentAt(LocalDateTime.now());
        message.setStatus(Message.MessageStatus.SENT);

        Message savedMessage = conversationRepository.saveMessage(conversationId, message);

        // Update conversation last activity
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Send push notification to other participant(s)
        List<String> otherParticipants = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(senderId))
                .toList();

        System.out.println("🔔 ConversationService: Sending notifications to " + otherParticipants.size() + " participants");
        for (String participantId : otherParticipants) {
            System.out.println("🔔 ConversationService: Calling notification service for participant: " + participantId);
            notificationService.sendMessageNotification(participantId, senderId, conversationId, content);
        }

        // Broadcast message via WebSocket if template is available
        broadcastMessageToWebSocket(conversationId, savedMessage);

        return savedMessage;
    }

    @Cacheable(value = "messages", key = "'conversation_messages_' + #conversationId + '_' + #page + '_' + #limit")
    public List<Message> getConversationMessages(String conversationId, String userId, int page, int limit) {
        System.out.println("🔍 ConversationService: getConversationMessages called");
        System.out.println("🔍 ConversationService: conversationId = '" + conversationId + "'");
        System.out.println("🔍 ConversationService: userId = '" + userId + "'");
        System.out.println("🔍 ConversationService: page = " + page + ", limit = " + limit);
        
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            System.out.println("❌ ConversationService: Conversation not found: " + conversationId);
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        if (page == 0 && limit == 0) {
            // Get all messages
            return conversationRepository.findMessagesByConversationId(conversationId);
        } else {
            // Get paginated messages
            return conversationRepository.findMessagesByConversationId(conversationId, page, limit);
        }
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public void markMessagesAsRead(String conversationId, String userId) {
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        // Mark messages as read
        conversationRepository.markMessagesAsRead(conversationId, userId);

        // Update unread count to 0 for this user
        conversationRepository.updateUnreadCount(conversationId, userId, 0);
    }

    @CacheEvict(value = "conversations", allEntries = true)
    public void archiveConversation(String conversationId, String userId) {
        System.out.println("📁 ConversationService.archiveConversation CALLED");
        System.out.println("📁 ConversationService: conversationId = " + conversationId);
        System.out.println("📁 ConversationService: userId = " + userId);
        
        // Verify user is participant
        System.out.println("📁 ConversationService: Verifying conversation exists...");
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            System.out.println("❌ ConversationService: Conversation not found: " + conversationId);
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        System.out.println("📁 ConversationService: Found conversation with participants: " + conversation.getParticipantIds());
        
        if (!conversation.getParticipantIds().contains(userId)) {
            System.out.println("❌ ConversationService: User " + userId + " is not a participant");
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        System.out.println("📁 ConversationService: User verified as participant");
        System.out.println("📁 ConversationService: Current archived status: " + conversation.isArchived());
        System.out.println("📁 ConversationService: Calling conversationRepository.markAsArchived...");
        
        conversationRepository.markAsArchived(conversationId, true);
        
        System.out.println("📁 ConversationService: markAsArchived call completed");
        System.out.println("✅ ConversationService: Archive operation finished");
    }

    @CacheEvict(value = "conversations", allEntries = true)
    public void unarchiveConversation(String conversationId, String userId) {
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        conversationRepository.markAsArchived(conversationId, false);
    }

    /**
     * Unmatch conversation by setting status to UNMATCHED
     */
    public void unmatchConversation(String matchId) {
        System.out.println("🚫 ConversationService: Unmatching conversation for match ID: " + matchId);
        
        // Find conversation by conversation ID (which is the same as match ID)
        Optional<Conversation> conversationOpt = conversationRepository.findById(matchId);
        if (!conversationOpt.isPresent()) {
            System.err.println("❌ ConversationService: Conversation not found for match: " + matchId);
            throw new RuntimeException("Conversation not found for match: " + matchId);
        }
        
        Conversation conversation = conversationOpt.get();
        conversation.setStatus(Conversation.ConversationStatus.UNMATCHED);
        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        
        conversationRepository.save(conversation);
        System.out.println("✅ ConversationService: Conversation status updated to UNMATCHED for match: " + matchId);
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public void endConversation(String conversationId, String userId) {
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        // Update conversation status
        conversationRepository.updateConversationStatus(conversationId, Conversation.ConversationStatus.UNMATCHED.name());

        // Notify other participants
        List<String> otherParticipants = conversation.getParticipantIds().stream()
                .filter(id -> !id.equals(userId))
                .toList();

        for (String participantId : otherParticipants) {
            notificationService.sendConversationEndedNotification(participantId, conversationId);
        }
    }

    public List<Conversation> getArchivedConversations(String userId) {
        return conversationRepository.findArchivedByParticipantId(userId);
    }

    public long getUnreadConversationCount(String userId) {
        return conversationRepository.countUnreadConversations(userId);
    }

    public long getActiveConversationCount(String userId) {
        return conversationRepository.countActiveConversations(userId);
    }

    public List<Conversation> getRecentConversations(String userId, int days) {
        return conversationRepository.findRecentConversations(userId, days);
    }

    @CacheEvict(value = "conversations", allEntries = true)
    public void autoArchiveInactiveConversations() {
        // Archive conversations that haven't been active for 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        conversationRepository.autoArchiveInactiveConversations(cutoffDate);
    }

    public Optional<Conversation> findConversationBetweenUsers(String userId1, String userId2) {
        return conversationRepository.findConversationBetweenUsers(userId1, userId2);
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public void deleteConversation(String conversationId, String userId) {
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        // Delete conversation and all messages
        conversationRepository.deleteById(conversationId);
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public void deleteUserConversations(String userId) {
        conversationRepository.deleteByParticipantId(userId);
    }

    @CacheEvict(value = "conversations", allEntries = true)
    public Conversation saveConversation(Conversation conversation) {
        return conversationRepository.save(conversation);
    }

    /**
     * Generate deterministic conversation ID from two connect IDs
     * Format: connectid1_connectid2 (sorted to ensure consistency)
     */
    private String generateConversationId(String connectId1, String connectId2) {
        // Sort IDs to ensure consistency regardless of order
        if (connectId1.compareTo(connectId2) <= 0) {
            return connectId1 + "_" + connectId2;
        } else {
            return connectId2 + "_" + connectId1;
        }
    }

    /**
     * Broadcast message to WebSocket subscribers
     */
    private void broadcastMessageToWebSocket(String conversationId, Message message) {
        // Try simple WebSocket handler first
        if (simpleWebSocketHandler != null) {
            try {
                System.out.println("📡 ConversationService: Broadcasting message via SimpleWebSocketHandler for conversation: " + conversationId);
                
                // Convert message to map for JSON serialization
                Map<String, Object> messageData = Map.of(
                    "id", message.getId(),
                    "conversationId", message.getConversationId(),
                    "senderId", message.getSenderId(),
                    "content", message.getContent(),
                    "sentAt", message.getSentAt().toString(),
                    "status", message.getStatus() != null ? message.getStatus().toString() : "SENT"
                );

                // Wrap in structured format expected by Flutter client
                Map<String, Object> broadcastMessage = Map.of(
                    "type", "message",
                    "data", messageData
                );

                // Broadcast via simple WebSocket handler
                simpleWebSocketHandler.broadcastToConversation(conversationId, broadcastMessage);
                System.out.println("✅ ConversationService: Message broadcasted via SimpleWebSocketHandler successfully");
                return;
                
            } catch (Exception e) {
                System.err.println("💥 ConversationService: Error broadcasting via SimpleWebSocketHandler: " + e.getMessage());
            }
        }
        
        // Fallback to STOMP messaging template
        if (messagingTemplate != null) {
            try {
                System.out.println("📡 ConversationService: Broadcasting message via STOMP template for conversation: " + conversationId);
                
                // Convert message to map for JSON serialization
                Map<String, Object> messageData = Map.of(
                    "id", message.getId(),
                    "conversationId", message.getConversationId(),
                    "senderId", message.getSenderId(),
                    "content", message.getContent(),
                    "sentAt", message.getSentAt().toString(),
                    "status", message.getStatus() != null ? message.getStatus().toString() : "SENT"
                );

                // Wrap in structured format expected by Flutter client
                Map<String, Object> broadcastMessage = Map.of(
                    "type", "message",
                    "data", messageData
                );

                // Send to topic subscribers
                messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, broadcastMessage);
                System.out.println("✅ ConversationService: Message broadcasted via STOMP template successfully");
                
            } catch (Exception e) {
                System.err.println("💥 ConversationService: Error broadcasting via STOMP template: " + e.getMessage());
            }
        } else {
            System.out.println("⚠️ ConversationService: No WebSocket handlers available, skipping broadcast");
        }
    }
    
    // Admin-specific methods
    
    /**
     * Get user's conversations for admin review
     */
    public List<Conversation> getUserConversationsForAdmin(String connectId, int page, int size) {
        return conversationRepository.findRecentByUserId(connectId, size);
    }
    
    /**
     * Get total conversations count for pagination
     */
    public long getTotalConversationsCount(String connectId) {
        return conversationRepository.countByUserId(connectId);
    }
    
    /**
     * Get total messages count for user stats
     */
    public int countMessagesByUserId(String connectId) {
        return (int) conversationRepository.countMessagesByUserId(connectId);
    }
}