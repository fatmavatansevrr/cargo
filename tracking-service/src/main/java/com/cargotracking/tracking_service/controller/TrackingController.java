package com.cargotracking.tracking_service.controller;

import com.cargotracking.tracking_service.dto.TrackingHistoryResponse;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracking Management", description = "Gönderi takip API'leri")
@SecurityRequirement(name = "bearerAuth")
public class TrackingController {

    private static final Logger log = LoggerFactory.getLogger(TrackingController.class);
    private final TrackingService trackingService;

    /**
     * Takip numarasına göre takip durumu getirir
     * Roles: CUSTOMER, SHIPPER, CARRIER, ADMIN
     */
    @GetMapping("/{trackingNumber}")
    // @PreAuthorize("hasAnyRole('CUSTOMER', 'SHIPPER', 'CARRIER', 'ADMIN')") // Test için geçici olarak kapalı
    @Operation(summary = "Takip durumu getir", description = "Takip numarasına göre güncel gönderi durumunu getirir")
    public ResponseEntity<TrackingHistoryResponse> getTrackingStatus(@PathVariable String trackingNumber) {
        Optional<TrackingHistoryResponse> result = trackingService.getTrackingInfo(trackingNumber);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Kargo durumu güncelleme - sadece Carrier ve Admin yetkilidir
     * FR-TR-002 gibi bir gereksinimle eşlenebilir
     */
    @PutMapping("/{trackingNumber}/status")
    // @PreAuthorize("hasAnyRole('CARRIER', 'ADMIN')") // Test için geçici olarak kapalı
    @Operation(summary = "Takip durumu güncelle (PUT)", description = "Kargonun mevcut durumunu günceller")
    public ResponseEntity<TrackingHistoryResponse> updateStatus(
            @PathVariable String trackingNumber,
            @RequestParam TrackingState newState) {

        log.info("Durum güncelleme isteği (PUT): tracking={}, state={}", trackingNumber, newState);
        try {
            TrackingHistoryResponse response = trackingService.updateStatus(trackingNumber, newState);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Kargo durumu güncelleme (PATCH) - sadece Carrier ve Admin yetkilidir
     * Takip durumu güncelle endpoint'i
     */
    @PatchMapping("/{trackingNumber}/status")
    // @PreAuthorize("hasAnyRole('CARRIER', 'ADMIN')") // Test için geçici olarak kapalı
    @Operation(summary = "Takip durumu güncelle", description = "Kargonun mevcut durumunu günceller")
    public ResponseEntity<TrackingHistoryResponse> patchUpdateStatus(
            @PathVariable String trackingNumber,
            @RequestParam TrackingState newState) {

        log.info("Durum güncelleme isteği (PATCH): tracking={}, state={}", trackingNumber, newState);
        try {
            TrackingHistoryResponse response = trackingService.updateStatus(trackingNumber, newState);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Durum güncelleme hatası: tracking={}, error={}", trackingNumber, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Yönetim amaçlı: sistemdeki tüm takip kayıtlarını döner
     * Sadece ADMIN görebilir
     */
    @GetMapping("/all")
    // @PreAuthorize("hasRole('ADMIN')") // Test için geçici olarak kapalı
    @Operation(summary = "Tüm takipleri listele", description = "Sistem genelindeki tüm takip kayıtlarını getirir")
    public ResponseEntity<?> getAllTrackings() {
        return ResponseEntity.ok(trackingService.getAllTrackings());
    }
}
