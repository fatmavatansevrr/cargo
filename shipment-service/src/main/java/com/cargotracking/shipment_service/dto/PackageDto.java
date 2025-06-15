package com.cargotracking.shipment_service.dto;

import com.cargotracking.shipment_service.model.Package;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Package DTO
 * Requirements: FR-SM-001.1 - Paket detayları
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageDto {
    
    @NotNull(message = "Ağırlık boş olamaz")
    @DecimalMin(value = "0.1", message = "Ağırlık en az 0.1 kg olmalıdır")
    private BigDecimal weight;
    
    @NotNull(message = "Uzunluk boş olamaz")
    @DecimalMin(value = "1.0", message = "Uzunluk en az 1 cm olmalıdır")
    private BigDecimal length;
    
    @NotNull(message = "Genişlik boş olamaz")
    @DecimalMin(value = "1.0", message = "Genişlik en az 1 cm olmalıdır")
    private BigDecimal width;
    
    @NotNull(message = "Yükseklik boş olamaz")
    @DecimalMin(value = "1.0", message = "Yükseklik en az 1 cm olmalıdır")
    private BigDecimal height;
    
    @NotNull(message = "İçerik tipi boş olamaz")
    private Package.ContentType contentType;
    
    @Size(max = 500, message = "İçerik açıklaması 500 karakterden uzun olamaz")
    private String contentDescription;
    
    @NotNull(message = "Değer boş olamaz")
    @DecimalMin(value = "0.01", message = "Değer en az 0.01 TL olmalıdır")
    private BigDecimal declaredValue;
    
    private Boolean isFragile = false;
    
    private Boolean requiresSignature = false;
} 