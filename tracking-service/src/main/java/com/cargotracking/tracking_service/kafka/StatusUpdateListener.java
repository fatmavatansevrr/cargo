package com.cargotracking.tracking_service.kafka;

import com.cargotracking.tracking_service.dto.TrackingEvent;
import com.cargotracking.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusUpdateListener {

    private final TrackingService trackingService;

    @KafkaListener(topics = "status-update-topic", groupId = "tracking-status-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleStatusUpdate(TrackingEvent event) {
        log.info("Kafka'dan durum güncelleme eventi alındı: {}", event);
        try {
            trackingService.updateStatus(event.getTrackingNumber(), event.getNewStatus(), event.getLocation(), event.getUpdatedBy());
            log.info("Takip durumu başarıyla güncellendi: {}", event.getTrackingNumber());
        } catch (Exception e) {
            log.error("Takip durumu güncellenirken hata oluştu: shipmentId={}, error={}", event.getTrackingNumber(), e.getMessage(), e);
            // Burada hatayı işlemek için ek mantık ekleyebilirsiniz (örn: bir dead-letter queue'ya göndermek)
        }
    }
} 