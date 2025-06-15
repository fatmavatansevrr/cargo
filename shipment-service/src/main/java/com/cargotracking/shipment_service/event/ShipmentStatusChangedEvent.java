package com.cargotracking.shipment_service.event;

import com.cargotracking.shipment_service.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentStatusChangedEvent {
    private Long shipmentId;
    private String trackingNumber;
    private ShipmentStatus oldStatus;
    private ShipmentStatus newStatus;
    private String senderEmail;
    private String receiverEmail;
    private LocalDateTime changedAt;
    private String eventType = "SHIPMENT_STATUS_CHANGED";
}
