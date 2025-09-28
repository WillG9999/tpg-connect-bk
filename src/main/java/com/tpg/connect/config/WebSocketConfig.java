package com.tpg.connect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Temporarily disabled - using SimpleWebSocketConfig instead
// @Configuration
// @EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry messages back to the client
        // on destinations prefixed with "/topic"
        config.enableSimpleBroker("/topic");
        
        // Designate the "/app" prefix for messages that are bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint for WebSocket connections
        // Allow connections from any origin for development (should be restricted in production)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
        
        // Also register a plain WebSocket endpoint without SockJS for native mobile clients
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }
}