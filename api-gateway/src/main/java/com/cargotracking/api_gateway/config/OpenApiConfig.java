package com.cargotracking.api_gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * OpenAPI Configuration for API Gateway
 * Requirements: NFR-USAB-001, FR-GW-001
 * - NFR-USAB-001: API Documentation
 * - FR-GW-001: Merkezi API yönetimi
 */
@Configuration
@Profile("!prod") // Prodüksiyon dışındaki tüm profile'larda aktif
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cargo Tracking System - API Gateway")
                        .description("Kargo Takip Sistemi - Merkezi API Gateway Dokümantasyonu")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Cargo Tracking Team")
                                .email("support@cargotracking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development API Gateway"),
                        new Server()
                                .url("https://api.cargotracking.com")
                                .description("Production API Gateway")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT Authorization header using the Bearer scheme")));
    }
} 