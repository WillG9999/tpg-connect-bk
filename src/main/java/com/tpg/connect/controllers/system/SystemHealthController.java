package com.tpg.connect.controllers.system;

import com.tpg.connect.config.FirebaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system/health")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class SystemHealthController {
    
    @Autowired
    private FirebaseConfig firebaseConfig;
    
    /**
     * General health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "Connect Backend");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Firebase connection health check
     */
    @GetMapping("/firebase")
    public ResponseEntity<Map<String, Object>> firebaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            String connectionStatus = firebaseConfig.getConnectionStatus();
            boolean isHealthy = firebaseConfig.isFirestoreHealthy();
            
            health.put("firestore", Map.of(
                "status", connectionStatus,
                "healthy", isHealthy,
                "timestamp", System.currentTimeMillis()
            ));
            
            // Return 200 for healthy, 503 for unhealthy
            HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            health.put("overall", isHealthy ? "UP" : "DOWN");
            
            return ResponseEntity.status(status).body(health);
            
        } catch (Exception e) {
            health.put("firestore", Map.of(
                "status", "ERROR",
                "healthy", false,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
            
            health.put("overall", "DOWN");
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
    
    /**
     * Detailed system health including all components
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Firebase health
            String connectionStatus = firebaseConfig.getConnectionStatus();
            boolean firestoreHealthy = firebaseConfig.isFirestoreHealthy();
            
            health.put("components", Map.of(
                "firestore", Map.of(
                    "status", connectionStatus,
                    "healthy", firestoreHealthy,
                    "details", "Firebase Firestore database connection"
                ),
                "application", Map.of(
                    "status", "UP",
                    "healthy", true,
                    "details", "Spring Boot application"
                )
            ));
            
            // Overall status
            boolean overallHealthy = firestoreHealthy;
            health.put("status", overallHealthy ? "UP" : "DOWN");
            health.put("timestamp", System.currentTimeMillis());
            
            // Return appropriate status code
            HttpStatus status = overallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            return ResponseEntity.status(status).body(health);
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }
}