package com.tpg.connect.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {
    private String userId;
    private String username;
    private String email;
    private String role;
    private boolean authenticated;

    public static UserContext fromUser(User user) {
        return new UserContext(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            true
        );
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