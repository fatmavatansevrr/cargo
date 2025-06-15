package com.cargotracking.shipment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address Entity
 * Requirements: FR-SM-001.1 - Gönderici ve Alıcı bilgileri (isim, adres, iletişim)
 */
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "İsim boş olamaz")
    @Size(max = 100, message = "İsim 100 karakterden uzun olamaz")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @NotBlank(message = "Adres satırı 1 boş olamaz")
    @Size(max = 200, message = "Adres satırı 1 200 karakterden uzun olamaz")
    @Column(name = "address_line1", nullable = false, length = 200)
    private String addressLine1;
    
    @Size(max = 200, message = "Adres satırı 2 200 karakterden uzun olamaz")
    @Column(name = "address_line2", length = 200)
    private String addressLine2;
    
    @NotBlank(message = "Şehir boş olamaz")
    @Size(max = 50, message = "Şehir 50 karakterden uzun olamaz")
    @Column(name = "city", nullable = false, length = 50)
    private String city;
    
    @NotBlank(message = "İl boş olamaz")
    @Size(max = 50, message = "İl 50 karakterden uzun olamaz")
    @Column(name = "state", nullable = false, length = 50)
    private String state;
    
    @NotBlank(message = "Posta kodu boş olamaz")
    @Size(max = 10, message = "Posta kodu 10 karakterden uzun olamaz")
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;
    
    @NotBlank(message = "Ülke boş olamaz")
    @Size(max = 50, message = "Ülke 50 karakterden uzun olamaz")
    @Column(name = "country", nullable = false, length = 50)
    private String country;
    
    @Size(max = 20, message = "Telefon numarası 20 karakterden uzun olamaz")
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    @Column(name = "email", length = 100)
    private String email;
} 