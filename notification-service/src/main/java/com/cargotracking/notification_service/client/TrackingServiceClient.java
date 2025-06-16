package com.cargotracking.notification_service.client;

import com.cargotracking.notification_service.config.ServiceClientConfig;
import com.cargotracking.notification_service.dto.TrackingDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Tracking service ile iletişim için client
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceClientConfig config;
    
    /**
     * Tracking number ile tracking bilgilerini getir
     */
    public TrackingDto getTrackingByNumber(String trackingNumber) {
        try {
            String url = config.getTrackingServiceUrl() + "/api/tracking/" + trackingNumber;
            log.info("🔍 Tracking service'den tracking bilgisi alınıyor: {}", trackingNumber);
            
            TrackingDto tracking = restTemplate.getForObject(url, TrackingDto.class);
            
            if (tracking != null && tracking.isActive()) {
                log.info("✅ Tracking bulundu: {} ({})", tracking.getTrackingNumber(), tracking.getCurrentStatus());
                return tracking;
            } else {
                log.warn("⚠️ Tracking bulunamadı veya aktif değil: {}", trackingNumber);
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ Tracking service bağlantı hatası - Tracking: {}", trackingNumber, e);
            return null;
        }
    }
    
    /**
     * Tracking service'in sağlık durumunu kontrol et
     */
    public boolean isTrackingServiceHealthy() {
        try {
            String url = config.getTrackingServiceUrl() + "/actuator/health";
            String response = restTemplate.getForObject(url, String.class);
            return response != null && response.contains("UP");
        } catch (Exception e) {
            log.error("❌ Tracking service sağlık kontrolü başarısız", e);
            return false;
        }
    }
} 