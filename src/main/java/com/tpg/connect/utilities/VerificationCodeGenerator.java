package com.tpg.connect.utilities;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for generating verification codes
 * 
 * Generates secure random verification codes for email verification,
 * password reset, and other authentication flows.
 */
@Component
public class VerificationCodeGenerator {
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    /**
     * Generate a 6-digit verification code
     * 
     * @return 6-digit numeric verification code as string
     */
    public String generateSixDigitCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
    
    /**
     * Generate a 4-digit verification code
     * 
     * @return 4-digit numeric verification code as string
     */
    public String generateFourDigitCode() {
        int code = 1000 + SECURE_RANDOM.nextInt(9000);
        return String.valueOf(code);
    }
    
    /**
     * Generate a verification code with custom length
     * 
     * @param length Length of the verification code (4-8 digits)
     * @return Numeric verification code as string
     */
    public String generateCode(int length) {
        if (length < 4 || length > 8) {
            throw new IllegalArgumentException("Code length must be between 4 and 8 digits");
        }
        
        int min = (int) Math.pow(10, length - 1);
        int max = (int) Math.pow(10, length) - 1;
        int range = max - min + 1;
        
        int code = min + SECURE_RANDOM.nextInt(range);
        return String.valueOf(code);
    }
    
    /**
     * Generate an alphanumeric verification code
     * 
     * @param length Length of the code
     * @return Alphanumeric verification code
     */
    public String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    /**
     * Generate a verification token (for URLs)
     * 
     * @return URL-safe verification token
     */
    public String generateVerificationToken() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < 32; i++) {
            token.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        
        return token.toString();
    }
    
    /**
     * Validate if a code matches the expected format
     * 
     * @param code The code to validate
     * @param expectedLength Expected length of the code
     * @return true if code matches expected format
     */
    public boolean isValidCode(String code, int expectedLength) {
        if (code == null || code.length() != expectedLength) {
            return false;
        }
        
        return code.matches("\\d{" + expectedLength + "}");
    }
    
    /**
     * Check if verification code is expired
     * 
     * @param createdAt When the code was created
     * @param expirationMinutes How many minutes until expiration
     * @return true if code is expired
     */
    public boolean isExpired(LocalDateTime createdAt, int expirationMinutes) {
        LocalDateTime expirationTime = createdAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        return LocalDateTime.now().isAfter(expirationTime);
    }
    
    /**
     * Get remaining time until code expires
     * 
     * @param createdAt When the code was created
     * @param expirationMinutes How many minutes until expiration
     * @return Remaining minutes (0 if expired)
     */
    public long getRemainingMinutes(LocalDateTime createdAt, int expirationMinutes) {
        LocalDateTime expirationTime = createdAt.plus(expirationMinutes, ChronoUnit.MINUTES);
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(expirationTime)) {
            return 0;
        }
        
        return ChronoUnit.MINUTES.between(now, expirationTime);
    }
    
    /**
     * Format verification code for display (adds spaces for readability)
     * Example: "123456" becomes "123 456"
     * 
     * @param code The verification code
     * @return Formatted code with spaces
     */
    public String formatCodeForDisplay(String code) {
        if (code == null || code.length() < 4) {
            return code;
        }
        
        if (code.length() == 4) {
            return code.substring(0, 2) + " " + code.substring(2);
        } else if (code.length() == 6) {
            return code.substring(0, 3) + " " + code.substring(3);
        } else {
            return code;
        }
    }
}