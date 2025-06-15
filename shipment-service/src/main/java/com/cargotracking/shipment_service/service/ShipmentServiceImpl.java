package com.cargotracking.shipment_service.service;



import com.cargotracking.shipment_service.dto.CreateShipmentRequest;
import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.dto.UpdateShipmentRequest;
import com.cargotracking.shipment_service.event.ShipmentCreatedEvent;
import com.cargotracking.shipment_service.event.ShipmentStatusChangedEvent;
import com.cargotracking.shipment_service.model.Shipment;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import com.cargotracking.shipment_service.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentMapper shipmentMapper;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final EventPublisherService eventPublisherService;

    @Override
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        log.info("Creating new shipment for sender: {}", request.getSenderId());

        String trackingNumber = generateUniqueTrackingNumber();

        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setTrackingNumber(trackingNumber);
        shipment.setStatus(ShipmentStatus.CREATED);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());

        Shipment savedShipment = shipmentRepository.save(shipment);

        // Publish shipment created event
        ShipmentCreatedEvent event = shipmentMapper.toCreatedEvent(savedShipment);
        eventPublisherService.publishShipmentCreatedEvent(event);

        log.info("Shipment created successfully with tracking number: {}", trackingNumber);
        return shipmentMapper.toResponse(savedShipment);
    }

    @Override
    public ShipmentResponse updateShipment(Long id, UpdateShipmentRequest request) {
        log.info("Updating shipment with id: {}", id);

        Shipment shipment = findShipmentById(id);
        ShipmentStatus oldStatus = shipment.getStatus();

        shipmentMapper.updateEntityFromRequest(request, shipment);
        shipment.setUpdatedAt(LocalDateTime.now());

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // If status changed, publish status change event
        if (request.getStatus() != null && !oldStatus.equals(request.getStatus())) {
            ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                    .shipmentId(updatedShipment.getId())
                    .trackingNumber(updatedShipment.getTrackingNumber())
                    .oldStatus(oldStatus)
                    .newStatus(request.getStatus())
                    .senderEmail(updatedShipment.getSenderEmail())
                    .receiverEmail(updatedShipment.getReceiverEmail())
                    .changedAt(LocalDateTime.now())
                    .build();

            eventPublisherService.publishShipmentStatusChangedEvent(event);
        }

        log.info("Shipment updated successfully: {}", id);
        return shipmentMapper.toResponse(updatedShipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentById(Long id) {
        log.info("Fetching shipment by id: {}", id);
        Shipment shipment = findShipmentById(id);
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        log.info("Fetching shipment by tracking number: {}", trackingNumber);
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with tracking number: " + trackingNumber));
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getAllShipments(Pageable pageable) {
        log.info("Fetching all shipments with pagination");
        Page<Shipment> shipments = shipmentRepository.findAll(pageable);
        return shipments.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsBySender(Long senderId, Pageable pageable) {
        log.info("Fetching shipments by sender id: {}", senderId);
        Page<Shipment> shipments = shipmentRepository.findBySenderId(senderId, pageable);
        return shipments.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByReceiver(Long receiverId, Pageable pageable) {
        log.info("Fetching shipments by receiver id: {}", receiverId);
        Page<Shipment> shipments = shipmentRepository.findByReceiverId(receiverId, pageable);
        return shipments.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByUser(Long userId, Pageable pageable) {
        log.info("Fetching shipments by user id: {}", userId);
        Page<Shipment> shipments = shipmentRepository.findByUserIdAsShipperOrReceiver(userId, pageable);
        return shipments.map(shipmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getShipmentsByStatus(ShipmentStatus status, Pageable pageable) {
        log.info("Fetching shipments by status: {}", status);
        Page<Shipment> shipments = shipmentRepository.findByStatus(status, pageable);
        return shipments.map(shipmentMapper::toResponse);
    }

    @Override
    public ShipmentResponse updateShipmentStatus(Long id, ShipmentStatus status) {
        log.info("Updating shipment status for id: {} to status: {}", id, status);

        Shipment shipment = findShipmentById(id);
        ShipmentStatus oldStatus = shipment.getStatus();

        shipment.setStatus(status);
        shipment.setUpdatedAt(LocalDateTime.now());

        if (status == ShipmentStatus.DELIVERED) {
            shipment.setActualDeliveryDate(LocalDateTime.now());
        }

        Shipment updatedShipment = shipmentRepository.save(shipment);

        // Publish status change event
        ShipmentStatusChangedEvent event = ShipmentStatusChangedEvent.builder()
                .shipmentId(updatedShipment.getId())
                .trackingNumber(updatedShipment.getTrackingNumber())
                .oldStatus(oldStatus)
                .newStatus(status)
                .senderEmail(updatedShipment.getSenderEmail())
                .receiverEmail(updatedShipment.getReceiverEmail())
                .changedAt(LocalDateTime.now())
                .build();

        eventPublisherService.publishShipmentStatusChangedEvent(event);

        log.info("Shipment status updated successfully for id: {}", id);
        return shipmentMapper.toResponse(updatedShipment);
    }

    @Override
    public void deleteShipment(Long id) {
        log.info("Deleting shipment with id: {}", id);
        Shipment shipment = findShipmentById(id);

        // Only allow deletion if shipment is in CREATED status
        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new IllegalStateException("Cannot delete shipment that is not in CREATED status");
        }

        shipmentRepository.delete(shipment);
        log.info("Shipment deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getShipmentsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching shipments between {} and {}", startDate, endDate);
        List<Shipment> shipments = shipmentRepository.findByCreatedAtBetween(startDate, endDate);
        return shipments.stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getOverdueShipments() {
        log.info("Fetching overdue shipments");
        List<Shipment> shipments = shipmentRepository.findOverdueShipments(LocalDateTime.now());
        return shipments.stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getShipmentCountBySenderAndStatus(Long senderId, ShipmentStatus status) {
        log.info("Getting shipment count for sender: {} with status: {}", senderId, status);
        return shipmentRepository.countBySenderIdAndStatus(senderId, status);
    }

    private Shipment findShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with id: " + id));
    }

    private String generateUniqueTrackingNumber() {
        String trackingNumber;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            trackingNumber = trackingNumberGenerator.generate();
            attempts++;

            if (attempts > maxAttempts) {
                throw new RuntimeException("Unable to generate unique tracking number after " + maxAttempts + " attempts");
            }
        } while (shipmentRepository.existsByTrackingNumber(trackingNumber));

        return trackingNumber;
    }
}
