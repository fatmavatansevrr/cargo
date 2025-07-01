package com.cargotracking.analytics_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
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

    // Additional fields for analytics
    private Long totalShipments;
    private BigDecimal totalRevenue;
    private Double averageDeliveryTime;
    private Double customerSatisfaction;
    private LocalDateTime timestamp;
} 