package com.cargotracking.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.List;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Authorization rules - Şimdilik tüm endpoint'leri açık bırak
                .authorizeExchange(exchanges -> exchanges
                    .anyExchange().permitAll()
                )
                
                .build();
    }

    /**
     * CORS Configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * JWT Decoder Bean - OAuth2 Resource Server için
     * Şimdilik devre dışı - route'lar çalışsın diye
     */
    /*
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // JWT secret key ile SecretKey objesi oluştur
        String secretKeyString = "MySecretKeyForJWTSigningMustBeAtLeast256BitsLongForSecurityReasons123456789";
        SecretKey secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
    */
} 