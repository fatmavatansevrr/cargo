package com.cargotracking.tracking_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing) yapılandırma sınıfı
 * 
 * Bu sınıf, farklı domain'lerden gelen HTTP isteklerine izin vermek için
 * CORS ayarlarını yapılandırır. Özellikle frontend (React) uygulamasının
 * backend API'larına erişebilmesi için gereklidir.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 