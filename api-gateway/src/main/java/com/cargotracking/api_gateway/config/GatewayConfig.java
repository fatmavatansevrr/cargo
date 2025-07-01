package com.cargotracking.api_gateway.config;

import com.cargotracking.api_gateway.security.JwtAuthenticationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtFilter) {
        return builder.routes()

                // Auth endpoint → public
                .route("auth", r -> r.path("/api/auth/**")
                        .uri("lb://user-management-service"))

                // Admin endpoint → JWT korumalı
                .route("admin", r -> r.path("/api/admin/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://user-management-service"))

                // Shipment → korumalı
                .route("shipment", r -> r.path("/api/shipments/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://shipment-service"))

                // Tracking → korumalı
                .route("tracking", r -> r.path("/api/tracking/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://tracking-service"))

                // Notification → korumalı
                .route("notification", r -> r.path("/api/notifications/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://notification-service"))

                // Analytics → korumalı
                .route("analytics", r -> r.path("/api/analytics/**")
                        .filters(f -> f.filter(jwtFilter.apply(new JwtAuthenticationFilter.Config())))
                        .uri("lb://analytics-service"))

                .build();
    }
}
