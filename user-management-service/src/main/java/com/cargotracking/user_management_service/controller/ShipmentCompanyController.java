package com.cargotracking.user_management_service.controller;

import com.cargotracking.user_management_service.dto.ApiResponseWrapper;
import com.cargotracking.user_management_service.dto.UserResponse;
import com.cargotracking.user_management_service.model.User;
import com.cargotracking.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shipment Company Controller - Requirements FR-UM-004.4
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipment Company", description = "Kargo şirketi yönetim API'leri")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentCompanyController {

    private final UserService userService;

    /**
     * Tüm kullanıcıları listele veya arama yap - FR-UM-004.4
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Kullanıcıları Listele/Ara", 
               description = "Sistemdeki tüm kullanıcıları listeler veya arama terimi ile filtreler - Kargo şirketi yetkisi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcılar başarıyla listelendi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - SHIPMENT_COMPANY rolü gerekli")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(value = "search", required = false) String searchTerm) {
        try {
            List<UserResponse> users;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                users = userService.searchUsers(searchTerm);
            } else {
                users = userService.getAllUsers();
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Error durumunda boş liste döndürüp log ekleyebiliriz
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * ID'ye göre kullanıcı detayı getir - FR-UM-004.4
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Kullanıcı Detayları", 
               description = "Belirtilen ID'ye sahip kullanıcının detaylarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı detayları başarıyla getirildi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz kullanıcı ID"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - SHIPMENT_COMPANY rolü gerekli"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    })
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Kullanıcı durumunu değiştir (aktif/pasif)
     */
    @PutMapping("/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Kullanıcı Durumu Değiştir", 
               description = "Belirtilen kullanıcının aktif/pasif durumunu değiştirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı durumu başarıyla değiştirildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz kullanıcı ID"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - SHIPMENT_COMPANY rolü gerekli")
    })
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long userId) {
        try {
            userService.toggleUserStatus(userId);
            return ResponseEntity.ok(Map.of("message", "Kullanıcı durumu başarıyla değiştirildi"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Sistem durumu kontrolü
     */
    @GetMapping("/system/status")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Sistem Durumu", description = "Sistem sağlık durumunu kontrol eder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sistem çalışıyor"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - SHIPMENT_COMPANY rolü gerekli")
    })
    public ResponseEntity<?> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "user-management-service",
            "message", "Sistem çalışıyor"
        ));
    }

    /**
     * Belirli bir kargo şirketinin tüm carrier'larını getir
     * Otomatik gönderi atama için kullanılır
     */
    @GetMapping("/companies/{companyId}/carriers")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Şirket Carrier'ları", description = "Belirli bir kargo şirketinin tüm carrier'larını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrier'lar başarıyla getirildi"),
        @ApiResponse(responseCode = "400", description = "Hata oluştu"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok")
    })
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getCompanyCarriers(@PathVariable Long companyId) {
        try {
            List<User> carriers = userService.getCarriersByCompanyId(companyId);
            List<UserResponse> carrierResponses = carriers.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, carrierResponses, "Carrier'lar başarıyla getirildi"));
        } catch (Exception e) {
            log.error("Carrier'lar getirilemedi. Company ID: {}, Hata: {}", companyId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Carrier'lar getirilemedi: " + e.getMessage()));
        }
    }

    /**
     * Tüm aktif kargo şirketlerini getir
     * Shipment oluşturma sırasında dropdown için kullanılır
     */
    /*
    @GetMapping("/companies")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
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
    */

    /**
     * User entity'yi UserResponse'a dönüştürür
     */
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
                .companyId(user.getCompanyId())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Internal endpoint - Kargo şirketlerini getir (Microservice communication için)
     * Authentication gerektirmez
     */
    @GetMapping("/internal/companies")
    @Operation(summary = "Internal - Kargo Şirketleri", description = "Tüm aktif kargo şirketlerini getirir (Internal)")
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getShipmentCompaniesInternal() {
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
     * Internal endpoint - Şirket carrier'larını getir (Microservice communication için)
     * Authentication gerektirmez
     */
    @GetMapping("/internal/companies/{companyId}/carriers")
    @Operation(summary = "Internal - Şirket Carrier'ları", description = "Belirli bir kargo şirketinin tüm carrier'larını getirir (Internal)")
    public ResponseEntity<ApiResponseWrapper<List<UserResponse>>> getCompanyCarriersInternal(@PathVariable Long companyId) {
        try {
            List<User> carriers = userService.getCarriersByCompanyId(companyId);
            List<UserResponse> carrierResponses = carriers.stream()
                    .map(this::convertToUserResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, carrierResponses, "Carrier'lar başarıyla getirildi"));
        } catch (Exception e) {
            log.error("Carrier'lar getirilemedi. Company ID: {}, Hata: {}", companyId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Carrier'lar getirilemedi: " + e.getMessage()));
        }
    }
} 