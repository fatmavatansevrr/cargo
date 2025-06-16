package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * NotificationPreferenceController - Notification preferences REST API
 * Kullanıcı bildirim tercihlerini yönetmek için REST endpoints
 */
@RestController
@RequestMapping("/api/notification-preferences")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationPreferenceController {
    
    private final NotificationPreferenceRepository preferenceRepository;
    
    /**
     * Kullanıcının bildirim tercihlerini getir
     * GET /api/notification-preferences/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<NotificationPreference> getUserPreferences(@PathVariable Long userId) {
        
        log.info("Getting notification preferences for user: {}", userId);
        
        try {
            Optional<NotificationPreference> preference = preferenceRepository.findByUserId(userId);
            
            if (preference.isPresent()) {
                return ResponseEntity.ok(preference.get());
            } else {
                // Varsayılan tercihler oluştur
                NotificationPreference defaultPreference = createDefaultPreferences(userId);
                return ResponseEntity.ok(defaultPreference);
            }
            
        } catch (Exception e) {
            log.error("Error getting preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Kullanıcının bildirim tercihlerini güncelle veya oluştur
     * PUT /api/notification-preferences/user/{userId}
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<NotificationPreference> updateUserPreferences(
            @PathVariable Long userId,
            @Valid @RequestBody NotificationPreference preferences) {
        
        log.info("Updating notification preferences for user: {}", userId);
        
        try {
            // Mevcut tercihleri kontrol et
            Optional<NotificationPreference> existingPreference = preferenceRepository.findByUserId(userId);
            
            NotificationPreference toSave;
            if (existingPreference.isPresent()) {
                // Mevcut tercihleri güncelle
                toSave = existingPreference.get();
                toSave.setEmail(preferences.getEmail());
                toSave.setPhoneNumber(preferences.getPhoneNumber());
                toSave.setPreferences(preferences.getPreferences());
                toSave.setEmailEnabled(preferences.isEmailEnabled());
                toSave.setSmsEnabled(preferences.isSmsEnabled());
                toSave.setPushEnabled(preferences.isPushEnabled());
                toSave.setInAppEnabled(preferences.isInAppEnabled());
                toSave.setTimezone(preferences.getTimezone());
                toSave.setQuietHoursStart(preferences.getQuietHoursStart());
                toSave.setQuietHoursEnd(preferences.getQuietHoursEnd());
                toSave.setRealTimeNotifications(preferences.isRealTimeNotifications());
                toSave.setDailySummary(preferences.isDailySummary());
                toSave.setWeeklySummary(preferences.isWeeklySummary());
                toSave.setUpdatedAt(LocalDateTime.now());
            } else {
                // Yeni tercihler oluştur
                toSave = preferences;
                toSave.setUserId(userId);
                toSave.setCreatedAt(LocalDateTime.now());
                toSave.setUpdatedAt(LocalDateTime.now());
            }
            
            NotificationPreference saved = preferenceRepository.save(toSave);
            
            return ResponseEntity.ok(saved);
            
        } catch (Exception e) {
            log.error("Error updating preferences for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Belirli bir bildirim türü için kanalları güncelle
     * PUT /api/notification-preferences/user/{userId}/type/{notificationType}
     */
    @PutMapping("/user/{userId}/type/{notificationType}")
    public ResponseEntity<Map<String, Object>> updateNotificationTypePreferences(
            @PathVariable Long userId,
            @PathVariable String notificationType,
            @RequestBody Map<String, Boolean> channelPreferences) {
        
        log.info("Updating preferences for user: {}, type: {}", userId, notificationType);
        
        try {
            Optional<NotificationPreference> existingPreference = preferenceRepository.findByUserId(userId);
            
            NotificationPreference preference;
            if (existingPreference.isPresent()) {
                preference = existingPreference.get();
            } else {
                preference = createDefaultPreferences(userId);
            }
            
            // Channel preferences güncelle
            // TODO: Implementation for specific notification type preferences
            
            preference.setUpdatedAt(LocalDateTime.now());
            NotificationPreference saved = preferenceRepository.save(preference);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("notificationType", notificationType);
            response.put("updated", true);
            response.put("preferences", saved);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating type preferences for user: {}, type: {}", userId, notificationType, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Email tercihini güncelle
     * PUT /api/notification-preferences/user/{userId}/email
     */
    @PutMapping("/user/{userId}/email")
    public ResponseEntity<Map<String, Object>> updateEmailPreference(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> emailData) {
        
        log.info("Updating email preference for user: {}", userId);
        
        try {
            Optional<NotificationPreference> existingPreference = preferenceRepository.findByUserId(userId);
            
            NotificationPreference preference;
            if (existingPreference.isPresent()) {
                preference = existingPreference.get();
            } else {
                preference = createDefaultPreferences(userId);
            }
            
            // Email bilgilerini güncelle
            if (emailData.containsKey("email")) {
                preference.setEmail((String) emailData.get("email"));
            }
            if (emailData.containsKey("enabled")) {
                preference.setEmailEnabled((Boolean) emailData.get("enabled"));
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            NotificationPreference saved = preferenceRepository.save(preference);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("email", saved.getEmail());
            response.put("emailEnabled", saved.isEmailEnabled());
            response.put("updated", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating email preference for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * SMS tercihini güncelle
     * PUT /api/notification-preferences/user/{userId}/sms
     */
    @PutMapping("/user/{userId}/sms")
    public ResponseEntity<Map<String, Object>> updateSmsPreference(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> smsData) {
        
        log.info("Updating SMS preference for user: {}", userId);
        
        try {
            Optional<NotificationPreference> existingPreference = preferenceRepository.findByUserId(userId);
            
            NotificationPreference preference;
            if (existingPreference.isPresent()) {
                preference = existingPreference.get();
            } else {
                preference = createDefaultPreferences(userId);
            }
            
            // SMS bilgilerini güncelle
            if (smsData.containsKey("phoneNumber")) {
                preference.setPhoneNumber((String) smsData.get("phoneNumber"));
            }
            if (smsData.containsKey("enabled")) {
                preference.setSmsEnabled((Boolean) smsData.get("enabled"));
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            NotificationPreference saved = preferenceRepository.save(preference);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("phoneNumber", saved.getPhoneNumber());
            response.put("smsEnabled", saved.isSmsEnabled());
            response.put("updated", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating SMS preference for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Sessiz saatleri güncelle
     * PUT /api/notification-preferences/user/{userId}/quiet-hours
     */
    @PutMapping("/user/{userId}/quiet-hours")
    public ResponseEntity<Map<String, Object>> updateQuietHours(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> quietHours) {
        
        log.info("Updating quiet hours for user: {}", userId);
        
        try {
            Optional<NotificationPreference> existingPreference = preferenceRepository.findByUserId(userId);
            
            NotificationPreference preference;
            if (existingPreference.isPresent()) {
                preference = existingPreference.get();
            } else {
                preference = createDefaultPreferences(userId);
            }
            
            if (quietHours.containsKey("start")) {
                preference.setQuietHoursStart(quietHours.get("start"));
            }
            if (quietHours.containsKey("end")) {
                preference.setQuietHoursEnd(quietHours.get("end"));
            }
            
            preference.setUpdatedAt(LocalDateTime.now());
            NotificationPreference saved = preferenceRepository.save(preference);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("quietHoursStart", saved.getQuietHoursStart());
            response.put("quietHoursEnd", saved.getQuietHoursEnd());
            response.put("updated", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating quiet hours for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Kullanıcı tercihlerini sil
     * DELETE /api/notification-preferences/user/{userId}
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> deleteUserPreferences(@PathVariable Long userId) {
        
        log.info("Deleting notification preferences for user: {}", userId);
        
        try {
            preferenceRepository.deleteByUserId(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("userId", userId.toString());
            response.put("status", "deleted");
            response.put("message", "Kullanıcı bildirim tercihleri silindi");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting preferences for user: {}", userId, e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Tercihler silinemedi");
            errorResponse.put("userId", userId.toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Varsayılan tercihler oluştur
     */
    private NotificationPreference createDefaultPreferences(Long userId) {
        NotificationPreference defaultPreference = new NotificationPreference();
        defaultPreference.setUserId(userId);
        defaultPreference.setEmailEnabled(true);
        defaultPreference.setSmsEnabled(false);
        defaultPreference.setPushEnabled(true);
        defaultPreference.setInAppEnabled(true);
        defaultPreference.setTimezone("Europe/Istanbul");
        defaultPreference.setQuietHoursStart(22);
        defaultPreference.setQuietHoursEnd(8);
        defaultPreference.setRealTimeNotifications(true);
        defaultPreference.setDailySummary(false);
        defaultPreference.setWeeklySummary(false);
        defaultPreference.setCreatedAt(LocalDateTime.now());
        defaultPreference.setUpdatedAt(LocalDateTime.now());
        
        return preferenceRepository.save(defaultPreference);
    }
} 