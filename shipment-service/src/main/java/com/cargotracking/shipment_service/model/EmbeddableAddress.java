package com.cargotracking.shipment_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable Address - Embedded kullanım için
 * Requirements: FR-SM-009 - Teslimat tercihleri için alternatif adres
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddableAddress {
    
    @Size(max = 100, message = "İsim 100 karakterden uzun olamaz")
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Size(max = 200, message = "Adres satırı 1 200 karakterden uzun olamaz")
    @Column(name = "address_line1", length = 200)
    private String addressLine1;
    
    @Size(max = 200, message = "Adres satırı 2 200 karakterden uzun olamaz")
    @Column(name = "address_line2", length = 200)
    private String addressLine2;
    
    @Size(max = 50, message = "Şehir 50 karakterden uzun olamaz")
    @Column(name = "city", length = 50)
    private String city;
    
    @Size(max = 50, message = "İl 50 karakterden uzun olamaz")
    @Column(name = "state", length = 50)
    private String state;
    
    @Size(max = 10, message = "Posta kodu 10 karakterden uzun olamaz")
    @Column(name = "postal_code", length = 10)
    private String postalCode;
    
    @Size(max = 50, message = "Ülke 50 karakterden uzun olamaz")
    @Column(name = "country", length = 50)
    private String country;
    
    @Size(max = 20, message = "Telefon numarası 20 karakterden uzun olamaz")
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    @Column(name = "email", length = 100)
    private String email;
} 