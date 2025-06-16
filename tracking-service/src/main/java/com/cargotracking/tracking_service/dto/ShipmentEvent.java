package com.cargotracking.tracking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentEvent {
    private String eventType; // shipment.created, shipment.updated, shipment.canceled
    private Long shipmentId;
    private String trackingNumber;
    private Long senderUserId;
    private String status; // ACTIVE, FINISHED, CANCELLED
    private String previousStatus;
    private LocalDateTime eventTimestamp;
    private String eventSource;
    private Object eventData;
}
