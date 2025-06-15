package com.cargotracking.tracking_service.event;

import com.cargotracking.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShipmentCreatedEventListener {

    private final TrackingService trackingService;

    @KafkaListener(topics = "shipment-created", groupId = "tracking-group")
    public void consume(String shipmentId) {
        trackingService.updateStatus(shipmentId, "CREATED");
    }
}

