package com.tpg.connect.model.user;

public enum ApplicationStatus {
    PENDING_APPROVAL("Your application is being reviewed"),
    APPROVED("Congratulations! Your application has been approved. Complete your membership to access the platform."),
    REJECTED("We appreciate your interest, but we're unable to approve your application at this time"),
    ACTIVE("Active member with full platform access"),
    SUSPENDED("Account suspended");
    
    private final String description;
    
    ApplicationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    public boolean needsPayment() {
        return this == APPROVED;
    }
    
    public boolean isPending() {
        return this == PENDING_APPROVAL;
    }
}