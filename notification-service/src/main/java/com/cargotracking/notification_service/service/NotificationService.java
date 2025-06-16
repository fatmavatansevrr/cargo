package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.model.NotificationTemplate;
import com.cargotracking.notification_service.repository.NotificationRepository;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import com.cargotracking.notification_service.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * NotificationService - Ana bildirim servisi
 * Event-driven bildirim gönderimi ve yönetimi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final PushNotificationService pushNotificationService;
    
    /**
     * Shipment event'i için bildirim oluştur ve gönder
     */
    public void processShipmentEvent(ShipmentEvent event) {
        log.info("Processing shipment event: {}", event.getEventType());
        
        try {
            // Event türüne göre bildirim türünü belirle
            Notification.NotificationType notificationType = mapShipmentEventToNotificationType(event.getEventType());
            
            // Kullanıcı tercihlerini al
            Optional<NotificationPreference> preference = preferenceRepository.findByUserId(event.getSenderUserId());
            
            // Her kanal için bildirim gönder
            if (preference.isPresent()) {
                sendNotificationsForEvent(event, notificationType, preference.get());
            } else {
                // Varsayılan tercihlerle bildirim gönder
                sendNotificationsWithDefaults(event, notificationType);
            }
            
        } catch (Exception e) {
            log.error("Error processing shipment event: {}", event.getEventType(), e);
        }
    }
    
    /**
     * Tracking event'i için bildirim oluştur ve gönder
     */
    public void processTrackingEvent(TrackingEvent event) {
        log.info("Processing tracking event: {}", event.getEventType());
        
        try {
            Notification.NotificationType notificationType = mapTrackingEventToNotificationType(event.getEventType());
            
            Optional<NotificationPreference> preference = preferenceRepository.findByUserId(event.getUserId());
            
            if (preference.isPresent()) {
                sendNotificationsForTrackingEvent(event, notificationType, preference.get());
            } else {
                sendTrackingNotificationsWithDefaults(event, notificationType);
            }
            
        } catch (Exception e) {
            log.error("Error processing tracking event: {}", event.getEventType(), e);
        }
    }
    
    /**
     * Manuel bildirim gönderimi
     */
    public Notification sendNotification(Notification notification) {
        try {
            // Template varsa uygula
            applyTemplate(notification);
            
            // Bildirim kaydı oluştur
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStatus(Notification.NotificationStatus.PENDING);
            Notification saved = notificationRepository.save(notification);
            
            // Kanala göre gönder
            boolean sent = sendToChannel(saved);
            
            // Durumu güncelle
            saved.setStatus(sent ? Notification.NotificationStatus.SENT : Notification.NotificationStatus.FAILED);
            saved.setSentAt(sent ? LocalDateTime.now() : null);
            
            return notificationRepository.save(saved);
            
        } catch (Exception e) {
            log.error("Error sending notification", e);
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            return notificationRepository.save(notification);
        }
    }
    
    /**
     * Kullanıcının bildirimlerini al
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    /**
     * Okunmamış bildirim sayısı
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.SENT);
    }
    
    /**
     * Bildirimi okundu olarak işaretle
     */
    public void markAsRead(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            Notification n = notification.get();
            n.setStatus(Notification.NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }
    
    /**
     * Başarısız bildirimleri yeniden dene
     */
    public void retryFailedNotifications() {
        List<Notification> failedNotifications = notificationRepository.findFailedNotificationsForRetry(3);
        
        for (Notification notification : failedNotifications) {
            try {
                boolean sent = sendToChannel(notification);
                notification.setRetryCount(notification.getRetryCount() + 1);
                
                if (sent) {
                    notification.setStatus(Notification.NotificationStatus.SENT);
                    notification.setSentAt(LocalDateTime.now());
                    notification.setErrorMessage(null);
                } else if (notification.getRetryCount() >= notification.getMaxRetries()) {
                    log.warn("Max retry attempts reached for notification: {}", notification.getId());
                }
                
                notificationRepository.save(notification);
                
            } catch (Exception e) {
                log.error("Error retrying notification: {}", notification.getId(), e);
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
            }
        }
    }
    
    // Private helper methods
    
    private void sendNotificationsForEvent(ShipmentEvent event, Notification.NotificationType type, NotificationPreference preference) {
        for (Notification.NotificationChannel channel : Notification.NotificationChannel.values()) {
            if (preference.isChannelEnabledForType(type, channel)) {
                Notification notification = createNotificationFromShipmentEvent(event, type, channel, preference);
                sendNotification(notification);
            }
        }
    }
    
    private void sendNotificationsWithDefaults(ShipmentEvent event, Notification.NotificationType type) {
        // Varsayılan olarak sadece email gönder
        NotificationPreference defaultPreference = createDefaultPreference(event.getSenderUserId(), event.getCustomerEmail());
        Notification notification = createNotificationFromShipmentEvent(event, type, Notification.NotificationChannel.EMAIL, defaultPreference);
        sendNotification(notification);
    }
    
    private void sendNotificationsForTrackingEvent(TrackingEvent event, Notification.NotificationType type, NotificationPreference preference) {
        for (Notification.NotificationChannel channel : Notification.NotificationChannel.values()) {
            if (preference.isChannelEnabledForType(type, channel)) {
                Notification notification = createNotificationFromTrackingEvent(event, type, channel, preference);
                sendNotification(notification);
            }
        }
    }
    
    private void sendTrackingNotificationsWithDefaults(TrackingEvent event, Notification.NotificationType type) {
        NotificationPreference defaultPreference = createDefaultPreference(event.getUserId(), event.getCustomerEmail());
        Notification notification = createNotificationFromTrackingEvent(event, type, Notification.NotificationChannel.EMAIL, defaultPreference);
        sendNotification(notification);
    }
    
    private Notification createNotificationFromShipmentEvent(ShipmentEvent event, Notification.NotificationType type, 
                                                           Notification.NotificationChannel channel, NotificationPreference preference) {
        Notification notification = new Notification();
        notification.setUserId(event.getSenderUserId());
        notification.setTrackingNumber(event.getTrackingNumber());
        notification.setShipmentId(event.getShipmentId());
        notification.setType(type);
        notification.setChannel(channel);
        notification.setEventType(event.getEventType());
        notification.setEventSource(event.getEventSource());
        
        // Recipient bilgilerini ayarla
        switch (channel) {
            case EMAIL -> notification.setRecipient(preference.getEmail());
            case SMS -> notification.setRecipient(preference.getPhoneNumber());
            case PUSH_NOTIFICATION, IN_APP -> notification.setRecipient(event.getSenderUserId().toString());
        }
        
        // Template data hazırla
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("trackingNumber", event.getTrackingNumber());
        templateData.put("status", event.getStatus());
        templateData.put("carrierName", event.getCarrierName());
        notification.setTemplateData(templateData);
        
        return notification;
    }
    
    private Notification createNotificationFromTrackingEvent(TrackingEvent event, Notification.NotificationType type,
                                                           Notification.NotificationChannel channel, NotificationPreference preference) {
        Notification notification = new Notification();
        notification.setUserId(event.getUserId());
        notification.setTrackingNumber(event.getTrackingNumber());
        notification.setShipmentId(event.getShipmentId());
        notification.setType(type);
        notification.setChannel(channel);
        notification.setEventType(event.getEventType());
        notification.setEventSource(event.getEventSource());
        
        switch (channel) {
            case EMAIL -> notification.setRecipient(preference.getEmail());
            case SMS -> notification.setRecipient(preference.getPhoneNumber());
            case PUSH_NOTIFICATION, IN_APP -> notification.setRecipient(event.getUserId().toString());
        }
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("customerName", event.getCustomerName());
        templateData.put("trackingNumber", event.getTrackingNumber());
        templateData.put("currentStatus", event.getCurrentStatus());
        templateData.put("previousStatus", event.getPreviousStatus());
        templateData.put("location", event.getLocation());
        templateData.put("carrierName", event.getCarrierName());
        notification.setTemplateData(templateData);
        
        return notification;
    }
    
    private NotificationPreference createDefaultPreference(Long userId, String email) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEmail(email);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setPushEnabled(false);
        preference.setInAppEnabled(true);
        return preference;
    }
    
    private void applyTemplate(Notification notification) {
        Optional<NotificationTemplate> template = templateRepository.findByNotificationTypeAndChannelAndActiveTrue(
                notification.getType(), notification.getChannel());
        
        if (template.isPresent()) {
            NotificationTemplate t = template.get();
            notification.setTitle(processTemplate(t.getTitle(), notification.getTemplateData()));
            notification.setMessage(processTemplate(t.getBody(), notification.getTemplateData()));
        }
    }
    
    private String processTemplate(String template, Map<String, Object> data) {
        if (template == null || data == null) return template;
        
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", String.valueOf(entry.getValue()));
        }
        return result;
    }
    
    private boolean sendToChannel(Notification notification) {
        return switch (notification.getChannel()) {
            case EMAIL -> emailService.sendEmail(notification);
            case SMS -> smsService.sendSms(notification);
            case PUSH_NOTIFICATION -> pushNotificationService.sendPushNotification(notification);
            case IN_APP -> true; // In-app notifications are stored in database only
        };
    }
    
    private Notification.NotificationType mapShipmentEventToNotificationType(String eventType) {
        return switch (eventType) {
            case "shipment.created" -> Notification.NotificationType.SHIPMENT_CREATED;
            case "shipment.canceled" -> Notification.NotificationType.SHIPMENT_CANCELLED;
            default -> Notification.NotificationType.SYSTEM_ALERT;
        };
    }
    
    private Notification.NotificationType mapTrackingEventToNotificationType(String eventType) {
        return switch (eventType) {
            case "status.changed" -> Notification.NotificationType.STATUS_CHANGED;
            case "delivery.completed" -> Notification.NotificationType.DELIVERY_COMPLETED;
            case "delivery.failed" -> Notification.NotificationType.DELIVERY_FAILED;
            default -> Notification.NotificationType.SYSTEM_ALERT;
        };
    }
} 