package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification Entity - MongoDB koleksiyonu
 * Bildirim kayıtlarını saklar ve geçmişi tutar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {
    
    @Id
    private String id;
    
    private Long userId;
    private String trackingNumber;
    private Long shipmentId;
    
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;
    
    private String title;
    private String message;
    private Map<String, Object> templateData;
    
    private String recipient; // Email address, phone number, etc.
    private String sender;
    
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    
    private String eventType; // shipment.created, status.changed, etc.
    private String eventSource; // shipment-service, tracking-service, etc.
    
    private String errorMessage;
    private Integer retryCount = 0;
    private Integer maxRetries = 3;
    
    public enum NotificationType {
        SHIPMENT_CREATED,
        STATUS_CHANGED,
        DELIVERY_COMPLETED,
        DELIVERY_FAILED,
        SHIPMENT_CANCELLED,
        DELIVERY_REMINDER,
        SYSTEM_ALERT
    }
    
    public enum NotificationChannel {
        EMAIL,
        SMS,
        PUSH_NOTIFICATION,
        IN_APP
    }
    
    public enum NotificationStatus {
        PENDING,
        SENT,
        DELIVERED,
        READ,
        FAILED,
        CANCELLED
    }
} 