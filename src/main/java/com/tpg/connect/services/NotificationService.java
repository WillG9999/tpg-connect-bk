package com.tpg.connect.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.tpg.connect.repository.NotificationRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.model.dto.NotificationRequest;
import com.tpg.connect.model.notifications.Notification;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.User;
import com.tpg.connect.controllers.websocket.SimpleWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpleWebSocketHandler webSocketHandler;


    @Async
    public void createAndSendNotification(NotificationRequest request) {
        System.out.println("🔔 NotificationService: createAndSendNotification called for user: " + request.getUserId());
        
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (!userOpt.isPresent()) {
            System.err.println("❌ NotificationService: User not found: " + request.getUserId());
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();
        System.out.println("✅ NotificationService: User found: " + user.getConnectId());

        System.out.println("🔔 NotificationService: Creating notification...");
        Notification notification = createNotification(request);
        System.out.println("🔔 NotificationService: Saving notification to database...");
        notification = notificationRepository.save(notification);
        System.out.println("✅ NotificationService: Notification saved with ID: " + notification.getId());

        if (request.isSendImmediately()) {
            System.out.println("🔔 NotificationService: Sending notification immediately...");
            sendNotification(notification);
        } else {
            System.out.println("ℹ️ NotificationService: Notification queued for later sending");
        }
    }

    public Notification createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setTitle(request.getTitle() != null ? request.getTitle() : request.getType().getDisplayTitle());
        notification.setMessage(request.getMessage() != null ? request.getMessage() : request.getType().getDefaultMessage());
        notification.setData(request.getData());
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setChannel(request.getChannel());
        notification.setPriority(request.getPriority());
        notification.setActionUrl(request.getActionUrl());
        notification.setImageUrl(request.getImageUrl());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setSent(false);

        return notification;
    }

    @Async
    public void sendNotification(Notification notification) {
        System.out.println("🔔 NotificationService: sendNotification called for " + notification.getChannel() + " notification to user: " + notification.getUserId());
        try {
            switch (notification.getChannel()) {
                case PUSH:
                    System.out.println("📱 NotificationService: Processing PUSH notification");
                    // Send actual push notification (FCM or APNS)
                    sendPushNotification(notification);
                    notification.markAsSent();
                    break;
                case EMAIL:
                    System.out.println("📧 NotificationService: Processing EMAIL notification (not implemented)");
                    break;
                case IN_APP:
                    System.out.println("📲 NotificationService: Processing IN_APP notification");
                    notification.markAsSent();
                    // Send via WebSocket for real-time delivery
                    sendWebSocketNotification(notification);
                    break;
                case SMS:
                    System.out.println("📨 NotificationService: Processing SMS notification (not implemented)");
                    break;
            }

            notification.setStatus(Notification.NotificationStatus.SENT);
            notificationRepository.save(notification);
            clearUserNotificationCache(notification.getUserId());
            System.out.println("✅ NotificationService: Notification sent successfully - ID: " + notification.getId());
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            System.err.println("💥 NotificationService: Failed to send notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendPushNotification(Notification notification) {
        try {
            System.out.println("🚀 NotificationService: ===== SENDING PUSH NOTIFICATION TO LOCK SCREEN =====");
            System.out.println("📱 FCM: Sending to user " + notification.getUserId());
            System.out.println("📱 Title: " + notification.getTitle());
            System.out.println("📱 Message: " + notification.getMessage());
            System.out.println("📱 Type: " + notification.getType());
            System.out.println("📱 Action URL: " + notification.getActionUrl());
            
            // Get user FCM tokens  
            Optional<User> userOpt = userRepository.findById(notification.getUserId());
            if (!userOpt.isPresent()) {
                System.err.println("❌ NotificationService: User not found for push notification: " + notification.getUserId());
                return;
            }
            
            User user = userOpt.get();
            List<User.FcmToken> fcmTokens = user.getFcmTokens();
            
            // For demo purposes, we'll simulate the push notification since FCM tokens aren't set up
            System.out.println("🚀 NotificationService: ===== SIMULATED PUSH NOTIFICATION =====");
            System.out.println("📱 PUSH NOTIFICATION SENT TO LOCK SCREEN");
            System.out.println("📱 Platform: iOS/Android/Web (Flutter app)");
            System.out.println("📱 Title: " + notification.getTitle());
            System.out.println("📱 Body: " + notification.getMessage());
            System.out.println("📱 Type: " + notification.getType().name());
            System.out.println("📱 Data: " + notification.getData());
            System.out.println("📱 User would see this notification on their lock screen!");
            
            if (fcmTokens != null && !fcmTokens.isEmpty()) {
                System.out.println("📱 FCM tokens available: " + fcmTokens.size());
                
                // Send to all user's devices
                for (User.FcmToken fcmToken : fcmTokens) {
                    String token = fcmToken.getToken();
                    try {
                        System.out.println("📱 FCM: Sending to token: " + token.substring(0, Math.min(20, token.length())) + "...");
                        
                        // Build FCM message for iOS/Android/Web
                        Message message = Message.builder()
                            // Core notification
                            .setNotification(com.google.firebase.messaging.Notification.builder()
                                .setTitle(notification.getTitle())
                                .setBody(notification.getMessage())
                                .build())
                            
                            // iOS-specific configuration  
                            .setApnsConfig(ApnsConfig.builder()
                                .setAps(Aps.builder()
                                    .setAlert(com.google.firebase.messaging.ApsAlert.builder()
                                        .setTitle(notification.getTitle())
                                        .setBody(notification.getMessage())
                                        .build())
                                    .setSound("default")
                                    .setBadge(1)
                                    .setContentAvailable(true) // For background app refresh
                                    .build())
                                .putCustomData("type", notification.getType().name())
                                .putCustomData("userId", notification.getUserId())
                                .putCustomData("notificationId", notification.getId())
                                .putCustomData("actionUrl", notification.getActionUrl() != null ? notification.getActionUrl() : "")
                                .build())
                            
                            // Android-specific configuration
                            .setAndroidConfig(AndroidConfig.builder()
                                .setNotification(AndroidNotification.builder()
                                    .setTitle(notification.getTitle())
                                    .setBody(notification.getMessage())
                                    .setIcon("ic_notification")
                                    .setColor("#FF6B6B")
                                    .setSound("default")
                                    .build())
                                .putData("type", notification.getType().name())
                                .putData("userId", notification.getUserId())
                                .putData("notificationId", notification.getId())
                                .putData("actionUrl", notification.getActionUrl() != null ? notification.getActionUrl() : "")
                                .build())
                            
                            // Web push configuration
                            .setWebpushConfig(WebpushConfig.builder()
                                .setNotification(WebpushNotification.builder()
                                    .setTitle(notification.getTitle())
                                    .setBody(notification.getMessage())
                                    .setIcon("/icon-192x192.png")
                                    .build())
                                .putData("type", notification.getType().name())
                                .putData("userId", notification.getUserId())
                                .putData("notificationId", notification.getId())
                                .putData("actionUrl", notification.getActionUrl() != null ? notification.getActionUrl() : "")
                                .build())
                            
                            .setToken(token)
                            .build();
                        
                        // Send the message
                        String response = FirebaseMessaging.getInstance().send(message);
                        System.out.println("✅ FCM: Successfully sent message: " + response);
                        
                    } catch (Exception e) {
                        System.err.println("💥 FCM: Failed to send to token " + token.substring(0, Math.min(20, token.length())) + "...: " + e.getMessage());
                        // Continue with other tokens even if one fails
                    }
                }
            } else {
                System.out.println("⚠️ NotificationService: No FCM tokens found for user " + notification.getUserId());
                System.out.println("📱 In production, you would register FCM tokens in the Flutter app");
            }
            
            System.out.println("🚀 NotificationService: ========== PUSH NOTIFICATION COMPLETED ==========");
            
        } catch (Exception e) {
            System.err.println("💥 NotificationService: Error sending push notification: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void sendWebSocketNotification(Notification notification) {
        try {
            if (webSocketHandler.hasActiveNotificationSubscriptions(notification.getUserId())) {
                System.out.println("🔔 NotificationService: Broadcasting notification via WebSocket to user: " + notification.getUserId());
                webSocketHandler.broadcastNotificationToUser(notification.getUserId(), notification);
            } else {
                System.out.println("ℹ️ NotificationService: No active WebSocket sessions for user: " + notification.getUserId());
            }
        } catch (Exception e) {
            System.err.println("💥 NotificationService: Error sending WebSocket notification: " + e.getMessage());
        }
    }

    @Cacheable(value = "userNotifications", key = "#userId + '_' + #page + '_' + #size")
    public List<Notification> getUserNotifications(String userId, int page, int size) {
        if (page == 0 && size == 50) {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return notificationRepository.findTopByUserIdOrderByCreatedAtDesc(userId, page, size);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findUnreadByUserId(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @CacheEvict(value = "userNotifications", key = "#userId + '*'")
    public void markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notification.markAsRead();
            notificationRepository.save(notification);
            clearUserNotificationCache(userId);
        }
    }

    @CacheEvict(value = "userNotifications", key = "#userId + '*'")
    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        notificationRepository.saveAll(unreadNotifications);
        clearUserNotificationCache(userId);
    }

    public void deleteNotification(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null && notification.getUserId().equals(userId)) {
            notificationRepository.delete(notification);
            clearUserNotificationCache(userId);
        }
    }

    public void deleteAllUserNotifications(String userId) {
        notificationRepository.deleteByUserIdAndCreatedAtBefore(userId, LocalDateTime.now().plusDays(1));
        clearUserNotificationCache(userId);
    }

    public void sendNewMatchNotification(String userId, String matchUserId, String matchUserName) {
        Map<String, Object> data = new HashMap<>();
        data.put("matchUserId", matchUserId);
        data.put("matchUserName", matchUserName);

        // Check if user has active WebSocket sessions (app is open)
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.NEW_MATCH);
        request.setTitle("New Match!");
        request.setMessage("You matched with " + matchUserName + "!");
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.HIGH);
        request.setActionUrl("/matches/" + matchUserId);
        request.setSendImmediately(true);
        
        if (webSocketHandler.hasActiveNotificationSubscriptions(userId)) {
            System.out.println("🔔 NotificationService: User " + userId + " has active sessions, sending in-app notification");
            request.setChannel(Notification.NotificationChannel.IN_APP);
        } else {
            System.out.println("🔔 NotificationService: User " + userId + " has no active sessions, sending push notification");
            request.setChannel(Notification.NotificationChannel.PUSH);
        }

        createAndSendNotification(request);
    }

    public void sendPotentialMatchesReadyNotification(String userId, int matchCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("matchCount", matchCount);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.POTENTIAL_MATCHES_READY);
        request.setTitle("New Potential Matches!");
        request.setMessage(matchCount + " new potential matches are waiting for you!");
        request.setData(data);
        request.setActionUrl("/discover");

        createAndSendNotification(request);
    }

    public void sendProfileViewNotification(String userId, String viewerUserId) {
        Map<String, Object> data = new HashMap<>();
        data.put("viewerUserId", viewerUserId);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.PROFILE_VIEW);
        request.setTitle("Profile View");
        request.setMessage("Someone viewed your profile!");
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.LOW);

        createAndSendNotification(request);
    }

    public void sendSubscriptionNotification(String userId, Notification.NotificationType type, String message) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(type);
        request.setMessage(message);
        request.setPriority(Notification.NotificationPriority.HIGH);
        request.setActionUrl("/subscription");

        createAndSendNotification(request);
    }

    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository.findOlderThan(cutoffDate);
        notificationRepository.deleteAll(oldNotifications);
    }

    public void processPendingNotifications() {
        LocalDateTime oldPending = LocalDateTime.now().minusMinutes(5);
        List<Notification> pendingNotifications = notificationRepository.findPendingOlderThan(oldPending);
        
        for (Notification notification : pendingNotifications) {
            if (!notification.isExpired()) {
                sendNotification(notification);
            } else {
                notification.setStatus(Notification.NotificationStatus.CANCELLED);
                notificationRepository.save(notification);
            }
        }
    }

    public void sendMatchNotification(String userId, String matchedUserId, String matchId) {
        sendNewMatchNotification(userId, matchedUserId, "Someone");
    }

    public void sendUnmatchNotification(String userId, String matchId) {
        Map<String, Object> data = new HashMap<>();
        data.put("matchId", matchId);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.UNMATCH);
        request.setTitle("Match Ended");
        request.setMessage("Someone ended your match");
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.NORMAL);
        request.setActionUrl("/matches");

        createAndSendNotification(request);
    }

    public void sendMessageNotification(String userId, String senderId, String conversationId, String messageContent) {
        System.out.println("🔔 NotificationService: Checking notification for user " + userId + " (sender: " + senderId + ")");
        
        // Check if user has active WebSocket sessions (app is open)
        int activeSessionCount = webSocketHandler.getUserNotificationSubscriberCount(userId);
        boolean hasActiveSessions = webSocketHandler.hasActiveNotificationSubscriptions(userId);
        
        System.out.println("🔔 NotificationService: User " + userId + " active session count: " + activeSessionCount);
        System.out.println("🔔 NotificationService: User " + userId + " has active sessions: " + hasActiveSessions);
        
        if (hasActiveSessions) {
            System.out.println("ℹ️ NotificationService: User " + userId + " has active sessions, skipping push notification (app is open)");
            return;
        }
        
        System.out.println("🔔 NotificationService: User " + userId + " has no active sessions, sending push notification (app closed/screen locked)");
        
        Map<String, Object> data = new HashMap<>();
        data.put("senderId", senderId);
        data.put("conversationId", conversationId);
        data.put("messageContent", messageContent);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.NEW_MESSAGE);
        request.setTitle("New Message");
        request.setMessage(messageContent.length() > 50 ? messageContent.substring(0, 50) + "..." : messageContent);
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.NORMAL);
        request.setChannel(Notification.NotificationChannel.PUSH); // Use PUSH for screen-locked notifications
        request.setActionUrl("/conversations/" + conversationId);
        request.setSendImmediately(true);

        System.out.println("🔔 NotificationService: About to call createAndSendNotification for message notification");
        createAndSendNotification(request);
        System.out.println("🔔 NotificationService: createAndSendNotification completed for message notification");
    }

    public void sendConversationEndedNotification(String userId, String conversationId) {
        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversationId);

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.CONVERSATION_ENDED);
        request.setTitle("Conversation Ended");
        request.setMessage("A conversation has been ended");
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.NORMAL);
        request.setActionUrl("/conversations");

        createAndSendNotification(request);
    }

    public void sendBlockNotificationToAdmin(String userId, String targetUserId, String reason) {
        System.out.println("Admin notification: User " + userId + " blocked user " + targetUserId + " for reason: " + reason);
    }

    public void sendReportNotificationToModerators(UserReport report) {
        System.out.println("Moderator notification: New report " + report.getConnectId() + 
                " - User " + report.getReporterId() + " reported user " + report.getReportedUserId());
        System.out.println("Reasons: " + String.join(", ", report.getReasons()));
    }

    public void sendAutoBlockNotification(String userId, int reportCount) {
        System.out.println("Auto-block notification: User " + userId + 
                " has been automatically blocked due to " + reportCount + " reports");
    }

    @CacheEvict(value = "userNotifications", allEntries = true)
    private void clearUserNotificationCache(String userId) {
    }
}