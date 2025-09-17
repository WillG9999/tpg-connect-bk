package com.tpg.connect.model;

public class UserContext {
    private String userId;
    private String username;
    private String email;
    private String role;
    private boolean authenticated;

    public UserContext() {}

    public UserContext(String userId, String username, String email, String role, boolean authenticated) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.authenticated = authenticated;
    }

    public static UserContext fromUser(User user) {
        return new UserContext(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            true
        );
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public boolean hasRole(String role) {
        return this.role != null && this.role.equals(role);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isUser() {
        return hasRole("USER");
    }
}