package com.tpg.connect.model.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    private String id;
    
    private String userId;
    
    private NotificationType type;
    
    private String title;
    
    private String message;
    
    private Map<String, Object> data;
    
    private NotificationStatus status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime readAt;
    
    private LocalDateTime sentAt;
    
    private String deviceToken;
    
    private NotificationChannel channel;
    
    private boolean isRead;
    
    private boolean isSent;
    
    private String actionUrl;
    
    private String imageUrl;
    
    private NotificationPriority priority;
    
    public enum NotificationType {
        NEW_MATCH("New Match", "You have a new match!"),
        NEW_MESSAGE("New Message", "You have a new message"),
        PROFILE_VIEW("Profile View", "Someone viewed your profile"),
        POTENTIAL_MATCHES_READY("Daily Matches", "Your potential matches are ready"),
        SUBSCRIPTION_RENEWAL("Subscription", "Your subscription will renew soon"),
        SUBSCRIPTION_EXPIRED("Subscription", "Your subscription has expired"),
        PAYMENT_FAILED("Payment", "Payment failed - update required"),
        WELCOME("Welcome", "Welcome to Connect!"),
        PROFILE_INCOMPLETE("Profile", "Complete your profile to get better matches"),
        ACCOUNT_VERIFICATION("Verification", "Please verify your account"),
        SAFETY_ALERT("Safety", "Important safety information"),
        SYSTEM_MAINTENANCE("System", "Scheduled maintenance notification"),
        PROMOTIONAL("Promotion", "Special offer available"),
        UNMATCH("Match Ended", "Someone ended your match"),
        CONVERSATION_ENDED("Conversation Ended", "A conversation has been ended");
        
        private final String displayTitle;
        private final String defaultMessage;
        
        NotificationType(String displayTitle, String defaultMessage) {
            this.displayTitle = displayTitle;
            this.defaultMessage = defaultMessage;
        }
        
        public String getDisplayTitle() {
            return displayTitle;
        }
        
        public String getDefaultMessage() {
            return defaultMessage;
        }
    }
    
    public enum NotificationStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        CANCELLED
    }
    
    public enum NotificationChannel {
        PUSH,
        EMAIL,
        IN_APP,
        SMS
    }
    
    public enum NotificationPriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
    
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
        this.status = NotificationStatus.SENT;
    }
    
    public boolean isExpired() {
        if (createdAt == null) return false;
        
        LocalDateTime expiryTime = switch (type) {
            case NEW_MESSAGE, NEW_MATCH -> createdAt.plusHours(24);
            case POTENTIAL_MATCHES_READY -> createdAt.plusHours(12);
            case SUBSCRIPTION_RENEWAL, PAYMENT_FAILED -> createdAt.plusDays(7);
            default -> createdAt.plusDays(3);
        };
        
        return LocalDateTime.now().isAfter(expiryTime);
    }
}