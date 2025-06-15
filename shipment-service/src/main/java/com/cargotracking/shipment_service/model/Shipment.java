package com.cargotracking.shipment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Shipment Entity - Ana gönderi modeli
 * Requirements: FR-SM-001, FR-SM-002, FR-SM-007
 */
@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_number", unique = true, nullable = false, length = 20)
    private String trackingNumber; // FR-SM-002: Benzersiz takip numarası
    
    @NotNull(message = "Gönderici ID boş olamaz")
    @Column(name = "sender_user_id", nullable = false)
    private Long senderUserId; // User Management Service'den gelen kullanıcı ID
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_address_id", nullable = false)
    private Address senderAddress;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_address_id", nullable = false)
    private Address recipientAddress;
    
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private Package packageInfo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status = ShipmentStatus.ACTIVE;
    
    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;
    
    @Size(max = 1000, message = "Özel talimatlar 1000 karakterden uzun olamaz")
    @Column(name = "special_instructions", length = 1000)
    private String specialInstructions; // FR-SM-009: Teslimat tercihleri
    
    @Size(max = 500, message = "Notlar 500 karakterden uzun olamaz")
    @Column(name = "notes", length = 500)
    private String notes;
    
    @Column(name = "assigned_carrier_id")
    private Long assignedCarrierId; // Taşıyıcı kullanıcı ID
    
    // FR-SM-009: Teslimat tercihleri
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_preferences_id")
    private DeliveryPreferences deliveryPreferences;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Hizmet Tipi Enum
     * Requirements: FR-SM-001.1 - Hizmet tipi (standart, ekspres vb.)
     */
    public enum ServiceType {
        STANDARD("Standart", 3, 5),
        EXPRESS("Ekspres", 1, 2),
        OVERNIGHT("Gece Teslimat", 1, 1),
        ECONOMY("Ekonomik", 5, 7),
        INTERNATIONAL("Uluslararası", 7, 14);
        
        private final String displayName;
        private final int minDeliveryDays;
        private final int maxDeliveryDays;
        
        ServiceType(String displayName, int minDeliveryDays, int maxDeliveryDays) {
            this.displayName = displayName;
            this.minDeliveryDays = minDeliveryDays;
            this.maxDeliveryDays = maxDeliveryDays;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getMinDeliveryDays() {
            return minDeliveryDays;
        }
        
        public int getMaxDeliveryDays() {
            return maxDeliveryDays;
        }
    }
    
    /**
     * Gönderi Durumu Enum
     * Requirements: FR-SM-006, FR-SM-007 - Gönderi yaşam döngüsü
     */
    public enum ShipmentStatus {
        ACTIVE("Aktif"),
        FINISHED("Tamamlandı"),
        CANCELLED("İptal Edildi");
        
        private final String displayName;
        
        ShipmentStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Durum geçişlerinin geçerli olup olmadığını kontrol eder
         */
        public boolean canTransitionTo(ShipmentStatus newStatus) {
            return switch (this) {
                case ACTIVE -> newStatus == FINISHED || newStatus == CANCELLED;
                case FINISHED, CANCELLED -> false; // Final durumlar
            };
        }
    }
} 