package com.tpg.connect.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Apple Push Notification Service (APNs)
 * 
 * Handles all iOS push notification client settings including
 * certificates, endpoints, timeouts, and environment configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "apns")
public class ApnsClientConfig {
    
    /**
     * Whether APNs is enabled
     */
    private boolean enabled = false;
    
    /**
     * APNs environment: "development" or "production"
     */
    private String environment = "development";
    
    /**
     * iOS app bundle identifier
     */
    private String bundleId;
    
    /**
     * Apple Team ID (10-character string)
     */
    private String teamId;
    
    /**
     * APNs Key ID (10-character string)
     */
    private String keyId;
    
    /**
     * Path to the APNs authentication key file (.p8)
     */
    private String keyPath;
    
    /**
     * APNs authentication token (JWT)
     */
    private String authToken;
    
    /**
     * Connection timeout in seconds
     */
    private int connectionTimeout = 30;
    
    /**
     * Request timeout in seconds
     */
    private int requestTimeout = 10;
    
    /**
     * Maximum number of concurrent connections
     */
    private int maxConnections = 10;
    
    /**
     * Enable/disable notification batching
     */
    private boolean batchingEnabled = true;
    
    /**
     * Maximum batch size for notifications
     */
    private int maxBatchSize = 100;
    
    /**
     * Retry attempts for failed notifications
     */
    private int retryAttempts = 3;
    
    /**
     * Retry delay in milliseconds
     */
    private long retryDelay = 1000;
    
    /**
     * APNs server URLs
     */
    private static final String DEVELOPMENT_URL = "https://api.sandbox.push.apple.com";
    private static final String PRODUCTION_URL = "https://api.push.apple.com";
    
    // Getters and Setters
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public String getBundleId() {
        return bundleId;
    }
    
    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
    
    public String getKeyId() {
        return keyId;
    }
    
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
    
    public String getKeyPath() {
        return keyPath;
    }
    
    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getRequestTimeout() {
        return requestTimeout;
    }
    
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    
    public int getMaxConnections() {
        return maxConnections;
    }
    
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
    
    public boolean isBatchingEnabled() {
        return batchingEnabled;
    }
    
    public void setBatchingEnabled(boolean batchingEnabled) {
        this.batchingEnabled = batchingEnabled;
    }
    
    public int getMaxBatchSize() {
        return maxBatchSize;
    }
    
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
    
    public int getRetryAttempts() {
        return retryAttempts;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    public long getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    /**
     * Get the appropriate APNs URL based on environment
     */
    public String getApnsUrl() {
        return isProduction() ? PRODUCTION_URL : DEVELOPMENT_URL;
    }
    
    /**
     * Check if this is production environment
     */
    public boolean isProduction() {
        return "production".equalsIgnoreCase(environment);
    }
    
    /**
     * Validate configuration settings
     */
    public boolean isValid() {
        return enabled && 
               bundleId != null && !bundleId.trim().isEmpty() &&
               teamId != null && !teamId.trim().isEmpty() &&
               keyId != null && !keyId.trim().isEmpty() &&
               (keyPath != null || authToken != null);
    }
    
    @Override
    public String toString() {
        return "ApnsClientConfig{" +
                "enabled=" + enabled +
                ", environment='" + environment + '\'' +
                ", bundleId='" + bundleId + '\'' +
                ", teamId='" + teamId + '\'' +
                ", keyId='" + keyId + '\'' +
                ", keyPath='" + (keyPath != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", authToken='" + (authToken != null ? "[CONFIGURED]" : "[NOT SET]") + '\'' +
                ", connectionTimeout=" + connectionTimeout +
                ", requestTimeout=" + requestTimeout +
                ", maxConnections=" + maxConnections +
                ", batchingEnabled=" + batchingEnabled +
                ", maxBatchSize=" + maxBatchSize +
                ", retryAttempts=" + retryAttempts +
                ", retryDelay=" + retryDelay +
                '}';
    }
}