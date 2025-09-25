package com.tpg.connect.controllers.admin;

import com.tpg.connect.model.User;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.util.ConnectIdGenerator;
import com.google.cloud.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/setup")
@Slf4j
public class AdminSeederController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ConnectIdGenerator connectIdGenerator;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @PostMapping("/create-admin")
    public ResponseEntity<Map<String, Object>> createAdminUser(@RequestBody CreateAdminRequest request) {
        log.info("🔧 Creating admin user with email: {}", request.getEmail());
        
        try {
            // Check if admin already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("User with this email already exists"));
            }
            
            // Create admin user
            User adminUser = User.builder()
                    .connectId(connectIdGenerator.generateUniqueConnectId(userRepository))
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .role("ADMIN")
                    .active(true)
                    .emailVerified(true)
                    .createdAt(Timestamp.now())
                    .updatedAt(Timestamp.now())
                    .build();
            
            userRepository.createUser(adminUser);
            
            log.info("✅ Admin user created successfully with ConnectID: {}", adminUser.getConnectId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Admin user created successfully");
            response.put("connectId", adminUser.getConnectId());
            response.put("email", adminUser.getEmail());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating admin user: ", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to create admin user: " + e.getMessage()));
        }
    }
    
    @PostMapping("/make-admin")
    public ResponseEntity<Map<String, Object>> makeUserAdmin(@RequestBody MakeAdminRequest request) {
        log.info("🔧 Making user admin with email: {}", request.getEmail());
        
        try {
            // Find user by email
            var userOpt = userRepository.findByEmail(request.getEmail());
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("User not found with email: " + request.getEmail()));
            }
            
            User user = userOpt.get();
            
            // Update user role to ADMIN
            user.setRole("ADMIN");
            user.setUpdatedAt(Timestamp.now());
            
            User updatedUser = userRepository.updateUser(user);
            
            log.info("✅ User role updated to ADMIN for ConnectID: {}", updatedUser.getConnectId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User role updated to ADMIN successfully");
            response.put("connectId", updatedUser.getConnectId());
            response.put("email", updatedUser.getEmail());
            response.put("role", updatedUser.getRole());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error making user admin: ", e);
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Failed to make user admin: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Admin setup endpoint is available");
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
    
    public static class CreateAdminRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class MakeAdminRequest {
        private String email;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}