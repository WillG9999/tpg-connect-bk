package com.tpg.connect.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SettingsDTO {
    // Privacy Settings (to match frontend expectations)
    private boolean notificationsEnabled = true;
    private boolean locationServicesEnabled = true;
    private boolean profilePaused = false;
    
    // Discovery Settings (to match frontend expectations)
    private int minAge = 18;
    private int maxAge = 35;
    private double maxDistance = 50.0;
    private String genderPreference = "Everyone";
    private boolean showMeToMenOnly = false;
    private boolean showMeToWomenOnly = false;
    
    // Notification Settings
    private boolean pushNotificationsEnabled = true;
    private boolean emailNotificationsEnabled = false;
    private boolean matchNotificationsEnabled = true;
    private boolean messageNotificationsEnabled = true;
    private boolean likeNotificationsEnabled = true;
}