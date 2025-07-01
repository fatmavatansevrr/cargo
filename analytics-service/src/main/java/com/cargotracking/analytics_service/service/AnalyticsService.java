package com.cargotracking.analytics_service.service;

import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    ShipmentAnalytics saveAnalytics(ShipmentAnalytics analytics);

    List<ShipmentAnalytics> getAnalyticsByCarrier(String carrierId);

    List<ShipmentAnalytics> getAnalyticsByShipper(String shipperId);

    List<ShipmentAnalytics> getAnalyticsByCustomer(String customerId);

    List<ShipmentAnalytics> getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Double getAverageDeliveryDelayByCarrier(String carrierId);

    Map<String, Long> getStatusDistribution();

    Map<String, Double> getCarrierPerformanceMetrics(String carrierId);

    List<ShipmentAnalytics> getAllAnalytics();

    void generateSampleData();
}