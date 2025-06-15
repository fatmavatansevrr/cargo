package com.cargotracking.tracking_service.repository;

import com.cargotracking.tracking_service.model.TrackingRecord;
import com.cargotracking.tracking_service.model.TrackingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TrackingStatusRepository extends MongoRepository<TrackingRecord, String> {
    List<TrackingRecord> findByShipmentIdOrderByUpdatedAtDesc(String shipmentId);


}

