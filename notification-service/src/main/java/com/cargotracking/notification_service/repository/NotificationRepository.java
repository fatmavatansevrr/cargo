package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationRepository - MongoDB operations for notifications
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Kullanıcı bazlı sorgular
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Notification.NotificationStatus status);
    
    // Takip numarası bazlı sorgular
    List<Notification> findByTrackingNumberOrderByCreatedAtDesc(String trackingNumber);
    
    // Durum bazlı sorgular
    List<Notification> findByStatusAndCreatedAtBefore(Notification.NotificationStatus status, LocalDateTime cutoffTime);
    
    // Retry işlemleri için
    List<Notification> findByStatusAndRetryCountLessThanEqual(Notification.NotificationStatus status, Integer maxRetries);
    
    // Bildirim türü bazlı sorgular
    List<Notification> findByTypeAndCreatedAtBetween(Notification.NotificationType type, LocalDateTime start, LocalDateTime end);
    
    // Kanal bazlı sorgular
    List<Notification> findByChannelAndStatus(Notification.NotificationChannel channel, Notification.NotificationStatus status);
    
    // Event source bazlı sorgular
    List<Notification> findByEventSourceAndEventType(String eventSource, String eventType);
    
    // Başarısız bildirimler
    @Query("{ 'status': 'FAILED', 'retryCount': { $lt: ?0 } }")
    List<Notification> findFailedNotificationsForRetry(Integer maxRetries);
    
    // Okunmamış bildirimler
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
    
    // Belirli tarih aralığındaki bildirimler
    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<Notification> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // Cleanup için eski bildirimler
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);
} 