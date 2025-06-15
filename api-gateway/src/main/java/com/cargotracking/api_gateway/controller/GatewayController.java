package com.cargotracking.api_gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * API Gateway Controller
 * Requirements: FR-GW-001, NFR-OBSV-003
 * - FR-GW-001: Merkezi API yönetimi
 * - NFR-OBSV-003: Health check endpoint'leri
 */
@RestController
@Tag(name = "Gateway", description = "API Gateway yönetim endpoint'leri")
public class GatewayController {

    /**
     * Root endpoint - Gateway sağlık durumu
     */
    @GetMapping("/")
    @Operation(summary = "Gateway Health Check", description = "API Gateway'in sağlık durumunu kontrol eder")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "service", "API Gateway",
            "version", "1.0.0",
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "description", "Cargo Tracking System - Central API Gateway",
            "routes", Map.of(
                "authentication", "/api/auth/**",
                "admin", "/api/admin/**",
                "shipments", "/api/shipments/**",
                "tracking", "/api/tracking/**",
                "notifications", "/api/notifications/**",
                "analytics", "/api/analytics/**"
            )
        ));
    }

    /**
     * Health endpoint - Basit health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Gateway servis sağlık durumunu kontrol eder")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "api-gateway"
        ));
    }

    /**
     * Routes bilgisi - Hangi servislere yönlendirme yapıldığı
     */
    @GetMapping("/routes")
    @Operation(summary = "Route Information", description = "API Gateway'in yönlendirme kurallarını listeler")
    public ResponseEntity<Map<String, Object>> routes() {
        return ResponseEntity.ok(Map.of(
            "routes", Map.of(
                "user-management-auth", Map.of(
                    "path", "/api/auth/**",
                    "url", "http://localhost:8081",
                    "description", "Kullanıcı kimlik doğrulama"
                ),
                "user-management-admin", Map.of(
                    "path", "/api/admin/**",
                    "url", "http://localhost:8081",
                    "description", "Admin kullanıcı yönetimi"
                ),
                "shipment-service", Map.of(
                    "path", "/api/shipments/**",
                    "url", "http://localhost:8082",
                    "description", "Gönderi oluşturma ve yönetimi"
                ),
                "tracking-service", Map.of(
                    "path", "/api/tracking/**",
                    "url", "http://localhost:8083",
                    "description", "Gönderi takip ve durum güncelleme"
                ),
                "notification-service", Map.of(
                    "path", "/api/notifications/**",
                    "url", "http://localhost:8084",
                    "description", "Bildirim yönetimi"
                ),
                "analytics-service", Map.of(
                    "path", "/api/analytics/**",
                    "url", "http://localhost:8085",
                    "description", "Raporlama ve analitik"
                )
            ),
            "security", Map.of(
                "authentication", "JWT Bearer Token",
                "publicEndpoints", new String[]{"/api/auth/login", "/api/auth/register", "/health", "/", "/routes"}
            ),
            "timestamp", LocalDateTime.now().toString()
        ));
    }
} 