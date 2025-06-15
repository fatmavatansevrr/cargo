package com.cargotracking.shipment_service.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Teslimat tercihleri entity
 * Requirements: FR-SM-009
 */
@Entity
@Table(name = "delivery_preferences")
@Data
public class DeliveryPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Teslimat seçenekleri
    @Column(name = "leave_at_door")
    private Boolean leaveAtDoor = false;
    
    @Column(name = "leave_with_neighbor")
    private Boolean leaveWithNeighbor = false;
    
    @Column(name = "leave_with_security")
    private Boolean leaveWithSecurity = false;
    
    @Column(name = "require_signature")
    private Boolean requireSignature = false;
    
    @Column(name = "require_id_check")
    private Boolean requireIdCheck = false;
    
    // Teslimat zamanı tercihleri
    @Column(name = "preferred_time_slot", length = 50)
    private String preferredTimeSlot;
    
    @Column(name = "preferred_day", length = 20)
    private String preferredDay;
    
    // Özel talimatlar
    @Column(name = "delivery_instructions", length = 500)
    private String deliveryInstructions;
    
    // Alternatif teslimat adresi
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "fullName", column = @Column(name = "alt_full_name")),
        @AttributeOverride(name = "addressLine1", column = @Column(name = "alt_address_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "alt_address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "alt_city")),
        @AttributeOverride(name = "state", column = @Column(name = "alt_state")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "alt_postal_code")),
        @AttributeOverride(name = "country", column = @Column(name = "alt_country")),
        @AttributeOverride(name = "phone", column = @Column(name = "alt_phone")),
        @AttributeOverride(name = "email", column = @Column(name = "alt_email"))
    })
    private EmbeddableAddress alternativeAddress;
    
    // İletişim tercihleri
    @Column(name = "sms_notification")
    private Boolean smsNotification = true;
    
    @Column(name = "email_notification")
    private Boolean emailNotification = true;
    
    @Column(name = "call_before_delivery")
    private Boolean callBeforeDelivery = false;
    
    @Column(name = "alternative_phone", length = 20)
    private String alternativePhone;
} 