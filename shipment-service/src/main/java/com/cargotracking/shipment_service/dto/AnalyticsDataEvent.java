package com.cargotracking.shipment_service.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDataEvent {
    private String shipmentId;
    private String trackingNumber;
    private String companyId;
    private String carrierId;
    private String shipperId;
    private String status;
    private Long totalShipments;
    private BigDecimal totalRevenue;
    private Double averageDeliveryTime;
    private Integer deliveryDelayHours;
    private Double customerSatisfaction;
    private Map<String, Long> shipmentsByStatus;
    private LocalDateTime timestamp;
} 