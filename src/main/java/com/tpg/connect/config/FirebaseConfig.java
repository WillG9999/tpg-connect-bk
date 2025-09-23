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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.config.path:#{null}}")
    private String firebaseConfigPath;

    @Value("${firebase.database.url:#{null}}")
    private String databaseUrl;

    @Value("${firebase.storage.bucket:#{null}}")
    private String storageBucket;

    @Value("${firebase.project.id:#{null}}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.debug("Firebase config path value received: '{}'", firebaseConfigPath);
            
            if (firebaseConfigPath == null || firebaseConfigPath.isEmpty()) {
                throw new RuntimeException("Firebase config path not specified. Please set firebase.config.path property or pass --firebase.config.path=/path/to/your/service-account.json");
            }
            
            logger.info("Initializing Firebase with config path: {}", firebaseConfigPath);
            
            InputStream serviceAccount;
            try {
                serviceAccount = new FileInputStream(firebaseConfigPath);
            } catch (IOException e) {
                logger.error("Failed to load Firebase service account from: {}", firebaseConfigPath);
                throw new RuntimeException("Firebase service account file not found. Please ensure the file exists at: " + firebaseConfigPath + 
                    "\nYou can specify the path using: --firebase.config.path=/path/to/your/service-account.json", e);
            }

            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
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
            FirebaseApp app = FirebaseApp.initializeApp(options);
            
            logger.info("Firebase initialized successfully with storage bucket: {}", storageBucket);
            return app;
        }
        logger.info("Firebase app already initialized, returning existing instance");
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore() throws IOException {
        logger.info("Creating Firestore client");
        try {
            Firestore firestore = FirestoreClient.getFirestore(firebaseApp());
            logger.info("Firestore client created successfully");
            return firestore;
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
        
        if (firebaseConfigPath == null || firebaseConfigPath.isEmpty()) {
            throw new RuntimeException("Firebase config path not specified for Cloud Storage. Please set firebase.config.path property");
        }
        
        InputStream serviceAccount = new FileInputStream(firebaseConfigPath);
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
                .getService();
    }
}