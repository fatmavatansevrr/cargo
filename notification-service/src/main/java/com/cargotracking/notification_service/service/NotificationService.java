package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.StatusEvent;
import com.cargotracking.notification_service.model.NotificationLog;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.repository.NotificationLogRepository;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Ana notification service
 * FR-NT-001: Gönderi durumu değişikliklerinde otomatik bildirimler
 * FR-NT-002: Kafka olaylarını işler
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final EmailService emailService;
    private final NotificationPreferenceRepository preferenceRepository;
    private final NotificationLogRepository logRepository;
    
    /**
     * Shipment event'i işler ve uygun bildirimleri gönderir
     * FR-NT-002: Kafka'dan gelen shipment.* eventlerini işler
     */
    @Async
    public CompletableFuture<Void> processShipmentEvent(ShipmentEvent event) {
        try {
            log.info("Shipment event işleniyor: {} - {}", event.getEventType(), event.getTrackingNumber());
            
            // Event'ten notification bilgilerini çıkar
            event.extractDataFromEventData();
            
            // Gönderici için bildirim gönder
            if (event.getSenderUserId() != null) {
                processNotificationForUser(
                    event.getSenderUserId(),
                    event.getEventType(),
                    event.getTrackingNumber(),
                    event.getStatus(),
                    event.getShipmentId(),
                    true // isSender
                );
            }
            
            // Müşteri için bildirim gönder (eğer farklı ise)
            if (event.getCustomerEmail() != null && !event.getCustomerEmail().isEmpty()) {
                processNotificationForEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event.getEventType(),
                    event.getTrackingNumber(),
                    event.getStatus(),
                    event.getShipmentId(),
                    false // isSender
                );
            }
            
            log.info("Shipment event başarıyla işlendi: {}", event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Shipment event işleme hatası: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Status event'i işler ve uygun bildirimleri gönderir
     * FR-NT-002: Kafka'dan gelen status.updated eventlerini işler
     */
    @Async
    public CompletableFuture<Void> processStatusEvent(StatusEvent event) {
        try {
            log.info("Status event işleniyor: {} - {} -> {}", 
                    event.getTrackingNumber(), event.getPreviousStatus(), event.getNewStatus());
            
            // Bildirim gerekli mi kontrol et
            if (!event.requiresNotification()) {
                log.debug("Status değişikliği bildirim gerektirmiyor: {}", event.getTrackingNumber());
                return CompletableFuture.completedFuture(null);
            }
            
            // Müşteri için bildirim gönder
            if (event.hasCustomerNotificationInfo()) {
                processStatusNotificationForEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event,
                    false // isSender
                );
            }
            
            // Gönderici için bildirim gönder
            if (event.hasSenderNotificationInfo()) {
                processStatusNotificationForEmail(
                    event.getSenderEmail(),
                    event.getSenderName(),
                    event,
                    true // isSender
                );
            }
            
            log.info("Status event başarıyla işlendi: {}", event.getTrackingNumber());
            
        } catch (Exception e) {
            log.error("Status event işleme hatası: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Kullanıcı ID'si ile notification işlemi
     */
    private void processNotificationForUser(Long userId, String eventType, String trackingNumber, 
                                           String status, Long shipmentId, boolean isSender) {
        try {
            // Kullanıcının notification tercihlerini al
            NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(createDefaultPreference(userId));
            
            // Bu event tipi için bildirim aktif mi kontrol et
            if (!preference.isNotificationEnabledForEventType(eventType)) {
                log.debug("Event tipi {} için bildirim devre dışı. User: {}", eventType, userId);
                return;
            }
            
            // Email bildirimi gönder
            if (preference.canSendEmail()) {
                sendEmailNotification(preference, eventType, trackingNumber, status, shipmentId, isSender);
            }
            
            // SMS bildirimi gönder (gelecekte implementasyonu)
            if (preference.canSendSms()) {
                log.info("SMS bildirimi henüz desteklenmiyor. User: {}", userId);
            }
            
        } catch (Exception e) {
            log.error("Kullanıcı notification işleme hatası: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Email adresi ile notification işlemi
     */
    private void processNotificationForEmail(String email, String customerName, String eventType, 
                                           String trackingNumber, String status, Long shipmentId, boolean isSender) {
        try {
            // Email ile preference ara, yoksa default oluştur
            NotificationPreference preference = preferenceRepository.findByUserEmail(email)
                .orElse(createDefaultPreferenceForEmail(email, customerName));
            
            // Bu event tipi için bildirim aktif mi kontrol et
            if (!preference.isNotificationEnabledForEventType(eventType)) {
                log.debug("Event tipi {} için bildirim devre dışı. Email: {}", eventType, email);
                return;
            }
            
            // Email bildirimi gönder
            if (preference.canSendEmail()) {
                sendEmailNotification(preference, eventType, trackingNumber, status, shipmentId, isSender);
            }
            
        } catch (Exception e) {
            log.error("Email notification işleme hatası: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Status değişikliği için özel email notification
     */
    private void processStatusNotificationForEmail(String email, String customerName, 
                                                 StatusEvent event, boolean isSender) {
        try {
            // Email ile preference ara
            NotificationPreference preference = preferenceRepository.findByUserEmail(email)
                .orElse(createDefaultPreferenceForEmail(email, customerName));
            
            // Bu status için bildirim aktif mi kontrol et
            if (!preference.isNotificationEnabledForStatus(event.getNewStatus())) {
                log.debug("Status {} için bildirim devre dışı. Email: {}", event.getNewStatus(), email);
                return;
            }
            
            // Email bildirimi gönder
            if (preference.canSendEmail()) {
                sendStatusEmailNotification(preference, event, isSender);
            }
            
        } catch (Exception e) {
            log.error("Status email notification işleme hatası: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Email bildirimi gönderir
     */
    private void sendEmailNotification(NotificationPreference preference, String eventType, 
                                     String trackingNumber, String status, Long shipmentId, boolean isSender) {
        
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setUserId(preference.getUserId());
        notificationLog.setUserEmail(preference.getUserEmail());
        notificationLog.setNotificationChannel("EMAIL");
        notificationLog.setEventType(eventType);
        notificationLog.setTrackingNumber(trackingNumber);
        notificationLog.setShipmentId(shipmentId);
        notificationLog.setShipmentStatus(status);
        notificationLog.setCreatedAt(LocalDateTime.now());
        notificationLog.setStatus("PENDING");
        
        try {
            String subject = emailService.createEmailSubject(trackingNumber, status);
            String content = emailService.createShipmentStatusEmailContent(
                trackingNumber, status, preference.getUserEmail(), "Sistem"
            );
            
            notificationLog.setSubject(subject);
            notificationLog.setMessage(content);
            
            // Email gönder
            CompletableFuture<Boolean> result = emailService.sendEmail(
                preference.getUserEmail(), subject, content
            );
            
            // Sonucu bekle ve log'u güncelle
            result.thenAccept(success -> {
                if (success) {
                    notificationLog.markAsSent();
                } else {
                    notificationLog.markAsFailed("Email gönderme başarısız");
                }
                logRepository.save(notificationLog);
            });
            
        } catch (Exception e) {
            notificationLog.markAsFailed("Email gönderme hatası: " + e.getMessage());
            logRepository.save(notificationLog);
            log.error("Email notification gönderme hatası: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Status değişikliği için email bildirimi gönderir
     */
    private void sendStatusEmailNotification(NotificationPreference preference, StatusEvent event, boolean isSender) {
        
        NotificationLog notificationLog = new NotificationLog();
        notificationLog.setUserId(preference.getUserId());
        notificationLog.setUserEmail(preference.getUserEmail());
        notificationLog.setNotificationChannel("EMAIL");
        notificationLog.setEventType(event.getEventType());
        notificationLog.setTrackingNumber(event.getTrackingNumber());
        notificationLog.setShipmentId(event.getShipmentId());
        notificationLog.setShipmentStatus(event.getNewStatus());
        notificationLog.setCreatedAt(LocalDateTime.now());
        notificationLog.setStatus("PENDING");
        
        try {
            String subject = emailService.createEmailSubject(event.getTrackingNumber(), event.getNewStatus());
            String content = emailService.createShipmentStatusEmailContent(
                event.getTrackingNumber(), 
                event.getNewStatus(), 
                preference.getUserEmail(), 
                event.getCurrentLocation()
            );
            
            notificationLog.setSubject(subject);
            notificationLog.setMessage(content);
            
            // Email gönder
            CompletableFuture<Boolean> result = emailService.sendEmail(
                preference.getUserEmail(), subject, content
            );
            
            // Sonucu bekle ve log'u güncelle
            result.thenAccept(success -> {
                if (success) {
                    notificationLog.markAsSent();
                } else {
                    notificationLog.markAsFailed("Email gönderme başarısız");
                }
                logRepository.save(notificationLog);
            });
            
        } catch (Exception e) {
            notificationLog.markAsFailed("Email gönderme hatası: " + e.getMessage());
            logRepository.save(notificationLog);
            log.error("Status email notification gönderme hatası: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Default notification preference oluşturur
     */
    private NotificationPreference createDefaultPreference(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }
    
    /**
     * Email için default notification preference oluşturur
     */
    private NotificationPreference createDefaultPreferenceForEmail(String email, String customerName) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserEmail(email);
        preference.setCreatedAt(LocalDateTime.now());
        preference.setUpdatedAt(LocalDateTime.now());
        return preference;
    }
} 