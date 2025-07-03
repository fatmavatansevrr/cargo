package com.cargotracking.analytics_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Shipment Analytics Data")
public class ShipmentAnalyticsDTO {
    @Schema(description = "Unique identifier for the analytics record")
    private String id;

    @Schema(description = "Shipment ID")
    private String shipmentId;

    @Schema(description = "Tracking Number")
    private String trackingNumber;

    @Schema(description = "Company ID")
    private String companyId;

    @Schema(description = "Carrier ID")
    private String carrierId;

    @Schema(description = "Shipper ID")
    private String shipperId;

    @Schema(description = "Current status of the shipment")
    private String status;

    @Schema(description = "Total number of shipments")
    private Long totalShipments;

    @Schema(description = "Total revenue generated")
    private BigDecimal totalRevenue;

    @Schema(description = "Average delivery time in hours")
    private Double averageDeliveryTime;

    @Schema(description = "Delivery delay in hours")
    private Integer deliveryDelayHours;

    @Schema(description = "Customer satisfaction rating (1-5)")
    private Double customerSatisfaction;

    @Schema(description = "Distribution of shipments by status")
    private Map<String, Long> shipmentsByStatus;

    @Schema(description = "Timestamp of the analytics data")
    private LocalDateTime timestamp;
} 