package com.cargotracking.analytics_service.repository;

import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentAnalyticsRepository extends MongoRepository<ShipmentAnalytics, String> {

    List<ShipmentAnalytics> findByCarrierId(String carrierId);

    List<ShipmentAnalytics> findByCompanyId(String companyId);

    List<ShipmentAnalytics> findByShipperId(String shipperId);

    List<ShipmentAnalytics> findByCustomerId(String customerId);

    List<ShipmentAnalytics> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    long countByStatus(String status);

    Optional<ShipmentAnalytics> findByTrackingNumber(String trackingNumber);
} 