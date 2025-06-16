package com.cargotracking.notification_service.dto;

import lombok.Data;

/**
 * Shipment service'den dönen shipment bilgileri
 */
@Data
public class ShipmentDto {
    private String id;
    private String trackingNumber;
    private String status;
    private Long senderUserId;
    private Long recipientUserId;
    private String senderEmail;
    private String recipientEmail;
    private boolean active;
} 