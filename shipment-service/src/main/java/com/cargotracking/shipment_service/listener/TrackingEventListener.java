package com.cargotracking.shipment_service.listener;

import com.cargotracking.shipment_service.dto.StatusChangedEvent;
import com.cargotracking.shipment_service.model.TrackingState;
import com.cargotracking.shipment_service.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingEventListener {

    private final ShipmentService shipmentService;

    @KafkaListener(topics = "shipment-status-events", groupId = "shipment-service", containerFactory = "kafkaListenerContainerFactory")
    public void handleStatusChangeEvent(StatusChangedEvent event) {
        log.info("Received status change event for shipment ID: {}. New status: {}", event.getShipmentId(), event.getNewStatus());

        if (event.getNewStatus() == TrackingState.DELIVERED) {
            try {
                shipmentService.finalizeShipment(event.getShipmentId());
                log.info("Shipment with ID: {} has been marked as FINISHED.", event.getShipmentId());
            } catch (Exception e) {
                log.error("Error while finalizing shipment with ID: {}", event.getShipmentId(), e);
            }
        }
    }
} 