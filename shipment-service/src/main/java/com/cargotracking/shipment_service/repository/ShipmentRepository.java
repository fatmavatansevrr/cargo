package com.cargotracking.shipment_service.repository;

import com.cargotracking.shipment_service.model.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Shipment Repository
 * Requirements: FR-SM-001, FR-SM-003, FR-SM-004
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    
    /**
     * Takip numarasına göre gönderi bulma
     * Requirements: FR-SM-003 - Gönderi detaylarını görüntüleme
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    /**
     * Gönderici kullanıcı ID'sine göre gönderileri bulma
     */
    Page<Shipment> findBySenderCustomerId(Long senderCustomerId, Pageable pageable);
    
    /**
     * Durum bazında gönderileri bulma
     */
    Page<Shipment> findByStatus(Shipment.ShipmentStatus status, Pageable pageable);
    
    /**
     * Belirli durumlardan birinde olan gönderileri bulma
     */
    List<Shipment> findByStatusIn(List<Shipment.ShipmentStatus> statuses);
    
    /**
     * Taşıyıcıya atanmış gönderileri bulma
     */
    Page<Shipment> findByAssignedCarrierId(Long carrierId, Pageable pageable);
    
    /**
     * Belirli tarih aralığında oluşturulan gönderileri bulma
     */
    @Query("SELECT s FROM Shipment s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<Shipment> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * Gönderici ID ve durum bazında gönderileri bulma
     */
    Page<Shipment> findBySenderCustomerIdAndStatus(Long senderCustomerId, 
                                              Shipment.ShipmentStatus status, 
                                              Pageable pageable);
    
    /**
     * Takip numarasının benzersiz olup olmadığını kontrol etme
     */
    boolean existsByTrackingNumber(String trackingNumber);
    
    /**
     * Aktif gönderileri sayma (teslim edilmemiş)
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status NOT IN ('DELIVERED', 'CANCELLED', 'RETURNED_TO_SENDER')")
    long countActiveShipments();
    
    /**
     * Belirli hizmet tipindeki gönderileri bulma
     */
    Page<Shipment> findByServiceType(Shipment.ServiceType serviceType, Pageable pageable);
    
    /**
     * Belirli carrier'a atanmış ve belirli durumdaki gönderileri sayma
     * Otomatik carrier atama için kullanılır
     */
    Long countByAssignedCarrierIdAndStatus(Long carrierId, Shipment.ShipmentStatus status);
    
    /**
     * Belirli carrier'a atanmış ve belirli durumdaki gönderileri listeleme
     * Carrier dashboard için kullanılır
     */
    List<Shipment> findByAssignedCarrierIdAndStatus(Long carrierId, Shipment.ShipmentStatus status);
} 