package com.cargotracking.analytics_service.repository.jpa;

import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShipmentAnalyticsRepository extends JpaRepository<ShipmentAnalytics, String> {

    List<ShipmentAnalytics> findByCarrierId(String carrierId);

    List<ShipmentAnalytics> findByShipperId(String shipperId);

    List<ShipmentAnalytics> findByCustomerId(String customerId);

    @Query("SELECT sa FROM ShipmentAnalytics sa WHERE sa.statusTimestamp BETWEEN ?1 AND ?2")
    List<ShipmentAnalytics> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT AVG(sa.deliveryDelayHours) FROM ShipmentAnalytics sa WHERE sa.carrierId = ?1")
    Double findAverageDeliveryDelayByCarrier(String carrierId);

    @Query("SELECT COUNT(sa) FROM ShipmentAnalytics sa WHERE sa.status = ?1")
    Long countByStatus(String status);
} 