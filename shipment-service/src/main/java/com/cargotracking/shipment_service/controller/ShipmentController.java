package com.cargotracking.shipment_service.controller;


import com.cargotracking.shipment_service.dto.CreateShipmentRequest;
import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.dto.UpdateShipmentRequest;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import com.cargotracking.shipment_service.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
@Slf4j
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        log.info("Creating new shipment request received");
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponse> updateShipment(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateShipmentRequest request) {
        log.info("Updating shipment with id: {}", id);
        ShipmentResponse response = shipmentService.updateShipment(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(@PathVariable Long id) {
        log.info("Getting shipment by id: {}", id);
        ShipmentResponse response = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("Getting shipment by tracking number: {}", trackingNumber);
        ShipmentResponse response = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getAllShipments(@PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting all shipments with pagination");
        Page<ShipmentResponse> response = shipmentService.getAllShipments(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sender/{senderId}")
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsBySender(@PathVariable Long senderId,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting shipments by sender id: {}", senderId);
        Page<ShipmentResponse> response = shipmentService.getShipmentsBySender(senderId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/receiver/{receiverId}")
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsByReceiver(@PathVariable Long receiverId,
                                                                         @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting shipments by receiver id: {}", receiverId);
        Page<ShipmentResponse> response = shipmentService.getShipmentsByReceiver(receiverId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsByUser(@PathVariable Long userId,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting shipments by user id: {}", userId);
        Page<ShipmentResponse> response = shipmentService.getShipmentsByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ShipmentResponse>> getShipmentsByStatus(@PathVariable ShipmentStatus status,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting shipments by status: {}", status);
        Page<ShipmentResponse> response = shipmentService.getShipmentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(@PathVariable Long id,
                                                                 @RequestParam ShipmentStatus status) {
        log.info("Updating shipment status for id: {} to status: {}", id, status);
        ShipmentResponse response = shipmentService.updateShipmentStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(@PathVariable Long id) {
        log.info("Deleting shipment with id: {}", id);
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Getting shipments between {} and {}", startDate, endDate);
        List<ShipmentResponse> response = shipmentService.getShipmentsInDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<ShipmentResponse>> getOverdueShipments() {
        log.info("Getting overdue shipments");
        List<ShipmentResponse> response = shipmentService.getOverdueShipments();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getShipmentCountBySenderAndStatus(@RequestParam Long senderId,
                                                                  @RequestParam ShipmentStatus status) {
        log.info("Getting shipment count for sender: {} with status: {}", senderId, status);
        Long count = shipmentService.getShipmentCountBySenderAndStatus(senderId, status);
        return ResponseEntity.ok(count);
    }
}
