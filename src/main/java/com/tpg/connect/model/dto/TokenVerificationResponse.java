package com.tpg.connect.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationResponse {
    private boolean valid;
    private String message;
    private String email; // Masked email like j***@gmail.com for user context
    private boolean expired;
    private long expiresInMinutes; // Time remaining until token expires
}