package com.cargotracking.shipment_service.controller;

import com.cargotracking.shipment_service.client.UserServiceClient;
import com.cargotracking.shipment_service.dto.CreateShipmentRequest;
import com.cargotracking.shipment_service.dto.UpdateShipmentRequest;
import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.model.Shipment;
import com.cargotracking.shipment_service.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Shipment Controller - Gönderi yönetimi REST API
 * Requirements: FR-SM-001, FR-SM-003, FR-SM-004, FR-SM-005, FR-SM-006
 */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shipment Management", description = "Gönderi yönetimi API'leri")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {
    
    private final ShipmentService shipmentService;
    private final UserServiceClient userServiceClient;
    
    /**
     * Yeni gönderi oluşturma
     * Requirements: FR-SM-001 - Yetkili kullanıcılar yeni kargo gönderileri oluşturabilmelidir
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Yeni gönderi oluştur", description = "Yeni bir kargo gönderisi oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Gönderi başarıyla oluşturuldu"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
        @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli"),
        @ApiResponse(responseCode = "403", description = "Yetki yok")
    })
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request,
            Authentication authentication) {
        
        log.info("Yeni gönderi oluşturma isteği alındı");
        
        // Şimdilik test için sabit kullanıcı ID
        Long userId = 1L; // getUserIdFromAuthentication(authentication);
        
        ShipmentResponse response = shipmentService.createShipment(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Takip numarasına göre gönderi sorgulama
     * Requirements: FR-SM-003 - Gönderi detaylarını görüntüleme
     */
    @GetMapping("/track/{trackingNumber}")
    @Operation(summary = "Gönderi takip et", description = "Takip numarası ile gönderi durumunu sorgular")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gönderi bulundu"),
        @ApiResponse(responseCode = "404", description = "Gönderi bulunamadı")
    })
    public ResponseEntity<ShipmentResponse> trackShipment(
            @Parameter(description = "Takip numarası", example = "CT1234567890")
            @PathVariable String trackingNumber) {
        
        log.info("Gönderi takip isteği. Takip numarası: {}", trackingNumber);
        
        return shipmentService.findByTrackingNumber(trackingNumber)
                .map(shipment -> ResponseEntity.ok(shipment))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Gönderi bilgilerini güncelleme
     * Requirements: FR-SM-004 - Gönderi bilgilerini güncelleme
     */
    @PutMapping("/{shipmentId}")
    @Operation(summary = "Gönderi güncelle", description = "Mevcut gönderi bilgilerini günceller")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gönderi başarıyla güncellendi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz istek verisi"),
        @ApiResponse(responseCode = "404", description = "Gönderi bulunamadı"),
        @ApiResponse(responseCode = "409", description = "Gönderi bu durumda güncellenemez")
    })
    public ResponseEntity<ShipmentResponse> updateShipment(
            @Parameter(description = "Gönderi ID", example = "1")
            @PathVariable Long shipmentId,
            @Valid @RequestBody UpdateShipmentRequest request) {
        
        log.info("Gönderi güncelleme isteği. ID: {}", shipmentId);
        
        Long userId = 1L; // Test için sabit kullanıcı ID
        
        try {
            ShipmentResponse response = shipmentService.updateShipment(shipmentId, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Gönderi güncelleme hatası: {}", e.getMessage());
            if (e.getMessage().contains("bulunamadı")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("güncellenemez")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Gönderi iptal etme
     * Requirements: FR-SM-005 - Gönderi iptal etme
     */
    @DeleteMapping("/{shipmentId}")
    @Operation(summary = "Gönderi iptal et", description = "Mevcut gönderiyi iptal eder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gönderi başarıyla iptal edildi"),
        @ApiResponse(responseCode = "404", description = "Gönderi bulunamadı"),
        @ApiResponse(responseCode = "409", description = "Gönderi bu durumda iptal edilemez")
    })
    public ResponseEntity<ShipmentResponse> cancelShipment(
            @Parameter(description = "Gönderi ID", example = "1")
            @PathVariable Long shipmentId) {
        
        log.info("Gönderi iptal isteği. ID: {}", shipmentId);
        
        Long userId = 1L; // Test için sabit kullanıcı ID
        
        try {
            ShipmentResponse response = shipmentService.cancelShipment(shipmentId, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Gönderi iptal hatası: {}", e.getMessage());
            if (e.getMessage().contains("bulunamadı")) {
                return ResponseEntity.notFound().build();
            } else if (e.getMessage().contains("iptal edilemez")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Gönderi durumu güncelleme
     * Requirements: FR-SM-006 - Gönderi durumu güncelleme
     */
    @PatchMapping("/{shipmentId}/status")
    @Operation(summary = "Gönderi durumu güncelle", description = "Gönderi durumunu günceller")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Durum başarıyla güncellendi"),
        @ApiResponse(responseCode = "400", description = "Geçersiz durum geçişi"),
        @ApiResponse(responseCode = "404", description = "Gönderi bulunamadı")
    })
    public ResponseEntity<ShipmentResponse> updateShipmentStatus(
            @Parameter(description = "Gönderi ID", example = "1")
            @PathVariable Long shipmentId,
            @Parameter(description = "Yeni durum")
            @RequestParam Shipment.ShipmentStatus status) {
        
        log.info("Gönderi durum güncelleme isteği. ID: {}, Yeni durum: {}", shipmentId, status);
        
        Long userId = 1L; // Test için sabit kullanıcı ID
        
        try {
            ShipmentResponse response = shipmentService.updateShipmentStatus(shipmentId, status, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Durum güncelleme hatası: {}", e.getMessage());
            if (e.getMessage().contains("bulunamadı")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Kullanıcının gönderilerini listeleme
     * Requirements: FR-SM-003 - Gönderi detaylarını görüntüleme
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcı gönderileri", description = "Belirtilen kullanıcının gönderilerini listeler")
    @ApiResponse(responseCode = "200", description = "Gönderiler başarıyla listelendi")
    public ResponseEntity<Page<ShipmentResponse>> getUserShipments(
            @Parameter(description = "Kullanıcı ID", example = "1")
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Kullanıcı gönderileri listeleme isteği. Kullanıcı ID: {}", userId);
        
        Page<ShipmentResponse> shipments = shipmentService.findUserShipments(userId, pageable);
        return ResponseEntity.ok(shipments);
    }
    
    /**
     * Carrier'a atanmış gönderileri listeleme
     * Requirements: Carrier rolündeki kullanıcıların atandığı gönderileri görüntülemesi
     */
    @GetMapping("/carrier/assigned")
    @PreAuthorize("hasRole('CARRIER')")
    @Operation(summary = "Carrier gönderileri", description = "Carrier'a atanmış gönderileri listeler")
    @ApiResponse(responseCode = "200", description = "Carrier gönderileri başarıyla listelendi")
    public ResponseEntity<List<ShipmentResponse>> getCarrierShipments(Authentication authentication) {
        
        log.info("🔍 Carrier gönderileri listeleme isteği. Username: {}", authentication.getName());
        
        try {
            // JWT'den username al ve user service'ten user ID'yi getir
            String username = authentication.getName();
            
            // UserService'ten user bilgilerini al
            UserServiceClient.ApiResponseWrapper<UserServiceClient.UserDto> userResponse = 
                userServiceClient.getUserByUsername(username);
            
            if (!userResponse.isSuccess() || userResponse.getData() == null) {
                log.error("❌ Kullanıcı bulunamadı. Username: {}", username);
                return ResponseEntity.badRequest().build();
            }
            
            Long carrierId = userResponse.getData().getId();
            log.info("✅ Carrier ID bulundu: {} (Username: {})", carrierId, username);
            
            List<ShipmentResponse> shipments = shipmentService.findCarrierShipments(carrierId);
            return ResponseEntity.ok(shipments);
            
        } catch (Exception e) {
            log.error("❌ Carrier gönderileri listeleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Tüm kargo şirketlerini getir
     * Shipment oluşturma sırasında dropdown için kullanılır
     */
    @GetMapping("/companies")
    @Operation(summary = "Kargo şirketleri", description = "Tüm aktif kargo şirketlerini listeler")
    @ApiResponse(responseCode = "200", description = "Kargo şirketleri başarıyla listelendi")
    public ResponseEntity<List<UserServiceClient.UserDto>> getShipmentCompanies() {
        log.info("Kargo şirketleri listeleme isteği alındı");
        
        try {
            UserServiceClient.ApiResponseWrapper<List<UserServiceClient.UserDto>> response = 
                userServiceClient.getShipmentCompanies();
            
            if (response.isSuccess() && response.getData() != null) {
                return ResponseEntity.ok(response.getData());
            } else {
                log.warn("Kargo şirketleri getirilemedi: {}", response.getMessage());
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
            log.error("Kargo şirketleri getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gönderi ID ile detay görüntüleme
     * Requirements: FR-SM-003 - Gönderi detaylarını görüntüleme
     */
    @GetMapping("/{shipmentId}")
    @Operation(summary = "Gönderi detayı", description = "Gönderi ID ile detayları getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Gönderi bulundu"),
        @ApiResponse(responseCode = "404", description = "Gönderi bulunamadı")
    })
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @Parameter(description = "Gönderi ID", example = "1")
            @PathVariable Long shipmentId) {
        
        log.info("Gönderi detay isteği. ID: {}", shipmentId);
        
        return shipmentService.findById(shipmentId)
                .map(shipment -> ResponseEntity.ok(shipment))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Servis sağlık durumunu kontrol eder")
    @ApiResponse(responseCode = "200", description = "Servis çalışıyor")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "shipment-service",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    /**
     * Authentication'dan kullanıcı ID'sini alma
     * Gerçek implementasyonda JWT token'dan parse edilecek
     */
    /*
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Şimdilik basit implementasyon - gerçekte JWT'den parse edilecek
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            // Eğer username numeric değilse, default olarak 1 döndür (test için)
            return 1L;
        }
    }
    */

    /**
     * Dashboard istatistikleri getirme
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Dashboard istatistikleri", description = "Dashboard için temel istatistikleri getirir")
    @ApiResponse(responseCode = "200", description = "İstatistikler başarıyla getirildi")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        log.info("Dashboard istatistikleri istendi");
        
        try {
            Map<String, Object> stats = shipmentService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Dashboard istatistikleri alınırken hata: {}", e.getMessage());
            // Hata durumunda fallback veriler
            Map<String, Object> fallbackStats = Map.of(
                "totalShipments", 0,
                "activeShipments", 0,
                "deliveredShipments", 0,
                "monthlyGrowth", 0.0,
                "recentShipments", List.of()
            );
            return ResponseEntity.ok(fallbackStats);
        }
    }

    /**
     * Test verisi oluşturma - sadece development için
     */
    @PostMapping("/create-test-data")
    @Operation(summary = "Test verisi oluştur", description = "Carrier test etmek için örnek gönderiler oluşturur")
    @ApiResponse(responseCode = "200", description = "Test verisi başarıyla oluşturuldu")
    public ResponseEntity<Map<String, Object>> createTestData() {
        log.info("Test verisi oluşturuluyor");
        
        try {
            List<ShipmentResponse> createdShipments = shipmentService.createTestShipments();
            return ResponseEntity.ok(Map.of(
                "message", "Test verisi başarıyla oluşturuldu",
                "createdShipments", createdShipments.size(),
                "shipments", createdShipments
            ));
        } catch (Exception e) {
            log.error("Test verisi oluştururken hata: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Test verisi oluşturulamadı: " + e.getMessage()
            ));
        }
    }

}
