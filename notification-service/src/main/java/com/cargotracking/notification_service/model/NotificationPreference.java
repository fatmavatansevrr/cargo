package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * NotificationPreference Entity - MongoDB koleksiyonu
 * Kullanıcıların bildirim tercihlerini saklar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_preferences")
public class NotificationPreference {
    
    @Id
    private String id;
    
    private Long userId;
    private String email;
    private String phoneNumber;
    
    // Bildirim türleri için tercihler
    private Map<Notification.NotificationType, Set<Notification.NotificationChannel>> preferences;
    
    // Genel tercihler
    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private boolean pushEnabled = true;
    private boolean inAppEnabled = true;
    
    // Zaman tercihleri
    private String timezone = "UTC";
    private Integer quietHoursStart; // 22 (10 PM)
    private Integer quietHoursEnd; // 8 (8 AM)
    
    // Sıklık tercihleri
    private boolean realTimeNotifications = true;
    private boolean dailySummary = false;
    private boolean weeklySummary = false;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Belirli bir bildirim türü için hangi kanalların aktif olduğunu kontrol eder
     */
    public boolean isChannelEnabledForType(Notification.NotificationType type, Notification.NotificationChannel channel) {
        if (preferences == null || !preferences.containsKey(type)) {
            return isChannelGenerallyEnabled(channel);
        }
        
        return preferences.get(type).contains(channel) && isChannelGenerallyEnabled(channel);
    }
    
    /**
     * Genel kanal ayarlarını kontrol eder
     */
    private boolean isChannelGenerallyEnabled(Notification.NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
            case PUSH_NOTIFICATION -> pushEnabled;
            case IN_APP -> inAppEnabled;
        };
    }
} 