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
    
    // Alıcı iletişim bilgileri - takip numarası gönderimi için
    @NotNull(message = "Alıcı e-posta adresi boş olamaz")
    private String recipientEmail;
    
    @NotNull(message = "Alıcı telefon numarası boş olamaz")
    private String recipientPhone;
    
    @Valid
    @NotNull(message = "Paket bilgileri boş olamaz")
    private PackageDto packageInfo;
    
    @NotNull(message = "Hizmet tipi boş olamaz")
    private Shipment.ServiceType serviceType;
    
    @NotNull(message = "Kargo şirketi seçimi boş olamaz")
    private Long shipmentCompanyId;
    
    @Size(max = 1000, message = "Özel talimatlar 1000 karakterden uzun olamaz")
    private String specialInstructions;
    
    @Size(max = 500, message = "Notlar 500 karakterden uzun olamaz")
    private String notes;
    
    // FR-SM-009: Teslimat tercihleri
    @Valid
    private DeliveryPreferencesDto deliveryPreferences;
} 