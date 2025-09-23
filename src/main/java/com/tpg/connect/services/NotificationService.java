package com.tpg.connect.services;

import com.tpg.connect.repository.NotificationRepository;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.model.dto.NotificationRequest;
import com.tpg.connect.model.notifications.Notification;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.User;
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


    @Async
    public void createAndSendNotification(NotificationRequest request) {
        Optional<User> userOpt = userRepository.findById(request.getUserId());
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }
        User user = userOpt.get();

        Notification notification = createNotification(request);
        notification = notificationRepository.save(notification);

        if (request.isSendImmediately()) {
            sendNotification(notification);
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
        try {
            switch (notification.getChannel()) {
                case PUSH:
                    notification.markAsSent();
                    break;
                case EMAIL:
                    break;
                case IN_APP:
                    notification.markAsSent();
                    break;
                case SMS:
                    break;
            }

            notification.setStatus(Notification.NotificationStatus.SENT);
            notificationRepository.save(notification);
            clearUserNotificationCache(notification.getUserId());
        } catch (Exception e) {
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            System.err.println("Failed to send notification: " + e.getMessage());
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

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setType(Notification.NotificationType.NEW_MATCH);
        request.setTitle("New Match!");
        request.setMessage("You matched with " + matchUserName + "!");
        request.setData(data);
        request.setPriority(Notification.NotificationPriority.HIGH);
        request.setActionUrl("/matches/" + matchUserId);

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
        System.out.println("Sending unmatch notification to user " + userId + " for match " + matchId);
    }

    public void sendMessageNotification(String userId, String senderId, String messageId) {
        System.out.println("Sending message notification to user " + userId + " from " + senderId + " (messageId: " + messageId + ")");
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