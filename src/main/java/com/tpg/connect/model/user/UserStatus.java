package com.tpg.connect.model.user;

public enum UserStatus {
    ACTIVE("Active user with full platform access"),
    SUSPENDED("Account temporarily suspended"),
    BANNED("Account permanently banned");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    public boolean isRestricted() {
        return this == SUSPENDED || this == BANNED;
    }
    
    public boolean isPermanentlyRestricted() {
        return this == BANNED;
    }
}