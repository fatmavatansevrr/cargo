package com.cargotracking.analytics_service.controller;

import com.cargotracking.analytics_service.dto.CarrierPerformanceDTO;
import com.cargotracking.analytics_service.dto.ShipmentAnalyticsDTO;
import com.cargotracking.analytics_service.dto.StatusDistributionDTO;
import com.cargotracking.analytics_service.mapper.AnalyticsMapper;
import com.cargotracking.analytics_service.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Analytics API for cargo tracking system")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsMapper analyticsMapper;

    @PostMapping
    @Operation(summary = "Save shipment analytics data", description = "Creates or updates analytics data for a shipment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics data saved successfully",
            content = @Content(schema = @Schema(implementation = ShipmentAnalyticsDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ShipmentAnalyticsDTO> saveAnalytics(
            @Parameter(description = "Shipment analytics data", required = true)
            @RequestBody ShipmentAnalyticsDTO analyticsDTO) {
            return ResponseEntity.ok(analyticsMapper.toDTO(
                analyticsService.saveAnalytics(analyticsMapper.toEntity(analyticsDTO))
            ));
    }

    @GetMapping("/carrier/{carrierId}")
    @Operation(summary = "Get analytics data by carrier ID", description = "Retrieves all analytics data for a specific carrier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Carrier not found")
    })
    public ResponseEntity<List<ShipmentAnalyticsDTO>> getAnalyticsByCarrier(
            @Parameter(description = "Carrier ID", required = true)
            @PathVariable String carrierId) {
            return ResponseEntity.ok(analyticsMapper.toDTOList(
                analyticsService.getAnalyticsByCarrier(carrierId)
            ));
    }

    @GetMapping("/shipper/{shipperId}")
    @Operation(summary = "Get analytics data by shipper ID", description = "Retrieves all analytics data for a specific shipper")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Shipper not found")
    })
    public ResponseEntity<List<ShipmentAnalyticsDTO>> getAnalyticsByShipper(
            @Parameter(description = "Shipper ID", required = true)
            @PathVariable String shipperId) {
            return ResponseEntity.ok(analyticsMapper.toDTOList(
                analyticsService.getAnalyticsByShipper(shipperId)
            ));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get analytics data by customer ID", description = "Retrieves all analytics data for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<List<ShipmentAnalyticsDTO>> getAnalyticsByCustomer(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable String customerId) {
            return ResponseEntity.ok(analyticsMapper.toDTOList(
                analyticsService.getAnalyticsByCustomer(customerId)
            ));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get analytics data by date range", description = "Retrieves analytics data within a specified date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Analytics data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    public ResponseEntity<List<ShipmentAnalyticsDTO>> getAnalyticsByDateRange(
            @Parameter(description = "Start date and time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
            return ResponseEntity.ok(analyticsMapper.toDTOList(
                analyticsService.getAnalyticsByDateRange(startDate, endDate)
            ));
    }

    @GetMapping("/carrier/{carrierId}/performance")
    @Operation(summary = "Get carrier performance metrics", description = "Retrieves detailed performance metrics for a specific carrier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Performance metrics retrieved successfully",
            content = @Content(schema = @Schema(implementation = CarrierPerformanceDTO.class))),
        @ApiResponse(responseCode = "404", description = "Carrier not found")
    })
    public ResponseEntity<CarrierPerformanceDTO> getCarrierPerformance(
            @Parameter(description = "Carrier ID", required = true)
            @PathVariable String carrierId) {
            return ResponseEntity.ok(analyticsMapper.toCarrierPerformanceDTO(
                carrierId,
                analyticsService.getAnalyticsByCarrier(carrierId)
            ));
    }

    @GetMapping("/status-distribution")
    @Operation(summary = "Get shipment status distribution", description = "Retrieves the distribution of shipment statuses across all shipments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status distribution retrieved successfully",
            content = @Content(schema = @Schema(implementation = StatusDistributionDTO.class)))
    })
    public ResponseEntity<StatusDistributionDTO> getStatusDistribution() {
            return ResponseEntity.ok(analyticsMapper.toStatusDistributionDTO(
                analyticsService.getAllAnalytics()
            ));
    }
} 