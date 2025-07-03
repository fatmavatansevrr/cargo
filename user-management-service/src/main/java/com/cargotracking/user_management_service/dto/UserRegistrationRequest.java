package com.cargotracking.user_management_service.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * User registration request DTO - Requirements FR-UM-001
 */
@Data
public class UserRegistrationRequest {

    @NotBlank(message = "Kullanıcı adı boş olamaz")
    @Size(min = 3, max = 50, message = "Kullanıcı adı 3-50 karakter arasında olmalıdır")
    private String username;

    @NotBlank(message = "Email boş olamaz")
    @Email(message = "Geçerli bir email adresi giriniz")
    private String email;

    @NotBlank(message = "Şifre boş olamaz")
    @Size(min = 6, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    @NotBlank(message = "Ad boş olamaz")
    private String firstName;

    @NotBlank(message = "Soyad boş olamaz")
    private String lastName;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Geçerli bir telefon numarası giriniz")
    private String phone;

    private String address;

    @NotEmpty(message = "En az bir rol seçilmelidir")
    private List<String> roles;

    // Kurye (CARRIER) kaydı için - Hangi kargo şirketine bağlı olduğunu belirtir
    private Long companyId;

    // Kargo Şirketi (SHIPMENT_COMPANY) kaydı için - Şirketin adı
    private String companyName;
} 