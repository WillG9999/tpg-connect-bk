package com.tpg.connect.controllers.user;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.user.CompleteUserProfile;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.services.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController extends BaseController implements UserControllerApi {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserProfileService userProfileService;

    @Override
    public ResponseEntity<Map<String, Object>> getCurrentUserProfile(String authHeader, boolean includePreferences) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Missing or invalid authorization header"));
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid or expired token"));
        }

        String username = authService.extractUsername(token);
        
        String userId = getUserIdFromUsername(username);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "User not found"));
        }

        CompleteUserProfile profile = userProfileService.getCurrentUserProfile(userId, includePreferences);
        
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "User profile not found"));
        }

        if (profile.getAge() < 18 || profile.getAge() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Invalid age range"));
        }

        if (profile.getPhotos() != null && (profile.getPhotos().size() < 1 || profile.getPhotos().size() > 6)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Photos must contain 1-6 items"));
        }

        return successResponse(profile);
    }

    private String getUserIdFromUsername(String username) {
        switch (username) {
            case "admin":
                return "1";
            case "user":
                return "2";
            case "alex":
                return "user_123";
            default:
                return null;
        }
    }
}