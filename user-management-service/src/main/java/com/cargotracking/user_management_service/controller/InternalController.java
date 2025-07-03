package com.cargotracking.user_management_service.controller;

import com.cargotracking.user_management_service.dto.ApiResponseWrapper;
import com.cargotracking.user_management_service.dto.UserResponse;
import com.cargotracking.user_management_service.model.User;
import com.cargotracking.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/internal") // Base path for all internal endpoints
@RequiredArgsConstructor
@Slf4j
@Hidden
@Tag(name = "Internal API", description = "Endpoints for inter-service communication")
public class InternalController {

    private final UserService userService;

    /**
     * Internal endpoint - Username ile kullanıcı getir (Microservice communication için)
     * Authentication gerektirmez
     */
    @GetMapping("/users/username/{username}")
    @Operation(summary = "Internal - Username ile Kullanıcı", description = "Username ile kullanıcı bilgilerini getirir (Internal)")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> getUserByUsernameInternal(@PathVariable String username) {
        try {
            log.info("🔍 Internal getUserByUsername isteği. Username: {}", username);
            
            User user = userService.getUserByUsername(username);
            UserResponse userResponse = convertToUserResponse(user);
            
            log.info("✅ Kullanıcı bulundu. ID: {}, Username: {}", user.getId(), username);
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, userResponse, "Kullanıcı başarıyla getirildi"));
        } catch (Exception e) {
            log.error("❌ Kullanıcı getirilemedi. Username: {}, Hata: {}", username, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Kullanıcı getirilemedi: " + e.getMessage()));
        }
    }

    /**
     * Internal endpoint - ID ile kullanıcı getir (Microservice communication için)
     * Authentication gerektirmez
     */
    @GetMapping("/users/id/{userId}")
    @Operation(summary = "Internal - ID ile Kullanıcı", description = "ID ile kullanıcı bilgilerini getirir (Internal)")
    public ResponseEntity<ApiResponseWrapper<UserResponse>> getUserByIdInternal(@PathVariable Long userId) {
        try {
            log.info("🔍 Internal getUserById isteği. User ID: {}", userId);
            UserResponse userResponse = userService.getUserById(userId);
            log.info("✅ Kullanıcı bulundu. ID: {}", userId);
            return ResponseEntity.ok(new ApiResponseWrapper<>(true, userResponse, "Kullanıcı başarıyla getirildi"));
        } catch (Exception e) {
            log.error("❌ Kullanıcı getirilemedi. User ID: {}, Hata: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponseWrapper<>(false, null, "Kullanıcı getirilemedi: " + e.getMessage()));
        }
    }

    /**
     * Internal endpoint - Kargo şirketlerini getir (Microservice communication için)
     * Authentication gerektirmez
     */
    @GetMapping("/companies")
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
    @GetMapping("/companies/{companyId}/carriers")
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
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
} 