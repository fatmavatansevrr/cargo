package com.cargotracking.shipment_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Address DTO
 * Requirements: FR-SM-001.1 - Gönderici ve Alıcı bilgileri
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfo {
    
    @NotBlank(message = "İsim boş olamaz")
    @Size(max = 100, message = "İsim 100 karakterden uzun olamaz")
    private String fullName;
    
    @NotBlank(message = "Adres satırı 1 boş olamaz")
    @Size(max = 200, message = "Adres satırı 1 200 karakterden uzun olamaz")
    private String addressLine1;
    
    @Size(max = 200, message = "Adres satırı 2 200 karakterden uzun olamaz")
    private String addressLine2;
    
    @NotBlank(message = "Şehir boş olamaz")
    @Size(max = 50, message = "Şehir 50 karakterden uzun olamaz")
    private String city;
    
    @NotBlank(message = "İl boş olamaz")
    @Size(max = 50, message = "İl 50 karakterden uzun olamaz")
    private String state;
    
    @NotBlank(message = "Posta kodu boş olamaz")
    @Size(max = 10, message = "Posta kodu 10 karakterden uzun olamaz")
    private String postalCode;
    
    @NotBlank(message = "Ülke boş olamaz")
    @Size(max = 50, message = "Ülke 50 karakterden uzun olamaz")
    private String country;
    
    @Size(max = 20, message = "Telefon numarası 20 karakterden uzun olamaz")
    private String phone;
    
    @Email(message = "Geçerli bir email adresi giriniz")
    @Size(max = 100, message = "Email 100 karakterden uzun olamaz")
    private String email;
} 