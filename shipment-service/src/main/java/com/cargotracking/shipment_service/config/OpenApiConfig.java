package com.cargotracking.shipment_service.config;

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

import java.util.List;

/**
 * OpenAPI Configuration for Shipment Service
 * Requirements: FR-SM-001 to FR-SM-008 documentation
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI shipmentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shipment Service API")
                        .description("""
                                Kargo Gönderi Yönetimi Servisi
                                
                                Bu servis aşağıdaki gereksinimleri karşılar:
                                - FR-SM-001: Yeni kargo gönderileri oluşturma
                                - FR-SM-002: Benzersiz takip numarası oluşturma
                                - FR-SM-003: Gönderi detaylarını görüntüleme
                                - FR-SM-004: Gönderi listesi ve filtreleme
                                - FR-SM-005: Gönderi iptal etme
                                - FR-SM-006: Durum güncellemeleri
                                - FR-SM-007: Gönderi yaşam döngüsü yönetimi
                                - FR-SM-008: Kafka olayları yayınlama
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Cargo Tracking Team")
                                .email("support@cargotracking.com")
                                .url("https://cargotracking.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080/api/shipments")
                                .description("API Gateway Route")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token for authentication")));
    }
} 