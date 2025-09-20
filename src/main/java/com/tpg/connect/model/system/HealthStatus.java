package com.tpg.connect.model.system;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatus {
    private String status;
    private LocalDateTime timestamp;
    private String version;
    private Map<String, ServiceHealth> services;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String status;
        private long responseTimeMs;
        private String message;
    }
}