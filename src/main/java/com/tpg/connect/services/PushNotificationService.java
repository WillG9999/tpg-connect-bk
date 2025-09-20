package com.tpg.connect.services;

import com.google.firebase.messaging.*;
import com.tpg.connect.repository.DeviceTokenRepository;
import com.tpg.connect.repository.NotificationRepository;
import com.tpg.connect.model.notifications.DeviceToken;
import com.tpg.connect.model.notifications.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PushNotificationService {

    @Autowired
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public void sendPushNotification(Notification notification) {
        List<DeviceToken> activeTokens = deviceTokenRepository.findActiveTokensByUserId(notification.getUserId());
        
        if (activeTokens.isEmpty()) {
            System.out.println("No active device tokens found for user: " + notification.getUserId());
            return;
        }

        for (DeviceToken deviceToken : activeTokens) {
            try {
                sendNotificationToDevice(notification, deviceToken);
                deviceToken.updateLastUsed();
                deviceTokenRepository.save(deviceToken);
            } catch (Exception e) {
                System.err.println("Failed to send notification to device " + deviceToken.getDeviceId() + ": " + e.getMessage());
                
                if (e.getMessage().contains("registration-token-not-registered") || 
                    e.getMessage().contains("invalid-registration-token")) {
                    deviceToken.setActive(false);
                    deviceTokenRepository.save(deviceToken);
                }
            }
        }
    }

    public void sendBulkNotifications(List<Notification> notifications) {
        Map<String, List<Notification>> userNotifications = new HashMap<>();
        
        for (Notification notification : notifications) {
            userNotifications.computeIfAbsent(notification.getUserId(), k -> new ArrayList<>())
                           .add(notification);
        }

        for (Map.Entry<String, List<Notification>> entry : userNotifications.entrySet()) {
            String userId = entry.getKey();
            List<Notification> userNotifs = entry.getValue();
            
            List<DeviceToken> activeTokens = deviceTokenRepository.findActiveTokensByUserId(userId);
            
            for (DeviceToken deviceToken : activeTokens) {
                try {
                    for (Notification notification : userNotifs) {
                        sendNotificationToDevice(notification, deviceToken);
                    }
                    deviceToken.updateLastUsed();
                    deviceTokenRepository.save(deviceToken);
                } catch (Exception e) {
                    System.err.println("Failed to send bulk notifications to device " + deviceToken.getDeviceId() + ": " + e.getMessage());
                }
            }
        }
    }

    private void sendNotificationToDevice(Notification notification, DeviceToken deviceToken) throws FirebaseMessagingException {
        if (firebaseMessaging == null) {
            System.err.println("Firebase messaging not initialized");
            return;
        }

        Message.Builder messageBuilder = Message.builder()
            .setToken(deviceToken.getToken())
            .setNotification(com.google.firebase.messaging.Notification.builder()
                .setTitle(notification.getTitle())
                .setBody(notification.getMessage())
                .setImage(notification.getImageUrl())
                .build());

        if (notification.getData() != null && !notification.getData().isEmpty()) {
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<String, Object> entry : notification.getData().entrySet()) {
                data.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            messageBuilder.putAllData(data);
        }

        messageBuilder.putData("notificationId", notification.getId());
        messageBuilder.putData("type", notification.getType().name());
        messageBuilder.putData("actionUrl", notification.getActionUrl() != null ? notification.getActionUrl() : "");

        if (deviceToken.getDeviceType() == DeviceToken.DeviceType.ANDROID) {
            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(getAndroidPriority(notification.getPriority()))
                .setNotification(AndroidNotification.builder()
                    .setIcon("ic_notification")
                    .setColor("#FF6B6B")
                    .setChannelId(getChannelId(notification.getType()))
                    .build())
                .build();
            messageBuilder.setAndroidConfig(androidConfig);
        } else if (deviceToken.getDeviceType() == DeviceToken.DeviceType.IOS) {
            ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder()
                    .setAlert(ApsAlert.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getMessage())
                        .build())
                    .setBadge(getBadgeCount(notification.getUserId()))
                    .setSound("default")
                    .build())
                .build();
            messageBuilder.setApnsConfig(apnsConfig);
        }

        String response = firebaseMessaging.send(messageBuilder.build());
        System.out.println("Successfully sent message: " + response);
        
        notification.markAsSent();
    }

    private AndroidConfig.Priority getAndroidPriority(Notification.NotificationPriority priority) {
        return switch (priority) {
            case HIGH, URGENT -> AndroidConfig.Priority.HIGH;
            default -> AndroidConfig.Priority.NORMAL;
        };
    }

    private String getChannelId(Notification.NotificationType type) {
        return switch (type) {
            case NEW_MATCH, NEW_MESSAGE -> "messages";
            case POTENTIAL_MATCHES_READY -> "daily_matches";
            case SUBSCRIPTION_RENEWAL, SUBSCRIPTION_EXPIRED, PAYMENT_FAILED -> "subscription";
            case SAFETY_ALERT -> "safety";
            default -> "general";
        };
    }

    private int getBadgeCount(String userId) {
        return (int) notificationRepository.countUnreadByUserId(userId);
    }

    public void registerDeviceToken(String userId, String token, DeviceToken.DeviceType deviceType, String deviceId) {
        DeviceToken existingToken = deviceTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
            .orElse(null);

        if (existingToken != null) {
            existingToken.setToken(token);
            existingToken.setActive(true);
            existingToken.setLastUsedAt(LocalDateTime.now());
        } else {
            existingToken = new DeviceToken();
            existingToken.setUserId(userId);
            existingToken.setToken(token);
            existingToken.setDeviceType(deviceType);
            existingToken.setDeviceId(deviceId);
            existingToken.setActive(true);
            existingToken.setCreatedAt(LocalDateTime.now());
            existingToken.setLastUsedAt(LocalDateTime.now());
        }

        deviceTokenRepository.save(existingToken);
    }

    public void unregisterDeviceToken(String userId, String deviceId) {
        deviceTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
    }

    public void cleanupExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
        List<DeviceToken> inactiveTokens = deviceTokenRepository.findInactiveTokens(cutoffTime);
        
        for (DeviceToken token : inactiveTokens) {
            token.setActive(false);
        }
        
        deviceTokenRepository.saveAll(inactiveTokens);
        
        List<DeviceToken> expiredTokens = deviceTokenRepository.findExpiredTokens(LocalDateTime.now());
        deviceTokenRepository.deleteAll(expiredTokens);
    }
}