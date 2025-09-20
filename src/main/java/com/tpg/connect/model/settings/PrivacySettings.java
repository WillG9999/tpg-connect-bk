package com.tpg.connect.model.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettings {
    private String userId;
    private String profileVisibility; // "public", "private", "matches_only"
    private boolean showOnlineStatus;
    private boolean showLastSeen;
    private boolean showReadReceipts;
    private boolean allowMessagesFromMatches;
    private boolean showDistance;
    private boolean showAge;
    private boolean indexProfile; // Allow profile to be indexed/searchable
    private boolean showProfileInSearch;
    private boolean dataProcessingConsent;
    private boolean locationSharing;
}