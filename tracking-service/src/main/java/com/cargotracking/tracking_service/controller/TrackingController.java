package com.cargotracking.tracking_service.controller;

import com.cargotracking.tracking_service.dto.TrackingHistoryResponse;
import com.cargotracking.tracking_service.model.TrackingState;
import com.cargotracking.tracking_service.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tracking Management", description = "Gönderi takip API'leri")
@SecurityRequirement(name = "bearerAuth")
public class TrackingController {

    private final TrackingService trackingService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String STATUS_UPDATE_TOPIC = "status-update-topic";

    /**
    // 🔽 CACHE DESTEKLİ TEK TAKİP DURUMU GETİR
    @Operation(summary = "Tek bir takip kaydı getir", description = "Redis cache desteklidir. Cache varsa oradan, yoksa MongoDB'den döner.")
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<TrackingHistoryResponse> getTrackingInfo(@PathVariable String trackingNumber) {
        return trackingService.getTrackingInfo(trackingNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
     */

    /**
     * Takip numarasına göre takip durumu getirir
     * Roles: CUSTOMER, CARRIER, SHIPMENT_COMPANY
     */
    /**
    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CARRIER', 'SHIPMENT_COMPANY')")
    @Operation(summary = "Takip durumu getir", description = "Takip numarasına göre güncel gönderi durumunu getirir")
    public ResponseEntity<TrackingHistoryResponse> getTrackingStatus(@PathVariable String trackingNumber) {
        Optional<TrackingHistoryResponse> result = trackingService.getTrackingInfo(trackingNumber);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
     */


    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CARRIER', 'SHIPMENT_COMPANY')")
    @Operation(summary = "Takip durumu getir", description = "Redis cache desteklidir. Cache varsa oradan, yoksa MongoDB'den döner.")
    public ResponseEntity<TrackingHistoryResponse> getTrackingStatus(@PathVariable String trackingNumber) {
        Optional<TrackingHistoryResponse> result = trackingService.getTrackingInfo(trackingNumber);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }


    /**
     * Kargo durumu güncelleme - sadece Carrier yetkilidir
     * FR-TR-002 gibi bir gereksinimle eşlenebilir
     */
    @PutMapping("/{trackingNumber}/status")
    @PreAuthorize("hasRole('CARRIER')")
    @Operation(summary = "Takip durumu güncelle (PUT)", description = "Kargonun mevcut durumunu günceller. Bu işlem doğrudan veritabanında güncellenir.")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String trackingNumber,
            @RequestParam TrackingState newState,
            @RequestParam(required = false) String location) {

        log.info("Durum güncelleme isteği (PUT) alındı: tracking={}, state={}", trackingNumber, newState);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication != null ? authentication.getName() : "anonymous";

        try {
            trackingService.updateStatus(trackingNumber, newState, location, updatedBy);
            log.info("Durum başarıyla güncellendi: tracking={}, state={}", trackingNumber, newState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Durum güncellenirken hata oluştu: tracking={}, error={}", trackingNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Kargo durumu güncelleme (PATCH) - sadece Carrier yetkilidir
     * Takip durumu güncelle endpoint'i
     */
    @PatchMapping("/{trackingNumber}/status")
    @PreAuthorize("hasRole('CARRIER')")
    @Operation(summary = "Takip durumu güncelle (PATCH)", description = "Kargonun mevcut durumunu günceller. Bu işlem doğrudan veritabanında güncellenir.")
    public ResponseEntity<Void> patchUpdateStatus(
            @PathVariable String trackingNumber,
            @RequestParam TrackingState newState,
            @RequestParam(required = false) String location) {

        log.info("Durum güncelleme isteği (PATCH) alındı: tracking={}, state={}", trackingNumber, newState);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String updatedBy = authentication != null ? authentication.getName() : "anonymous";

        try {
            trackingService.updateStatus(trackingNumber, newState, location, updatedBy);
            log.info("Durum başarıyla güncellendi: tracking={}, state={}", trackingNumber, newState);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Durum güncellenirken hata oluştu: tracking={}, error={}", trackingNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Yönetim amaçlı: sistemdeki tüm takip kayıtlarını döner
     * Sadece SHIPMENT_COMPANY görebilir
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SHIPMENT_COMPANY')")
    @Operation(summary = "Tüm takipleri listele", description = "Sistem genelindeki tüm takip kayıtlarını getirir")
    public ResponseEntity<?> getAllTrackings() {
        return ResponseEntity.ok(trackingService.getAllTrackings());
    }

    /**
     * Test verisi oluşturma endpoint'i
     * CARRIER ve SHIPMENT_COMPANY yetkilidir
     */
    @PostMapping("/create-test-data")
    @PreAuthorize("hasAnyRole('CARRIER', 'SHIPMENT_COMPANY')")
    @Operation(summary = "Test takip verisi oluştur", description = "Test amaçlı tracking kayıtları oluşturur")
    public ResponseEntity<?> createTestTrackingData() {
        log.info("Test tracking verisi oluşturuluyor");
        try {
            var result = trackingService.createTestTrackingData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Test tracking verisi oluşturulurken hata: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Test verisi oluşturulamadı: " + e.getMessage());
        }
    }
}
