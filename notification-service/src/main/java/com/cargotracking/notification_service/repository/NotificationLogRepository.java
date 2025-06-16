package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationLog repository interface
 * MongoDB ile bildirim loglarını yönetir
 */
@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    
    /**
     * Kullanıcı ID'sine göre notification loglarını bulur
     */
    List<NotificationLog> findByUserId(Long userId);
    
    /**
     * Tracking number'a göre notification loglarını bulur
     */
    List<NotificationLog> findByTrackingNumber(String trackingNumber);
    
    /**
     * Shipment ID'sine göre notification loglarını bulur
     */
    List<NotificationLog> findByShipmentId(Long shipmentId);
    
    /**
     * Status'a göre notification loglarını bulur
     */
    List<NotificationLog> findByStatus(String status);
    
    /**
     * Belirli bir tarih aralığındaki notification loglarını bulur
     */
    List<NotificationLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Başarısız ve retry edilebilir notification'ları bulur
     */
    @Query("{'status': 'FAILED', 'retryCount': {$lt: ?0}}")
    List<NotificationLog> findFailedNotificationsForRetry(int maxRetries);
    
    /**
     * Kullanıcı ID ve notification channel'a göre logları bulur
     */
    List<NotificationLog> findByUserIdAndNotificationChannel(Long userId, String notificationChannel);
    
    /**
     * Belirli bir kullanıcının son notification loglarını sayfalı olarak getirir
     */
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Belirli bir tracking number için son notification loglarını getirir
     */
    Page<NotificationLog> findByTrackingNumberOrderByCreatedAtDesc(String trackingNumber, Pageable pageable);
} 