package com.tpg.connect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * Configuration class that provides secret-aware beans.
 * This allows other configuration classes to access secrets from Google Cloud Secret Manager
 * or fall back to environment variables.
 */
@Configuration
@ConditionalOnProperty(name = "app.secret-manager.enabled", havingValue = "true", matchIfMissing = false)
@DependsOn("secretManagerConfig")
public class SecretAwareConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(SecretAwareConfiguration.class);

    @Autowired
    private SecretManagerConfig secretManagerConfig;

    /**
     * Bean that provides JWT secret from Secret Manager or environment.
     */
    @Bean(name = "jwtSecretFromSecretManager")
    public String jwtSecret() {
        String secret = secretManagerConfig.getSecret("jwt-secret", "JWT_SECRET");
        if (secret == null) {
            logger.error("JWT secret not found in Secret Manager or environment variables!");
            throw new IllegalStateException("JWT secret is required but not configured");
        }
        logger.info("JWT secret successfully loaded from secret store");
        return secret;
    }

    /**
     * Bean that provides email password from Secret Manager or environment.
     */
    @Bean(name = "emailPasswordFromSecretManager")
    public String emailPassword() {
        String password = secretManagerConfig.getSecret("email-password", "EMAIL_PASSWORD");
        if (password == null) {
            logger.warn("Email password not found in Secret Manager or environment variables");
            return "";
        }
        logger.info("Email password successfully loaded from secret store");
        return password;
    }

    /**
     * Bean that provides Firebase service account JSON from Secret Manager or environment.
     */
    @Bean(name = "firebaseServiceAccountFromSecretManager")
    public String firebaseServiceAccount() {
        String serviceAccount = secretManagerConfig.getSecret("firebase-service-account", "FIREBASE_SERVICE_ACCOUNT_JSON");
        if (serviceAccount == null) {
            logger.warn("Firebase service account JSON not found in Secret Manager or environment variables");
            return "";
        }
        logger.info("Firebase service account successfully loaded from secret store");
        return serviceAccount;
    }

    /**
     * Bean that provides database connection string from Secret Manager or environment.
     */
    @Bean(name = "databaseUrlFromSecretManager")
    public String databaseUrl() {
        String dbUrl = secretManagerConfig.getSecret("database-url", "DATABASE_URL");
        if (dbUrl == null) {
            logger.warn("Database URL not found in Secret Manager or environment variables");
            return "";
        }
        logger.info("Database URL successfully loaded from secret store");
        return dbUrl;
    }
}