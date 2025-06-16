package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * NotificationTemplate Entity - MongoDB koleksiyonu
 * Bildirim şablonlarını saklar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_templates")
public class NotificationTemplate {
    
    @Id
    private String id;
    
    private String templateName;
    private Notification.NotificationType notificationType;
    private Notification.NotificationChannel channel;
    
    private String language = "tr"; // Varsayılan Türkçe
    
    private String subject; // Email için
    private String title; // Push notification için
    private String body; // Ana mesaj içeriği
    private String htmlBody; // Email için HTML içerik
    
    // Thymeleaf template dosya adı
    private String templateFile;
    
    // Default template variables
    private Map<String, Object> defaultVariables;
    
    private boolean active = true;
    private String version = "1.0";
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit setter methods (in addition to Lombok @Data)
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    public void setNotificationType(Notification.NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    
    public void setChannel(Notification.NotificationChannel channel) {
        this.channel = channel;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setBody(String body) {
        this.body = body;
    }
    
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Static factory methods for default templates
     */
    public static NotificationTemplate createShipmentCreatedEmailTemplate() {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName("shipment-created-email");
        template.setNotificationType(Notification.NotificationType.SHIPMENT_CREATED);
        template.setChannel(Notification.NotificationChannel.EMAIL);
        template.setSubject("Kargonuz Oluşturuldu - Takip No: {{trackingNumber}}");
        template.setTitle("Kargo Oluşturuldu");
        template.setBody("Sayın {{customerName}}, {{trackingNumber}} takip numaralı kargonuz başarıyla oluşturuldu.");
        template.setTemplateFile("shipment-created-email");
        template.setActive(true);
        template.setCreatedAt(LocalDateTime.now());
        return template;
    }
    
    public static NotificationTemplate createStatusChangedEmailTemplate() {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName("status-changed-email");
        template.setNotificationType(Notification.NotificationType.STATUS_CHANGED);
        template.setChannel(Notification.NotificationChannel.EMAIL);
        template.setSubject("Kargo Durumu Güncellendi - {{trackingNumber}}");
        template.setTitle("Durum Güncellemesi");
        template.setBody("{{trackingNumber}} takip numaralı kargonuzun durumu '{{newStatus}}' olarak güncellendi.");
        template.setTemplateFile("status-changed-email");
        template.setActive(true);
        template.setCreatedAt(LocalDateTime.now());
        return template;
    }
    
    public static NotificationTemplate createDeliveryCompletedEmailTemplate() {
        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateName("delivery-completed-email");
        template.setNotificationType(Notification.NotificationType.DELIVERY_COMPLETED);
        template.setChannel(Notification.NotificationChannel.EMAIL);
        template.setSubject("Kargonuz Teslim Edildi - {{trackingNumber}}");
        template.setTitle("Teslimat Tamamlandı");
        template.setBody("{{trackingNumber}} takip numaralı kargonuz başarıyla teslim edilmiştir.");
        template.setTemplateFile("delivery-completed-email");
        template.setActive(true);
        template.setCreatedAt(LocalDateTime.now());
        return template;
    }
} 