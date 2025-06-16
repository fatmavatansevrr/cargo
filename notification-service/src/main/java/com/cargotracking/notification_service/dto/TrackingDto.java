package com.cargotracking.notification_service.dto;

import lombok.Data;

/**
 * Tracking service'den dönen tracking bilgileri
 */
@Data
public class TrackingDto {
    private String id;
    private String trackingNumber;
    private String currentStatus;
    private String location;
    private String shipmentId;
    private boolean active;
} 