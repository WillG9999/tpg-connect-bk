package com.tpg.connect.controllers.discovery;

import com.tpg.connect.constants.enums.EndpointConstants;
import com.tpg.connect.controllers.BaseController;
import com.tpg.connect.model.api.PotentialMatchesResponse;
import com.tpg.connect.model.dto.MatchActionsRequest;
import com.tpg.connect.services.AuthService;
import com.tpg.connect.services.PotentialMatchesService;
import com.tpg.connect.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery/matches")
public class PotentialMatchesController extends BaseController implements PotentialMatchesControllerApi {

    @Autowired
    private AuthService authService;

    @Autowired
    private PotentialMatchesService potentialMatchesService;

    @Autowired
    private UserService userService;

    @Override
    public ResponseEntity<Map<String, Object>> getMatchesStatus(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> status = potentialMatchesService.getMatchesStatus(userId);
            return successResponse(status);
        } catch (Exception e) {
            return errorResponse("Failed to get matches status: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<PotentialMatchesResponse> getTodaysMatches(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new PotentialMatchesResponse(false, "Invalid or missing authorization", null, null, 0, false));
        }

        try {
            // Check if it's after 7pm
            LocalTime now = LocalTime.now();
            LocalTime releaseTime = LocalTime.of(19, 0); // 7:00 PM
            
            if (now.isBefore(releaseTime)) {
                Duration timeUntilRelease = Duration.between(now, releaseTime);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new PotentialMatchesResponse(false, 
                        "New matches available at 7:00 PM. Time remaining: " + formatDuration(timeUntilRelease), 
                        null, null, 0, false));
            }

            PotentialMatchesResponse response = potentialMatchesService.getTodaysMatches(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PotentialMatchesResponse(false, "Failed to get today's matches: " + e.getMessage(), 
                    null, null, 0, false));
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> submitMatchActions(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody MatchActionsRequest request) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> result = potentialMatchesService.submitMatchActions(userId, request);
            return successResponse(result);
        } catch (Exception e) {
            return errorResponse("Failed to submit match actions: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMatchesCountdown(
            @RequestHeader("Authorization") String authHeader) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            LocalTime now = LocalTime.now();
            LocalDateTime nextRelease;
            
            // Calculate next 7pm
            if (now.isBefore(LocalTime.of(19, 0))) {
                // Before 7pm today
                nextRelease = LocalDateTime.now().with(LocalTime.of(19, 0));
            } else {
                // After 7pm today, next release is tomorrow at 7pm
                nextRelease = LocalDateTime.now().plusDays(1).with(LocalTime.of(19, 0));
            }
            
            Duration timeUntilRelease = Duration.between(LocalDateTime.now(), nextRelease);
            
            Map<String, Object> countdown = Map.of(
                "nextMatchesTime", nextRelease,
                "timeUntilMatches", timeUntilRelease.toMinutes(),
                "formattedTime", formatDuration(timeUntilRelease),
                "matchesAvailable", now.isAfter(LocalTime.of(19, 0))
            );
            
            return successResponse(countdown);
        } catch (Exception e) {
            return errorResponse("Failed to get matches countdown: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> getMatchesHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String userId = validateAndExtractUserId(authHeader);
        if (userId == null) {
            return unauthorizedResponse("Invalid or missing authorization");
        }

        try {
            Map<String, Object> history = potentialMatchesService.getMatchesHistory(userId, page, size);
            return successResponse(history);
        } catch (Exception e) {
            return errorResponse("Failed to get matches history: " + e.getMessage());
        }
    }

    private String validateAndExtractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(EndpointConstants.Headers.BEARER_PREFIX)) {
            return null;
        }

        String token = authHeader.substring(EndpointConstants.Headers.BEARER_PREFIX.length());
        
        if (!authService.validateToken(token)) {
            return null;
        }

        String username = authService.extractUsername(token);
        return getUserIdFromUsername(username);
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

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        if (hours > 0) {
            return String.format("%d hours, %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d minutes, %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", seconds);
        }
    }

    protected ResponseEntity<Map<String, Object>> unauthorizedResponse(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", message));
    }

    protected ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("success", false, "message", message));
    }
}