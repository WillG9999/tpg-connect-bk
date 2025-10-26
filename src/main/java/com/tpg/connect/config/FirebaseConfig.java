package com.tpg.connect.config;

// TODO: REMOVE FIREBASE CREDENTIALS FILE BEFORE COMMITTING TO GIT
// File: src/main/resources/firebase/connect-dev-firebase-adminsdk.json
// SECURITY WARNING: Contains private keys and should never be committed

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PreDestroy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${FIREBASE_CONFIG_PATH:#{null}}")
    private String firebaseConfigPath;
    
    @Value("${firebase.credentials.json:#{null}}")
    private String firebaseCredentialsJson;

    @Value("${firebase.database.url:#{null}}")
    private String databaseUrl;

    @Value("${firebase.storage.bucket:#{null}}")
    private String storageBucket;

    @Value("${firebase.project.id:#{null}}")
    private String projectId;
    
    @Autowired
    private Environment environment;
    
    // Instance variables to track client lifecycle
    private FirebaseApp firebaseAppInstance;
    private Firestore firestoreInstance;
    private volatile boolean isShuttingDown = false;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.info("🔥 Firebase initialization starting...");
            logger.info("🔍 Checking Secret Manager integration status...");
            
            // Log Spring Cloud GCP configuration
            logger.info("🔧 Spring Cloud GCP Secret Manager enabled: {}", 
                environment.getProperty("spring.cloud.gcp.secretmanager.enabled", "not set"));
            logger.info("🔧 Spring Cloud GCP Project ID: {}", 
                environment.getProperty("spring.cloud.gcp.project-id", "not set"));
            
            // Log what we received
            logger.info("Firebase config path value received: '{}'", firebaseConfigPath);
            logger.info("Firebase credentials JSON available: {}", firebaseCredentialsJson != null);
            
            // Log the COMPLETE RAW value we received for FIREBASE_CREDENTIALS_JSON
            logger.info("🔍 COMPLETE RAW FIREBASE_CREDENTIALS_JSON value:");
            logger.info("=== START FIREBASE CREDENTIALS ===");
            logger.info("{}", firebaseCredentialsJson != null ? firebaseCredentialsJson : "NULL");
            logger.info("=== END FIREBASE CREDENTIALS ===");
            
            logger.info("🔍 COMPLETE RAW FIREBASE_CREDENTIALS_JSON from environment:");
            logger.info("=== START ENVIRONMENT FIREBASE CREDENTIALS ===");
            String envValue = environment.getProperty("FIREBASE_CREDENTIALS_JSON", "NOT FOUND");
            logger.info("{}", envValue);
            logger.info("=== END ENVIRONMENT FIREBASE CREDENTIALS ===");
            
            if (firebaseCredentialsJson != null) {
                logger.info("Firebase credentials JSON length: {}", firebaseCredentialsJson.length());
                
                // Check if Spring Cloud GCP Secret Manager substitution worked
                if (firebaseCredentialsJson.contains("${sm://")) {
                    logger.error("❌ Secret Manager placeholder NOT replaced by Spring Cloud GCP!");
                    logger.error("   Raw value: '{}'", firebaseCredentialsJson);
                    logger.error("   This means Spring Cloud GCP Secret Manager integration is not working");
                } else {
                    logger.info("✅ Secret Manager placeholder appears to have been replaced");
                    logger.info("Firebase credentials JSON first 100 chars: '{}'", 
                        firebaseCredentialsJson.length() > 100 ? firebaseCredentialsJson.substring(0, 100) + "..." : firebaseCredentialsJson);
                    
                    // Log if it looks like escaped JSON or raw JSON
                    if (firebaseCredentialsJson.startsWith("{")) {
                        logger.info("📋 Credentials appear to be raw JSON format");
                    } else if (firebaseCredentialsJson.startsWith("\"{")) {
                        logger.info("📋 Credentials appear to be escaped JSON string format");
                    } else {
                        logger.warn("⚠️ Credentials format unexpected - does not start with { or \"{");
                    }
                }
            } else {
                logger.error("❌ Firebase credentials JSON is null - Secret Manager integration failed");
            }
            
            InputStream serviceAccount;
            
            // Priority: Use JSON credentials from environment variable if available
            if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
                logger.info("🔧 Initializing Firebase with credentials from environment variable");
                
                try {
                    serviceAccount = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
                    logger.info("✅ Successfully created input stream from credentials JSON");
                } catch (Exception e) {
                    logger.error("❌ Failed to create input stream from credentials JSON: {}", e.getMessage());
                    throw e;
                }
            }
            // Fallback: Use file path
            else if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                logger.info("Initializing Firebase with config path: {}", firebaseConfigPath);
                try {
                    serviceAccount = new FileInputStream(firebaseConfigPath);
                } catch (IOException e) {
                    logger.error("Failed to load Firebase service account from: {}", firebaseConfigPath);
                    throw new RuntimeException("Firebase service account file not found. Please ensure the file exists at: " + firebaseConfigPath + 
                        "\nYou can specify the path using: --firebase.config.path=/path/to/your/service-account.json", e);
                }
            }
            // No credentials provided
            else {
                throw new RuntimeException("Firebase credentials not specified. Please set FIREBASE_CREDENTIALS_JSON environment variable or FIREBASE_CONFIG_PATH property");
            }

            logger.info("🔐 Attempting to parse Google credentials from input stream...");
            GoogleCredentials credentials;
            try {
                credentials = GoogleCredentials.fromStream(serviceAccount);
                logger.info("✅ Successfully parsed Google credentials");
            } catch (Exception e) {
                logger.error("❌ Failed to parse Google credentials: {}", e.getMessage());
                logger.error("❌ Exception type: {}", e.getClass().getSimpleName());
                if (firebaseCredentialsJson != null && firebaseCredentialsJson.length() > 0) {
                    logger.error("❌ Credentials content analysis:");
                    logger.error("   - Length: {}", firebaseCredentialsJson.length());
                    logger.error("   - Starts with: '{}'", firebaseCredentialsJson.substring(0, Math.min(50, firebaseCredentialsJson.length())));
                    logger.error("   - Is valid JSON start: {}", firebaseCredentialsJson.trim().startsWith("{"));
                }
                throw e;
            }

            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket(storageBucket);

            if (databaseUrl != null && !databaseUrl.isEmpty()) {
                optionsBuilder.setDatabaseUrl(databaseUrl);
                logger.info("Firebase database URL configured: {}", databaseUrl);
            }

            if (projectId != null && !projectId.isEmpty()) {
                optionsBuilder.setProjectId(projectId);
                logger.info("Firebase project ID configured: {}", projectId);
            }

            FirebaseOptions options = optionsBuilder.build();
            firebaseAppInstance = FirebaseApp.initializeApp(options);
            
            logger.info("Firebase initialized successfully with storage bucket: {}", storageBucket);
            return firebaseAppInstance;
        }
        logger.info("Firebase app already initialized, returning existing instance");
        firebaseAppInstance = FirebaseApp.getInstance();
        return firebaseAppInstance;
    }

    @Bean
    public Firestore firestore() throws IOException {
        logger.info("Creating Firestore client");
        try {
            if (isShuttingDown) {
                logger.warn("Application is shutting down, skipping Firestore client creation");
                throw new RuntimeException("Application is shutting down");
            }
            
            firestoreInstance = FirestoreClient.getFirestore(firebaseApp());
            logger.info("Firestore client created successfully");
            return firestoreInstance;
        } catch (Exception e) {
            logger.error("Failed to create Firestore client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        logger.info("Creating Firebase Auth client");
        return FirebaseAuth.getInstance(firebaseApp());
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        logger.info("Creating Firebase Messaging client");
        return FirebaseMessaging.getInstance(firebaseApp());
    }

    @Bean
    public StorageClient firebaseStorage() throws IOException {
        logger.info("Creating Firebase Storage client");
        return StorageClient.getInstance(firebaseApp());
    }

    @Bean
    public Storage cloudStorage() throws IOException {
        logger.info("Creating Google Cloud Storage client");
        
        InputStream serviceAccount;
        
        // Priority: Use JSON credentials from environment variable if available
        if (firebaseCredentialsJson != null && !firebaseCredentialsJson.trim().isEmpty()) {
            logger.info("Creating Cloud Storage client with credentials from environment variable");
            serviceAccount = new ByteArrayInputStream(firebaseCredentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        // Fallback: Use file path
        else if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
            logger.info("Creating Cloud Storage client with config path: {}", firebaseConfigPath);
            serviceAccount = new FileInputStream(firebaseConfigPath);
        }
        // No credentials provided
        else {
            throw new RuntimeException("Firebase credentials not specified for Cloud Storage. Please set FIREBASE_CREDENTIALS_JSON environment variable or FIREBASE_CONFIG_PATH property");
        }
        
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
    }
    
    /**
     * Clean shutdown of Firebase resources
     */
    @PreDestroy
    public void cleanup() {
        logger.info("🧹 Starting Firebase cleanup...");
        isShuttingDown = true;
        
        try {
            // Close Firestore client if it exists
            if (firestoreInstance != null) {
                try {
                    firestoreInstance.close();
                    logger.info("✅ Firestore client closed successfully");
                } catch (Exception e) {
                    logger.warn("⚠️ Error closing Firestore client: {}", e.getMessage());
                }
            }
            
            // Delete Firebase app instances to free resources
            if (firebaseAppInstance != null) {
                try {
                    firebaseAppInstance.delete();
                    logger.info("✅ Firebase app instance deleted successfully");
                } catch (Exception e) {
                    logger.warn("⚠️ Error deleting Firebase app: {}", e.getMessage());
                }
            }
            
            // Clean up all Firebase apps as fallback
            FirebaseApp.getApps().forEach(app -> {
                try {
                    app.delete();
                    logger.debug("🧹 Cleaned up Firebase app: {}", app.getName());
                } catch (Exception e) {
                    logger.warn("⚠️ Error cleaning up Firebase app {}: {}", app.getName(), e.getMessage());
                }
            });
            
            logger.info("✅ Firebase cleanup completed");
            
        } catch (Exception e) {
            logger.error("❌ Error during Firebase cleanup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Check if Firestore connection is healthy
     */
    public boolean isFirestoreHealthy() {
        if (isShuttingDown || firestoreInstance == null) {
            return false;
        }
        
        try {
            // Simple health check - attempt to access a collection
            firestoreInstance.collection("health-check").limit(1);
            return true;
        } catch (Exception e) {
            logger.warn("🔴 Firestore health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current connection status for monitoring
     */
    public String getConnectionStatus() {
        if (isShuttingDown) {
            return "SHUTTING_DOWN";
        }
        if (firestoreInstance == null) {
            return "NOT_INITIALIZED";
        }
        return isFirestoreHealthy() ? "HEALTHY" : "UNHEALTHY";
    }
}