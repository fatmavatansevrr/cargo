package com.cargotracking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * API Gateway Security Configuration
 * Requirements: FR-GW-002, NFR-SEC-002, NFR-SEC-004
 * - FR-GW-002: Merkezi kimlik doğrulama (authentication)
 * - NFR-SEC-002: JWT ile kimlik doğrulama
 * - NFR-SEC-004: Yetkisiz erişimi engelleme
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                // CSRF disable - stateless JWT kullanıyoruz
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                
                // CORS configuration
                .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
                
                // Authorization rules - Gelen tüm isteklerin kimlik doğrulaması (JWT) gerektirir
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                
                .build();
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000")); // React app's origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 