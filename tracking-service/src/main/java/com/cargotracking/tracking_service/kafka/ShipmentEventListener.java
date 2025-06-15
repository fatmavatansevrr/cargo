package com.cargotracking.tracking_service.kafka;

import com.cargotracking.tracking_service.dto.ShipmentEvent;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

    private final TrackingService trackingService;

    @KafkaListener(topics = "shipment-events", groupId = "tracking-group")
    public void handleShipmentEvent(ShipmentEvent event) {
        log.info("Received Shipment Event: {}", event);

        if ("CREATED".equalsIgnoreCase(event.getEventType())) {
            trackingService.updateStatus(event.getShipmentId(), TrackingState.CREATED);
        } else if ("CANCELLED".equalsIgnoreCase(event.getEventType())) {
            trackingService.updateStatus(event.getShipmentId(), TrackingState.CANCELLED);
        } else {
            log.warn("Unknown shipment event type: {}", event.getEventType());
        }
    }
}
