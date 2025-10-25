package com.tpg.connect.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import jakarta.annotation.PostConstruct;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Production monitoring configuration for Connect Dating App
 * 
 * Provides comprehensive metrics collection for production observability
 * including business metrics, technical metrics, and health indicators.
 */
@Configuration
public class MonitoringConfig {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringConfig.class);

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private HealthEndpoint healthEndpoint;

    // Business Metrics Counters
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger dailyMatches = new AtomicInteger(0);
    private final AtomicInteger dailyMessages = new AtomicInteger(0);

    /**
     * Initialize custom metrics for production monitoring
     */
    @PostConstruct
    public void initializeCustomMetrics() {
        logger.info("🔍 Initializing production metrics for Connect Dating App");

        // Business Metrics
        Gauge.builder("connect.users.active", activeUsers, AtomicInteger::get)
                .description("Number of active users in the last 24 hours")
                .register(meterRegistry);

        Gauge.builder("connect.matches.daily", dailyMatches, AtomicInteger::get)
                .description("Number of matches created today")
                .register(meterRegistry);

        Gauge.builder("connect.messages.daily", dailyMessages, AtomicInteger::get)
                .description("Number of messages sent today")
                .register(meterRegistry);

        // Redis Health Metrics
        Gauge.builder("connect.redis.health", this, MonitoringConfig::getRedisHealth)
                .description("Redis connection health (1=healthy, 0=unhealthy)")
                .register(meterRegistry);

        // Application Health Metrics
        Gauge.builder("connect.application.health", this, MonitoringConfig::getApplicationHealth)
                .description("Overall application health (1=healthy, 0=unhealthy)")
                .register(meterRegistry);

        logger.info("✅ Custom metrics initialized successfully");
    }

    /**
     * Counter for user registration events
     */
    @Bean
    public Counter userRegistrationCounter() {
        return Counter.builder("connect.users.registrations")
                .description("Total number of user registrations")
                .tag("type", "registration")
                .register(meterRegistry);
    }

    /**
     * Counter for user login events
     */
    @Bean
    public Counter userLoginCounter() {
        return Counter.builder("connect.users.logins")
                .description("Total number of user logins")
                .tag("type", "login")
                .register(meterRegistry);
    }

    /**
     * Counter for match creation events
     */
    @Bean
    public Counter matchCreationCounter() {
        return Counter.builder("connect.matches.created")
                .description("Total number of matches created")
                .tag("type", "match")
                .register(meterRegistry);
    }

    /**
     * Counter for message sending events
     */
    @Bean
    public Counter messageSentCounter() {
        return Counter.builder("connect.messages.sent")
                .description("Total number of messages sent")
                .tag("type", "message")
                .register(meterRegistry);
    }

    /**
     * Timer for authentication operations
     */
    @Bean
    public Timer authenticationTimer() {
        return Timer.builder("connect.auth.duration")
                .description("Duration of authentication operations")
                .tag("operation", "authentication")
                .register(meterRegistry);
    }

    /**
     * Timer for database operations
     */
    @Bean
    public Timer databaseOperationTimer() {
        return Timer.builder("connect.database.duration")
                .description("Duration of database operations")
                .tag("operation", "database")
                .register(meterRegistry);
    }

    /**
     * Counter for API errors
     */
    @Bean
    public Counter apiErrorCounter() {
        return Counter.builder("connect.api.errors")
                .description("Total number of API errors")
                .tag("type", "error")
                .register(meterRegistry);
    }

    /**
     * Counter for security events
     */
    @Bean
    public Counter securityEventCounter() {
        return Counter.builder("connect.security.events")
                .description("Total number of security events")
                .tag("type", "security")
                .register(meterRegistry);
    }

    /**
     * Check Redis health for monitoring
     */
    private double getRedisHealth() {
        try {
            redisTemplate.opsForValue().get("health_check");
            return 1.0; // Healthy
        } catch (Exception e) {
            logger.warn("Redis health check failed: {}", e.getMessage());
            return 0.0; // Unhealthy
        }
    }

    /**
     * Check overall application health
     */
    private double getApplicationHealth() {
        try {
            Status status = healthEndpoint.health().getStatus();
            return Status.UP.equals(status) ? 1.0 : 0.0;
        } catch (Exception e) {
            logger.warn("Application health check failed: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Update active users count (called from business logic)
     */
    public void updateActiveUsersCount(int count) {
        activeUsers.set(count);
    }

    /**
     * Update daily matches count (called from business logic)
     */
    public void updateDailyMatchesCount(int count) {
        dailyMatches.set(count);
    }

    /**
     * Update daily messages count (called from business logic)
     */
    public void updateDailyMessagesCount(int count) {
        dailyMessages.set(count);
    }
}