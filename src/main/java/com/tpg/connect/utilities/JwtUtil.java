package com.tpg.connect.utilities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String SECRET_KEY;
    
    private final long ACCESS_TOKEN_EXPIRATION = 3600000; // 1 hour
    private final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    
    private final Set<String> blacklistedTokens = new HashSet<>();

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
            
            if (blacklistedTokens.contains(token)) {
                return false;
            }
            
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
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
            return (extractedUsername.equals(username) && !isTokenExpired(token) && !blacklistedTokens.contains(token));
        } catch (Exception e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
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