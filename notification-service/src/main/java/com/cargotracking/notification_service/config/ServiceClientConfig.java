package com.cargotracking.notification_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Diğer microservice'lerle iletişim için configuration
 */
@Configuration
public class ServiceClientConfig {
    
    @Value("${external.services.user-service.url:http://localhost:8080}")
    private String userServiceUrl;
    
    @Value("${external.services.shipment-service.url:http://localhost:8082}")
    private String shipmentServiceUrl;
    
    @Value("${external.services.tracking-service.url:http://localhost:8083}")
    private String trackingServiceUrl;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    public String getUserServiceUrl() {
        return userServiceUrl;
    }
    
    public String getShipmentServiceUrl() {
        return shipmentServiceUrl;
    }
    
    public String getTrackingServiceUrl() {
        return trackingServiceUrl;
    }
} 