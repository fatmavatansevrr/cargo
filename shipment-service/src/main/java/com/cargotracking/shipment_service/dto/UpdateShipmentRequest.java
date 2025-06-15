package com.cargotracking.shipment_service.dto;

import com.cargotracking.shipment_service.model.Shipment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Gönderi güncelleme request DTO
 * Requirements: FR-SM-004
 */
@Data
public class UpdateShipmentRequest {
    
    @Valid
    private AddressDto senderAddress;
    
    @Valid
    private AddressDto recipientAddress;
    
    @Valid
    private PackageDto packageInfo;
    
    private Shipment.ServiceType serviceType;
    
    @Size(max = 500, message = "Özel talimatlar 500 karakteri geçemez")
    private String specialInstructions;
    
    @Size(max = 1000, message = "Notlar 1000 karakteri geçemez")
    private String notes;
    
    // FR-SM-009: Teslimat tercihleri
    private DeliveryPreferencesDto deliveryPreferences;
} 