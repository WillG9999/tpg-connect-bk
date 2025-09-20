package com.tpg.connect.model.conversation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String conversationId;
    private String senderId;
    private String content;
    private LocalDateTime sentAt;
    private MessageStatus status;
    
    public enum MessageStatus {
        SENT,
        DELIVERED,
        READ
    }
}