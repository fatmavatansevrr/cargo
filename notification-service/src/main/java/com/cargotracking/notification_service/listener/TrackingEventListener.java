package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TrackingEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "shipment-status-events", groupId = "notification-group")
    public void handleTrackingEvents(@Payload String payload, Acknowledgment ack) {
        try {
            TrackingEvent event = objectMapper.readValue(payload, TrackingEvent.class);
            String status = event.getCurrentStatus();

            if (status == null) {
                log.warn("⚠️ Event'in currentStatus'u null geldi! Event: {}", event);
                ack.acknowledge();
                return;
            }

            switch (status.toUpperCase()) {
                case "DELIVERED" -> notificationService.processDeliveryCompletedEvent(event);
                case "DELIVERY_FAILED" -> notificationService.processDeliveryFailedEvent(event);
                case "CANCELLED", "CANCELED" -> notificationService.processStatusChangedEvent(event); // İptal için özel mail gerekiyorsa ayrı fonk açabilirsin
                default -> notificationService.processStatusChangedEvent(event); // Diğer tüm statusler
            }

            ack.acknowledge();
            log.info("✅ Status event işlendi ve acknowledge edildi: {} → {}", event.getTrackingNumber(), status);
        } catch (Exception e) {
            log.error("❌ Tracking event işlenirken hata: {}", payload, e);
            ack.acknowledge(); // Hatalı eventleri de atla, queue tıkanmasın
        }
    }
}
