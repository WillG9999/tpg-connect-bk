package com.tpg.connect.model.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSettings {
    private String userId;
    private String email;
    private boolean emailVerified;
    private String phoneNumber;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;
    private String accountStatus; // "active", "inactive", "suspended"
    private String timezone;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}