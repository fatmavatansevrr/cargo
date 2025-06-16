package com.cargotracking.shipment_service.event;

import com.cargotracking.shipment_service.model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shipment Event - Kafka için olay modeli
 * Requirements: FR-SM-008 - Kafka olayları yayınlama
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentEvent {
    
    private String eventType; // shipment.created, shipment.updated, shipment.canceled
    private Long shipmentId;
    private String trackingNumber;
    private Long senderUserId;
    private String status; // ACTIVE, FINISHED, CANCELLED
    private String previousStatus;
    private LocalDateTime eventTimestamp;
    private String eventSource = "shipment-service";
    private Object eventData; // Detaylı shipment bilgileri
    
    /**
     * Gönderi oluşturuldu olayı
     */
    public static ShipmentEvent created(Long shipmentId, String trackingNumber, Long senderUserId, Object shipmentData) {
        return new ShipmentEvent(
            "shipment.created",
            shipmentId,
            trackingNumber,
            senderUserId,
            "ACTIVE",
            null,
            LocalDateTime.now(),
            "shipment-service",
            shipmentData
        );
    }
    
    /**
     * Gönderi güncellendi olayı
     */
    public static ShipmentEvent updated(Long shipmentId, String trackingNumber, Long senderUserId,
                                       Shipment.ShipmentStatus newStatus, Shipment.ShipmentStatus previousStatus,
                                       Object shipmentData) {
        return new ShipmentEvent(
            "shipment.updated",
            shipmentId,
            trackingNumber,
            senderUserId,
            newStatus != null ? newStatus.name() : null,
            previousStatus != null ? previousStatus.name() : null,
            LocalDateTime.now(),
            "shipment-service",
            shipmentData
        );
    }

    /**
     * Gönderi tamamlandı olayı
     */
    public static ShipmentEvent finished(Long shipmentId, String trackingNumber, Long senderUserId, Object shipmentData) {
        return new ShipmentEvent(
                "shipment.finished",
                shipmentId,
                trackingNumber,
                senderUserId,
                "FINISHED",
                null,
                LocalDateTime.now(),
                "shipment-service",
                shipmentData
        );
    }

    /**
     * Gönderi iptal edildi olayı
     */
    public static ShipmentEvent canceled(Long shipmentId, String trackingNumber, Long senderUserId, Object shipmentData) {
        return new ShipmentEvent(
            "shipment.canceled",
            shipmentId,
            trackingNumber,
            senderUserId,
            "CANCELLED",
            null,
            LocalDateTime.now(),
            "shipment-service",
            shipmentData
        );
    }
} 