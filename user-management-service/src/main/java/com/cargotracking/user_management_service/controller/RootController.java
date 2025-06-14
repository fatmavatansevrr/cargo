package com.cargotracking.user_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Root Controller - Ana endpoint'ler
 * Public endpoint'ler için
 */
@RestController
@Tag(name = "Root", description = "Ana sistem endpoint'leri")
public class RootController {

    /**
     * Root endpoint - Health check ve service bilgisi
     */
    @GetMapping("/")
    @Operation(summary = "Service Health Check", description = "Servis durumu ve temel bilgileri getirir")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "service", "User Management Service",
            "version", "1.0.0",
            "status", "UP",
            "timestamp", java.time.LocalDateTime.now().toString(),
            "endpoints", Map.of(
                "auth", "/api/auth/**",
                "swagger", "/swagger-ui.html",
                "docs", "/v3/api-docs"
            )
        ));
    }

    /**
     * Health endpoint - Basit health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Servis sağlık durumunu kontrol eder")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
} 