package com.cargotracking.tracking_service.service;

import com.cargotracking.tracking_service.model.TrackingStatus;
import com.cargotracking.tracking_service.repository.TrackingStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackingService {
    private final TrackingStatusRepository repository;

    public void updateStatus(String shipmentId, String status) {
        TrackingStatus ts = new TrackingStatus(
                UUID.randomUUID().toString(),
                shipmentId,
                status,
                LocalDateTime.now()
        );
        repository.save(ts);
    }

    public List<TrackingStatus> getHistory(String shipmentId) {
        return repository.findByShipmentId(shipmentId);
    }
}
