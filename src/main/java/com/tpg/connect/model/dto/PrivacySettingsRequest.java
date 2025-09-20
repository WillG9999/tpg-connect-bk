package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsRequest {
    @Pattern(regexp = "^(public|private|matches_only)$", message = "Profile visibility must be 'public', 'private', or 'matches_only'")
    private String profileVisibility;
    
    private Boolean showOnlineStatus;
    private Boolean showLastSeen;
    private Boolean showReadReceipts;
    private Boolean allowMessagesFromMatches;
    private Boolean showDistance;
    private Boolean showAge;
    private Boolean indexProfile;
    private Boolean showProfileInSearch;
    private Boolean dataProcessingConsent;
    private Boolean locationSharing;
}