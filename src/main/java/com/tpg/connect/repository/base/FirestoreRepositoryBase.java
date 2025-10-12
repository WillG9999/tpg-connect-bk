package com.tpg.connect.repository.base;

import com.google.cloud.firestore.Firestore;
import com.tpg.connect.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for Firestore repositories with connection recovery capabilities
 */
@Component
public abstract class FirestoreRepositoryBase {
    
    private static final Logger log = LoggerFactory.getLogger(FirestoreRepositoryBase.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    @Autowired
    protected Firestore firestore;
    
    @Autowired
    private FirebaseConfig firebaseConfig;
    
    /**
     * Execute a Firestore operation with automatic retry and connection recovery
     */
    protected <T> T executeWithRetry(FirestoreOperation<T> operation, String operationName) {
        AtomicInteger attempts = new AtomicInteger(0);
        Exception lastException = null;
        
        while (attempts.get() < MAX_RETRY_ATTEMPTS) {
            try {
                // Check connection health before attempting operation
                if (!isConnectionHealthy()) {
                    log.warn("🔴 Firestore connection unhealthy, attempting operation anyway (attempt {})", 
                            attempts.get() + 1);
                }
                
                T result = operation.execute(firestore);
                
                // If we succeeded after retries, log the recovery
                if (attempts.get() > 0) {
                    log.info("✅ {} succeeded after {} retries", operationName, attempts.get());
                }
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                attempts.incrementAndGet();
                
                log.warn("⚠️ {} failed (attempt {}): {}", operationName, attempts.get(), e.getMessage());
                
                // Check if this is a connection-related error
                if (isConnectionError(e)) {
                    log.warn("🔄 Connection error detected, will retry operation");
                    
                    // Wait before retry (except on last attempt)
                    if (attempts.get() < MAX_RETRY_ATTEMPTS) {
                        try {
                            Thread.sleep(RETRY_DELAY_MS * attempts.get()); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Operation interrupted", ie);
                        }
                    }
                } else {
                    // Non-connection error, don't retry
                    log.error("❌ {} failed with non-connection error: {}", operationName, e.getMessage());
                    throw new RuntimeException(operationName + " failed", e);
                }
            }
        }
        
        // All retries exhausted
        log.error("❌ {} failed after {} attempts", operationName, MAX_RETRY_ATTEMPTS);
        throw new RuntimeException(operationName + " failed after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }
    
    /**
     * Check if the Firestore connection is healthy
     */
    private boolean isConnectionHealthy() {
        try {
            return firebaseConfig.isFirestoreHealthy();
        } catch (Exception e) {
            log.warn("🔴 Error checking connection health: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if an exception is related to connection issues
     */
    private boolean isConnectionError(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("client has already been closed") ||
               lowerMessage.contains("connection") ||
               lowerMessage.contains("unavailable") ||
               lowerMessage.contains("timeout") ||
               lowerMessage.contains("network") ||
               lowerMessage.contains("firestore client") ||
               e.getClass().getSimpleName().contains("Unavailable");
    }
    
    /**
     * Get current connection status for logging
     */
    protected String getConnectionStatus() {
        try {
            return firebaseConfig.getConnectionStatus();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Functional interface for Firestore operations
     */
    @FunctionalInterface
    public interface FirestoreOperation<T> {
        T execute(Firestore firestore) throws Exception;
    }
}