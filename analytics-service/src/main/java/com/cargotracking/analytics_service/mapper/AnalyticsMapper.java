package com.cargotracking.analytics_service.mapper;

import com.cargotracking.analytics_service.dto.CarrierPerformanceDTO;
import com.cargotracking.analytics_service.dto.ShipmentAnalyticsDTO;
import com.cargotracking.analytics_service.dto.StatusDistributionDTO;
import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytics Mapper - DTO ve Entity dönüşümleri
 * Circular dependency'yi önlemek için service injection yok
 */
@Component
public class AnalyticsMapper {

    /**
     * ShipmentAnalytics entity'sini DTO'ya dönüştürür
     */
    public ShipmentAnalyticsDTO toDTO(ShipmentAnalytics analytics) {
        if (analytics == null) {
            return ShipmentAnalyticsDTO.builder()
                    .totalShipments(0L)
                    .totalRevenue(BigDecimal.ZERO)
                    .averageDeliveryTime(0.0)
                    .shipmentsByStatus(new HashMap<>())
                    .build();
        }

        Map<String, Long> statusMap = new HashMap<>();
        if (analytics.getStatus() != null) {
            statusMap.put(analytics.getStatus(), 1L);
        }

        // Calculate delivery time if both estimated and actual times are available
        Double deliveryTime = null;
        if (analytics.getEstimatedDeliveryTime() != null && analytics.getActualDeliveryTime() != null) {
            deliveryTime = (double) ChronoUnit.HOURS.between(
                analytics.getEstimatedDeliveryTime(),
                analytics.getActualDeliveryTime()
            );
        }

        return ShipmentAnalyticsDTO.builder()
                .shipmentId(analytics.getShipmentId())
                .carrierId(analytics.getCarrierId())
                .status(analytics.getStatus())
                .totalShipments(1L)
                .totalRevenue(BigDecimal.ZERO) // No revenue field in model
                .averageDeliveryTime(deliveryTime != null ? deliveryTime : 0.0)
                .deliveryDelayHours(analytics.getDeliveryDelayHours())
                .customerSatisfaction(0.0) // No satisfaction field in model
                .shipmentsByStatus(statusMap)
                .timestamp(analytics.getStatusTimestamp())
                .build();
    }

    /**
     * ShipmentAnalyticsDTO'yu entity'ye dönüştürür
     */
    public ShipmentAnalytics toEntity(ShipmentAnalyticsDTO dto) {
        if (dto == null) {
            return null;
        }

        ShipmentAnalytics analytics = new ShipmentAnalytics();
        analytics.setId(dto.getShipmentId()); // Using shipmentId as the entity ID
        analytics.setShipmentId(dto.getShipmentId());
        analytics.setCarrierId(dto.getCarrierId());
        analytics.setStatus(dto.getStatus());
        analytics.setDeliveryDelayHours(dto.getDeliveryDelayHours());
        analytics.setStatusTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        // estimatedDeliveryTime and actualDeliveryTime are not set from DTO (not present)
        return analytics;
    }

    /**
     * Analytics listesini DTO listesine dönüştürür
     */
    public List<ShipmentAnalyticsDTO> toDTOList(List<ShipmentAnalytics> analytics) {
        return analytics.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Carrier performance DTO'su oluşturur
     */
    public CarrierPerformanceDTO toCarrierPerformanceDTO(String carrierId, List<ShipmentAnalytics> analytics) {
        long totalShipments = analytics.size();
        long successfulDeliveries = analytics.stream()
                .filter(sa -> sa.getDeliveryDelayHours() != null && sa.getDeliveryDelayHours() <= 0)
                .count();
        long delayedDeliveries = totalShipments - successfulDeliveries;
        
        double onTimeDeliveryRate = totalShipments > 0 ? (double) successfulDeliveries / totalShipments : 0.0;
        double averageDelayTime = analytics.stream()
                .filter(sa -> sa.getDeliveryDelayHours() != null)
                .mapToInt(ShipmentAnalytics::getDeliveryDelayHours)
                .average()
                .orElse(0.0);

        Map<String, Long> deliveriesByStatus = analytics.stream()
                .filter(sa -> sa.getStatus() != null)
                .collect(Collectors.groupingBy(
                        ShipmentAnalytics::getStatus,
                        Collectors.counting()
                ));

        return CarrierPerformanceDTO.builder()
                .carrierId(carrierId)
                .totalShipments(totalShipments)
                .onTimeDeliveryRate(onTimeDeliveryRate)
                .averageDelayTime(averageDelayTime)
                .delayedDeliveries(delayedDeliveries)
                .satisfactionRating(0.0) // No satisfaction data in model
                .deliveriesByStatus(deliveriesByStatus)
                .totalRevenue(BigDecimal.ZERO) // No revenue data in model
                .periodStart(LocalDateTime.now().minusMonths(1))
                .periodEnd(LocalDateTime.now())
                .build();
    }

    /**
     * Status distribution DTO'su oluşturur
     */
    public StatusDistributionDTO toStatusDistributionDTO(List<ShipmentAnalytics> analytics) {
        // Calculate status distribution for all shipments in the provided list
        Map<String, Long> statusCounts = analytics.stream()
                .filter(sa -> sa.getStatus() != null)
                .collect(Collectors.groupingBy(
                        ShipmentAnalytics::getStatus,
                        Collectors.counting()
                ));

        return StatusDistributionDTO.builder()
                .statusDistribution(statusCounts)
                .build();
    }
} 