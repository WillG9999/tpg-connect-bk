package com.tpg.connect.config;

import com.tpg.connect.controllers.websocket.SimpleWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Simple WebSocket configuration without STOMP protocol
 */
@Configuration
@EnableWebSocket
public class SimpleWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SimpleWebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("🔧 SimpleWebSocketConfig: Registering simple WebSocket handler at /simple-ws");
        
        registry.addHandler(webSocketHandler, "/simple-ws")
                .setAllowedOrigins("*"); // Allow all origins for development
                // .withSockJS(); // Disable SockJS for pure WebSocket support
    }
}