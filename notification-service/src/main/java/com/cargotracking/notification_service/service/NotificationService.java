package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.client.UserServiceClient;
import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.repository.NotificationRepository;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Basit NotificationService - Sadece temel işlevler
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final UserServiceClient userServiceClient;

    /**
     * Shipment event işle
     */
    @Async
    public void processShipmentEvent(ShipmentEvent event) {
        log.info("📦 Processing shipment event: {}", event.getEventType());
        
        String customerEmail = extractCustomerEmail(event);
        if (customerEmail != null) {
            String subject = "Shipment Update: " + event.getEventType();
            String message = "Your shipment " + event.getTrackingNumber() + " has been " + event.getEventType();
            
            sendEmailNotification(customerEmail, subject, message);
        }
    }

    /**
     * Tracking event işle
     */
    @Async
    public void processTrackingEvent(TrackingEvent event) {
        log.info("📍 Processing tracking event: {}", event.getCurrentStatus());
        
        String customerEmail = event.getCustomerEmail();
        if (customerEmail != null) {
            String subject = "Status Update: " + event.getCurrentStatus();
            String message = "Your shipment " + event.getTrackingNumber() + " is now: " + event.getCurrentStatus();
            
            sendEmailNotification(customerEmail, subject, message);
        }
    }

    /**
     * Kullanıcının email tercihlerini al
     */
    public NotificationPreference getUserPreferences(Long userId) {
        // Önce user'ın var olup olmadığını kontrol et
        if (!userServiceClient.userExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        return preferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreferences(userId));
    }

    /**
     * Email tercihlerini güncelle
     */
    public NotificationPreference updateEmailPreference(Long userId, boolean emailEnabled) {
        // Önce user'ın var olup olmadığını kontrol et
        if (!userServiceClient.userExists(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        
        NotificationPreference preference = getUserPreferences(userId);
        preference.setEmailEnabled(emailEnabled);
        preference.setUpdatedAt(LocalDateTime.now());
        
        NotificationPreference saved = preferenceRepository.save(preference);
        log.info("📧 Updated email preference for user {}: {}", userId, emailEnabled);
        return saved;
    }

    /**
     * Kullanıcının bildirimlerini al
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * Manual notification gönder (Test için)
     */
    public Notification sendManualNotification(String userId, String type, String subject, String content) {
        log.info("📤 Sending manual notification: userId={}, type={}, subject={}", userId, type, subject);
        
        try {
            Long userIdLong = Long.valueOf(userId);
            
            // Önce user'ın var olup olmadığını kontrol et (Geçici olarak devre dışı)
            /*
            if (!userServiceClient.userExists(userIdLong)) {
                throw new IllegalArgumentException("User not found with ID: " + userId);
            }
            */
            
            // Type validation
            Notification.NotificationChannel channel;
            try {
                channel = Notification.NotificationChannel.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid notification type: " + type + ". Valid types: EMAIL, SMS, PUSH_NOTIFICATION, IN_APP");
            }
            
            // User'ın gerçek email adresini al
            String userEmail = userServiceClient.getUserEmail(userIdLong);
            String recipient = userEmail != null ? userEmail : userId + "@test.com";
            
            Notification notification = new Notification();
            notification.setUserId(userIdLong);
            notification.setRecipient(recipient);
            notification.setTitle(subject);
            notification.setMessage(content);
            notification.setChannel(channel);
            notification.setType(Notification.NotificationType.SYSTEM_ALERT);
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setCreatedAt(LocalDateTime.now());
            
            Notification saved = notificationRepository.save(notification);
            
            // Channel'a göre gönder
            switch (channel) {
                case EMAIL:
                    emailService.sendEmail(notification.getRecipient(), subject, content);
                    break;
                case SMS:
                    smsService.sendSms(notification.getRecipient(), content);
                    break;
                case PUSH_NOTIFICATION:
                    log.info("📱 Push notification: {}", subject);
                    break;
                case IN_APP:
                    log.info("📱 In-app notification: {}", subject);
                    break;
            }
            
            saved.markAsSent();
            saved = notificationRepository.save(saved);
            
            log.info("✅ Manual notification sent successfully: id={}", saved.getId());
            return saved;
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Failed to send manual notification", e);
            throw new RuntimeException("Manual notification gönderilirken hata oluştu", e);
        }
    }

    // Private helper methods
    
    private void sendEmailNotification(String to, String subject, String message) {
        try {
            emailService.sendEmail(to, subject, message);
            saveNotificationRecord(to, subject, message, "EMAIL");
            log.info("✅ Email notification sent to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email notification", e);
        }
    }
    
    private void saveNotificationRecord(String recipient, String subject, String message, String channel) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setTitle(subject);
            notification.setMessage(message);
            notification.setChannel(Notification.NotificationChannel.valueOf(channel));
            notification.setType(Notification.NotificationType.STATUS_CHANGED);
            notification.setStatus(Notification.NotificationStatus.SENT);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSentAt(LocalDateTime.now());
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("❌ Failed to save notification record", e);
        }
    }
    
    private String extractCustomerEmail(ShipmentEvent event) {
        if (event.getRecipientEmail() != null) {
            return event.getRecipientEmail();
        }
        if (event.getCustomerEmail() != null) {
            return event.getCustomerEmail();
        }
        return null;
    }
    
    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setPushEnabled(false);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }
} 