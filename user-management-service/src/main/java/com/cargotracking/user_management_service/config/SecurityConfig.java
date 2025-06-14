package com.cargotracking.user_management_service.config;

import com.cargotracking.user_management_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration - Requirements NFR-SEC-002
 * JWT tabanlı authentication ve authorization yapılandırması
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Security Filter Chain - Güvenlik kurallarını tanımlar
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disable - JWT kullandığımız için gereksiz
            .csrf(AbstractHttpConfigurer::disable)
            
            // Session management - Stateless JWT authentication
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization kuralları
            .authorizeHttpRequests(auth -> auth
                // Public endpoint'ler - Authentication gerektirmeyen
                .requestMatchers(
                    "/api/auth/**",           // Authentication endpoint'leri
                    "/v3/api-docs/**",        // OpenAPI docs
                    "/v3/api-docs",           // OpenAPI docs (without trailing slash)
                    "/swagger-ui/**",         // Swagger UI
                    "/swagger-ui.html",       // Swagger UI ana sayfa
                    "/swagger-ui/index.html", // Swagger UI index
                    "/swagger-resources/**",  // Swagger resources
                    "/webjars/**",            // WebJars
                    "/actuator/**",           // Spring Boot Actuator
                    "/error",                 // Error handling
                    "/favicon.ico",           // Favicon
                    "/",                      // Root endpoint
                    "/health"                 // Health check endpoint
                ).permitAll()
                
                // Admin endpoint'leri - Sadece ADMIN rolü
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Diğer tüm endpoint'ler authentication gerektirir
                .anyRequest().authenticated()
            )
            
            // JWT Authentication Filter'ı ekle
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Password Encoder Bean - BCrypt kullanır
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
