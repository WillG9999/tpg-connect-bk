package com.tpg.connect.model.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    private String userId;
    
    // General notification channels
    private boolean pushNotificationsEnabled;
    private boolean emailNotificationsEnabled;
    private boolean smsNotificationsEnabled;
    
    // Match-related notifications
    private boolean newMatchNotifications;
    private boolean newMessageNotifications;
    private boolean likeNotifications;
    private boolean superLikeNotifications;
    
    // Discovery notifications
    private boolean newMatchesAvailableNotifications;
    private boolean profileViewNotifications;
    
    // Marketing notifications
    private boolean promotionalEmailsEnabled;
    private boolean tipsAndTricksEmailsEnabled;
    
    // Timing preferences
    private boolean quietHoursEnabled;
    private String quietHoursStart; // "22:00"
    private String quietHoursEnd;   // "08:00"
}