package com.cargotracking.shipment_service.event;


import com.cargotracking.shipment_service.model.Priority;
import com.cargotracking.shipment_service.model.ShipmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentCreatedEvent {
    private Long shipmentId;
    private String trackingNumber;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String senderEmail;
    private String receiverName;
    private String receiverEmail;
    private ShipmentType type;
    private Priority priority;
    private Double weight;
    private BigDecimal cost;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime createdAt;
    private String eventType = "SHIPMENT_CREATED";
}