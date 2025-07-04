package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification Entity - MongoDB document
 * Kullanıcılara gönderilen bildirimleri saklar
 */
@Document(collection = "notifications")
@CompoundIndexes({
    @CompoundIndex(name = "user_status_idx", def = "{'userId' : 1, 'status' : 1}"),
    @CompoundIndex(name = "tracking_type_idx", def = "{'trackingNumber' : 1, 'type' : 1}"),
    @CompoundIndex(name = "created_type_idx", def = "{'createdAt' : -1, 'type' : 1}"),
    @CompoundIndex(name = "user_created_idx", def = "{'userId' : 1, 'createdAt' : -1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    private String id;
    
    @Field("user_id")
    @Indexed
    @NotNull(message = "User ID gereklidir")
    private Long userId;
    
    @Field("tracking_number")
    @Indexed
    private String trackingNumber;
    
    @Field("shipment_id")
    private String shipmentId;
    
    @Field("type")
    @Indexed
    @NotNull(message = "Notification type gereklidir")
    private NotificationType type;
    
    @Field("channel")
    @NotNull(message = "Notification channel gereklidir")
    private NotificationChannel channel;
    
    @Field("title")
    @NotBlank(message = "Notification title gereklidir")
    private String title;
    
    @Field("message")
    @NotBlank(message = "Notification message gereklidir")
    private String message;
    
    @Field("recipient")
    @NotBlank(message = "Recipient gereklidir")
    private String recipient; // Email, phone number, or user ID
    
    @Field("status")
    @Indexed
    @NotNull(message = "Notification status gereklidir")
    private NotificationStatus status = NotificationStatus.PENDING;
    
    @Field("created_at")
    @CreatedDate
    @Indexed
    private LocalDateTime createdAt;
    
    @Field("sent_at")
    private LocalDateTime sentAt;
    
    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Notification Types
     */
    public enum NotificationType {
        SHIPMENT_CREATED("Gönderi Oluşturuldu"),
        SHIPMENT_UPDATED("Gönderi Güncellendi"),
        SHIPMENT_CANCELLED("Gönderi İptal Edildi"),
        STATUS_CHANGED("Durum Değişti"),
        DELIVERY_COMPLETED("Teslimat Tamamlandı"),
        DELIVERY_FAILED("Teslimat Başarısız"),
        OUT_FOR_DELIVERY("Teslimat İçin Yola Çıktı"),
        PACKAGE_ARRIVED("Paket Geldi"),
        DELIVERY_EXCEPTION("Teslimat İstisnası"),
        SYSTEM_ALERT("Sistem Uyarısı"),
        REMINDER("Hatırlatma");
        
        private final String description;
        
        NotificationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Notification Channels
     */
    public enum NotificationChannel {
        EMAIL("Email"),
        SMS("SMS"),
        PUSH_NOTIFICATION("Push Notification"),
        IN_APP("In-App Notification");
        
        private final String description;
        
        NotificationChannel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Notification Status
     */
    public enum NotificationStatus {
        PENDING("Beklemede"),
        SENT("Gönderildi"),
        DELIVERED("Teslim Edildi"),
        FAILED("Başarısız"),
        READ("Okundu"),
        EXPIRED("Süresi Doldu");
        
        private final String description;
        
        NotificationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Notification'ı sent olarak işaretle
     */
    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
} 