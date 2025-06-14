package com.cargotracking.user_management_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request DTO - Requirements FR-UM-002
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Kullanıcı adı veya email boş olamaz")
    private String usernameOrEmail;

    @NotBlank(message = "Şifre boş olamaz")
    private String password;
} 