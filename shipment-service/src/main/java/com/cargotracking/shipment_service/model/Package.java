package com.cargotracking.shipment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Package Entity
 * Requirements: FR-SM-001.1 - Paket detayları (boyut, ağırlık, içerik tipi)
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Ağırlık boş olamaz")
    @DecimalMin(value = "0.1", message = "Ağırlık en az 0.1 kg olmalıdır")
    @Column(name = "weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal weight; // kg cinsinden
    
    @NotNull(message = "Uzunluk boş olamaz")
    @DecimalMin(value = "1.0", message = "Uzunluk en az 1 cm olmalıdır")
    @Column(name = "length", nullable = false, precision = 10, scale = 2)
    private BigDecimal length; // cm cinsinden
    
    @NotNull(message = "Genişlik boş olamaz")
    @DecimalMin(value = "1.0", message = "Genişlik en az 1 cm olmalıdır")
    @Column(name = "width", nullable = false, precision = 10, scale = 2)
    private BigDecimal width; // cm cinsinden
    
    @NotNull(message = "Yükseklik boş olamaz")
    @DecimalMin(value = "1.0", message = "Yükseklik en az 1 cm olmalıdır")
    @Column(name = "height", nullable = false, precision = 10, scale = 2)
    private BigDecimal height; // cm cinsinden
    
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;
    
    @Size(max = 500, message = "İçerik açıklaması 500 karakterden uzun olamaz")
    @Column(name = "content_description", length = 500)
    private String contentDescription;
    
    @NotNull(message = "Değer boş olamaz")
    @DecimalMin(value = "0.01", message = "Değer en az 0.01 TL olmalıdır")
    @Column(name = "declared_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal declaredValue; // TL cinsinden
    
    @Column(name = "is_fragile", nullable = false)
    private Boolean isFragile = false;
    
    @Column(name = "requires_signature", nullable = false)
    private Boolean requiresSignature = false;
    
    /**
     * İçerik Tipi Enum
     */
    public enum ContentType {
        DOCUMENTS("Belgeler"),
        ELECTRONICS("Elektronik"),
        CLOTHING("Giyim"),
        FOOD("Gıda"),
        FRAGILE("Kırılabilir"),
        LIQUID("Sıvı"),
        HAZARDOUS("Tehlikeli Madde"),
        BOOKS("Kitap"),
        JEWELRY("Mücevher"),
        OTHER("Diğer");
        
        private final String displayName;
        
        ContentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 