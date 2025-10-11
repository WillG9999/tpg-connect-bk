package com.tpg.connect.repository.impl;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.conversation.Message;
import com.tpg.connect.repository.ConversationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class ConversationRepositoryImpl implements ConversationRepository {

    private static final String COLLECTION_NAME = "conversations";
    private static final String MESSAGES_SUBCOLLECTION = "messages";
    
    @Autowired
    private Firestore firestore;

    @Override
    public Conversation save(Conversation conversation) {
        try {
            if (conversation.getId() == null) {
                conversation.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversation.getId());
            docRef.set(convertToMap(conversation)).get();
            
            return conversation;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save conversation", e);
        }
    }

    @Override
    public Optional<Conversation> findById(String conversationId) {
        try {
            System.out.println("🔍 ConversationRepositoryImpl: Looking up conversation ID: " + conversationId);
            
            DocumentSnapshot doc = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .get()
                    .get();
            
            if (doc.exists()) {
                System.out.println("✅ ConversationRepositoryImpl: Found conversation: " + conversationId);
                return Optional.of(convertToConversation(doc));
            } else {
                System.out.println("❌ ConversationRepositoryImpl: Conversation not found: " + conversationId);
                // Log all available conversations for debugging
                _logAvailableConversations();
                return Optional.empty();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find conversation by id", e);
        }
    }
    
    private void _logAvailableConversations() {
        try {
            System.out.println("🔍 ConversationRepositoryImpl: Listing available conversations for debugging...");
            QuerySnapshot allConversations = firestore.collection(COLLECTION_NAME)
                    .limit(10) // Limit to first 10 for debugging
                    .get()
                    .get();
            
            if (allConversations.isEmpty()) {
                System.out.println("📭 ConversationRepositoryImpl: No conversations exist in database");
            } else {
                System.out.println("📋 ConversationRepositoryImpl: Available conversations:");
                for (DocumentSnapshot doc : allConversations.getDocuments()) {
                    System.out.println("  - ID: " + doc.getId());
                }
            }
        } catch (Exception e) {
            System.out.println("💥 ConversationRepositoryImpl: Error listing conversations: " + e.getMessage());
        }
    }

    @Override
    public List<Conversation> findByParticipantId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participantIds", userId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToConversation)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find conversations by participant", e);
        }
    }

    @Override
    public List<Conversation> findByParticipantIdAndStatus(String userId, Object status) {
        try {
            String statusStr = status instanceof Conversation.ConversationStatus ? 
                    ((Conversation.ConversationStatus) status).name() : status.toString();
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participantIds", userId)
                    .whereEqualTo("status", statusStr)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToConversation)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find conversations by participant and status", e);
        }
    }

    @Override
    public List<Conversation> findActiveByParticipantId(String userId) {
        return findByParticipantIdAndStatus(userId, Conversation.ConversationStatus.ACTIVE);
    }

    @Override
    public List<Conversation> findArchivedByParticipantId(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participantIds", userId)
                    .whereEqualTo("archived", true)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToConversation)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find archived conversations", e);
        }
    }

    @Override
    public void deleteById(String conversationId) {
        try {
            // Delete all messages in the conversation first
            firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .get()
                    .get()
                    .getDocuments()
                    .forEach(doc -> {
                        try {
                            doc.getReference().delete().get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException("Failed to delete message", e);
                        }
                    });
            
            // Delete the conversation document
            firestore.collection(COLLECTION_NAME).document(conversationId).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete conversation", e);
        }
    }

    @Override
    public void deleteByParticipantId(String userId) {
        try {
            List<Conversation> userConversations = findByParticipantId(userId);
            
            WriteBatch batch = firestore.batch();
            
            for (Conversation conversation : userConversations) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversation.getId());
                batch.delete(docRef);
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete conversations by participant", e);
        }
    }

    @Override
    public Conversation updateLastMessage(String conversationId, Message lastMessage) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversationId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastMessage", convertMessageToMap(lastMessage));
            updates.put("updatedAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(conversationId).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update last message", e);
        }
    }

    @Override
    public Conversation updateConversationStatus(String conversationId, String status) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversationId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);
            updates.put("updatedAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(conversationId).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update conversation status", e);
        }
    }

    @Override
    public Conversation updateUnreadCount(String conversationId, String userId, int count) {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversationId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("unreadCount", count);
            updates.put("updatedAt", Timestamp.now());
            
            docRef.update(updates).get();
            
            return findById(conversationId).orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to update unread count", e);
        }
    }

    @Override
    public Conversation markAsArchived(String conversationId, boolean archived) {
        try {
            System.out.println("📁 ConversationRepositoryImpl.markAsArchived CALLED");
            System.out.println("📁 ConversationRepositoryImpl: conversationId = " + conversationId);
            System.out.println("📁 ConversationRepositoryImpl: archived = " + archived);
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversationId);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("archived", archived);
            updates.put("updatedAt", Timestamp.now());
            
            System.out.println("📁 ConversationRepositoryImpl: Update map = " + updates);
            System.out.println("📁 ConversationRepositoryImpl: Executing Firestore update...");
            
            docRef.update(updates).get();
            
            System.out.println("📁 ConversationRepositoryImpl: Firestore update completed");
            System.out.println("📁 ConversationRepositoryImpl: Retrieving updated conversation...");
            
            Conversation updatedConversation = findById(conversationId).orElse(null);
            
            if (updatedConversation != null) {
                System.out.println("📁 ConversationRepositoryImpl: Retrieved conversation with archived status: " + updatedConversation.isArchived());
            } else {
                System.out.println("❌ ConversationRepositoryImpl: Failed to retrieve updated conversation");
            }
            
            System.out.println("✅ ConversationRepositoryImpl: markAsArchived operation completed");
            return updatedConversation;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ ConversationRepositoryImpl: Error in markAsArchived: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to mark conversation as archived", e);
        }
    }

    @Override
    public Message saveMessage(String conversationId, Message message) {
        try {
            if (message.getId() == null) {
                message.setId(UUID.randomUUID().toString());
            }
            
            DocumentReference docRef = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .document(message.getId());
            
            docRef.set(convertMessageToMap(message)).get();
            
            // Update conversation's last message and timestamp
            updateLastMessage(conversationId, message);
            
            return message;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save message", e);
        }
    }

    @Override
    public List<Message> findMessagesByConversationId(String conversationId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .orderBy("sentAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMessage)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find messages by conversation", e);
        }
    }

    @Override
    public List<Message> findMessagesByConversationId(String conversationId, int page, int limit) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .orderBy("sentAt", Query.Direction.DESCENDING)
                    .offset(page * limit)
                    .limit(limit)
                    .get()
                    .get();
            
            List<Message> messages = querySnapshot.getDocuments().stream()
                    .map(this::convertToMessage)
                    .collect(Collectors.toList());
            
            // Reverse to maintain chronological order
            Collections.reverse(messages);
            return messages;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find paginated messages", e);
        }
    }

    @Override
    public List<Message> findUnreadMessages(String conversationId, String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .whereNotEqualTo("senderId", userId)
                    .whereEqualTo("status", Message.MessageStatus.DELIVERED.name())
                    .orderBy("sentAt", Query.Direction.ASCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToMessage)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find unread messages", e);
        }
    }

    @Override
    public void markMessagesAsRead(String conversationId, String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .whereNotEqualTo("senderId", userId)
                    .whereEqualTo("status", Message.MessageStatus.DELIVERED.name())
                    .get()
                    .get();
            
            WriteBatch batch = firestore.batch();
            
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                batch.update(doc.getReference(), "status", Message.MessageStatus.READ.name());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to mark messages as read", e);
        }
    }

    @Override
    public void deleteMessage(String conversationId, String messageId) {
        try {
            firestore.collection(COLLECTION_NAME)
                    .document(conversationId)
                    .collection(MESSAGES_SUBCOLLECTION)
                    .document(messageId)
                    .delete()
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete message", e);
        }
    }

    @Override
    public long countUnreadConversations(String userId) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participantIds", userId)
                    .whereGreaterThan("unreadCount", 0)
                    .get()
                    .get();
            
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count unread conversations", e);
        }
    }

    @Override
    public long countActiveConversations(String userId) {
        return findActiveByParticipantId(userId).size();
    }

    @Override
    public List<Conversation> findRecentConversations(String userId, int days) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContains("participantIds", userId)
                    .whereGreaterThan("updatedAt", cutoffTimestamp)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToConversation)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find recent conversations", e);
        }
    }

    @Override
    public Optional<Conversation> findConversationBetweenUsers(String userId1, String userId2) {
        try {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereArrayContainsAny("participantIds", Arrays.asList(userId1, userId2))
                    .get()
                    .get();
            
            // Find conversation that contains both users
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                Conversation conversation = convertToConversation(doc);
                if (conversation.getParticipantIds().contains(userId1) && 
                    conversation.getParticipantIds().contains(userId2)) {
                    return Optional.of(conversation);
                }
            }
            
            return Optional.empty();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find conversation between users", e);
        }
    }

    @Override
    public List<Conversation> findConversationsToArchive(LocalDateTime cutoffDate) {
        try {
            Timestamp cutoffTimestamp = Timestamp.of(Date.from(cutoffDate.atZone(ZoneId.systemDefault()).toInstant()));
            
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereLessThan("updatedAt", cutoffTimestamp)
                    .whereEqualTo("archived", false)
                    .whereEqualTo("status", Conversation.ConversationStatus.ACTIVE.name())
                    .get()
                    .get();
            
            return querySnapshot.getDocuments().stream()
                    .map(this::convertToConversation)
                    .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find conversations to archive", e);
        }
    }

    @Override
    public void autoArchiveInactiveConversations(LocalDateTime cutoffDate) {
        try {
            List<Conversation> conversationsToArchive = findConversationsToArchive(cutoffDate);
            
            WriteBatch batch = firestore.batch();
            
            for (Conversation conversation : conversationsToArchive) {
                DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(conversation.getId());
                batch.update(docRef, "archived", true, "updatedAt", Timestamp.now());
            }
            
            batch.commit().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to auto-archive conversations", e);
        }
    }

    // Helper methods for conversion
    private Map<String, Object> convertToMap(Conversation conversation) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", conversation.getId());
        map.put("matchId", conversation.getMatchId());
        map.put("participantIds", conversation.getParticipantIds());
        map.put("status", conversation.getStatus() != null ? conversation.getStatus().name() : null);
        map.put("unreadCount", conversation.getUnreadCount());
        map.put("archived", conversation.isArchived()); // Use actual archived status
        
        if (conversation.getLastMessage() != null) {
            map.put("lastMessage", convertMessageToMap(conversation.getLastMessage()));
        }
        
        // Convert LocalDateTime to Timestamp
        if (conversation.getMatchedAt() != null) {
            map.put("matchedAt", Timestamp.of(Date.from(conversation.getMatchedAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        if (conversation.getUpdatedAt() != null) {
            map.put("updatedAt", Timestamp.of(Date.from(conversation.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant())));
        } else {
            map.put("updatedAt", Timestamp.now());
        }
        
        return map;
    }

    private Map<String, Object> convertMessageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("conversationId", message.getConversationId());
        map.put("senderId", message.getSenderId());
        map.put("content", message.getContent());
        map.put("status", message.getStatus() != null ? message.getStatus().name() : null);
        
        if (message.getSentAt() != null) {
            map.put("sentAt", Timestamp.of(Date.from(message.getSentAt().atZone(ZoneId.systemDefault()).toInstant())));
        }
        
        return map;
    }

    private Conversation convertToConversation(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        Conversation conversation = new Conversation();
        conversation.setId(doc.getId());
        conversation.setMatchId((String) data.get("matchId"));
        conversation.setUnreadCount(data.get("unreadCount") != null ? 
                ((Number) data.get("unreadCount")).intValue() : 0);
        
        // Handle participant IDs
        Object participantIds = data.get("participantIds");
        if (participantIds instanceof List) {
            conversation.setParticipantIds((List<String>) participantIds);
        }
        
        // Convert enum values
        if (data.get("status") != null) {
            conversation.setStatus(Conversation.ConversationStatus.valueOf((String) data.get("status")));
        }
        
        // Convert last message
        Object lastMessageData = data.get("lastMessage");
        if (lastMessageData instanceof Map) {
            conversation.setLastMessage(convertMapToMessage((Map<String, Object>) lastMessageData));
        }
        
        // Set archived status
        if (data.get("archived") != null) {
            conversation.setArchived((Boolean) data.get("archived"));
        } else {
            conversation.setArchived(false); // Default to not archived if field missing
        }
        
        // Convert Timestamp to LocalDateTime
        if (data.get("matchedAt") instanceof Timestamp) {
            conversation.setMatchedAt(((Timestamp) data.get("matchedAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (data.get("updatedAt") instanceof Timestamp) {
            conversation.setUpdatedAt(((Timestamp) data.get("updatedAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return conversation;
    }

    private Message convertToMessage(DocumentSnapshot doc) {
        Map<String, Object> data = doc.getData();
        if (data == null) {
            throw new RuntimeException("Document data is null");
        }
        
        return convertMapToMessage(data);
    }

    private Message convertMapToMessage(Map<String, Object> data) {
        Message message = new Message();
        message.setId((String) data.get("id"));
        message.setConversationId((String) data.get("conversationId"));
        message.setSenderId((String) data.get("senderId"));
        message.setContent((String) data.get("content"));
        
        // Convert enum values
        if (data.get("status") != null) {
            message.setStatus(Message.MessageStatus.valueOf((String) data.get("status")));
        }
        
        // Convert Timestamp to LocalDateTime
        if (data.get("sentAt") instanceof Timestamp) {
            message.setSentAt(((Timestamp) data.get("sentAt")).toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return message;
    }

    @Override
    public long countByUserId(String userId) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection.whereArrayContains("participantIds", userId);
            
            QuerySnapshot querySnapshot = query.get().get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count conversations by userId", e);
        }
    }

    @Override
    public long countMessagesByUserId(String userId) {
        try {
            // This is a simplified implementation - in a real scenario,
            // you might need to query all conversations and count messages
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection.whereArrayContains("participantIds", userId);
            
            QuerySnapshot querySnapshot = query.get().get();
            long totalMessages = 0;
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                List<Map<String, Object>> messages = (List<Map<String, Object>>) doc.get("messages");
                if (messages != null) {
                    totalMessages += messages.size();
                }
            }
            
            return totalMessages;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to count messages by userId", e);
        }
    }

    @Override
    public List<Conversation> findRecentByUserId(String userId, int limit) {
        try {
            CollectionReference collection = firestore.collection(COLLECTION_NAME);
            Query query = collection
                    .whereArrayContains("participantIds", userId)
                    .orderBy("lastMessageAt", Query.Direction.DESCENDING)
                    .limit(limit);
            
            QuerySnapshot querySnapshot = query.get().get();
            List<Conversation> conversations = new ArrayList<>();
            
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                conversations.add(convertToConversation(doc));
            }
            
            return conversations;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to find recent conversations by userId", e);
        }
    }
}