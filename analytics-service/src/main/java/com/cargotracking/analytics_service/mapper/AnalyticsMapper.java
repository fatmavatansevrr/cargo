package com.cargotracking.analytics_service.mapper;

import com.cargotracking.analytics_service.dto.CarrierPerformanceDTO;
import com.cargotracking.analytics_service.dto.ShipmentAnalyticsDTO;
import com.cargotracking.analytics_service.dto.StatusDistributionDTO;
import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import com.cargotracking.analytics_service.service.impl.AnalyticsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalyticsMapper {

    private final AnalyticsServiceImpl analyticsService;

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
                .totalShipments(analytics.getTotalShipments() != null ? analytics.getTotalShipments() : 1L)
                .totalRevenue(analytics.getTotalRevenue() != null ? analytics.getTotalRevenue() : BigDecimal.ZERO)
                .averageDeliveryTime(analytics.getAverageDeliveryTime() != null ? analytics.getAverageDeliveryTime() : (deliveryTime != null ? deliveryTime : 0.0))
                .deliveryDelayHours(analytics.getDeliveryDelayHours())
                .customerSatisfaction(analytics.getCustomerSatisfaction() != null ? analytics.getCustomerSatisfaction() : 0.0)
                .shipmentsByStatus(statusMap)
                .timestamp(analytics.getTimestamp() != null ? analytics.getTimestamp() : analytics.getStatusTimestamp())
                .build();
    }

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
        analytics.setTotalShipments(dto.getTotalShipments());
        analytics.setTotalRevenue(dto.getTotalRevenue());
        analytics.setAverageDeliveryTime(dto.getAverageDeliveryTime());
        analytics.setCustomerSatisfaction(dto.getCustomerSatisfaction());
        analytics.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
        return analytics;
    }

    public List<ShipmentAnalyticsDTO> toDTOList(List<ShipmentAnalytics> analytics) {
        return analytics.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

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

        // Calculate total revenue and average satisfaction
        BigDecimal totalRevenue = analytics.stream()
                .filter(sa -> sa.getTotalRevenue() != null)
                .map(ShipmentAnalytics::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgSatisfaction = analytics.stream()
                .filter(sa -> sa.getCustomerSatisfaction() != null && sa.getCustomerSatisfaction() > 0)
                .mapToDouble(ShipmentAnalytics::getCustomerSatisfaction)
                .average()
                .orElse(0.0);

        // Calculate average delivery time
        double avgDeliveryTime = analytics.stream()
                .filter(sa -> sa.getAverageDeliveryTime() != null)
                .mapToDouble(ShipmentAnalytics::getAverageDeliveryTime)
                .average()
                .orElse(0.0);

        return CarrierPerformanceDTO.builder()
                .carrierId(carrierId)
                .totalShipments(totalShipments)
                .onTimeDeliveryRate(onTimeDeliveryRate)
                .averageDeliveryTime(avgDeliveryTime)
                .averageDelayTime(averageDelayTime)
                .delayedDeliveries(delayedDeliveries)
                .satisfactionRating(avgSatisfaction)
                .deliveriesByStatus(deliveriesByStatus)
                .totalRevenue(totalRevenue)
                .periodStart(LocalDateTime.now().minusMonths(1))
                .periodEnd(LocalDateTime.now())
                .build();
    }

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