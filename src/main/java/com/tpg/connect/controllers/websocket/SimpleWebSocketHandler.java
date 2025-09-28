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
}