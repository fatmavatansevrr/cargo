package com.cargotracking.user_management_service.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Authentication response DTO - Requirements FR-UM-002, NFR-SEC-002
 */
@Data
@Builder
public class AuthResponse {

    private String token;
    private String tokenType;
    private UserResponse user;

    public static AuthResponse of(String token, UserResponse user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(user)
                .build();
    }
} 