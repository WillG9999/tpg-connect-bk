package com.tpg.connect.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${jwt.secret:mySecretKey}")
    private String SECRET_KEY;
    
    private final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("type", "access")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String username) {
        return generateAccessToken(username, "USER");
    }

    public String generateTokenWithClaims(String username, String role) {
        return generateAccessToken(username, role);
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return false;
            }
            
            // Check Redis blacklist
            if (isTokenBlacklisted(token)) {
                logger.debug("Token validation failed: token is blacklisted");
                return false;
            }
            
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractTokenType(String token) {
        return extractClaims(token).get("type", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token) && !isTokenBlacklisted(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Blacklist a token in Redis with automatic expiration
     * 
     * @param token JWT token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String redisKey = BLACKLIST_PREFIX + token;
            
            // Calculate remaining TTL based on token expiration
            long ttlSeconds = calculateTokenTTL(token);
            
            if (ttlSeconds > 0) {
                redisTemplate.opsForValue().set(redisKey, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
                logger.info("🚫 Token blacklisted in Redis with TTL: {} seconds", ttlSeconds);
            } else {
                // Token already expired, no need to blacklist
                logger.debug("Token already expired, skipping blacklist");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to blacklist token in Redis: {}", e.getMessage());
            // TODO: Fallback to in-memory storage or throw exception based on requirements
        }
    }

    /**
     * Check if token is blacklisted in Redis
     * 
     * @param token JWT token to check
     * @return true if token is blacklisted
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String redisKey = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            logger.error("❌ Failed to check token blacklist in Redis: {}", e.getMessage());
            // TODO: Decide on fallback strategy - fail open or fail closed
            return false; // Fail open - allow token if Redis is unavailable
        }
    }

    /**
     * Calculate remaining TTL for token based on expiration
     * 
     * @param token JWT token
     * @return TTL in seconds, or 0 if token is expired
     */
    private long calculateTokenTTL(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            long currentTime = System.currentTimeMillis();
            long expirationTime = expiration.getTime();
            
            if (expirationTime > currentTime) {
                return (expirationTime - currentTime) / 1000; // Convert to seconds
            } else {
                return 0; // Token already expired
            }
        } catch (Exception e) {
            logger.error("❌ Failed to calculate token TTL: {}", e.getMessage());
            return ACCESS_TOKEN_EXPIRATION / 1000; // Default to access token expiration
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessTokenExpiration() {
        return ACCESS_TOKEN_EXPIRATION;
    }
}