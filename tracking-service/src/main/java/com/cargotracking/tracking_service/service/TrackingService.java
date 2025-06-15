package com.cargotracking.tracking_service.service;

import com.cargotracking.tracking_service.dto.StatusChangedEvent;
import com.cargotracking.tracking_service.dto.TrackingHistoryResponse;
import com.cargotracking.tracking_service.model.TrackingRecord;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.repository.TrackingStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final TrackingStatusRepository repository;
    private final KafkaTemplate<String, StatusChangedEvent> kafkaTemplate;


    public TrackingHistoryResponse updateStatus(String shipmentId, TrackingState state) {
        LocalDateTime now = LocalDateTime.now();

        TrackingRecord record = new TrackingRecord();
        record.setShipmentId(shipmentId);
        record.setStatus(state); // enum
        record.setUpdatedAt(LocalDateTime.now());

        repository.save(record);

        // Kafka Event yayını
        StatusChangedEvent event = new StatusChangedEvent(
                shipmentId,
                state,
                "İstanbul Transfer Merkezi", // şimdilik sabit, sonra DTO'dan çekersin
                "system", // sonra DTO'dan alınır
                now
        );

        kafkaTemplate.send("shipment-status-events", event);

        return new TrackingHistoryResponse(
                shipmentId,
                state,
                record.getLocation(),
                record.getUpdatedBy(),
                now
        );
    }

    public List<TrackingHistoryResponse> getHistory(String shipmentId) {
        return repository.findByShipmentIdOrderByUpdatedAtDesc(shipmentId)
                .stream()
                .map(record -> new TrackingHistoryResponse(
                        record.getShipmentId(),
                        record.getStatus(),
                        record.getLocation(),
                        record.getUpdatedBy(),
                        record.getUpdatedAt()
                ))
                .toList();
    }

    public List<TrackingHistoryResponse> getAllTrackings() {
        return repository.findAll()
                .stream()
                .map(record -> new TrackingHistoryResponse(
                        record.getShipmentId(),
                        record.getStatus(),
                        record.getLocation(),
                        record.getUpdatedBy(),
                        record.getUpdatedAt()
                ))
                .toList();
    }

    public Optional<TrackingHistoryResponse> getTrackingInfo(String shipmentId) {
        return repository.findByShipmentIdOrderByUpdatedAtDesc(shipmentId)
                .stream()
                .findFirst()
                .map(record -> new TrackingHistoryResponse(
                        record.getShipmentId(),
                        record.getStatus(),
                        record.getLocation(),
                        record.getUpdatedBy(),
                        record.getUpdatedAt()
                ));
    }


}
