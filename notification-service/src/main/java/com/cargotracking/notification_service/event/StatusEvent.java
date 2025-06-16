package com.cargotracking.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * StatusEvent - Tracking service'den gelen durum güncellemeleri
 * FR-TR-004: Durum güncellemesi olduğunda status.updated eventi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusEvent {
    
    private String eventType; // status.updated
    private String trackingNumber;
    private Long shipmentId;
    
    // Status bilgileri
    private String newStatus;
    private String previousStatus;
    private String location;
    private String description;
    
    // Timestamp ve kaynak bilgileri
    private LocalDateTime eventTimestamp;
    private String eventSource;
    private Long updatedBy; // Güncellemeyi yapan kullanıcı ID
    
    // Notification için ek bilgiler
    private String customerEmail;
    private String customerPhone;
    private String customerName;
    private String senderEmail;
    private String senderName;
    
    // Lokasyon ve tahmini teslimat bilgileri
    private String currentLocation;
    private String nextLocation;
    private LocalDateTime estimatedDeliveryDate;
    
    /**
     * Bu durum değişikliği bildirim gerektirir mi kontrol eder
     */
    public boolean requiresNotification() {
        return newStatus != null && 
               !newStatus.equals(previousStatus) &&
               isImportantStatus(newStatus);
    }
    
    /**
     * Önemli durum mu kontrol eder (bildirim gönderilmesi gereken)
     */
    private boolean isImportantStatus(String status) {
        return switch (status) {
            case "PACKAGE_RECEIVED", 
                 "IN_TRANSIT", 
                 "OUT_FOR_DELIVERY", 
                 "DELIVERED", 
                 "DELIVERY_FAILED",
                 "RETURNED_TO_SENDER" -> true;
            default -> false;
        };
    }
    
    /**
     * Müşteri bildirim bilgileri mevcut mu kontrol eder
     */
    public boolean hasCustomerNotificationInfo() {
        return customerEmail != null && !customerEmail.trim().isEmpty();
    }
    
    /**
     * Gönderici bildirim bilgileri mevcut mu kontrol eder
     */
    public boolean hasSenderNotificationInfo() {
        return senderEmail != null && !senderEmail.trim().isEmpty();
    }
} 