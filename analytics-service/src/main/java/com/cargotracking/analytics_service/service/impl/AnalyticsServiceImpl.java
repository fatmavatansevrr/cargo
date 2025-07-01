package com.cargotracking.analytics_service.service.impl;

import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import com.cargotracking.analytics_service.repository.jpa.ShipmentAnalyticsRepository;
import com.cargotracking.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ShipmentAnalyticsRepository analyticsRepository;

    @Override
    public ShipmentAnalytics saveAnalytics(ShipmentAnalytics analytics) {
        return analyticsRepository.save(analytics);
    }

    @Override
    public List<ShipmentAnalytics> getAnalyticsByCarrier(String carrierId) {
        return analyticsRepository.findByCarrierId(carrierId);
    }

    @Override
    public List<ShipmentAnalytics> getAnalyticsByShipper(String shipperId) {
        return analyticsRepository.findByShipperId(shipperId);
    }

    @Override
    public List<ShipmentAnalytics> getAnalyticsByCustomer(String customerId) {
        return analyticsRepository.findByCustomerId(customerId);
    }

    @Override
    public List<ShipmentAnalytics> getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public Double getAverageDeliveryDelayByCarrier(String carrierId) {
        return analyticsRepository.findAverageDeliveryDelayByCarrier(carrierId);
    }

    @Override
    public Map<String, Long> getStatusDistribution() {
        List<ShipmentAnalytics> allAnalytics = analyticsRepository.findAll();
        return allAnalytics.stream()
                .collect(Collectors.groupingBy(
                        ShipmentAnalytics::getStatus,
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Double> getCarrierPerformanceMetrics(String carrierId) {
        Map<String, Double> metrics = new HashMap<>();
        List<ShipmentAnalytics> carrierAnalytics = analyticsRepository.findByCarrierId(carrierId);

        // Calculate on-time delivery rate
        long totalDeliveries = carrierAnalytics.size();
        long onTimeDeliveries = carrierAnalytics.stream()
                .filter(sa -> sa.getDeliveryDelayHours() != null && sa.getDeliveryDelayHours() <= 0)
                .count();

        metrics.put("onTimeDeliveryRate", totalDeliveries > 0 ?
                (double) onTimeDeliveries / totalDeliveries : 0.0);

        // Calculate average delivery delay
        Double avgDelay = analyticsRepository.findAverageDeliveryDelayByCarrier(carrierId);
        metrics.put("averageDeliveryDelay", avgDelay != null ? avgDelay : 0.0);

        return metrics;
    }



    @Override
    public List<ShipmentAnalytics> getAllAnalytics() {
        return analyticsRepository.findAll();
    }

    @Override
    public void generateSampleData() {
        // Sample data with different carriers, statuses, and realistic metrics
        List<ShipmentAnalytics> sampleData = new ArrayList<>();

        // Carrier 1: DHL
        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-001")
                .shipmentId("SHP-DHL-001")
                .carrierId("DHL")
                .shipperId("SHIPPER-001")
                .customerId("CUSTOMER-001")
                .status("DELIVERED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(250.50))
                .averageDeliveryTime(72.5)
                .deliveryDelayHours(0)
                .customerSatisfaction(4.8)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build());

        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-002")
                .shipmentId("SHP-DHL-002")
                .carrierId("DHL")
                .shipperId("SHIPPER-002")
                .customerId("CUSTOMER-002")
                .status("IN_TRANSIT")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(180.75))
                .averageDeliveryTime(48.0)
                .deliveryDelayHours(-12)
                .customerSatisfaction(4.5)
                .timestamp(LocalDateTime.now().minusHours(6))
                .build());

        // Carrier 2: FedEx
        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-003")
                .shipmentId("SHP-FDX-001")
                .carrierId("FEDEX")
                .shipperId("SHIPPER-003")
                .customerId("CUSTOMER-003")
                .status("DELIVERED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(320.00))
                .averageDeliveryTime(96.0)
                .deliveryDelayHours(24)
                .customerSatisfaction(3.9)
                .timestamp(LocalDateTime.now().minusDays(2))
                .build());

        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-004")
                .shipmentId("SHP-FDX-002")
                .carrierId("FEDEX")
                .shipperId("SHIPPER-001")
                .customerId("CUSTOMER-004")
                .status("OUT_FOR_DELIVERY")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(145.30))
                .averageDeliveryTime(36.0)
                .deliveryDelayHours(-6)
                .customerSatisfaction(4.2)
                .timestamp(LocalDateTime.now().minusHours(2))
                .build());

        // Carrier 3: UPS
        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-005")
                .shipmentId("SHP-UPS-001")
                .carrierId("UPS")
                .shipperId("SHIPPER-004")
                .customerId("CUSTOMER-005")
                .status("PROCESSING")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(89.99))
                .averageDeliveryTime(24.0)
                .deliveryDelayHours(0)
                .customerSatisfaction(4.0)
                .timestamp(LocalDateTime.now().minusHours(1))
                .build());

        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-006")
                .shipmentId("SHP-UPS-002")
                .carrierId("UPS")
                .shipperId("SHIPPER-002")
                .customerId("CUSTOMER-006")
                .status("DELIVERED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(195.45))
                .averageDeliveryTime(84.0)
                .deliveryDelayHours(12)
                .customerSatisfaction(4.1)
                .timestamp(LocalDateTime.now().minusDays(3))
                .build());

        // Carrier 4: Aras Kargo (Local Turkish carrier)
        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-007")
                .shipmentId("SHP-ARS-001")
                .carrierId("ARAS")
                .shipperId("SHIPPER-005")
                .customerId("CUSTOMER-007")
                .status("DELIVERED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(35.50))
                .averageDeliveryTime(48.0)
                .deliveryDelayHours(0)
                .customerSatisfaction(4.6)
                .timestamp(LocalDateTime.now().minusHours(18))
                .build());

        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-008")
                .shipmentId("SHP-ARS-002")
                .carrierId("ARAS")
                .shipperId("SHIPPER-003")
                .customerId("CUSTOMER-008")
                .status("EXCEPTION")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(42.75))
                .averageDeliveryTime(72.0)
                .deliveryDelayHours(48)
                .customerSatisfaction(2.8)
                .timestamp(LocalDateTime.now().minusDays(4))
                .build());

        // Additional data for better analytics
        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-009")
                .shipmentId("SHP-DHL-003")
                .carrierId("DHL")
                .shipperId("SHIPPER-001")
                .customerId("CUSTOMER-009")
                .status("RETURNED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(125.00))
                .averageDeliveryTime(120.0)
                .deliveryDelayHours(96)
                .customerSatisfaction(1.5)
                .timestamp(LocalDateTime.now().minusDays(5))
                .build());

        sampleData.add(ShipmentAnalytics.builder()
                .id("ANALYTICS-010")
                .shipmentId("SHP-FDX-003")
                .carrierId("FEDEX")
                .shipperId("SHIPPER-004")
                .customerId("CUSTOMER-010")
                .status("DELIVERED")
                .totalShipments(1L)
                .totalRevenue(java.math.BigDecimal.valueOf(275.80))
                .averageDeliveryTime(60.0)
                .deliveryDelayHours(-8)
                .customerSatisfaction(4.7)
                .timestamp(LocalDateTime.now().minusHours(30))
                .build());

        // Save all sample data
        analyticsRepository.saveAll(sampleData);
    }
} 