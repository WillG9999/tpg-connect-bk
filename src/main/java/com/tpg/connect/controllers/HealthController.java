package com.tpg.connect.controllers;

import com.tpg.connect.constants.EndpointConstants;
import com.tpg.connect.config.FirebaseConfig;
import com.tpg.connect.services.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Health", description = "Health check endpoints for container orchestration")
@RestController
@RequestMapping(EndpointConstants.Health.BASE)
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Autowired(required = false)
    private EmailService emailService;

    @Value("${spring.application.name:connect}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    private final Instant startTime = Instant.now();

    @Operation(summary = "Basic health check", description = "Returns basic health status for load balancers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is healthy"),
            @ApiResponse(responseCode = "503", description = "Application is unhealthy")
    })
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "message", "Application is running"));
    }

    @Operation(summary = "Liveness probe", description = "Kubernetes liveness probe endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is alive"),
            @ApiResponse(responseCode = "503", description = "Application should be restarted")
    })
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("application", applicationName);
        response.put("uptime", Instant.now().getEpochSecond() - startTime.getEpochSecond());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Readiness probe", description = "Kubernetes readiness probe endpoint with dependency checks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application is ready to serve traffic"),
            @ApiResponse(responseCode = "503", description = "Application dependencies not ready")
    })
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> checks = new HashMap<>();
        boolean allHealthy = true;

        // Check Firebase connection
        try {
            boolean firestoreHealthy = firebaseConfig.isFirestoreHealthy();
            checks.put("firestore", firestoreHealthy ? "UP" : "DOWN");
            if (!firestoreHealthy) {
                allHealthy = false;
            }
        } catch (Exception e) {
            logger.warn("Health check failed for Firestore: {}", e.getMessage());
            checks.put("firestore", "DOWN");
            allHealthy = false;
        }

        // Check email service
        if (emailService != null) {
            try {
                boolean emailHealthy = emailService.isHealthy();
                checks.put("email", emailHealthy ? "UP" : "DOWN");
                if (!emailHealthy) {
                    allHealthy = false;
                }
            } catch (Exception e) {
                logger.warn("Health check failed for email service: {}", e.getMessage());
                checks.put("email", "DOWN");
                allHealthy = false;
            }
        } else {
            checks.put("email", "NOT_CONFIGURED");
        }

        response.put("status", allHealthy ? "UP" : "DOWN");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("checks", checks);
        response.put("application", applicationName);

        HttpStatus status = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(summary = "Detailed health info", description = "Comprehensive health information for monitoring")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detailed health information")
    })
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> system = new HashMap<>();
        Map<String, Object> application = new HashMap<>();

        // System information
        Runtime runtime = Runtime.getRuntime();
        system.put("processors", runtime.availableProcessors());
        system.put("memory", Map.of(
            "total", runtime.totalMemory(),
            "free", runtime.freeMemory(),
            "max", runtime.maxMemory(),
            "used", runtime.totalMemory() - runtime.freeMemory()
        ));
        system.put("jvm", Map.of(
            "version", System.getProperty("java.version"),
            "vendor", System.getProperty("java.vendor"),
            "runtime", System.getProperty("java.runtime.name")
        ));

        // Application information
        application.put("name", applicationName);
        application.put("port", serverPort);
        application.put("startTime", startTime.toString());
        application.put("uptime", Instant.now().getEpochSecond() - startTime.getEpochSecond());
        application.put("profile", System.getProperty("spring.profiles.active", "default"));

        // Service status
        Map<String, String> services = new HashMap<>();
        services.put("firestore", firebaseConfig.getConnectionStatus());
        if (emailService != null) {
            services.put("email", emailService.isHealthy() ? "HEALTHY" : "UNHEALTHY");
        }

        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("system", system);
        response.put("application", application);
        response.put("services", services);

        return ResponseEntity.ok(response);
    }
}