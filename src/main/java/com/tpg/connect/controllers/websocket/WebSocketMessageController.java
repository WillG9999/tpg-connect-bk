package com.tpg.connect.controllers.websocket;

import com.tpg.connect.model.conversation.Message;
import com.tpg.connect.services.AuthenticationService;
import com.tpg.connect.services.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

// Temporarily disabled - using SimpleWebSocketHandler instead
// @Controller
public class WebSocketMessageController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private AuthenticationService authService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversation/{conversationId}/send")
    public void sendMessage(
            @DestinationVariable String conversationId,
            @Payload Map<String, Object> messageData,
            SimpMessageHeaderAccessor headerAccessor) {
        
        System.out.println("📡 WebSocketMessageController: Received message for conversation: " + conversationId);
        
        try {
            // Extract JWT token from WebSocket session
            String token = (String) headerAccessor.getSessionAttributes().get("token");
            if (token == null) {
                System.err.println("❌ WebSocketMessageController: No token in session");
                return;
            }

            // Validate token and extract user ID
            if (!authService.isTokenValid(token)) {
                System.err.println("❌ WebSocketMessageController: Invalid token");
                return;
            }

            String senderId = authService.extractUserIdFromToken(token);
            String content = (String) messageData.get("content");
            
            System.out.println("📡 WebSocketMessageController: Sending message from " + senderId + ": " + content);

            // Send message through existing ConversationService
            Message savedMessage = conversationService.sendMessage(conversationId, senderId, content);
            System.out.println("✅ WebSocketMessageController: Message saved with ID: " + savedMessage.getId());

            // Broadcast message to all subscribers of this conversation topic
            broadcastMessage(conversationId, savedMessage);

        } catch (Exception e) {
            System.err.println("💥 WebSocketMessageController: Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Broadcast a message to all subscribers of a conversation topic
     */
    public void broadcastMessage(String conversationId, Message message) {
        System.out.println("📡 WebSocketMessageController: Broadcasting message to /topic/conversation/" + conversationId);
        
        // Convert message to map for JSON serialization
        Map<String, Object> messageMap = Map.of(
            "id", message.getId(),
            "conversationId", message.getConversationId(),
            "senderId", message.getSenderId(),
            "content", message.getContent(),
            "sentAt", message.getSentAt().toString(),
            "status", message.getStatus() != null ? message.getStatus().toString() : "SENT"
        );

        // Send to topic subscribers
        messagingTemplate.convertAndSend("/topic/conversation/" + conversationId, messageMap);
        System.out.println("✅ WebSocketMessageController: Message broadcasted successfully");
    }

    @MessageMapping("/conversation/{conversationId}/join")
    public void joinConversation(
            @DestinationVariable String conversationId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        System.out.println("📡 WebSocketMessageController: User joining conversation: " + conversationId);
        
        try {
            // Extract and validate token
            String token = (String) headerAccessor.getSessionAttributes().get("token");
            if (token == null || !authService.isTokenValid(token)) {
                System.err.println("❌ WebSocketMessageController: Invalid token for join");
                return;
            }

            String userId = authService.extractUserIdFromToken(token);
            System.out.println("✅ WebSocketMessageController: User " + userId + " joined conversation " + conversationId);

            // Send confirmation back to user
            messagingTemplate.convertAndSendToUser(userId, "/queue/conversation/" + conversationId + "/joined", 
                Map.of("status", "joined", "conversationId", conversationId));

        } catch (Exception e) {
            System.err.println("💥 WebSocketMessageController: Error joining conversation: " + e.getMessage());
        }
    }
}