package com.cargotracking.user_management_service.controller;

import com.cargotracking.user_management_service.dto.UserResponse;
import com.cargotracking.user_management_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller - Requirements FR-UM-004.4
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Sistem yöneticisi API'leri - Sadece ADMIN rolü")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserService userService;

    /**
     * Tüm kullanıcıları listele veya arama yap - FR-UM-004.4
     */
    @GetMapping("/users")
    @Operation(summary = "Kullanıcıları Listele/Ara", 
               description = "Sistemdeki tüm kullanıcıları listeler veya arama terimi ile filtreler - Sadece ADMIN yetkisi")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcılar başarıyla listelendi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - ADMIN rolü gerekli")
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
    @Operation(summary = "Kullanıcı Detayları", 
               description = "Belirtilen ID'ye sahip kullanıcının detaylarını getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı detayları başarıyla getirildi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz kullanıcı ID"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - ADMIN rolü gerekli"),
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
    @Operation(summary = "Kullanıcı Durumu Değiştir", 
               description = "Belirtilen kullanıcının aktif/pasif durumunu değiştirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Kullanıcı durumu başarıyla değiştirildi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz kullanıcı ID"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - ADMIN rolü gerekli")
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
    @Operation(summary = "Sistem Durumu", description = "Sistem sağlık durumunu kontrol eder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sistem çalışıyor"),
        @ApiResponse(responseCode = "403", description = "Bu işlem için yetkiniz yok - ADMIN rolü gerekli")
    })
    public ResponseEntity<?> getSystemStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "user-management-service",
            "message", "Sistem çalışıyor"
        ));
    }
} 