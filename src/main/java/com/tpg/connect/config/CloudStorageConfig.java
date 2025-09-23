package com.tpg.connect.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class CloudStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(CloudStorageConfig.class);

    @Value("${gcp.storage.bucket:#{null}}")
    private String storageBucket;

    @Value("${gcp.project.id:#{null}}")
    private String projectId;

    @Value("${gcp.storage.credentials.location:#{null}}")
    private String credentialsLocation;

    @Value("${app.profile.photo.max-size:5MB}")
    private String maxPhotoSize;

    @Value("${app.profile.photo.max-count:6}")
    private int maxPhotoCount;

    @Value("${app.profile.photo.allowed-types:image/jpeg,image/png,image/webp}")
    private String allowedPhotoTypes;

    @Bean
    public Storage storage() {
        try {
            StorageOptions.Builder builder = StorageOptions.newBuilder();
            
            if (projectId != null) {
                builder.setProjectId(projectId);
                logger.info("Cloud Storage configured with project ID: {}", projectId);
            }
            
            // Configure credentials if path is provided
            if (credentialsLocation != null) {
                try (FileInputStream serviceAccountStream = new FileInputStream(credentialsLocation)) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);
                    builder.setCredentials(credentials);
                    logger.info("Cloud Storage configured with service account from: {}", credentialsLocation);
                } catch (IOException e) {
                    logger.error("Failed to load service account credentials from: {}", credentialsLocation, e);
                    throw new RuntimeException("Failed to load Cloud Storage credentials", e);
                }
            } else {
                logger.warn("No Cloud Storage credentials path configured - using default credentials");
            }
            
            Storage storage = builder.build().getService();
            logger.info("Cloud Storage service initialized successfully");
            return storage;
        } catch (Exception e) {
            logger.error("Failed to initialize Cloud Storage service", e);
            throw new RuntimeException("Failed to initialize Cloud Storage service", e);
        }
    }

    @Bean
    public CloudStorageProperties cloudStorageProperties() {
        CloudStorageProperties properties = new CloudStorageProperties();
        properties.setBucketName(storageBucket);
        properties.setMaxPhotoSize(maxPhotoSize);
        properties.setMaxPhotoCount(maxPhotoCount);
        properties.setAllowedPhotoTypes(allowedPhotoTypes.split(","));
        
        if (storageBucket != null) {
            logger.info("Cloud Storage configured with bucket: {}", storageBucket);
        } else {
            logger.warn("Cloud Storage bucket not configured - storage operations may fail");
        }
        logger.info("Photo upload settings - Max size: {}, Max count: {}, Allowed types: {}", 
                   maxPhotoSize, maxPhotoCount, allowedPhotoTypes);
        
        return properties;
    }

    public static class CloudStorageProperties {
        private String bucketName;
        private String maxPhotoSize;
        private int maxPhotoCount;
        private String[] allowedPhotoTypes;

        // Getters and setters
        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getMaxPhotoSize() {
            return maxPhotoSize;
        }

        public void setMaxPhotoSize(String maxPhotoSize) {
            this.maxPhotoSize = maxPhotoSize;
        }

        public int getMaxPhotoCount() {
            return maxPhotoCount;
        }

        public void setMaxPhotoCount(int maxPhotoCount) {
            this.maxPhotoCount = maxPhotoCount;
        }

        public String[] getAllowedPhotoTypes() {
            return allowedPhotoTypes;
        }

        public void setAllowedPhotoTypes(String[] allowedPhotoTypes) {
            this.allowedPhotoTypes = allowedPhotoTypes;
        }
        
        public long getMaxPhotoSizeBytes() {
            String size = maxPhotoSize.toLowerCase();
            if (size.endsWith("mb")) {
                return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024;
            } else if (size.endsWith("kb")) {
                return Long.parseLong(size.substring(0, size.length() - 2)) * 1024;
            } else if (size.endsWith("gb")) {
                return Long.parseLong(size.substring(0, size.length() - 2)) * 1024 * 1024 * 1024;
            }
            return Long.parseLong(size); // Assume bytes if no unit
        }
    }
}