package com.cargotracking.notification_service.client;

import com.cargotracking.notification_service.config.ServiceClientConfig;
import com.cargotracking.notification_service.dto.ShipmentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Shipment service ile iletişim için client
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceClientConfig config;
    
    /**
     * Tracking number ile shipment bilgilerini getir
     */
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        try {
            String url = config.getShipmentServiceUrl() + "/api/shipments/tracking/" + trackingNumber;
            log.info("🔍 Shipment service'den shipment bilgisi alınıyor: {}", trackingNumber);
            
            ShipmentDto shipment = restTemplate.getForObject(url, ShipmentDto.class);
            
            if (shipment != null && shipment.isActive()) {
                log.info("✅ Shipment bulundu: {} ({})", shipment.getTrackingNumber(), shipment.getStatus());
                return shipment;
            } else {
                log.warn("⚠️ Shipment bulunamadı veya aktif değil: {}", trackingNumber);
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ Shipment service bağlantı hatası - Tracking: {}", trackingNumber, e);
            return null;
        }
    }
    
    /**
     * Shipment service'in sağlık durumunu kontrol et
     */
    public boolean isShipmentServiceHealthy() {
        try {
            String url = config.getShipmentServiceUrl() + "/actuator/health";
            String response = restTemplate.getForObject(url, String.class);
            return response != null && response.contains("UP");
        } catch (Exception e) {
            log.error("❌ Shipment service sağlık kontrolü başarısız", e);
            return false;
        }
    }
} 