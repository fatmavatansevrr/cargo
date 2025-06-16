package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.StatusEvent;
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
 * Kafka event listener sınıfı
 * FR-NT-002: Apache Kafka üzerinden tüketilen olaylara göre bildirimler tetiklenir
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {
    
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    
    /**
     * Shipment eventlerini dinler
     * shipment.created, shipment.updated, shipment.canceled
     */
    @KafkaListener(
        topics = {"shipment.created", "shipment.updated", "shipment.canceled"},
        groupId = "notification-service-shipment-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleShipmentEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Shipment event alındı - Topic: {}, Partition: {}, Timestamp: {}", 
                    topic, partition, timestamp);
            log.debug("Shipment event mesajı: {}", message);
            
            // JSON'dan ShipmentEvent'e çevir
            ShipmentEvent shipmentEvent = objectMapper.readValue(message, ShipmentEvent.class);
            
            // Event tipini topic'ten al (eğer event'te yoksa)
            if (shipmentEvent.getEventType() == null) {
                shipmentEvent.setEventType(topic);
            }
            
            // Notification service'e gönder
            notificationService.processShipmentEvent(shipmentEvent);
            
            // Kafka acknowledgment
            acknowledgment.acknowledge();
            
            log.info("Shipment event başarıyla işlendi: {} - {}", 
                    topic, shipmentEvent.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Shipment event işleme hatası - Topic: {}, Mesaj: {}, Hata: {}", 
                     topic, message, e.getMessage(), e);
            
            // Hata durumunda da acknowledge et (DLQ'ya gönder)
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Status update eventlerini dinler
     * status.updated
     */
    @KafkaListener(
        topics = {"status.updated"},
        groupId = "notification-service-status-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleStatusEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Status event alındı - Topic: {}, Partition: {}, Timestamp: {}", 
                    topic, partition, timestamp);
            log.debug("Status event mesajı: {}", message);
            
            // JSON'dan StatusEvent'e çevir
            StatusEvent statusEvent = objectMapper.readValue(message, StatusEvent.class);
            
            // Event tipini topic'ten al (eğer event'te yoksa)
            if (statusEvent.getEventType() == null) {
                statusEvent.setEventType(topic);
            }
            
            // Notification service'e gönder
            notificationService.processStatusEvent(statusEvent);
            
            // Kafka acknowledgment
            acknowledgment.acknowledge();
            
            log.info("Status event başarıyla işlendi: {} - {} -> {}", 
                    statusEvent.getTrackingNumber(), 
                    statusEvent.getPreviousStatus(), 
                    statusEvent.getNewStatus());
            
        } catch (Exception e) {
            log.error("Status event işleme hatası - Topic: {}, Mesaj: {}, Hata: {}", 
                     topic, message, e.getMessage(), e);
            
            // Hata durumunda da acknowledge et (DLQ'ya gönder)
            acknowledgment.acknowledge();
        }
    }
    
    /**
     * Genel event handler (fallback için)
     * Diğer notification ile ilgili eventler
     */
    @KafkaListener(
        topics = {"notification.*"},
        groupId = "notification-service-general-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleGeneralNotificationEvents(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Genel notification event alındı - Topic: {}, Partition: {}, Timestamp: {}", 
                    topic, partition, timestamp);
            log.debug("Genel notification event mesajı: {}", message);
            
            // Gelecekte farklı notification event tipleri için işlem yapılabilir
            // Şimdilik sadece log'la
            
            // Kafka acknowledgment
            acknowledgment.acknowledge();
            
            log.info("Genel notification event başarıyla işlendi: {}", topic);
            
        } catch (Exception e) {
            log.error("Genel notification event işleme hatası - Topic: {}, Mesaj: {}, Hata: {}", 
                     topic, message, e.getMessage(), e);
            
            // Hata durumunda da acknowledge et
            acknowledgment.acknowledge();
        }
    }
} 