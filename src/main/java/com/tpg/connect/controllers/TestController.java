package com.tpg.connect.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tpg.connect.services.CloudStorageService;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private CloudStorageService cloudStorageService;

    @PostMapping("/json")
    public String testJsonDeserialization(@RequestBody TestRequest request) {
        logger.info("Received test request: email={}, name={}", request.getEmail(), request.getName());
        return "JSON deserialization working! Received: " + request.getEmail() + ", " + request.getName();
    }

    @PostMapping("/photo-upload")
    public ResponseEntity<Map<String, Object>> testPhotoUpload(
            @RequestParam("photo") MultipartFile photo) {
        try {
            logger.info("Testing photo upload: filename={}, size={}", photo.getOriginalFilename(), photo.getSize());
            
            String photoUrl = cloudStorageService.uploadProfilePhoto("test-user-123", photo);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "photoUrl", photoUrl,
                "message", "Photo uploaded successfully to Firebase Storage"
            ));
        } catch (Exception e) {
            logger.error("Photo upload test failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    public static class TestRequest {
        private String email;
        private String name;
        private boolean active;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}