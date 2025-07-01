package com.cargotracking.analytics_service.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "analytics_read")
public class AnalyticsReadModel {

    @Id
    private String id;

    private String shipmentId;
    private String carrierId;
    private String customerId;
    private String shipperId;

    private String status;
    private LocalDateTime statusTimestamp;

    private Integer deliveryDelayHours;

    private Map<String, Long> shipmentsByStatus;
    private LocalDateTime createdAt;
}
