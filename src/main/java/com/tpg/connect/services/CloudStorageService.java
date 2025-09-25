package com.tpg.connect.services;

import com.google.cloud.storage.*;
import com.google.api.gax.paging.Page;
import com.tpg.connect.config.CloudStorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(CloudStorageService.class);

    @Autowired
    private Storage storage;

    @Autowired
    private CloudStorageConfig.CloudStorageProperties storageProperties;

    public String uploadProfilePhoto(String userId, MultipartFile file) throws IOException {
        validatePhotoFile(file);
        
        String fileName = generatePhotoFileName(userId, file.getOriginalFilename());
        String folderPath = "profile_photos/" + userId + "/";
        String fullPath = folderPath + fileName;
        
        BlobId blobId = BlobId.of(storageProperties.getBucketName(), fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setMetadata(java.util.Map.of(
                    "userId", userId,
                    "uploadedAt", String.valueOf(System.currentTimeMillis()),
                    "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
                ))
                .build();

        try {
            Blob blob = storage.create(blobInfo, file.getBytes());
            logger.info("Photo uploaded successfully: {} for user: {}", fullPath, userId);
            return getPublicUrl(fullPath);
        } catch (Exception e) {
            logger.error("Failed to upload photo for user: {}", userId, e);
            throw new IOException("Failed to upload photo", e);
        }
    }

    public String uploadApplicationPhoto(String connectId, MultipartFile file, int photoIndex) throws IOException {
        validatePhotoFile(file);
        
        String fileName = generatePhotoFileName(connectId, file.getOriginalFilename());
        String folderPath = "applications/" + connectId + "/";
        String fullPath = folderPath + fileName;
        
        BlobId blobId = BlobId.of(storageProperties.getBucketName(), fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setMetadata(java.util.Map.of(
                    "connectId", connectId,
                    "photoIndex", String.valueOf(photoIndex),
                    "uploadedAt", String.valueOf(System.currentTimeMillis()),
                    "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
                ))
                .build();

        try {
            Blob blob = storage.create(blobInfo, file.getBytes());
            logger.info("Application photo uploaded successfully: {} for connectId: {}", fullPath, connectId);
            return getPublicUrl(fullPath);
        } catch (Exception e) {
            logger.error("Failed to upload application photo for connectId: {}", connectId, e);
            throw new IOException("Failed to upload application photo", e);
        }
    }

    public String uploadFile(String path, MultipartFile file) throws IOException {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String fullPath = path + "/" + fileName;
        
        BlobId blobId = BlobId.of(storageProperties.getBucketName(), fullPath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setMetadata(java.util.Map.of(
                    "uploadedAt", String.valueOf(System.currentTimeMillis()),
                    "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
                ))
                .build();

        try {
            Blob blob = storage.create(blobInfo, file.getBytes());
            logger.info("File uploaded successfully: {}", fullPath);
            return getPublicUrl(fullPath);
        } catch (Exception e) {
            logger.error("Failed to upload file: {}", fullPath, e);
            throw new IOException("Failed to upload file", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            BlobId blobId = BlobId.of(storageProperties.getBucketName(), filePath);
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                logger.info("File deleted successfully: {}", filePath);
            } else {
                logger.warn("File not found for deletion: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", filePath, e);
        }
    }

    /**
     * Delete a single photo from cloud storage using its signed URL
     */
    public boolean deletePhotoFromStorage(String photoUrl) {
        try {
            String filePath = extractFilePathFromUrl(photoUrl);
            if (filePath == null) {
                logger.warn("⚠️ Could not extract storage path from URL: {}", photoUrl);
                return false;
            }

            BlobId blobId = BlobId.of(storageProperties.getBucketName(), filePath);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                logger.info("🗑️ Successfully deleted photo from storage: {}", filePath);
            } else {
                logger.warn("⚠️ Photo not found in storage (may already be deleted): {}", filePath);
            }

            return deleted;

        } catch (Exception e) {
            logger.error("❌ Failed to delete photo from storage: {} - Error: {}", photoUrl, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete multiple photos from cloud storage using their signed URLs
     */
    public void deleteMultiplePhotos(java.util.List<String> photoUrls) {
        if (photoUrls == null || photoUrls.isEmpty()) {
            logger.debug("No photos to delete");
            return;
        }

        logger.info("🧹 Starting cleanup of {} photos", photoUrls.size());
        int deletedCount = 0;

        for (String photoUrl : photoUrls) {
            if (deletePhotoFromStorage(photoUrl)) {
                deletedCount++;
            }
        }

        logger.info("✅ Photo cleanup completed: {}/{} photos deleted successfully", 
                   deletedCount, photoUrls.size());
    }

    public void deleteUserPhotos(String userId) {
        try {
            String prefix = "profile_photos/" + userId + "/";
            Page<Blob> blobs = storage.list(storageProperties.getBucketName(), 
                    Storage.BlobListOption.prefix(prefix));
            
            for (Blob blob : blobs.iterateAll()) {
                blob.delete();
                logger.info("Deleted user photo: {}", blob.getName());
            }
        } catch (Exception e) {
            logger.error("Failed to delete photos for user: {}", userId, e);
        }
    }

    public String generateSignedUrl(String filePath, int expirationMinutes) {
        try {
            BlobId blobId = BlobId.of(storageProperties.getBucketName(), filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            
            return storage.signUrl(blobInfo, expirationMinutes, TimeUnit.MINUTES).toString();
        } catch (Exception e) {
            logger.error("Failed to generate signed URL for: {}", filePath, e);
            return null;
        }
    }

    public boolean fileExists(String filePath) {
        try {
            BlobId blobId = BlobId.of(storageProperties.getBucketName(), filePath);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            logger.error("Failed to check file existence: {}", filePath, e);
            return false;
        }
    }

    private void validatePhotoFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > storageProperties.getMaxPhotoSizeBytes()) {
            throw new IOException("File size exceeds maximum allowed size: " + storageProperties.getMaxPhotoSize());
        }

        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(storageProperties.getAllowedPhotoTypes()).contains(contentType)) {
            throw new IOException("File type not allowed. Allowed types: " + 
                                Arrays.toString(storageProperties.getAllowedPhotoTypes()));
        }
    }

    private String generatePhotoFileName(String userId, String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("photo_%s_%s%s", timestamp, uuid, extension);
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString();
        return uuid + extension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : "";
    }

    private String getPublicUrl(String filePath) {
        try {
            // Generate signed URL with authentication that's valid for 7 days
            BlobId blobId = BlobId.of(storageProperties.getBucketName(), filePath);
            Blob blob = storage.get(blobId);
            
            if (blob != null && blob.exists()) {
                // Create signed URL valid for 7 days (long enough for caching)
                return blob.signUrl(7, TimeUnit.DAYS).toString();
            } else {
                logger.warn("Blob does not exist for path: {}", filePath);
                return null;
            }
        } catch (Exception e) {
            logger.error("Failed to generate signed URL for: {}", filePath, e);
            return null;
        }
    }

    /**
     * Regenerate signed URLs for existing photo paths
     * This should be called periodically to refresh expiring URLs
     */
    public String regenerateSignedUrl(String filePath) {
        return getPublicUrl(filePath);
    }

    /**
     * Extract the storage file path from a signed URL
     * Used to get the original path when regenerating URLs
     */
    public String extractFilePathFromUrl(String signedUrl) {
        try {
            // Signed URLs contain the path after the bucket name
            String bucketPattern = "/" + storageProperties.getBucketName() + "/";
            int bucketIndex = signedUrl.indexOf(bucketPattern);
            if (bucketIndex != -1) {
                int pathStart = bucketIndex + bucketPattern.length();
                int queryStart = signedUrl.indexOf("?", pathStart);
                if (queryStart != -1) {
                    return signedUrl.substring(pathStart, queryStart);
                } else {
                    return signedUrl.substring(pathStart);
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to extract file path from URL: {}", signedUrl, e);
            return null;
        }
    }
}