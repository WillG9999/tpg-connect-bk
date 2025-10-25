package com.tpg.connect.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Configuration for Google Cloud Secret Manager integration.
 * This allows the application to fetch secrets from Google Cloud Secret Manager
 * instead of storing them in configuration files.
 */
@Configuration
@ConditionalOnProperty(name = "app.secret-manager.enabled", havingValue = "true", matchIfMissing = false)
public class SecretManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecretManagerConfig.class);

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Value("${app.secret-manager.prefix:connect-app}")
    private String secretPrefix;

    private final Environment environment;

    public SecretManagerConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SecretManagerServiceClient secretManagerServiceClient() throws IOException {
        return SecretManagerServiceClient.create();
    }

    @PostConstruct
    public void initializeSecrets() {
        if (projectId.isEmpty()) {
            logger.warn("GCP Project ID not configured, Secret Manager will not be used");
            return;
        }

        logger.info("Initializing Google Cloud Secret Manager with project: {} and prefix: {}", projectId, secretPrefix);
        
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // Test connection to Secret Manager
            logger.info("Successfully connected to Google Cloud Secret Manager");
        } catch (Exception e) {
            logger.error("Failed to initialize Secret Manager: {}", e.getMessage());
        }
    }

    /**
     * Retrieves a secret from Google Cloud Secret Manager.
     * Falls back to environment variable if Secret Manager is not available.
     *
     * @param secretName The name of the secret (without prefix)
     * @param fallbackEnvVar The environment variable to use as fallback
     * @return The secret value
     */
    public String getSecret(String secretName, String fallbackEnvVar) {
        // If Secret Manager is not enabled, use environment variable
        if (!isSecretManagerEnabled()) {
            String envValue = environment.getProperty(fallbackEnvVar);
            if (envValue != null) {
                logger.debug("Using environment variable {} for secret {}", fallbackEnvVar, secretName);
                return envValue;
            }
            logger.warn("No value found for secret {} in environment variable {}", secretName, fallbackEnvVar);
            return null;
        }

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            String fullSecretName = secretPrefix + "-" + secretName;
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, fullSecretName, "latest");
            
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            String secretValue = response.getPayload().getData().toStringUtf8();
            
            logger.debug("Successfully retrieved secret: {}", secretName);
            return secretValue;
            
        } catch (Exception e) {
            logger.warn("Failed to retrieve secret {} from Secret Manager, falling back to environment variable: {}", 
                       secretName, e.getMessage());
            
            String envValue = environment.getProperty(fallbackEnvVar);
            if (envValue != null) {
                return envValue;
            }
            
            logger.error("No fallback value found for secret {} in environment variable {}", secretName, fallbackEnvVar);
            return null;
        }
    }

    /**
     * Checks if Secret Manager is enabled and properly configured.
     */
    private boolean isSecretManagerEnabled() {
        return !projectId.isEmpty() && 
               environment.getProperty("app.secret-manager.enabled", Boolean.class, false);
    }
}