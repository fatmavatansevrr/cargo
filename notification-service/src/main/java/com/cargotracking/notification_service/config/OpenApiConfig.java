package com.cargotracking.notification_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8084");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.cargotracking.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("admin@cargotracking.com");
        contact.setName("Cargo Tracking Team");
        contact.setUrl("https://www.cargotracking.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Notification Service API")
                .version("1.0")
                .contact(contact)
                .description("Notification Service for Cargo Tracking System - Manages all types of notifications including email, SMS, push notifications and in-app notifications.")
                .termsOfService("https://www.cargotracking.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
} 