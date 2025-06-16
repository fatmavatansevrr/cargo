package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.NotificationLog;
import com.cargotracking.notification_service.repository.NotificationLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification log görüntüleme controller'ı
 * Admin ve debugging amaçlı kullanılır
 */
@RestController
@RequestMapping("/api/notification-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Logs", description = "Bildirim logları görüntüleme API'leri")
public class NotificationLogController {
    
    private final NotificationLogRepository logRepository;
    
    /**
     * Kullanıcının notification log'larını getirir
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcı bildirim logları", 
               description = "Belirtilen kullanıcının bildirim loglarını sayfalı olarak getirir")
    public ResponseEntity<Page<NotificationLog>> getUserLogs(
            @Parameter(description = "Kullanıcı ID'si") 
            @PathVariable Long userId,
            @Parameter(description = "Sayfa numarası (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Kullanıcı bildirim logları istendi: userId={}, page={}, size={}", userId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationLog> logs = logRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Kullanıcı bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Tracking number'a göre notification log'larını getirir
     */
    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Takip numarasına göre bildirim logları", 
               description = "Belirtilen takip numarasına ait bildirim loglarını sayfalı olarak getirir")
    public ResponseEntity<Page<NotificationLog>> getLogsByTrackingNumber(
            @Parameter(description = "Takip numarası") 
            @PathVariable String trackingNumber,
            @Parameter(description = "Sayfa numarası (0-based)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Sayfa boyutu") 
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Takip numarası bildirim logları istendi: trackingNumber={}, page={}, size={}", 
                trackingNumber, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationLog> logs = logRepository.findByTrackingNumberOrderByCreatedAtDesc(trackingNumber, pageable);
            
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Takip numarası bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Status'a göre notification log'larını getirir
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Duruma göre bildirim logları", 
               description = "Belirtilen durumdaki bildirim loglarını getirir (SENT, FAILED, PENDING)")
    public ResponseEntity<List<NotificationLog>> getLogsByStatus(
            @Parameter(description = "Bildirim durumu (SENT, FAILED, PENDING)") 
            @PathVariable String status) {
        
        log.info("Status bildirim logları istendi: status={}", status);
        
        try {
            List<NotificationLog> logs = logRepository.findByStatus(status);
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Status bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Tarih aralığına göre notification log'larını getirir
     */
    @GetMapping("/date-range")
    @Operation(summary = "Tarih aralığına göre bildirim logları", 
               description = "Belirtilen tarih aralığındaki bildirim loglarını getirir")
    public ResponseEntity<List<NotificationLog>> getLogsByDateRange(
            @Parameter(description = "Başlangıç tarihi (yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Bitiş tarihi (yyyy-MM-dd'T'HH:mm:ss)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Tarih aralığı bildirim logları istendi: startDate={}, endDate={}", startDate, endDate);
        
        try {
            List<NotificationLog> logs = logRepository.findByCreatedAtBetween(startDate, endDate);
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Tarih aralığı bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Başarısız bildirimler için retry işlemi
     */
    @GetMapping("/failed-retryable")
    @Operation(summary = "Tekrar denenebilir başarısız bildirimler", 
               description = "Tekrar deneme limitine ulaşmamış başarısız bildirimleri getirir")
    public ResponseEntity<List<NotificationLog>> getFailedRetryableLogs() {
        
        log.info("Tekrar denenebilir başarısız bildirimler istendi");
        
        try {
            List<NotificationLog> logs = logRepository.findFailedNotificationsForRetry(3); // max 3 retry
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Tekrar denenebilir başarısız bildirimler getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Kullanıcı ve kanal kombinasyonuna göre log'ları getirir
     */
    @GetMapping("/user/{userId}/channel/{channel}")
    @Operation(summary = "Kullanıcı ve kanala göre bildirim logları", 
               description = "Belirtilen kullanıcı ve bildirim kanalına göre logları getirir")
    public ResponseEntity<List<NotificationLog>> getLogsByUserAndChannel(
            @Parameter(description = "Kullanıcı ID'si") 
            @PathVariable Long userId,
            @Parameter(description = "Bildirim kanalı (EMAIL, SMS, PUSH)") 
            @PathVariable String channel) {
        
        log.info("Kullanıcı ve kanal bildirim logları istendi: userId={}, channel={}", userId, channel);
        
        try {
            List<NotificationLog> logs = logRepository.findByUserIdAndNotificationChannel(userId, channel);
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Kullanıcı ve kanal bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Belirli bir shipment'a ait tüm bildirimler
     */
    @GetMapping("/shipment/{shipmentId}")
    @Operation(summary = "Shipment bildirim logları", 
               description = "Belirtilen shipment ID'sine ait tüm bildirim loglarını getirir")
    public ResponseEntity<List<NotificationLog>> getLogsByShipment(
            @Parameter(description = "Shipment ID'si") 
            @PathVariable Long shipmentId) {
        
        log.info("Shipment bildirim logları istendi: shipmentId={}", shipmentId);
        
        try {
            List<NotificationLog> logs = logRepository.findByShipmentId(shipmentId);
            return ResponseEntity.ok(logs);
            
        } catch (Exception e) {
            log.error("Shipment bildirim logları getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 