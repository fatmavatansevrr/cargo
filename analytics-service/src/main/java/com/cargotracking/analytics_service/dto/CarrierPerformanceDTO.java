package com.cargotracking.analytics_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Carrier Performance DTO - Taşıyıcı performans metrikleri
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Carrier Performance Metrics")
public class CarrierPerformanceDTO {
    @Schema(description = "Carrier ID")
    private String carrierId;

    @Schema(description = "Total shipments handled")
    private Long totalShipments;

    @Schema(description = "On-time delivery rate (percentage)")
    private Double onTimeDeliveryRate;

    @Schema(description = "Average delivery time in hours")
    private Double averageDeliveryTime;

    @Schema(description = "Number of delayed deliveries")
    private Long delayedDeliveries;

    @Schema(description = "Average delay time in hours")
    private Double averageDelayTime;

    @Schema(description = "Customer satisfaction rating (1-5)")
    private Double satisfactionRating;

    @Schema(description = "Distribution of deliveries by status")
    private Map<String, Long> deliveriesByStatus;

    @Schema(description = "Total revenue generated")
    private BigDecimal totalRevenue;

    @Schema(description = "Time period start")
    private LocalDateTime periodStart;

    @Schema(description = "Time period end")
    private LocalDateTime periodEnd;
} 