package com.cargotracking.tracking_service.service;

import com.cargotracking.tracking_service.client.ShipmentServiceClient;
import com.cargotracking.tracking_service.dto.ShipmentDetailsDto;
import com.cargotracking.tracking_service.dto.TrackingEvent;
import com.cargotracking.tracking_service.dto.TrackingHistoryResponse;
import com.cargotracking.tracking_service.model.TrackingRecord;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.repository.TrackingStatusRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class TrackingService {

    private final TrackingStatusRepository repository;
    private final TrackingCacheService trackingCacheService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShipmentServiceClient shipmentServiceClient;


    public TrackingHistoryResponse updateStatus(String trackingNumber, TrackingState state, String location, String updatedBy) throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        ShipmentServiceClient.RecipientInfo recipientInfo = shipmentServiceClient.getRecipientInfoByTrackingNumber(trackingNumber);
        ShipmentDetailsDto shipmentDetails = shipmentServiceClient.getShipmentDetailsByTrackingNumber(trackingNumber);

        Long senderUserId = shipmentDetails != null ? shipmentDetails.getSenderCustomerId() : null;
        Long companyId = shipmentDetails != null ? shipmentDetails.getShipmentCompanyId() : null;
        Long carrierUserId = shipmentDetails != null ? shipmentDetails.getAssignedCarrierId() : null;


        TrackingRecord record = new TrackingRecord();
        record.setShipmentId(trackingNumber);
        record.setStatus(state);
        record.setLocation(location != null ? location : "Unknown Location");
        record.setUpdatedBy(updatedBy != null ? updatedBy : "system");
        record.setUpdatedAt(now);


        repository.save(record);
        trackingCacheService.cacheTrackingInfo(trackingNumber, record); // Güncel durumu cache'e yaz

        TrackingEvent event = new TrackingEvent(
                trackingNumber,
                state,
                record.getLocation(),
                record.getUpdatedBy(),
                now,
                recipientInfo != null ? recipientInfo.getEmail() : null,
                recipientInfo != null ? recipientInfo.getFullName() : null,
                senderUserId,
                companyId,
                carrierUserId
        );
        log.info("Kafka'ya gönderilecek event JSON: {}", new ObjectMapper().writeValueAsString(event));


        kafkaTemplate.send("shipment-status-events", event);

        return new TrackingHistoryResponse(
                trackingNumber,
                state,
                record.getLocation(),
                record.getUpdatedBy(),
                now,
                recipientInfo != null ? recipientInfo.getEmail() : null,
                recipientInfo != null ? recipientInfo.getFullName() : null,
                senderUserId,
                companyId,
                carrierUserId
        );
    }

    public List<TrackingHistoryResponse> getHistory(String trackingNumber) {
        return repository.findByShipmentIdOrderByUpdatedAtDesc(trackingNumber)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<TrackingHistoryResponse> getAllTrackings() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<TrackingHistoryResponse> getTrackingInfo(String trackingNumber) {
        // Önce Redis cache'den dene
        TrackingRecord cached = trackingCacheService.getTrackingInfo(trackingNumber);
        if (cached != null) {
            System.out.println(">> REDIS HIT: " + trackingNumber);
            return Optional.of(toResponse(cached));
        }
        System.out.println("❌ REDIS MISS → MongoDB'den alınıyor: " + trackingNumber);
        // Yoksa MongoDB'den bul, Redis'e yaz
        return repository.findByShipmentIdOrderByUpdatedAtDesc(trackingNumber)
                .stream()
                .findFirst()
                .map(record -> {
                    trackingCacheService.cacheTrackingInfo(trackingNumber, record);
                    return toResponse(record);
                });
    }

    public Map<String, Object> createTestTrackingData() {
        List<String> locations = Arrays.asList("İstanbul Transfer Merkezi", "Ankara Depo", "İzmir Şubesi", "Adana Dağıtım Merkezi", "Bursa Transfer");
        List<TrackingState> states = Arrays.asList(
                TrackingState.PICKED_UP,
                TrackingState.IN_TRANSIT,
                TrackingState.OUT_FOR_DELIVERY,
                TrackingState.DELIVERED
        );

        LocalDateTime now = LocalDateTime.now();
        List<TrackingRecord> testRecords = Arrays.asList(
                createTestRecord("CT" + System.currentTimeMillis() + "001", TrackingState.PICKED_UP, "İstanbul Transfer Merkezi", "system", now.minusHours(2),"test@gmail.com"),
                createTestRecord("CT" + System.currentTimeMillis() + "002", TrackingState.IN_TRANSIT, "Ankara Depo", "system", now.minusHours(1),"test1@gmail.com"),
                createTestRecord("CT" + System.currentTimeMillis() + "003", TrackingState.OUT_FOR_DELIVERY, "İzmir Şubesi", "carrier_user", now.minusMinutes(30),"test2@gmail.com"),
                createTestRecord("CT" + System.currentTimeMillis() + "004", TrackingState.DELIVERED, "Adana Dağıtım Merkezi", "delivery_agent", now.minusMinutes(15),"test3@gmail.com"),
                createTestRecord("CT" + System.currentTimeMillis() + "005", TrackingState.IN_TRANSIT, "Bursa Transfer", "system", now.minusMinutes(45),"test4@gmail.com")
        );

        List<TrackingRecord> savedRecords = repository.saveAll(testRecords);

        savedRecords.forEach(record -> {
            trackingCacheService.cacheTrackingInfo(record.getShipmentId(), record); // Cache'e yaz
            kafkaTemplate.send("shipment-status-events", new TrackingEvent(
                    record.getShipmentId(),
                    record.getStatus(),
                    record.getLocation(),
                    record.getUpdatedBy(),
                    record.getUpdatedAt(),
                    record.getReceiverEmail(),
                    record.getReceiverFullName(),
                    record.getSenderUserId(),
                    record.getCompanyId(),
                    record.getCarrierUserId()
            ));
        });

        return Map.of(
                "message", "Test tracking verisi başarıyla oluşturuldu",
                "createdRecords", savedRecords.size(),
                "trackingNumbers", savedRecords.stream().map(TrackingRecord::getShipmentId).toList()
        );
    }

    private TrackingRecord createTestRecord(String trackingNumber, TrackingState status, String location, String updatedBy, LocalDateTime updatedAt,String receiverEmail) {
        TrackingRecord record = new TrackingRecord();
        record.setShipmentId(trackingNumber);
        record.setStatus(status);
        record.setLocation(location);
        record.setUpdatedBy(updatedBy);
        record.setUpdatedAt(updatedAt);
        record.setReceiverEmail(receiverEmail);
        return record;
    }

    private TrackingHistoryResponse toResponse(TrackingRecord record) {
        return new TrackingHistoryResponse(
                record.getShipmentId(),
                record.getStatus(),
                record.getLocation(),
                record.getUpdatedBy(),
                record.getUpdatedAt(),
                record.getReceiverEmail(),
                record.getReceiverFullName(),
                record.getSenderUserId(),
                record.getCompanyId(),
                record.getCarrierUserId()
        );
    }


}
