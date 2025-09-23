package com.tpg.connect.services;

import com.tpg.connect.model.User;
import com.tpg.connect.model.user.auth.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authService;

    private final ThreadLocal<UserContext> currentUserContext = new ThreadLocal<>();

    public UserContext getCurrentUserContext() {
        return currentUserContext.get();
    }

    public void setCurrentUserContext(UserContext userContext) {
        currentUserContext.set(userContext);
    }

    public void clearCurrentUserContext() {
        currentUserContext.remove();
    }

    public UserContext createUserContextFromToken(String token) {
        if (token != null && authService.isTokenValid(token)) {
            String userId = authService.extractUserIdFromToken(token);
            User user = userService.findById(userId);
            
            if (user != null && user.isActive()) {
                return UserContext.fromUser(user);
            }
        }
        return null;
    }

    public boolean isCurrentUserAdmin() {
        UserContext context = getCurrentUserContext();
        return context != null && context.isAdmin();
    }

    public boolean isCurrentUserAuthenticated() {
        UserContext context = getCurrentUserContext();
        return context != null && context.isAuthenticated();
    }

    public String getCurrentUsername() {
        UserContext context = getCurrentUserContext();
        return context != null ? context.getUsername() : null;
    }

    public String getCurrentUserRole() {
        UserContext context = getCurrentUserContext();
        return context != null ? context.getRole() : null;
    }
}