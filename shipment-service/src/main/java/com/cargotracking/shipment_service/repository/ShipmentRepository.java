package com.cargotracking.shipment_service.repository;


import com.cargotracking.shipment_service.model.Shipment;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Page<Shipment> findBySenderId(Long senderId, Pageable pageable);

    Page<Shipment> findByReceiverId(Long receiverId, Pageable pageable);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.senderId = :userId OR s.receiverId = :userId")
    Page<Shipment> findByUserIdAsShipperOrReceiver(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM Shipment s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<Shipment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.senderId = :senderId AND s.status = :status")
    Long countBySenderIdAndStatus(@Param("senderId") Long senderId, @Param("status") ShipmentStatus status);

    @Query("SELECT s FROM Shipment s WHERE s.estimatedDeliveryDate < :date AND s.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED')")
    List<Shipment> findOverdueShipments(@Param("date") LocalDateTime date);

    boolean existsByTrackingNumber(String trackingNumber);
}