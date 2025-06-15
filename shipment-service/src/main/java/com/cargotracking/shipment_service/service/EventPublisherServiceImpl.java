package com.cargotracking.shipment_service.service;


import com.cargotracking.shipment_service.event.ShipmentCreatedEvent;
import com.cargotracking.shipment_service.event.ShipmentStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherServiceImpl implements EventPublisherService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.shipment-created:shipment.created}")
    private String shipmentCreatedTopic;

    @Value("${kafka.topics.shipment-status-changed:shipment.status.changed}")
    private String shipmentStatusChangedTopic;

    @Override
    public void publishShipmentCreatedEvent(ShipmentCreatedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(shipmentCreatedTopic, event.getTrackingNumber(), eventJson);
            log.info("Published shipment created event for tracking number: {}", event.getTrackingNumber());
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment created event", e);
            throw new RuntimeException("Failed to publish shipment created event", e);
        }
    }

    @Override
    public void publishShipmentStatusChangedEvent(ShipmentStatusChangedEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(shipmentStatusChangedTopic, event.getTrackingNumber(), eventJson);
            log.info("Published shipment status changed event for tracking number: {}", event.getTrackingNumber());
        } catch (JsonProcessingException e) {
            log.error("Error serializing shipment status changed event", e);
            throw new RuntimeException("Failed to publish shipment status changed event", e);
        }
    }
}