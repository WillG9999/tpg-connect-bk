package com.tpg.connect.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based rate limiting service for Connect Dating App
 * 
 * Provides distributed rate limiting to prevent API abuse and ensure fair usage
 * across multiple application instances. Uses Redis for consistent rate limiting
 * state shared between all servers.
 */
@Service
public class RateLimitingService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingService.class);
    
    // Redis key prefixes for different rate limit types
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String USER_ACTION_PREFIX = RATE_LIMIT_PREFIX + "user_action:";
    private static final String LOGIN_PREFIX = RATE_LIMIT_PREFIX + "login:";
    private static final String REGISTRATION_PREFIX = RATE_LIMIT_PREFIX + "registration:";
    private static final String PASSWORD_RESET_PREFIX = RATE_LIMIT_PREFIX + "password_reset:";
    private static final String MESSAGE_PREFIX = RATE_LIMIT_PREFIX + "message:";
    private static final String PHOTO_UPLOAD_PREFIX = RATE_LIMIT_PREFIX + "photo_upload:";
    private static final String PROFILE_UPDATE_PREFIX = RATE_LIMIT_PREFIX + "profile_update:";
    private static final String IP_GENERAL_PREFIX = RATE_LIMIT_PREFIX + "ip_general:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Rate limit configuration for different endpoint types
     */
    public enum RateLimitType {
        // User actions (like/pass) - 100 per minute
        USER_ACTION(100, Duration.ofMinutes(1)),
        
        // Login attempts - 10 per 15 minutes per IP
        LOGIN_ATTEMPT(10, Duration.ofMinutes(15)),
        
        // Registration attempts - 3 per hour per IP
        REGISTRATION(3, Duration.ofHours(1)),
        
        // Password reset requests - 5 per hour per email
        PASSWORD_RESET(5, Duration.ofHours(1)),
        
        // Messages - 60 per minute per user
        MESSAGING(60, Duration.ofMinutes(1)),
        
        // Photo uploads - 10 per hour per user
        PHOTO_UPLOAD(10, Duration.ofHours(1)),
        
        // Profile updates - 20 per hour per user
        PROFILE_UPDATE(20, Duration.ofHours(1)),
        
        // General API calls - 1000 per hour per IP
        GENERAL_API(1000, Duration.ofHours(1));

        private final int maxRequests;
        private final Duration window;

        RateLimitType(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public Duration getWindow() {
            return window;
        }
    }

    /**
     * Check if a request is allowed under the rate limit
     * 
     * @param rateLimitType Type of rate limit to check
     * @param identifier Unique identifier (user ID, IP address, email, etc.)
     * @return true if request is allowed, false if rate limited
     */
    public boolean isAllowed(RateLimitType rateLimitType, String identifier) {
        String redisKey = getRedisKey(rateLimitType, identifier);
        
        try {
            String currentCountStr = stringRedisTemplate.opsForValue().get(redisKey);
            int currentCount = currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
            
            if (currentCount >= rateLimitType.getMaxRequests()) {
                logger.warn("🚫 Rate limit exceeded for {} with identifier {}: {}/{} requests", 
                    rateLimitType, identifier, currentCount, rateLimitType.getMaxRequests());
                return false;
            }
            
            // Increment counter and set TTL if this is the first request
            if (currentCount == 0) {
                stringRedisTemplate.opsForValue().set(redisKey, "1", 
                    rateLimitType.getWindow().getSeconds(), TimeUnit.SECONDS);
                logger.debug("🔄 Started new rate limit window for {} with identifier {}: 1/{}", 
                    rateLimitType, identifier, rateLimitType.getMaxRequests());
            } else {
                stringRedisTemplate.opsForValue().increment(redisKey);
                logger.debug("🔄 Incremented rate limit counter for {} with identifier {}: {}/{}", 
                    rateLimitType, identifier, currentCount + 1, rateLimitType.getMaxRequests());
            }
            
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error checking rate limit for {} with identifier {}: {}", 
                rateLimitType, identifier, e.getMessage());
            // Fail open - allow request if Redis is unavailable
            return true;
        }
    }

    /**
     * Get current request count for a rate limit
     * 
     * @param rateLimitType Type of rate limit to check
     * @param identifier Unique identifier
     * @return current request count
     */
    public int getCurrentCount(RateLimitType rateLimitType, String identifier) {
        String redisKey = getRedisKey(rateLimitType, identifier);
        
        try {
            String currentCountStr = stringRedisTemplate.opsForValue().get(redisKey);
            return currentCountStr != null ? Integer.parseInt(currentCountStr) : 0;
        } catch (Exception e) {
            logger.error("❌ Error getting current count for {} with identifier {}: {}", 
                rateLimitType, identifier, e.getMessage());
            return 0;
        }
    }

    /**
     * Get remaining requests for a rate limit
     * 
     * @param rateLimitType Type of rate limit to check
     * @param identifier Unique identifier
     * @return remaining request count
     */
    public int getRemainingRequests(RateLimitType rateLimitType, String identifier) {
        int currentCount = getCurrentCount(rateLimitType, identifier);
        return Math.max(0, rateLimitType.getMaxRequests() - currentCount);
    }

    /**
     * Get time until rate limit window resets
     * 
     * @param rateLimitType Type of rate limit to check
     * @param identifier Unique identifier
     * @return seconds until reset, or 0 if no active limit
     */
    public long getSecondsUntilReset(RateLimitType rateLimitType, String identifier) {
        String redisKey = getRedisKey(rateLimitType, identifier);
        
        try {
            Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
            return ttl != null && ttl > 0 ? ttl : 0;
        } catch (Exception e) {
            logger.error("❌ Error getting TTL for {} with identifier {}: {}", 
                rateLimitType, identifier, e.getMessage());
            return 0;
        }
    }

    /**
     * Reset rate limit for a specific identifier (admin use)
     * 
     * @param rateLimitType Type of rate limit to reset
     * @param identifier Unique identifier
     */
    public void resetRateLimit(RateLimitType rateLimitType, String identifier) {
        String redisKey = getRedisKey(rateLimitType, identifier);
        
        try {
            stringRedisTemplate.delete(redisKey);
            logger.info("🔄 Reset rate limit for {} with identifier {}", rateLimitType, identifier);
        } catch (Exception e) {
            logger.error("❌ Error resetting rate limit for {} with identifier {}: {}", 
                rateLimitType, identifier, e.getMessage());
        }
    }

    /**
     * Check if user action rate limit allows request
     */
    public boolean isUserActionAllowed(String userId) {
        return isAllowed(RateLimitType.USER_ACTION, userId);
    }

    /**
     * Check if login attempt rate limit allows request
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed(RateLimitType.LOGIN_ATTEMPT, ipAddress);
    }

    /**
     * Check if registration rate limit allows request
     */
    public boolean isRegistrationAllowed(String ipAddress) {
        return isAllowed(RateLimitType.REGISTRATION, ipAddress);
    }

    /**
     * Check if password reset rate limit allows request
     */
    public boolean isPasswordResetAllowed(String email) {
        return isAllowed(RateLimitType.PASSWORD_RESET, email);
    }

    /**
     * Check if messaging rate limit allows request
     */
    public boolean isMessagingAllowed(String userId) {
        return isAllowed(RateLimitType.MESSAGING, userId);
    }

    /**
     * Check if photo upload rate limit allows request
     */
    public boolean isPhotoUploadAllowed(String userId) {
        return isAllowed(RateLimitType.PHOTO_UPLOAD, userId);
    }

    /**
     * Check if profile update rate limit allows request
     */
    public boolean isProfileUpdateAllowed(String userId) {
        return isAllowed(RateLimitType.PROFILE_UPDATE, userId);
    }

    /**
     * Check if general API rate limit allows request
     */
    public boolean isGeneralApiAllowed(String ipAddress) {
        return isAllowed(RateLimitType.GENERAL_API, ipAddress);
    }

    /**
     * Generate Redis key for rate limit tracking
     */
    private String getRedisKey(RateLimitType rateLimitType, String identifier) {
        String prefix = switch (rateLimitType) {
            case USER_ACTION -> USER_ACTION_PREFIX;
            case LOGIN_ATTEMPT -> LOGIN_PREFIX;
            case REGISTRATION -> REGISTRATION_PREFIX;
            case PASSWORD_RESET -> PASSWORD_RESET_PREFIX;
            case MESSAGING -> MESSAGE_PREFIX;
            case PHOTO_UPLOAD -> PHOTO_UPLOAD_PREFIX;
            case PROFILE_UPDATE -> PROFILE_UPDATE_PREFIX;
            case GENERAL_API -> IP_GENERAL_PREFIX;
        };
        
        return prefix + identifier;
    }

    /**
     * Get rate limit info for debugging/monitoring
     */
    public String getRateLimitInfo(RateLimitType rateLimitType, String identifier) {
        int current = getCurrentCount(rateLimitType, identifier);
        int remaining = getRemainingRequests(rateLimitType, identifier);
        long resetTime = getSecondsUntilReset(rateLimitType, identifier);
        
        return String.format("Rate Limit [%s:%s] - Used: %d/%d, Remaining: %d, Reset in: %ds", 
            rateLimitType, identifier, current, rateLimitType.getMaxRequests(), remaining, resetTime);
    }
}