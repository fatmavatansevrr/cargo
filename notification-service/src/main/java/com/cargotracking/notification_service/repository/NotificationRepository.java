package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Basit NotificationRepository - Sadece temel işlemler
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    // Kullanıcı bazlı sorgular
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Notification.NotificationStatus status);
    
    // Takip numarası bazlı sorgular
    List<Notification> findByTrackingNumberOrderByCreatedAtDesc(String trackingNumber);
    
    // Durum bazlı sorgular
    List<Notification> findByStatus(Notification.NotificationStatus status);
    
    // Okunmamış bildirimler sayısı
    long countByUserIdAndStatus(Long userId, Notification.NotificationStatus status);
} 