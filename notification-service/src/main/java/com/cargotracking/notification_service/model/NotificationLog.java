package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Gönderilen bildirimlerin log kaydı
 * Sistem takibi ve hata analizi için kullanılır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_logs")
public class NotificationLog {
    
    @Id
    private String id;
    
    private Long userId;
    private String userEmail;
    private String userPhone;
    
    // Notification detayları
    private String notificationChannel; // EMAIL, SMS, PUSH
    private String subject;
    private String message;
    private String status; // SENT, FAILED, PENDING
    private String errorMessage;
    
    // Event bilgileri
    private String eventType; // shipment.created, shipment.updated, status.updated
    private Long shipmentId;
    private String trackingNumber;
    private String shipmentStatus;
    
    // Zaman damgaları
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    
    // Retry bilgileri
    private int retryCount = 0;
    private int maxRetries = 3;
    
    /**
     * Notification başarılı olarak gönderildi mi kontrol eder
     */
    public boolean isSent() {
        return "SENT".equals(status);
    }
    
    /**
     * Notification başarısız oldu mu kontrol eder
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    /**
     * Tekrar deneme yapılabilir mi kontrol eder
     */
    public boolean canRetry() {
        return isFailed() && retryCount < maxRetries;
    }
    
    /**
     * Retry count'ı artır
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }
    
    /**
     * Notification'ı başarılı olarak işaretle
     */
    public void markAsSent() {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
    }
    
    /**
     * Notification'ı başarısız olarak işaretle
     */
    public void markAsFailed(String errorMessage) {
        this.status = "FAILED";
        this.errorMessage = errorMessage;
    }
} 