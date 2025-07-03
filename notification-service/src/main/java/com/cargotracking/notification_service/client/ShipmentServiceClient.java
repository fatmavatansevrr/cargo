// notification-service/src/main/java/com/cargotracking/notification_service/client/ShipmentServiceClient.java

package com.cargotracking.notification_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

/**
 * Shipment Service Client - Shipment bilgilerini almak için
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.shipment-service.url:http://shipment-service:8082}")
    private String shipmentServiceUrl;

    public String getRecipientEmailByTrackingNumber(String trackingNumber) {
        try {
            log.info("🔍 Shipment service'den recipient email alınıyor: {}", trackingNumber);

            String url = shipmentServiceUrl + "/api/internal/shipments/tracking/" + trackingNumber + "/recipient-email";

            String email = restTemplate.getForObject(url, String.class);

            if (email != null && !email.trim().isEmpty()) {
                log.info("✅ Recipient email bulundu: {} -> {}", trackingNumber, email);
                return email.trim();
            } else {
                log.warn("⚠️ Recipient email boş: {}", trackingNumber);
                return null;
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("⚠️ Shipment bulunamadı: {}", trackingNumber);
            return null;
        } catch (Exception e) {
            log.error("❌ Recipient email alınırken hata: {} - {}", trackingNumber, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getShipmentByTrackingNumber(String trackingNumber) {
        try {
            log.info("🔍 Shipment service'den shipment bilgileri alınıyor: {}", trackingNumber);

            String url = shipmentServiceUrl + "/api/shipments/tracking/" + trackingNumber;

            @SuppressWarnings("unchecked")
            Map<String, Object> shipment = restTemplate.getForObject(url, Map.class);

            if (shipment != null) {
                log.info("✅ Shipment bilgileri bulundu: {}", trackingNumber);
                return shipment;
            } else {
                log.warn("⚠️ Shipment bilgileri bulunamadı: {}", trackingNumber);
                return null;
            }

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("⚠️ Shipment bulunamadı: {}", trackingNumber);
            return null;
        } catch (Exception e) {
            log.error("❌ Shipment bilgileri alınırken hata: {} - {}", trackingNumber, e.getMessage());
            return null;
        }
    }
}
