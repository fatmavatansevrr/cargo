package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Kullanıcı bildirim tercihleri modeli
 * FR-NT-004: Kullanıcılar hangi durum değişikliklerinde bildirim alacaklarını yönetebilir
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_preferences")
public class NotificationPreference {
    
    @Id
    private String id;
    
    private Long userId;
    private String userEmail;
    private String userPhone;
    
    // Bildirim kanalları
    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private boolean pushNotificationEnabled = false;
    
    // Hangi durum değişikliklerinde bildirim alınacak
    private Set<String> enabledStatusNotifications = Set.of(
        "PACKAGE_RECEIVED",     // Kargoya Verildi
        "IN_TRANSIT",          // Yolda
        "OUT_FOR_DELIVERY",    // Dağıtıma Çıktı
        "DELIVERED",           // Teslim Edildi
        "DELIVERY_FAILED"      // Teslimat Başarısız
    );
    
    // Hangi event tiplerinde bildirim alınacak
    private Set<String> enabledEventTypes = Set.of(
        "shipment.created",
        "shipment.updated",
        "status.updated"
    );
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Belirli bir status için bildirim aktif mi kontrol eder
     */
    public boolean isNotificationEnabledForStatus(String status) {
        return enabledStatusNotifications.contains(status);
    }
    
    /**
     * Belirli bir event tipi için bildirim aktif mi kontrol eder
     */
    public boolean isNotificationEnabledForEventType(String eventType) {
        return enabledEventTypes.contains(eventType);
    }
    
    /**
     * Email bildirim aktif mi ve email adresi var mı kontrol eder
     */
    public boolean canSendEmail() {
        return emailEnabled && userEmail != null && !userEmail.trim().isEmpty();
    }
    
    /**
     * SMS bildirim aktif mi ve telefon numarası var mı kontrol eder
     */
    public boolean canSendSms() {
        return smsEnabled && userPhone != null && !userPhone.trim().isEmpty();
    }
} 