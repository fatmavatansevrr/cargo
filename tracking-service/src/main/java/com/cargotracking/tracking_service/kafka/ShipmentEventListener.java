package com.cargotracking.tracking_service.kafka;

import com.cargotracking.tracking_service.dto.ShipmentEvent;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventListener.class);
    private final TrackingService trackingService;

    @KafkaListener(topics = "shipment-events", groupId = "tracking-group")
    public void handleShipmentEvent(ShipmentEvent event) {
        log.info("📦 Kafka olayı alındı: {} - Tracking: {} - Status: {}", 
                event.getEventType(), event.getTrackingNumber(), event.getStatus());

        try {
            if ("shipment.created".equalsIgnoreCase(event.getEventType())) {
                log.info("🆕 Yeni gönderi takip kaydı oluşturuluyor: {}", event.getTrackingNumber());
                trackingService.updateStatus(event.getTrackingNumber(), TrackingState.CREATED);
            } else if ("shipment.updated".equalsIgnoreCase(event.getEventType())) {
                log.info("🔄 Gönderi durumu güncelleniyor: {} -> {}", event.getTrackingNumber(), event.getStatus());
                TrackingState newState = mapStatusToTrackingState(event.getStatus());
                trackingService.updateStatus(event.getTrackingNumber(), newState);
            } else if ("shipment.canceled".equalsIgnoreCase(event.getEventType())) {
                log.info("❌ Gönderi iptal edildi: {}", event.getTrackingNumber());
                trackingService.updateStatus(event.getTrackingNumber(), TrackingState.CANCELLED);
            } else if ("shipment.finished".equalsIgnoreCase(event.getEventType())) {
                log.info("✅ Gönderi tamamlandı: {}", event.getTrackingNumber());
                trackingService.updateStatus(event.getTrackingNumber(), TrackingState.DELIVERED);
            } else {
                log.warn("⚠️ Bilinmeyen olay türü: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("❌ Kafka olayı işlenirken hata: {} - {}", event.getEventType(), e.getMessage(), e);
        }
    }

    /**
     * Shipment status'unu TrackingState'e dönüştür
     */
    private TrackingState mapStatusToTrackingState(String status) {
        if (status == null) return TrackingState.CREATED;
        
        return switch (status.toUpperCase()) {
            case "ACTIVE" -> TrackingState.IN_TRANSIT;
            case "FINISHED" -> TrackingState.DELIVERED;
            case "CANCELLED" -> TrackingState.CANCELLED;
            default -> TrackingState.CREATED;
        };
    }
}
