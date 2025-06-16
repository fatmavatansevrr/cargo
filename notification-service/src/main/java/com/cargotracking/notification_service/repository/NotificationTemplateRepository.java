package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * NotificationTemplateRepository - MongoDB operations for notification templates
 */
@Repository
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {
    
    Optional<NotificationTemplate> findByNotificationTypeAndChannelAndActiveTrue(
            Notification.NotificationType notificationType, 
            Notification.NotificationChannel channel
    );
    
    Optional<NotificationTemplate> findByTemplateNameAndActiveTrue(String templateName);
    
    List<NotificationTemplate> findByNotificationTypeAndActiveTrue(Notification.NotificationType notificationType);
    
    List<NotificationTemplate> findByChannelAndActiveTrue(Notification.NotificationChannel channel);
    
    List<NotificationTemplate> findByLanguageAndActiveTrue(String language);
    
    List<NotificationTemplate> findByActiveTrue();
} 