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
import java.util.Map;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final TrackingStatusRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    public TrackingHistoryResponse updateStatus(String trackingNumber, TrackingState state, String location, String updatedBy) {
        LocalDateTime now = LocalDateTime.now();

        TrackingRecord record = new TrackingRecord();
        record.setShipmentId(trackingNumber); // MongoDB'de shipmentId field'ı trackingNumber'ı tutuyor
        record.setStatus(state);
        record.setLocation(location != null ? location : "Unknown Location");
        record.setUpdatedBy(updatedBy != null ? updatedBy : "system");
        record.setUpdatedAt(now);

        repository.save(record);

        // Kafka Event yayını
        StatusChangedEvent event = new StatusChangedEvent(
                trackingNumber,
                state,
                record.getLocation(),
                record.getUpdatedBy(),
                now
        );

        // Bu event, bildirim servisi gibi diğer servisler tarafından dinlenebilir
        kafkaTemplate.send("shipment-status-events", event);

        return new TrackingHistoryResponse(
                trackingNumber,
                state,
                record.getLocation(),
                record.getUpdatedBy(),
                now
        );
    }

    public List<TrackingHistoryResponse> getHistory(String trackingNumber) {
        return repository.findByShipmentIdOrderByUpdatedAtDesc(trackingNumber)
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

    public Optional<TrackingHistoryResponse> getTrackingInfo(String trackingNumber) {
        return repository.findByShipmentIdOrderByUpdatedAtDesc(trackingNumber)
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

    /**
     * Test amaçlı tracking verisi oluşturur
     * Farklı tracking numaraları için çeşitli durumlar ekler
     */
    public Map<String, Object> createTestTrackingData() {
        List<String> locations = Arrays.asList(
                "İstanbul Transfer Merkezi",
                "Ankara Depo",
                "İzmir Şubesi",
                "Adana Dağıtım Merkezi",
                "Bursa Transfer"
        );

        List<TrackingState> states = Arrays.asList(
                TrackingState.PICKED_UP,
                TrackingState.IN_TRANSIT,
                TrackingState.OUT_FOR_DELIVERY,
                TrackingState.DELIVERED
        );

        LocalDateTime now = LocalDateTime.now();
        List<TrackingRecord> testRecords = Arrays.asList(
                createTestRecord("CT" + System.currentTimeMillis() + "001", TrackingState.PICKED_UP, 
                               "İstanbul Transfer Merkezi", "system", now.minusHours(2)),
                createTestRecord("CT" + System.currentTimeMillis() + "002", TrackingState.IN_TRANSIT, 
                               "Ankara Depo", "system", now.minusHours(1)),
                createTestRecord("CT" + System.currentTimeMillis() + "003", TrackingState.OUT_FOR_DELIVERY, 
                               "İzmir Şubesi", "carrier_user", now.minusMinutes(30)),
                createTestRecord("CT" + System.currentTimeMillis() + "004", TrackingState.DELIVERED, 
                               "Adana Dağıtım Merkezi", "delivery_agent", now.minusMinutes(15)),
                createTestRecord("CT" + System.currentTimeMillis() + "005", TrackingState.IN_TRANSIT, 
                               "Bursa Transfer", "system", now.minusMinutes(45))
        );

        List<TrackingRecord> savedRecords = repository.saveAll(testRecords);

        // Her kayıt için Kafka event'i gönder
        savedRecords.forEach(record -> {
            StatusChangedEvent event = new StatusChangedEvent(
                    record.getShipmentId(),
                    record.getStatus(),
                    record.getLocation(),
                    record.getUpdatedBy(),
                    record.getUpdatedAt()
            );
            kafkaTemplate.send("shipment-status-events", event);
        });

        return Map.of(
                "message", "Test tracking verisi başarıyla oluşturuldu",
                "createdRecords", savedRecords.size(),
                "trackingNumbers", savedRecords.stream()
                        .map(TrackingRecord::getShipmentId)
                        .toList()
        );
    }

    /**
     * Test tracking kaydı oluşturur
     */
    private TrackingRecord createTestRecord(String trackingNumber, TrackingState status, 
                                           String location, String updatedBy, LocalDateTime updatedAt) {
        TrackingRecord record = new TrackingRecord();
        record.setShipmentId(trackingNumber);
        record.setStatus(status);
        record.setLocation(location);
        record.setUpdatedBy(updatedBy);
        record.setUpdatedAt(updatedAt);
        return record;
    }
}
