package com.cargotracking.shipment_service.dto;

import com.cargotracking.shipment_service.model.Shipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Shipment Response DTO
 * Requirements: FR-SM-003 - Gönderi detaylarını görüntüleme
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    
    private Long id;
    private String trackingNumber;
    private Long senderUserId;
    private AddressDto senderAddress;
    private AddressDto recipientAddress;
    private PackageDto packageInfo;
    private Shipment.ServiceType serviceType;
    private Shipment.ShipmentStatus status;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private BigDecimal shippingCost;
    private String specialInstructions;
    private String notes;
    private Long assignedCarrierId;
    private DeliveryPreferencesDto deliveryPreferences; // FR-SM-009
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
} 