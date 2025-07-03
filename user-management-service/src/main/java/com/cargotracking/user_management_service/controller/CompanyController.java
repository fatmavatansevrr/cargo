package com.cargotracking.user_management_service.controller;

import com.cargotracking.user_management_service.dto.ApiResponseWrapper;
import com.cargotracking.user_management_service.dto.UserResponse;
import com.cargotracking.user_management_service.model.User;
import com.cargotracking.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Management", description = "Genel şirket işlemleri API'leri")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final UserService userService;

    /**
     * Tüm aktif kargo şirketlerini getir (Public - Kayıt için)
     * Kurye kaydı sırasında şirket seçimi için kullanılır
     */
    @GetMapping("/public")
    @Operation(summary = "Kargo Şirketleri (Public)", description = "Kayıt sırasında şirket seçimi için tüm aktif kargo şirketlerini getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kargo şirketleri başarıyla getirildi"),
        @ApiResponse(responseCode = "400", description = "Hata oluştu")
    })
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getShipmentCompaniesPublic() {
        try {
            List<User> companies = userService.getShipmentCompanies();
            List<UserResponse> companyResponses = companies.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, companyResponses, "Kargo şirketleri başarıyla getirildi"));
        } catch (Exception e) {
            log.error("Kargo şirketleri getirilemedi. Hata: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Kargo şirketleri getirilemedi: " + e.getMessage()));
        }
    }

    /**
     * Tüm aktif kargo şirketlerini getir
     * Shipment oluşturma sırasında dropdown için kullanılır
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SHIPMENT_COMPANY') or hasRole('CARRIER')")
    @Operation(summary = "Kargo Şirketleri", description = "Tüm aktif kargo şirketlerini getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kargo şirketleri başarıyla getirildi"),
        @ApiResponse(responseCode = "400", description = "Hata oluştu"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok")
    })
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getShipmentCompanies() {
        try {
            List<User> companies = userService.getShipmentCompanies();
            List<UserResponse> companyResponses = companies.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, companyResponses, "Kargo şirketleri başarıyla getirildi"));
        } catch (Exception e) {
            log.error("Kargo şirketleri getirilemedi. Hata: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Kargo şirketleri getirilemedi: " + e.getMessage()));
        }
    }

    /**
     * Belirtilen şirkete ait kuryeleri (carrier) getirir
     */
    @GetMapping("/{companyId}/carriers")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Şirket Kuryeleri", description = "Belirtilen şirkete ait tüm aktif kuryeleri getirir")
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getCarriersByCompany(@PathVariable Long companyId) {
        try {
            List<User> carriers = userService.getCarriersByCompany(companyId);
            List<UserResponse> carrierResponses = carriers.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, carrierResponses, "Kuryeler başarıyla getirildi"));
        } catch (Exception e) {
            log.error("Kuryeler getirilemedi. Hata: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Kuryeler getirilemedi: " + e.getMessage()));
        }
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(user.getRoles())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 