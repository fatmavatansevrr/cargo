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
 * Basit ShipmentEventListener - Kafka'dan gelen shipment olaylarını dinler
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Shipment events topic'ini dinler
     */
    @KafkaListener(
        topics = "shipment-events",
        groupId = "notification-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShipmentEvents(
            @Payload String eventPayload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment acknowledgment) {
        
        log.info("📦 Shipment event alındı: {}", topic);
        
        try {
            // JSON payload'ı ShipmentEvent'e parse et
            ShipmentEvent event = objectMapper.readValue(eventPayload, ShipmentEvent.class);
            
            log.info("Shipment event işleniyor: type={}, tracking={}", 
                    event.getEventType(), event.getTrackingNumber());
            
            // Event'i notification service'e gönder
            notificationService.processShipmentEvent(event);
            
            // Message'i acknowledge et
            acknowledgment.acknowledge();
            
            log.info("✅ Shipment event başarıyla işlendi: {}", event.getEventType());
            
        } catch (Exception e) {
            log.error("❌ Shipment event işlenirken hata: {}", eventPayload, e);
            acknowledgment.acknowledge(); // Basit yaklaşım - hataları skip et
        }
    }
} 