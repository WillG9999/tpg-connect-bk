package com.tpg.connect.client.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing an Apple Push Notification (APNs) payload
 * 
 * Supports all standard APNs fields including alert, badge, sound,
 * silent notifications, and custom data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApnsNotification {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * The notification content (alert)
     */
    @JsonProperty("aps")
    private Aps aps;
    
    /**
     * Custom data fields
     */
    private Map<String, Object> customData;
    
    /**
     * Push type for APNs header
     */
    private String pushType = "alert";
    
    /**
     * Priority for APNs header (1-10)
     */
    private int priority = 10;
    
    /**
     * Expiration timestamp for APNs header
     */
    private long expiration = 0;
    
    /**
     * APNs topic (bundle ID)
     */
    private String topic;
    
    /**
     * Collapse ID for grouping notifications
     */
    private String collapseId;
    
    // Constructors
    
    public ApnsNotification() {
        this.aps = new Aps();
        this.customData = new HashMap<>();
    }
    
    public ApnsNotification(ApnsAlert alert) {
        this();
        this.aps.setAlert(alert);
    }
    
    public ApnsNotification(String title, String body) {
        this();
        this.aps.setAlert(new ApnsAlert(title, body));
    }
    
    // Builder methods
    
    public static ApnsNotification alert(String title, String body) {
        return new ApnsNotification(title, body);
    }
    
    public static ApnsNotification silent() {
        ApnsNotification notification = new ApnsNotification();
        notification.pushType = "background";
        notification.priority = 5;
        notification.aps.setContentAvailable(1);
        return notification;
    }
    
    public ApnsNotification withAlert(ApnsAlert alert) {
        this.aps.setAlert(alert);
        return this;
    }
    
    public ApnsNotification withBadge(Integer badge) {
        this.aps.setBadge(badge);
        return this;
    }
    
    public ApnsNotification withSound(String sound) {
        this.aps.setSound(sound);
        return this;
    }
    
    public ApnsNotification withCategory(String category) {
        this.aps.setCategory(category);
        return this;
    }
    
    public ApnsNotification withThreadId(String threadId) {
        this.aps.setThreadId(threadId);
        return this;
    }
    
    public ApnsNotification withCustomData(String key, Object value) {
        if (this.customData == null) {
            this.customData = new HashMap<>();
        }
        this.customData.put(key, value);
        return this;
    }
    
    public ApnsNotification withCustomData(Map<String, Object> data) {
        if (this.customData == null) {
            this.customData = new HashMap<>();
        }
        this.customData.putAll(data);
        return this;
    }
    
    public ApnsNotification withPriority(int priority) {
        this.priority = Math.max(1, Math.min(10, priority));
        return this;
    }
    
    public ApnsNotification withExpiration(long expiration) {
        this.expiration = expiration;
        return this;
    }
    
    public ApnsNotification withExpirationIn(long seconds) {
        this.expiration = Instant.now().getEpochSecond() + seconds;
        return this;
    }
    
    public ApnsNotification withTopic(String topic) {
        this.topic = topic;
        return this;
    }
    
    public ApnsNotification withCollapseId(String collapseId) {
        this.collapseId = collapseId;
        return this;
    }
    
    // Getters and Setters
    
    public Aps getAps() {
        return aps;
    }
    
    public void setAps(Aps aps) {
        this.aps = aps;
    }
    
    public Map<String, Object> getCustomData() {
        return customData;
    }
    
    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }
    
    public String getPushType() {
        return pushType;
    }
    
    public void setPushType(String pushType) {
        this.pushType = pushType;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
    public String getCollapseId() {
        return collapseId;
    }
    
    public void setCollapseId(String collapseId) {
        this.collapseId = collapseId;
    }
    
    // Utility methods
    
    /**
     * Convert notification to JSON string for APNs
     */
    public String toJson() {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("aps", aps);
            
            if (customData != null && !customData.isEmpty()) {
                payload.putAll(customData);
            }
            
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize APNs notification", e);
        }
    }
    
    /**
     * Check if this is a silent notification
     */
    public boolean isSilent() {
        return "background".equals(pushType) && 
               aps != null && 
               aps.getContentAvailable() != null && 
               aps.getContentAvailable() == 1;
    }
    
    /**
     * Check if notification has alert content
     */
    public boolean hasAlert() {
        return aps != null && aps.getAlert() != null;
    }
    
    /**
     * Inner class for APNs payload structure
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Aps {
        
        @JsonProperty("alert")
        private ApnsAlert alert;
        
        @JsonProperty("badge")
        private Integer badge;
        
        @JsonProperty("sound")
        private String sound;
        
        @JsonProperty("category")
        private String category;
        
        @JsonProperty("thread-id")
        private String threadId;
        
        @JsonProperty("content-available")
        private Integer contentAvailable;
        
        @JsonProperty("mutable-content")
        private Integer mutableContent;
        
        // Getters and Setters
        
        public ApnsAlert getAlert() {
            return alert;
        }
        
        public void setAlert(ApnsAlert alert) {
            this.alert = alert;
        }
        
        public Integer getBadge() {
            return badge;
        }
        
        public void setBadge(Integer badge) {
            this.badge = badge;
        }
        
        public String getSound() {
            return sound;
        }
        
        public void setSound(String sound) {
            this.sound = sound;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public String getThreadId() {
            return threadId;
        }
        
        public void setThreadId(String threadId) {
            this.threadId = threadId;
        }
        
        public Integer getContentAvailable() {
            return contentAvailable;
        }
        
        public void setContentAvailable(Integer contentAvailable) {
            this.contentAvailable = contentAvailable;
        }
        
        public Integer getMutableContent() {
            return mutableContent;
        }
        
        public void setMutableContent(Integer mutableContent) {
            this.mutableContent = mutableContent;
        }
    }
    
    @Override
    public String toString() {
        return "ApnsNotification{" +
                "aps=" + aps +
                ", customData=" + customData +
                ", pushType='" + pushType + '\'' +
                ", priority=" + priority +
                ", expiration=" + expiration +
                ", topic='" + topic + '\'' +
                ", collapseId='" + collapseId + '\'' +
                '}';
    }
}