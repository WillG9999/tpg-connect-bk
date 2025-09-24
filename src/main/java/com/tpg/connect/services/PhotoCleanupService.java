package com.tpg.connect.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PhotoCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoCleanupService.class);

    @Autowired
    private CloudStorageService cloudStorageService;

    /**
     * Asynchronously clean up old photos after profile update
     * This runs in background after user gets success response
     */
    @Async("photoCleanupExecutor")
    public CompletableFuture<Void> cleanupOldPhotos(String userId, List<String> oldPhotoUrls, List<String> newPhotoUrls) {
        try {
            List<String> photosToDelete = identifyPhotosToDelete(oldPhotoUrls, newPhotoUrls);
            
            if (photosToDelete.isEmpty()) {
                logger.debug("No photos to cleanup for user: {}", userId);
                return CompletableFuture.completedFuture(null);
            }

            logger.info("🧹 Starting async photo cleanup for user: {} - {} photos to delete", userId, photosToDelete.size());
            
            // Delete photos from cloud storage
            cloudStorageService.deleteMultiplePhotos(photosToDelete);
            
            logger.info("✅ Async photo cleanup completed for user: {}", userId);
            
        } catch (Exception e) {
            // Log error but don't let it bubble up - cleanup failure shouldn't affect user experience
            logger.error("❌ Async photo cleanup failed for user: {} - Error: {}", userId, e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Asynchronously clean up photos when user exceeds the 6-photo limit
     */
    @Async("photoCleanupExecutor")
    public CompletableFuture<Void> cleanupExcessPhotos(String userId, List<String> allPhotoUrls, int maxPhotos) {
        try {
            if (allPhotoUrls == null || allPhotoUrls.size() <= maxPhotos) {
                logger.debug("No excess photos to cleanup for user: {}", userId);
                return CompletableFuture.completedFuture(null);
            }

            // Keep first maxPhotos, delete the rest (oldest photos are typically at the end)
            List<String> excessPhotos = new ArrayList<>(allPhotoUrls.subList(maxPhotos, allPhotoUrls.size()));
            
            logger.info("🧹 Starting excess photo cleanup for user: {} - {} photos exceed limit of {}", 
                       userId, excessPhotos.size(), maxPhotos);
            
            cloudStorageService.deleteMultiplePhotos(excessPhotos);
            
            logger.info("✅ Excess photo cleanup completed for user: {}", userId);
            
        } catch (Exception e) {
            logger.error("❌ Excess photo cleanup failed for user: {} - Error: {}", userId, e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Asynchronously clean up a single photo (e.g., when user removes a specific photo)
     */
    @Async("photoCleanupExecutor")
    public CompletableFuture<Void> cleanupSinglePhoto(String userId, String photoUrl) {
        try {
            logger.info("🧹 Starting single photo cleanup for user: {} - Photo: {}", userId, photoUrl);
            
            boolean deleted = cloudStorageService.deletePhotoFromStorage(photoUrl);
            
            if (deleted) {
                logger.info("✅ Single photo cleanup completed for user: {}", userId);
            } else {
                logger.warn("⚠️ Single photo cleanup - photo not found for user: {}", userId);
            }
            
        } catch (Exception e) {
            logger.error("❌ Single photo cleanup failed for user: {} - Error: {}", userId, e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Identify which photos need to be deleted
     * Returns photos that exist in oldPhotoUrls but not in newPhotoUrls
     */
    private List<String> identifyPhotosToDelete(List<String> oldPhotoUrls, List<String> newPhotoUrls) {
        List<String> photosToDelete = new ArrayList<>();
        
        if (oldPhotoUrls == null || oldPhotoUrls.isEmpty()) {
            return photosToDelete;
        }
        
        // Convert newPhotoUrls to a Set for O(1) lookup performance
        java.util.Set<String> newPhotoSet = newPhotoUrls != null ? 
            new java.util.HashSet<>(newPhotoUrls) : new java.util.HashSet<>();
        
        for (String oldPhotoUrl : oldPhotoUrls) {
            if (oldPhotoUrl != null && !oldPhotoUrl.trim().isEmpty()) {
                if (!newPhotoSet.contains(oldPhotoUrl)) {
                    photosToDelete.add(oldPhotoUrl);
                    logger.debug("📝 Photo marked for deletion: {}", oldPhotoUrl);
                }
            }
        }
        
        return photosToDelete;
    }

    /**
     * Emergency cleanup method - can be called manually if needed
     * This is synchronous and should only be used for administrative purposes
     */
    public void emergencyCleanupUserPhotos(String userId) {
        try {
            logger.warn("🚨 Emergency cleanup initiated for user: {}", userId);
            cloudStorageService.deleteUserPhotos(userId);
            logger.warn("🚨 Emergency cleanup completed for user: {}", userId);
        } catch (Exception e) {
            logger.error("❌ Emergency cleanup failed for user: {} - Error: {}", userId, e.getMessage(), e);
        }
    }
}