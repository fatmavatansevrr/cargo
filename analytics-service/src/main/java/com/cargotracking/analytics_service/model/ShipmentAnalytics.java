package com.cargotracking.analytics_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shipment_analytics")
public class ShipmentAnalytics {
    @Id
    private String id;
    private String shipmentId;
    private String trackingNumber;
    private String status;
    private LocalDateTime statusTimestamp;
    private String carrierId;
    private String companyId;
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