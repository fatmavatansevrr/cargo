package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.TrackingEvent;
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
 * Basit TrackingEventListener - Status değişikliklerini dinler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TrackingEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Tracking status events topic'ini dinler
     */
    @KafkaListener(
        topics = "shipment-status-events",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTrackingEvents(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("🚚 Tracking event alındı: {}", topic);
        
        try {
            // JSON payload'ı TrackingEvent'e parse et
            TrackingEvent event = objectMapper.readValue(eventPayload, TrackingEvent.class);
            
            log.info("Tracking event işleniyor: type={}, tracking={}, status={}", 
                    event.getEventType(), event.getTrackingNumber(), event.getCurrentStatus());
            
            // Event'i notification service'e gönder
            notificationService.processTrackingEvent(event);
            
            // Message'i acknowledge et
            acknowledgment.acknowledge();
            
            log.info("✅ Tracking event başarıyla işlendi: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("❌ Tracking event işlenirken hata: {}", eventPayload, e);
            acknowledgment.acknowledge(); // Basit yaklaşım - hataları skip et
        }
    }
} 