package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.UserStatus;
import com.tpg.connect.model.user.UserProfile;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.model.match.Match;
import com.tpg.connect.model.conversation.ConversationSummary;
import com.tpg.connect.model.conversation.Conversation;
import com.tpg.connect.model.UserReport;
import com.tpg.connect.model.match.UserAction;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailDTO {
    // Basic user info
    private String connectId;
    private String email;
    private ApplicationStatus applicationStatus;
    private UserStatus userStatus;
    private Boolean active;
    private Boolean emailVerified;
    private Timestamp createdAt;
    private Timestamp lastActiveAt;
    private String role;
    
    // Profile information
    private CompleteUserProfile profile;
    
    // User activity stats
    private UserActivityStats activityStats;
    
    // Recent matches (limited to recent ones for performance)
    private List<Match> recentMatches;
    
    // Recent conversations (limited to recent ones for performance)
    private List<Conversation> recentConversations;
    
    // Reports involving this user (as reporter or reported)
    private List<UserReport> reportsInvolving;
    
    // Recent user actions (likes/passes)
    private List<UserAction> recentActions;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityStats {
        private Integer totalMatches;
        private Integer totalConversations;
        private Integer totalMessages;
        private Integer totalLikes;
        private Integer totalPasses;
        private Integer totalReportsBy;
        private Integer totalReportsAgainst;
        private Integer daysActive;
        private Timestamp lastLogin;
        private Integer loginCount;
    }
}