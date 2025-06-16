package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * Notification preferences REST controller
 * FR-NT-004: Kullanıcılar bildirim tercihlerini yönetebilir
 */
@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Notification Preferences", description = "Bildirim tercihleri yönetimi API'leri")
public class NotificationPreferenceController {
    
    private final NotificationPreferenceService preferenceService;
    
    /**
     * Kullanıcının bildirim tercihlerini getirir
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Kullanıcı bildirim tercihlerini getir", 
               description = "Belirtilen kullanıcının bildirim tercihlerini getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bildirim tercihleri başarıyla getirildi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = NotificationPreference.class))),
        @ApiResponse(responseCode = "404", description = "Kullanıcı tercihleri bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> getUserPreferences(@PathVariable Long userId) {
        try {
            log.info("Kullanıcı bildirim tercihleri istendi: {}", userId);
            NotificationPreference preference = preferenceService.getUserPreferences(userId);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            log.error("Kullanıcı tercihleri getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Email ile kullanıcının bildirim tercihlerini getirir
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Email ile bildirim tercihlerini getir", 
               description = "Belirtilen email adresine sahip kullanıcının bildirim tercihlerini getirir")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bildirim tercihleri başarıyla getirildi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = NotificationPreference.class))),
        @ApiResponse(responseCode = "404", description = "Email ile kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> getPreferencesByEmail(@PathVariable String email) {
        try {
            log.info("Email ile bildirim tercihleri istendi: {}", email);
            NotificationPreference preference = preferenceService.getPreferencesByEmail(email);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            log.error("Email ile tercihleri getirme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Kullanıcı bildirim tercihlerini oluşturur
     */
    @PostMapping
    @Operation(summary = "Bildirim tercihlerini oluştur", 
               description = "Yeni kullanıcı bildirim tercihleri oluşturur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bildirim tercihleri başarıyla oluşturuldu",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = NotificationPreference.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz veri"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> createPreferences(@Valid @RequestBody NotificationPreference preference) {
        try {
            log.info("Yeni bildirim tercihleri oluşturuluyor: userId={}, email={}", 
                    preference.getUserId(), preference.getUserEmail());
            
            NotificationPreference createdPreference = preferenceService.createPreferences(preference);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPreference);
        } catch (Exception e) {
            log.error("Bildirim tercihleri oluşturma hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Kullanıcı bildirim tercihlerini günceller
     */
    @PutMapping("/user/{userId}")
    @Operation(summary = "Bildirim tercihlerini güncelle", 
               description = "Mevcut kullanıcı bildirim tercihlerini günceller")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bildirim tercihleri başarıyla güncellendi",
                    content = @Content(mediaType = "application/json", 
                                     schema = @Schema(implementation = NotificationPreference.class))),
        @ApiResponse(responseCode = "400", description = "Geçersiz veri"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> updatePreferences(@PathVariable Long userId,
                                             @Valid @RequestBody NotificationPreference preference) {
        try {
            log.info("Bildirim tercihleri güncelleniyor: userId={}", userId);
            preference.setUserId(userId); // Ensure userId is set
            NotificationPreference updatedPreference = preferenceService.updatePreferences(preference);
            return ResponseEntity.ok(updatedPreference);
        } catch (Exception e) {
            log.error("Bildirim tercihleri güncelleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Email kanalını etkinleştirir/devre dışı bırakır
     */
    @PatchMapping("/user/{userId}/email")
    @Operation(summary = "Email bildirimlerini aç/kapat", 
               description = "Kullanıcının email bildirimlerini etkinleştirir veya devre dışı bırakır")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email bildirimleri başarıyla güncellendi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> toggleEmailNotifications(@PathVariable Long userId,
                                                    @RequestParam boolean enabled) {
        try {
            log.info("Email bildirimleri güncelleniyor: userId={}, enabled={}", userId, enabled);
            NotificationPreference preference = preferenceService.toggleEmailNotifications(userId, enabled);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            log.error("Email bildirim güncelleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * SMS kanalını etkinleştirir/devre dışı bırakır
     */
    @PatchMapping("/user/{userId}/sms")
    @Operation(summary = "SMS bildirimlerini aç/kapat", 
               description = "Kullanıcının SMS bildirimlerini etkinleştirir veya devre dışı bırakır")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "SMS bildirimleri başarıyla güncellendi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> toggleSmsNotifications(@PathVariable Long userId,
                                                  @RequestParam boolean enabled) {
        try {
            log.info("SMS bildirimleri güncelleniyor: userId={}, enabled={}", userId, enabled);
            NotificationPreference preference = preferenceService.toggleSmsNotifications(userId, enabled);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            log.error("SMS bildirim güncelleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Belirli bir status için bildirimleri etkinleştirir/devre dışı bırakır
     */
    @PatchMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Durum bildirimleri aç/kapat", 
               description = "Belirli bir durum için bildirimleri etkinleştirir veya devre dışı bırakır")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Durum bildirimleri başarıyla güncellendi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> toggleStatusNotification(@PathVariable Long userId,
                                                    @PathVariable String status,
                                                    @RequestParam boolean enabled) {
        try {
            log.info("Status bildirimi güncelleniyor: userId={}, status={}, enabled={}", 
                    userId, status, enabled);
            NotificationPreference preference = preferenceService.toggleStatusNotification(userId, status, enabled);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            log.error("Status bildirim güncelleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Kullanıcı bildirim tercihlerini siler
     */
    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Bildirim tercihlerini sil", 
               description = "Kullanıcının tüm bildirim tercihlerini siler")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bildirim tercihleri başarıyla silindi"),
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı"),
        @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    public ResponseEntity<?> deletePreferences(@PathVariable Long userId) {
        try {
            log.info("Bildirim tercihleri siliniyor: userId={}", userId);
            preferenceService.deletePreferences(userId);
            return ResponseEntity.ok(Map.of("message", "Bildirim tercihleri başarıyla silindi"));
        } catch (Exception e) {
            log.error("Bildirim tercihleri silme hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
} 