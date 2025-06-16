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
} 