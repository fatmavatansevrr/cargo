package com.cargotracking.shipment_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnalyticsDataRequest {
    private String shipmentId;
    private String trackingNumber;
    private String companyId;
    private String carrierId;
    private String shipperId;
    private String recipientId;
    private String status;
    private LocalDateTime statusTimestamp;
    private Long deliveryTimeInMinutes;
    private Double shippingCost;
    private Double packageWeight;
} 