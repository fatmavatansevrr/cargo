package com.cargotracking.shipment_service.service;


import com.cargotracking.shipment_service.dto.CreateShipmentRequest;
import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.dto.UpdateShipmentRequest;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ShipmentService {

    ShipmentResponse createShipment(CreateShipmentRequest request);

    ShipmentResponse updateShipment(Long id, UpdateShipmentRequest request);

    ShipmentResponse getShipmentById(Long id);

    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);

    Page<ShipmentResponse> getAllShipments(Pageable pageable);

    Page<ShipmentResponse> getShipmentsBySender(Long senderId, Pageable pageable);

    Page<ShipmentResponse> getShipmentsByReceiver(Long receiverId, Pageable pageable);

    Page<ShipmentResponse> getShipmentsByUser(Long userId, Pageable pageable);

    Page<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable);

    ShipmentResponse updateShipmentStatus(Long id, ShipmentStatus status);

    void deleteShipment(Long id);

    List<ShipmentResponse> getShipmentsInDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<ShipmentResponse> getOverdueShipments();

    Long getShipmentCountBySenderAndStatus(Long senderId, ShipmentStatus status);
}