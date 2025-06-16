package com.cargotracking.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ShipmentEvent - Kafka'dan gelen shipment olayları
 * Shipment service ile tamamen uyumlu event modeli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEvent {
    
    private String eventType; // shipment.created, shipment.updated, shipment.canceled
    private Long shipmentId;
    private String trackingNumber;
    private Long senderUserId;
    private String status; // String olarak enum değeri
    private String previousStatus;
    private LocalDateTime eventTimestamp;
    private String eventSource;
    private Object eventData; // Shipment detayları
    
    // Notification için ek bilgiler (eventData'dan parse edilecek)
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Shipment details (eventData'dan parse edilecek)
    private String originAddress;
    private String destinationAddress;
    private String carrierName;
    private String estimatedDeliveryDate;
    
    /**
     * eventData'dan notification için gerekli bilgileri çıkar
     */
    public void extractDataFromEventData() {
        if (eventData != null) {
            // eventData Map veya JSON object olabilir
            // Shipment service'den gelen eventData'yı parse et
            
            // TODO: Gerçek Shipment model yapısına göre mapping yapılacak
            // Örnek: 
            // Map<String, Object> data = (Map<String, Object>) eventData;
            // this.customerName = (String) data.get("customerName");
            // this.customerEmail = (String) data.get("customerEmail");
            // vs.
        }
    }
} 