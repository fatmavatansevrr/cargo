package com.cargotracking.analytics_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@Schema(description = "Shipment Status Distribution Analytics")
public class StatusDistributionDTO {
    @Schema(description = "Distribution of shipments by status")
    private Map<String, Long> statusDistribution;

    @Schema(description = "Distribution of status changes over time")
    private Map<LocalDateTime, Map<String, Long>> statusChangesOverTime;

    @Schema(description = "Average time spent in each status (in hours)")
    private Map<String, Double> averageTimeInStatus;

    @Schema(description = "Most common status transition paths")
    private Map<String, Long> statusTransitionPaths;

    @Schema(description = "Time period start")
    private LocalDateTime periodStart;

    @Schema(description = "Time period end")
    private LocalDateTime periodEnd;
} 