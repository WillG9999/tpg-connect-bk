package com.tpg.connect.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test-auth")
@CrossOrigin(origins = "*") // Allow frontend to connect during testing
public class TestAuthController {

    // Test registration endpoint
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> testRegister(@RequestBody Map<String, Object> request) {
        try {
            // Extract basic registration data
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            
            // Simulate user creation
            Map<String, Object> fieldVisibility = new java.util.HashMap<>();
            fieldVisibility.put("jobTitle", true);
            fieldVisibility.put("company", true);
            fieldVisibility.put("university", true);
            fieldVisibility.put("religiousBeliefs", true);
            fieldVisibility.put("politics", true);
            fieldVisibility.put("hometown", true);
            fieldVisibility.put("height", true);
            fieldVisibility.put("ethnicity", true);
            
            Map<String, Object> user = new java.util.HashMap<>();
            user.put("id", "test_user_" + System.currentTimeMillis());
            user.put("name", firstName + " " + lastName);
            user.put("age", request.getOrDefault("age", 25));
            user.put("bio", "");
            user.put("photos", java.util.List.of());
            user.put("location", request.getOrDefault("location", "Test City"));
            user.put("interests", java.util.List.of());
            user.put("pronouns", "");
            user.put("gender", request.getOrDefault("gender", ""));
            user.put("sexuality", "");
            user.put("interestedIn", "");
            user.put("jobTitle", "");
            user.put("company", "");
            user.put("university", "");
            user.put("educationLevel", "");
            user.put("religiousBeliefs", "");
            user.put("hometown", "");
            user.put("politics", "");
            user.put("languages", java.util.List.of());
            user.put("datingIntentions", "");
            user.put("relationshipType", "");
            user.put("height", "");
            user.put("ethnicity", "");
            user.put("children", "");
            user.put("familyPlans", "");
            user.put("pets", "");
            user.put("zodiacSign", "");
            user.put("writtenPrompts", java.util.List.of());
            user.put("pollPrompts", java.util.List.of());
            user.put("fieldVisibility", fieldVisibility);
            
            String mockToken = "test_jwt_token_" + System.currentTimeMillis();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user,
                "statusCode", 200,
                "accessToken", mockToken,
                "message", "Registration successful"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Registration failed: " + e.getMessage(),
                "statusCode", 400
            ));
        }
    }

    // Test login endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> testLogin(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String password = (String) request.get("password");
            
            // Simulate user authentication
            Map<String, Object> fieldVisibility = new java.util.HashMap<>();
            fieldVisibility.put("jobTitle", true);
            fieldVisibility.put("company", true);
            fieldVisibility.put("university", true);
            fieldVisibility.put("religiousBeliefs", false);
            fieldVisibility.put("politics", true);
            fieldVisibility.put("hometown", true);
            fieldVisibility.put("height", true);
            fieldVisibility.put("ethnicity", true);
            
            Map<String, Object> user = new java.util.HashMap<>();
            user.put("id", "test_user_existing");
            user.put("name", "Test User");
            user.put("age", 28);
            user.put("bio", "Test user for authentication testing");
            user.put("photos", java.util.List.of("https://picsum.photos/400/600?random=100"));
            user.put("location", "San Francisco, CA");
            user.put("interests", java.util.List.of("Technology", "Travel", "Music"));
            user.put("pronouns", "they/them");
            user.put("gender", "Non-binary");
            user.put("sexuality", "Pansexual");
            user.put("interestedIn", "Everyone");
            user.put("jobTitle", "Software Engineer");
            user.put("company", "Tech Corp");
            user.put("university", "UC Berkeley");
            user.put("educationLevel", "Bachelor's");
            user.put("religiousBeliefs", "Agnostic");
            user.put("hometown", "Los Angeles, CA");
            user.put("politics", "Progressive");
            user.put("languages", java.util.List.of("English", "Spanish"));
            user.put("datingIntentions", "Serious relationship");
            user.put("relationshipType", "Monogamous");
            user.put("height", "5'8\"");
            user.put("ethnicity", "Mixed");
            user.put("children", "No kids");
            user.put("familyPlans", "Open to kids");
            user.put("pets", "Cat person");
            user.put("zodiacSign", "Sagittarius");
            user.put("writtenPrompts", java.util.List.of(
                Map.of("prompt", "My simple pleasures", "answer", "Coffee and good books")
            ));
            user.put("pollPrompts", java.util.List.of(
                Map.of("prompt", "Best first date", "question", "What sounds perfect?", 
                       "options", java.util.List.of("Coffee", "Museum", "Hiking"))
            ));
            user.put("fieldVisibility", fieldVisibility);
            
            String mockToken = "test_jwt_token_" + System.currentTimeMillis();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user,
                "statusCode", 200,
                "accessToken", mockToken,
                "message", "Login successful"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", "Login failed: " + e.getMessage(),
                "statusCode", 400
            ));
        }
    }

    // Test protected endpoint
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "error", "Unauthorized - missing or invalid token",
                "statusCode", 401
            ));
        }
        
        // Simulate getting current user from token
        Map<String, Object> fieldVisibility = new java.util.HashMap<>();
        fieldVisibility.put("jobTitle", true);
        fieldVisibility.put("company", true);
        fieldVisibility.put("university", true);
        fieldVisibility.put("religiousBeliefs", true);
        fieldVisibility.put("politics", true);
        fieldVisibility.put("hometown", true);
        fieldVisibility.put("height", true);
        fieldVisibility.put("ethnicity", true);
        
        Map<String, Object> user = new java.util.HashMap<>();
        user.put("id", "test_user_from_token");
        user.put("name", "Current Test User");
        user.put("age", 30);
        user.put("bio", "Retrieved from JWT token");
        user.put("photos", java.util.List.of("https://picsum.photos/400/600?random=200"));
        user.put("location", "New York, NY");
        user.put("interests", java.util.List.of("Reading", "Cooking"));
        user.put("fieldVisibility", fieldVisibility);
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", user,
            "statusCode", 200
        ));
    }

    // Test logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> testLogout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logout successful",
            "statusCode", 200
        ));
    }
}