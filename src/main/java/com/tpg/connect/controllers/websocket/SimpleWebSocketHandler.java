package com.tpg.connect.controllers.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple WebSocket handler for real-time messaging without STOMP protocol
 */
@Component
public class SimpleWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Store WebSocket sessions by conversation ID
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();
    
    // Store WebSocket sessions by user ID for notifications
    private final Map<String, CopyOnWriteArrayList<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    
    // Store session to user mapping for cleanup
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();
    
    // Store all sessions for management
    private final CopyOnWriteArrayList<WebSocketSession> allSessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("🔌 SimpleWebSocketHandler: New WebSocket connection established: " + session.getId());
        allSessions.add(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            System.out.println("📡 SimpleWebSocketHandler: Received message: " + payload);
            
            try {
                // Parse the subscription message from Flutter
                Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
                String action = (String) messageData.get("action");
                
                if ("subscribe".equals(action)) {
                    String destination = (String) messageData.get("destination");
                    if (destination != null && destination.startsWith("/topic/conversation/")) {
                        String conversationId = destination.replace("/topic/conversation/", "");
                        subscribeToConversation(session, conversationId);
                        System.out.println("✅ SimpleWebSocketHandler: Subscribed session " + session.getId() + " to conversation: " + conversationId);
                    }
                    else if (destination != null && destination.startsWith("/topic/notifications/")) {
                        String userId = destination.replace("/topic/notifications/", "");
                        String authToken = (String) messageData.get("authorization");
                        subscribeToNotifications(session, userId, authToken);
                        System.out.println("✅ SimpleWebSocketHandler: Subscribed session " + session.getId() + " to notifications for user: " + userId);
                    }
                } else if ("unsubscribe".equals(action)) {
                    String destination = (String) messageData.get("destination");
                    if (destination != null && destination.startsWith("/topic/notifications/")) {
                        String userId = destination.replace("/topic/notifications/", "");
                        unsubscribeFromNotifications(session, userId);
                        System.out.println("✅ SimpleWebSocketHandler: Unsubscribed session " + session.getId() + " from notifications for user: " + userId);
                    }
                } else {
                    // Handle direct message types (like typing indicators)
                    String type = (String) messageData.get("type");
                    if ("typing".equals(type)) {
                        handleTypingIndicator(session, messageData);
                    }
                }
            } catch (Exception e) {
                System.err.println("💥 SimpleWebSocketHandler: Error parsing message: " + e.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("💥 SimpleWebSocketHandler: Transport error for session " + session.getId() + ": " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        System.out.println("🔌 SimpleWebSocketHandler: Connection closed: " + session.getId());
        allSessions.remove(session);
        
        // Remove from all conversation subscriptions
        conversationSessions.values().forEach(sessions -> sessions.remove(session));
        
        // Remove from user notification subscriptions
        String userId = sessionToUser.get(session.getId());
        if (userId != null) {
            userSessions.computeIfPresent(userId, (key, sessions) -> {
                sessions.remove(session);
                return sessions.isEmpty() ? null : sessions;
            });
            sessionToUser.remove(session.getId());
            System.out.println("🔌 SimpleWebSocketHandler: Removed session from user notifications: " + userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * Subscribe a WebSocket session to a conversation
     */
    private void subscribeToConversation(WebSocketSession session, String conversationId) {
        conversationSessions.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>()).add(session);
    }

    /**
     * Broadcast a message to all subscribers of a conversation
     */
    public void broadcastToConversation(String conversationId, Object messageData) {
        CopyOnWriteArrayList<WebSocketSession> sessions = conversationSessions.get(conversationId);
        if (sessions != null && !sessions.isEmpty()) {
            try {
                String jsonMessage = objectMapper.writeValueAsString(messageData);
                TextMessage textMessage = new TextMessage(jsonMessage);
                
                System.out.println("📡 SimpleWebSocketHandler: Broadcasting to " + sessions.size() + " subscribers of conversation: " + conversationId);
                
                // Send to all subscribed sessions
                sessions.removeIf(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(textMessage);
                            return false; // Keep session
                        } else {
                            System.out.println("🗑️ SimpleWebSocketHandler: Removing closed session: " + session.getId());
                            return true; // Remove closed session
                        }
                    } catch (IOException e) {
                        System.err.println("💥 SimpleWebSocketHandler: Error sending message to session " + session.getId() + ": " + e.getMessage());
                        return true; // Remove failed session
                    }
                });
                
                System.out.println("✅ SimpleWebSocketHandler: Successfully broadcasted message to conversation: " + conversationId);
            } catch (Exception e) {
                System.err.println("💥 SimpleWebSocketHandler: Error broadcasting message: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ SimpleWebSocketHandler: No subscribers found for conversation: " + conversationId);
        }
    }

    /**
     * Get the number of active connections
     */
    public int getConnectionCount() {
        return allSessions.size();
    }

    /**
     * Get the number of subscribers for a conversation
     */
    public int getConversationSubscriberCount(String conversationId) {
        CopyOnWriteArrayList<WebSocketSession> sessions = conversationSessions.get(conversationId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Handle typing indicator messages and broadcast to other participants
     */
    private void handleTypingIndicator(WebSocketSession senderSession, Map<String, Object> messageData) {
        try {
            Map<String, Object> data = (Map<String, Object>) messageData.get("data");
            if (data != null) {
                String conversationId = (String) data.get("conversationId");
                Boolean isTyping = (Boolean) data.get("isTyping");
                
                System.out.println("📡 SimpleWebSocketHandler: Handling typing indicator for conversation: " + conversationId + ", isTyping: " + isTyping);
                
                if (conversationId != null) {
                    // Broadcast typing indicator to all other subscribers (except sender)
                    CopyOnWriteArrayList<WebSocketSession> sessions = conversationSessions.get(conversationId);
                    if (sessions != null && !sessions.isEmpty()) {
                        
                        // Create typing indicator response with sender ID
                        Map<String, Object> typingResponse = Map.of(
                            "type", "typing",
                            "data", data
                        );
                        
                        String jsonMessage = objectMapper.writeValueAsString(typingResponse);
                        TextMessage textMessage = new TextMessage(jsonMessage);
                        
                        int sentCount = 0;
                        for (WebSocketSession session : sessions) {
                            // Don't send back to the sender
                            if (!session.getId().equals(senderSession.getId()) && session.isOpen()) {
                                try {
                                    session.sendMessage(textMessage);
                                    sentCount++;
                                } catch (IOException e) {
                                    System.err.println("💥 SimpleWebSocketHandler: Error sending typing indicator to session " + session.getId() + ": " + e.getMessage());
                                }
                            }
                        }
                        
                        System.out.println("✅ SimpleWebSocketHandler: Broadcasted typing indicator to " + sentCount + " other participants in conversation: " + conversationId);
                    } else {
                        System.out.println("ℹ️ SimpleWebSocketHandler: No other subscribers for typing indicator in conversation: " + conversationId);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("💥 SimpleWebSocketHandler: Error handling typing indicator: " + e.getMessage());
        }
    }

    /**
     * Subscribe a WebSocket session to user notifications
     */
    private void subscribeToNotifications(WebSocketSession session, String userId, String authToken) {
        // TODO: Add proper authentication validation here
        // For now, we'll trust the userId provided
        
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(session);
        sessionToUser.put(session.getId(), userId);
        
        System.out.println("✅ SimpleWebSocketHandler: User " + userId + " subscribed to notifications via session: " + session.getId());
    }

    /**
     * Unsubscribe a WebSocket session from user notifications
     */
    private void unsubscribeFromNotifications(WebSocketSession session, String userId) {
        System.out.println("🔔 SimpleWebSocketHandler: Unsubscribing session " + session.getId() + " from notifications for user: " + userId);
        
        // Remove session from user's notification list
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            
            // If no more sessions for this user, remove the user entry entirely
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
                System.out.println("🔔 SimpleWebSocketHandler: Removed user " + userId + " from notification subscriptions (no more sessions)");
            }
        }
        
        // Remove session-to-user mapping
        sessionToUser.remove(session.getId());
        
        System.out.println("✅ SimpleWebSocketHandler: Session " + session.getId() + " unsubscribed from notifications for user: " + userId);
    }

    /**
     * Broadcast a notification to all sessions for a specific user
     */
    public void broadcastNotificationToUser(String userId, Object notificationData) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null && !sessions.isEmpty()) {
            try {
                // Create notification message
                Map<String, Object> notificationMessage = Map.of(
                    "type", "notification",
                    "data", notificationData
                );
                
                String jsonMessage = objectMapper.writeValueAsString(notificationMessage);
                TextMessage textMessage = new TextMessage(jsonMessage);
                
                System.out.println("🔔 SimpleWebSocketHandler: Broadcasting notification to " + sessions.size() + " sessions for user: " + userId);
                
                // Send to all user sessions
                sessions.removeIf(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(textMessage);
                            return false; // Keep session
                        } else {
                            System.out.println("🗑️ SimpleWebSocketHandler: Removing closed session: " + session.getId());
                            sessionToUser.remove(session.getId());
                            return true; // Remove closed session
                        }
                    } catch (IOException e) {
                        System.err.println("💥 SimpleWebSocketHandler: Error sending notification to session " + session.getId() + ": " + e.getMessage());
                        sessionToUser.remove(session.getId());
                        return true; // Remove failed session
                    }
                });
                
                System.out.println("✅ SimpleWebSocketHandler: Successfully broadcasted notification to user: " + userId);
            } catch (Exception e) {
                System.err.println("💥 SimpleWebSocketHandler: Error broadcasting notification: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ SimpleWebSocketHandler: No active sessions found for user: " + userId);
        }
    }

    /**
     * Get the number of active notification subscribers for a user
     */
    public int getUserNotificationSubscriberCount(String userId) {
        CopyOnWriteArrayList<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Check if a user has active notification subscriptions
     */
    public boolean hasActiveNotificationSubscriptions(String userId) {
        return getUserNotificationSubscriberCount(userId) > 0;
    }
}