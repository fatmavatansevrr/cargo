package com.cargotracking.analytics_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Shipment Analytics Entity - Kargo analitik verileri
 * PostgreSQL veritabanında saklanır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shipment_analytics")
public class ShipmentAnalytics {
    @Id
    private String id;
    private String shipmentId;
    private String status;
    private LocalDateTime statusTimestamp;
    private String carrierId;
    private String shipperId;
    private String customerId;
    private String originLocation;
    private String destinationLocation;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private Integer deliveryDelayHours;
    private String route;
    private String notes;
} 