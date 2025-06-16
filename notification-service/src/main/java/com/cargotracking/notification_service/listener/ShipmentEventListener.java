package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * ShipmentEventListener - Kafka'dan gelen shipment olaylarını dinler
 * Shipment service'den gelen eventleri yakalar ve bildirim gönderir
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Shipment created event listener
     */
    @KafkaListener(
        topics = "shipment.created",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShipmentCreated(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received shipment created event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            ShipmentEvent event = objectMapper.readValue(eventPayload, ShipmentEvent.class);
            log.debug("Parsed shipment created event: {}", event);
            
            // Event'i notification service'e gönder
            notificationService.processShipmentEvent(event);
            
            // Message'i acknowledge et
            acknowledgment.acknowledge();
            log.info("Successfully processed shipment created event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing shipment created event: {}", eventPayload, e);
            // Hata durumunda acknowledge etmeyerek retry mekanizmasını tetikle
            // acknowledgment.acknowledge(); // Bu satır hata durumunda çağrılmaz
        }
    }
    
    /**
     * Shipment updated event listener
     */
    @KafkaListener(
        topics = "shipment.updated",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShipmentUpdated(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received shipment updated event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            ShipmentEvent event = objectMapper.readValue(eventPayload, ShipmentEvent.class);
            log.debug("Parsed shipment updated event: {}", event);
            
            notificationService.processShipmentEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed shipment updated event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing shipment updated event: {}", eventPayload, e);
        }
    }
    
    /**
     * Shipment canceled event listener
     */
    @KafkaListener(
        topics = "shipment.canceled",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShipmentCanceled(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received shipment canceled event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            ShipmentEvent event = objectMapper.readValue(eventPayload, ShipmentEvent.class);
            log.debug("Parsed shipment canceled event: {}", event);
            
            notificationService.processShipmentEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed shipment canceled event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing shipment canceled event: {}", eventPayload, e);
        }
    }
    
    /**
     * Generic shipment event listener - tüm shipment eventlerini dinler
     */
    @KafkaListener(
        topicPattern = "shipment\\..*",
        groupId = "notification-group-all",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAllShipmentEvents(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.debug("Received generic shipment event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            // Sadece loglama ve monitoring için
            ShipmentEvent event = objectMapper.readValue(eventPayload, ShipmentEvent.class);
            log.debug("Generic shipment event processed for tracking: {}", event.getTrackingNumber());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.warn("Error processing generic shipment event from topic {}: {}", topic, eventPayload, e);
        }
    }
} 