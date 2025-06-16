package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * TrackingEventListener - Kafka'dan gelen tracking olaylarını dinler
 * Tracking service'den gelen status update eventlerini yakalar ve bildirim gönderir
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(TrackingEventListener.class);
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Status changed event listener
     */
    @KafkaListener(
        topics = "status.changed",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatusChanged(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received status changed event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Parsed status changed event: {}", event);
            
            // Event'i notification service'e gönder
            notificationService.processTrackingEvent(event);
            
            // Message'i acknowledge et
            acknowledgment.acknowledge();
            log.info("Successfully processed status changed event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing status changed event: {}", eventPayload, e);
            // Hata durumunda acknowledge etmeyerek retry mekanizmasını tetikle
        }
    }
    
    /**
     * Delivery completed event listener
     */
    @KafkaListener(
        topics = "delivery.completed",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryCompleted(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received delivery completed event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Parsed delivery completed event: {}", event);
            
            notificationService.processTrackingEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed delivery completed event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing delivery completed event: {}", eventPayload, e);
        }
    }
    
    /**
     * Delivery failed event listener
     */
    @KafkaListener(
        topics = "delivery.failed",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDeliveryFailed(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received delivery failed event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Parsed delivery failed event: {}", event);
            
            notificationService.processTrackingEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed delivery failed event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing delivery failed event: {}", eventPayload, e);
        }
    }
    
    /**
     * Package arrived at hub event listener
     */
    @KafkaListener(
        topics = "package.arrived",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePackageArrived(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received package arrived event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Parsed package arrived event: {}", event);
            
            notificationService.processTrackingEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed package arrived event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing package arrived event: {}", eventPayload, e);
        }
    }
    
    /**
     * Out for delivery event listener
     */
    @KafkaListener(
        topics = "out.for.delivery",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOutForDelivery(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received out for delivery event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Parsed out for delivery event: {}", event);
            
            notificationService.processTrackingEvent(event);
            
            acknowledgment.acknowledge();
            log.info("Successfully processed out for delivery event for tracking: {}", 
                    event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Error processing out for delivery event: {}", eventPayload, e);
        }
    }
    
    /**
     * Generic tracking event listener - pattern matching ile tracking eventlerini dinler
     */
    @KafkaListener(
        topicPattern = "(status|delivery|package|out)\\..*",
        groupId = "notification-group-tracking",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleAllTrackingEvents(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.debug("Received generic tracking event from topic: {}, partition: {}, offset: {}", 
                topic, partition, offset);
        
        try {
            // Sadece monitoring ve loglama için
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            log.debug("Generic tracking event processed for tracking: {}", event.getTrackingNumber());
            
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.warn("Error processing generic tracking event from topic {}: {}", topic, eventPayload, e);
            acknowledgment.acknowledge(); // Monitoring eventleri için hata durumunda da acknowledge et
        }
    }
} 