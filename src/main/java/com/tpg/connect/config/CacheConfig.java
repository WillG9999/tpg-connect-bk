package com.tpg.connect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis-based cache configuration for Connect Dating App
 * 
 * Provides distributed caching for user profiles, matches, conversations,
 * and other frequently accessed data to improve performance and reduce
 * database load across multiple application instances.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    /**
     * Redis-based cache manager with custom TTL settings for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        logger.info("🗄️ Configuring Redis-based cache manager");
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Default TTL: 1 hour
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Custom TTL configurations for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // User profiles - cache for 2 hours (profiles change less frequently)
        cacheConfigurations.put("userProfiles", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // User matches - cache for 30 minutes (matches can change frequently)
        cacheConfigurations.put("matches", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userMatches", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // Conversations - cache for 15 minutes (conversations are very dynamic)
        cacheConfigurations.put("conversations", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("messages", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // Potential matches - cache for 24 hours (daily batches)
        cacheConfigurations.put("potentialMatches", defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // User settings - cache for 4 hours (settings change infrequently)
        cacheConfigurations.put("accountSettings", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("privacySettings", defaultConfig.entryTtl(Duration.ofHours(4)));
        cacheConfigurations.put("notificationSettings", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // Safety features - cache for 1 hour
        cacheConfigurations.put("blockedUsers", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("safetyBlocks", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("userBlocked", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Notifications - cache for 10 minutes (notifications are time-sensitive)
        cacheConfigurations.put("userNotifications", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // Subscriptions - cache for 1 hour
        cacheConfigurations.put("currentSubscription", defaultConfig.entryTtl(Duration.ofHours(1)));

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
        
        logger.info("✅ Redis cache manager configured with {} cache types", cacheConfigurations.size());
        logger.info("📋 Cache types: {}", String.join(", ", cacheConfigurations.keySet()));
        
        return cacheManager;
    }
}