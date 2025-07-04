// tracking-service/src/main/java/com/cargotracking/tracking_service/client/ShipmentServiceClient.java

package com.cargotracking.tracking_service.client;

import com.cargotracking.tracking_service.dto.ShipmentDetailsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

/**
 * Shipment Service Client - Recipient bilgilerini almak için
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.shipment-service.url:http://shipment-service:8082}")
    private String shipmentServiceUrl;



    /**
     * Tracking number'a göre recipient bilgilerini al (email + name)
     */
    public RecipientInfo getRecipientInfoByTrackingNumber(String trackingNumber) {
        try {
            log.debug("🔍 Recipient bilgileri alınıyor: {}", trackingNumber);

            String url = shipmentServiceUrl + "/api/internal/shipments/tracking/" + trackingNumber;

            @SuppressWarnings("unchecked")
            Map<String, Object> shipment = restTemplate.getForObject(url, Map.class);

            if (shipment != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> recipientAddress = (Map<String, Object>) shipment.get("recipientAddress");

                if (recipientAddress != null) {
                    String email = (String) recipientAddress.get("email");
                    String fullName = (String) recipientAddress.get("fullName");

                    return new RecipientInfo(email, fullName);
                }
            }

            return null;

        } catch (Exception e) {
            log.warn("❌ Recipient bilgileri alınırken hata: {} - {}", trackingNumber, e.getMessage());
            return null;
        }
    }

    public ShipmentDetailsDto getShipmentDetailsByTrackingNumber(String trackingNumber) {
        try {
            log.debug("🔍 Shipment detayları alınıyor: {}", trackingNumber);

            String url = shipmentServiceUrl + "/api/internal/shipments/tracking/" + trackingNumber + "/raw";

            ResponseEntity<ShipmentDetailsDto> response = restTemplate.getForEntity(url, ShipmentDetailsDto.class);
            return response.getBody();

        } catch (Exception e) {
            log.warn("❌ Shipment detayları alınırken hata: {} - {}", trackingNumber, e.getMessage());
            return null;
        }
    }



    /**
     * Recipient bilgileri için DTO
     */
    public static class RecipientInfo {
        private final String email;
        private final String fullName;

        public RecipientInfo(String email, String fullName) {
            this.email = email;
            this.fullName = fullName;
        }

        public String getEmail() { return email; }
        public String getFullName() { return fullName; }
    }
}