package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.Timestamp;
import com.tpg.connect.model.user.ApplicationStatus;
import com.tpg.connect.model.user.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryDTO {
    private String connectId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePhotoUrl;
    private ApplicationStatus applicationStatus;
    private UserStatus userStatus;
    private Boolean active;
    private Boolean emailVerified;
    private Timestamp createdAt;
    private Timestamp lastActiveAt;
    
    // User stats for admin overview
    private Integer totalMatches;
    private Integer totalConversations;
    private Integer totalReports;
    private Boolean hasActiveReports;
    private String location;
    private Integer age;
    private String gender;
}