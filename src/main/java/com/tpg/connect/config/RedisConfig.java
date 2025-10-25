package com.tpg.connect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * Redis configuration for Connect Dating App
 * 
 * Provides Redis connection pooling, templates, and serialization configuration
 * for token storage, caching, and session management.
 */
@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.data.redis.timeout:2000}")
    private int timeout;

    @Value("${spring.data.redis.lettuce.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.data.redis.lettuce.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.data.redis.lettuce.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.data.redis.lettuce.pool.max-wait:-1}")
    private long maxWait;

    /**
     * Redis connection factory with Lettuce client and connection pooling
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        logger.info("🔗 Configuring Redis connection to {}:{}", redisHost, redisPort);
        
        // Redis standalone configuration
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
            logger.info("🔐 Redis password authentication enabled");
        }

        // Connection pool configuration
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWait));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);

        // Client resources for connection management
        ClientResources clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();

        // Lettuce client configuration with pooling
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofMillis(timeout))
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        
        logger.info("✅ Redis connection factory configured with connection pooling");
        logger.info("📊 Pool config - Max Active: {}, Max Idle: {}, Min Idle: {}", maxActive, maxIdle, minIdle);
        
        return factory;
    }

    /**
     * Redis template for general object operations with JSON serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("🛠️ Creating Redis template with JSON serialization");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        logger.info("✅ Redis template configured with JSON serialization");
        return template;
    }

    /**
     * String Redis template for simple string operations (tokens, counters)
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("🛠️ Creating String Redis template for tokens and counters");
        
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        logger.info("✅ String Redis template configured");
        return template;
    }

    /**
     * Health check method for Redis connectivity
     */
    public boolean isRedisHealthy() {
        try {
            RedisConnectionFactory factory = redisConnectionFactory();
            return factory.getConnection().ping() != null;
        } catch (Exception e) {
            logger.warn("🔴 Redis health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get Redis connection status for monitoring
     */
    public String getRedisStatus() {
        try {
            if (isRedisHealthy()) {
                return "CONNECTED";
            } else {
                return "DISCONNECTED";
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}