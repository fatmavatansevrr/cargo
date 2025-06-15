package com.cargotracking.shipment_service.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Teslimat tercihleri DTO
 * Requirements: FR-SM-009
 */
@Data
public class DeliveryPreferencesDto {
    
    // Teslimat seçenekleri
    private Boolean leaveAtDoor; // Kapıya bırak
    private Boolean leaveWithNeighbor; // Komşuya bırak
    private Boolean leaveWithSecurity; // Güvenlik görevlisine bırak
    private Boolean requireSignature; // İmza gerekli
    private Boolean requireIdCheck; // Kimlik kontrolü gerekli
    
    // Teslimat zamanı tercihleri
    private String preferredTimeSlot; // "09:00-12:00", "13:00-17:00", "18:00-21:00"
    private String preferredDay; // "WEEKDAY", "WEEKEND", "ANY"
    
    // Özel talimatlar
    @Size(max = 500, message = "Teslimat talimatları 500 karakteri geçemez")
    private String deliveryInstructions;
    
    // Alternatif teslimat adresi
    private AddressDto alternativeAddress;
    
    // İletişim tercihleri
    private Boolean smsNotification; // SMS bildirimi
    private Boolean emailNotification; // E-posta bildirimi
    private Boolean callBeforeDelivery; // Teslimat öncesi arama
    
    @Size(max = 20, message = "Alternatif telefon numarası 20 karakteri geçemez")
    private String alternativePhone;
} 