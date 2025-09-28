package com.tpg.connect.services;

import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.conversation.Message;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.repository.ConversationRepository;
import com.tpg.connect.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
        // Get the match details
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (!matchOpt.isPresent()) {
            throw new IllegalArgumentException("Match not found: " + matchId);
        }

        Match match = matchOpt.get();
        
        // Check if conversation already exists
        if (match.getConversationId() != null) {
            Optional<Conversation> existingConv = conversationRepository.findById(match.getConversationId());
            if (existingConv.isPresent()) {
                return existingConv.get();
            }
        }

        // Create new conversation with deterministic ID
        String conversationId = generateConversationId(match.getUser1Id(), match.getUser2Id());
        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setMatchId(matchId);
        conversation.setParticipantIds(List.of(match.getUser1Id(), match.getUser2Id()));
        conversation.setStatus(Conversation.ConversationStatus.ACTIVE);
        conversation.setUnreadCount(0);
        conversation.setMatchedAt(match.getMatchedAt());
        conversation.setUpdatedAt(LocalDateTime.now());

        Conversation savedConversation = conversationRepository.save(conversation);

        // Update match with conversation ID if not already set
        if (match.getConversationId() == null) {
            match.setConversationId(savedConversation.getId());
            matchRepository.save(match);
        }

        return savedConversation;
    }

    @CacheEvict(value = {"conversations", "messages"}, allEntries = true)
    public Message sendMessage(String conversationId, String senderId, String content) {
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

        for (String participantId : otherParticipants) {
            notificationService.sendMessageNotification(participantId, senderId, conversationId, content);
        }

        return savedMessage;
    }

    @Cacheable(value = "messages", key = "'conversation_messages_' + #conversationId + '_' + #page + '_' + #limit")
    public List<Message> getConversationMessages(String conversationId, String userId, int page, int limit) {
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
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
        // Verify user is participant
        Optional<Conversation> conversationOpt = conversationRepository.findById(conversationId);
        if (!conversationOpt.isPresent()) {
            throw new IllegalArgumentException("Conversation not found: " + conversationId);
        }

        Conversation conversation = conversationOpt.get();
        if (!conversation.getParticipantIds().contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this conversation");
        }

        conversationRepository.markAsArchived(conversationId, true);
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
}