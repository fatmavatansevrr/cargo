package com.cargotracking.analytics_service.service.impl;

import com.cargotracking.analytics_service.dto.CarrierPerformanceDTO;
import com.cargotracking.analytics_service.dto.ShipmentAnalyticsDTO;
import com.cargotracking.analytics_service.dto.StatusDistributionDTO;
import com.cargotracking.analytics_service.mapper.AnalyticsMapper;
import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import com.cargotracking.analytics_service.repository.ShipmentAnalyticsRepository;
import com.cargotracking.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ShipmentAnalyticsRepository analyticsRepository;
    private final AnalyticsMapper analyticsMapper;

    public ShipmentAnalytics saveAnalytics(ShipmentAnalytics analytics) {
        Optional<ShipmentAnalytics> existingAnalyticsOpt = analyticsRepository.findByTrackingNumber(analytics.getTrackingNumber());

        if (existingAnalyticsOpt.isPresent()) {
            ShipmentAnalytics existingAnalytics = existingAnalyticsOpt.get();
            log.debug("Found existing analytics record. Updating for tracking number: {}", analytics.getTrackingNumber());
            existingAnalytics.setStatus(analytics.getStatus());
            existingAnalytics.setActualDeliveryTime(analytics.getActualDeliveryTime());
            existingAnalytics.setEstimatedDeliveryTime(analytics.getEstimatedDeliveryTime());
            existingAnalytics.setDeliveryDelayHours(analytics.getDeliveryDelayHours());
            existingAnalytics.setTimestamp(LocalDateTime.now());
            return analyticsRepository.save(existingAnalytics);
        } else {
            log.debug("No existing record found. Creating new analytics record for tracking number: {}", analytics.getTrackingNumber());
            analytics.setTimestamp(LocalDateTime.now());
            return analyticsRepository.save(analytics);
        }
    }

    public List<ShipmentAnalytics> getAnalyticsByCarrier(String carrierId) {
        return analyticsRepository.findByCarrierId(carrierId);
    }

    public List<ShipmentAnalytics> getAnalyticsByShipper(String shipperId) {
        return analyticsRepository.findByShipperId(shipperId);
    }

    public List<ShipmentAnalytics> getAnalyticsByCustomer(String customerId) {
        return analyticsRepository.findByCustomerId(customerId);
    }

    public List<ShipmentAnalytics> getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByTimestampBetween(startDate, endDate);
    }

    public Double getAverageDeliveryDelayByCarrier(String carrierId) {
        List<ShipmentAnalytics> carrierAnalytics = analyticsRepository.findByCarrierId(carrierId);
        return carrierAnalytics.stream()
                .filter(sa -> sa.getDeliveryDelayHours() != null)
                .mapToInt(ShipmentAnalytics::getDeliveryDelayHours)
                .average()
                .orElse(0.0);
    }

    public Map<String, Long> getStatusDistribution(String companyId) {
        return analyticsRepository.findByCompanyId(companyId).stream()
                .filter(sa -> sa.getStatus() != null)
                .collect(Collectors.groupingBy(ShipmentAnalytics::getStatus, Collectors.counting()));
    }

    public Map<String, Double> getCarrierPerformanceMetrics(String carrierId) {
        Map<String, Double> metrics = new HashMap<>();
        List<ShipmentAnalytics> carrierAnalytics = analyticsRepository.findByCarrierId(carrierId);

        long totalDeliveries = carrierAnalytics.size();
        long onTimeDeliveries = carrierAnalytics.stream()
                .filter(sa -> sa.getDeliveryDelayHours() != null && sa.getDeliveryDelayHours() <= 0)
                .count();

        metrics.put("onTimeDeliveryRate", totalDeliveries > 0 ? (double) onTimeDeliveries / totalDeliveries : 0.0);
        metrics.put("averageDeliveryDelay", getAverageDeliveryDelayByCarrier(carrierId));
        return metrics;
    }

    public List<ShipmentAnalytics> getAllAnalytics(String companyId) {
        return analyticsRepository.findByCompanyId(companyId);
    }

    public void generateSampleData() {
        log.info("Generating sample data...");
        analyticsRepository.deleteAll();
        List<ShipmentAnalytics> sampleData = new ArrayList<>();
        // Sample data generation logic can be added here if needed
        analyticsRepository.saveAll(sampleData);
        log.info("Sample data generation complete.");
    }

    public List<ShipmentAnalyticsDTO> getAllAnalytics() {
        log.info("Fetching all analytics data.");
        List<ShipmentAnalytics> analytics = analyticsRepository.findAll();
        if (analytics.isEmpty()){
            log.warn("No analytics data found.");
            return Collections.emptyList();
        }
        return analyticsMapper.toDTOList(analytics);
    }

    public ShipmentAnalyticsDTO getShipmentAnalytics(String trackingNumber) {
        log.info("Fetching analytics for tracking number: {}", trackingNumber);
        return analyticsRepository.findByTrackingNumber(trackingNumber)
                .map(analyticsMapper::toDTO)
                .orElse(null);
    }

    public List<CarrierPerformanceDTO> getCarrierPerformance() {
        log.info("Calculating carrier performance metrics.");
        List<ShipmentAnalytics> allAnalytics = analyticsRepository.findAll();
        if (allAnalytics.isEmpty()){
            log.warn("No analytics data available to calculate carrier performance.");
            return Collections.emptyList();
        }

        return allAnalytics.stream()
                .collect(Collectors.groupingBy(ShipmentAnalytics::getCarrierId))
                .entrySet().stream()
                .map(entry -> analyticsMapper.toCarrierPerformanceDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<StatusDistributionDTO> getStatusDistribution() {
        log.info("Calculating status distribution.");
        List<ShipmentAnalytics> allAnalytics = analyticsRepository.findAll();
        if (allAnalytics.isEmpty()){
            log.warn("No analytics data available to calculate status distribution.");
            return Collections.emptyList();
        }

        return allAnalytics.stream()
                .collect(Collectors.groupingBy(sa -> sa.getCompanyId() != null ? sa.getCompanyId() : "UNKNOWN"))
                .entrySet().stream()
                .map(entry -> analyticsMapper.toStatusDistributionDTO(entry.getValue()))
                .collect(Collectors.toList());
    }
} 