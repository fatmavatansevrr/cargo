package com.cargotracking.shipment_service.dto;

import com.cargotracking.shipment_service.model.Package;
import com.cargotracking.shipment_service.model.Shipment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Gönderi Oluşturma Request DTO
 * Requirements: FR-SM-001 - Yeni kargo gönderileri oluşturma
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {
    
    @Valid
    @NotNull(message = "Gönderici adresi boş olamaz")
    private AddressDto senderAddress;
    
    @Valid
    @NotNull(message = "Alıcı adresi boş olamaz")
    private AddressDto recipientAddress;
    
    @Valid
    @NotNull(message = "Paket bilgileri boş olamaz")
    private PackageDto packageInfo;
    
    @NotNull(message = "Hizmet tipi boş olamaz")
    private Shipment.ServiceType serviceType;
    
    @Size(max = 1000, message = "Özel talimatlar 1000 karakterden uzun olamaz")
    private String specialInstructions;
    
    @Size(max = 500, message = "Notlar 500 karakterden uzun olamaz")
    private String notes;
    
    // FR-SM-009: Teslimat tercihleri
    @Valid
    private DeliveryPreferencesDto deliveryPreferences;
} 