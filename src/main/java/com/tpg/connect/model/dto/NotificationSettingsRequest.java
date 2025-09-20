package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsRequest {
    // General notification channels
    private Boolean pushNotificationsEnabled;
    private Boolean emailNotificationsEnabled;
    private Boolean smsNotificationsEnabled;
    
    // Match-related notifications
    private Boolean newMatchNotifications;
    private Boolean newMessageNotifications;
    private Boolean likeNotifications;
    private Boolean superLikeNotifications;
    
    // Discovery notifications
    private Boolean newMatchesAvailableNotifications;
    private Boolean profileViewNotifications;
    
    // Marketing notifications
    private Boolean promotionalEmailsEnabled;
    private Boolean tipsAndTricksEmailsEnabled;
    
    // Timing preferences
    private Boolean quietHoursEnabled;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String quietHoursStart;
    
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format")
    private String quietHoursEnd;
}