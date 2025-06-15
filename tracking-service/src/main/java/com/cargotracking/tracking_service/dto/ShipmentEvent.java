package com.cargotracking.tracking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShipmentEvent {
    private String shipmentId;
    private String eventType; // örn. "CREATED", "CANCELLED"
    private LocalDateTime eventTime;
}
