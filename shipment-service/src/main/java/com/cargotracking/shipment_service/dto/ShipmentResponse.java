package com.cargotracking.shipment_service.dto;

import com.cargotracking.shipment_service.model.Priority;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import com.cargotracking.shipment_service.model.ShipmentType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipmentResponse {
    private Long id;
    private String trackingNumber;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String senderPhone;
    private String senderEmail;
    private AddressDTO senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private AddressDTO receiverAddress;
    private ShipmentStatus status;
    private ShipmentType type;
    private Priority priority;
    private String description;
    private Double weight;
    private BigDecimal cost;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String deliveryPreferences;
    private String specialInstructions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
