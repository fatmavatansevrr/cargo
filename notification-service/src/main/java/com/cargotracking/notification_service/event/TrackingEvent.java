package com.cargotracking.notification_service.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * TrackingEvent - Kafka'dan gelen tracking olayları
 * Tracking service ile tamamen uyumlu event modeli
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class TrackingEvent {
    
    // Event metadata
    @JsonProperty("eventType")
    private String eventType; // status.changed, delivery.completed, delivery.failed, etc.
    
    @JsonProperty("trackingNumber")
    private String trackingNumber;
    
    @JsonProperty("shipmentId")
    private Long shipmentId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("currentStatus")
    private String currentStatus;
    
    @JsonProperty("previousStatus")
    private String previousStatus;
    
    @JsonProperty("eventTimestamp")
    private LocalDateTime eventTimestamp;
    
    @JsonProperty("eventSource")
    private String eventSource = "tracking-service";
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("locationCode")
    private String locationCode;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("estimatedDeliveryDate")
    private LocalDateTime estimatedDeliveryDate;
    
    @JsonProperty("actualDeliveryDate")
    private LocalDateTime actualDeliveryDate;
    
    @JsonProperty("carrierName")
    private String carrierName;
    
    @JsonProperty("nextLocation")
    private String nextLocation;
    
    @JsonProperty("deliveryAttempt")
    private Integer deliveryAttempt;
    
    @JsonProperty("signature")
    private String signature;
    
    @JsonProperty("deliveredTo")
    private String deliveredTo;
    
    @JsonProperty("notes")
    private String notes;
    
    // Customer Information - shipment'tan alınacak
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    /**
     * Event'in priority level'ini belirle
     */
    public int getPriorityLevel() {
        if (eventType == null) return 1;
        
        return switch (eventType.toLowerCase()) {
            case "delivery.failed", "delivery.exception" -> 3; // High priority
            case "delivery.completed", "out.for.delivery" -> 3; // High priority
            case "status.changed" -> 2; // Medium priority
            case "package.arrived", "in.transit" -> 1; // Low priority
            default -> 1;
        };
    }
    
    /**
     * Event'in customer'a gönderilmesi gerekip gerekmediğini kontrol et
     */
    public boolean shouldNotifyCustomer() {
        if (eventType == null) return false;
        
        return switch (eventType.toLowerCase()) {
            case "delivery.completed", 
                 "delivery.failed", 
                 "out.for.delivery",
                 "delivery.exception",
                 "status.changed" -> true;
            case "package.arrived", 
                 "in.transit" -> isSignificantLocationUpdate();
            default -> false;
        };
    }
    
    /**
     * Location update'i önemli mi kontrol et
     */
    private boolean isSignificantLocationUpdate() {
        return location != null && (
            location.toLowerCase().contains("hub") ||
            location.toLowerCase().contains("facility") ||
            location.toLowerCase().contains("depot") ||
            location.toLowerCase().contains("center")
        );
    }
    
    /**
     * Status'un user-friendly açıklamasını al
     */
    public String getFriendlyStatusDescription() {
        if (currentStatus == null) return description;
        
        return switch (currentStatus.toLowerCase()) {
            case "picked_up" -> "Paketiniz alındı ve kargo merkezine doğru yola çıktı";
            case "in_transit" -> "Paketiniz " + (location != null ? location : "kargo merkezinde") + " transit halinde";
            case "out_for_delivery" -> "Paketiniz teslim için yola çıktı";
            case "delivered" -> "Paketiniz başarıyla teslim edildi" + (deliveredTo != null ? " (" + deliveredTo + ")" : "");
            case "delivery_failed" -> "Teslimat başarısız oldu. Tekrar denenecek.";
            case "exception" -> "Paketinizde beklenmeyen bir durum oluştu";
            case "returned" -> "Paket gönderene iade edildi";
            default -> description != null ? description : "Durum güncellendi";
        };
    }
    
    /**
     * Event türüne göre customer-friendly message oluştur
     */
    public String getCustomerMessage() {
        if (eventType == null) return getFriendlyStatusDescription();
        
        return switch (eventType.toLowerCase()) {
            case "status.changed" -> getFriendlyStatusDescription();
            case "delivery.completed" -> "🎉 Paketiniz başarıyla teslim edildi! " + 
                (deliveredTo != null ? "Teslim alan: " + deliveredTo : "");
            case "delivery.failed" -> "⚠️ Teslimat denemesi başarısız oldu. " + 
                (notes != null ? "Açıklama: " + notes : "Tekrar denenecek.");
            case "out.for.delivery" -> "🚚 Paketiniz teslim için yola çıktı! Bugün size ulaşacak.";
            case "package.arrived" -> "📦 Paketiniz " + (location != null ? location : "kargo merkezine") + " ulaştı.";
            case "delivery.exception" -> "⚠️ Paketinizde beklenmeyen bir durum oluştu. " + 
                (notes != null ? "Detay: " + notes : "Müşteri hizmetleri ile iletişime geçin.");
            default -> getFriendlyStatusDescription();
        };
    }
    
    /**
     * Event'in email subject'ini oluştur
     */
    public String getEmailSubject() {
        String trackingPrefix = trackingNumber != null ? "[" + trackingNumber + "] " : "";
        
        if (eventType == null) return trackingPrefix + "Kargo Durum Güncellemesi";
        
        return trackingPrefix + switch (eventType.toLowerCase()) {
            case "delivery.completed" -> "Paketiniz Teslim Edildi!";
            case "delivery.failed" -> "Teslimat Denemesi Başarısız";
            case "out.for.delivery" -> "Paketiniz Teslim İçin Yola Çıktı";
            case "status.changed" -> "Kargo Durum Güncellemesi";
            case "package.arrived" -> "Paketiniz Kargo Merkezine Ulaştı";
            case "delivery.exception" -> "Kargo Durumunda Özel Durum";
            default -> "Kargo Durum Güncellemesi";
        };
    }
    
    /**
     * Delivery attempt sayısına göre özel mesaj
     */
    public String getDeliveryAttemptMessage() {
        if (deliveryAttempt == null || deliveryAttempt <= 1) return "";
        
        return switch (deliveryAttempt) {
            case 2 -> "Bu ikinci teslimat denemesidir.";
            case 3 -> "Bu son teslimat denemesidir. Lütfen evde olduğunuzdan emin olun.";
            default -> "Bu " + deliveryAttempt + ". teslimat denemesidir.";
        };
    }
} 