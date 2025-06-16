package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Basit NotificationController - Sadece temel işlevler
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "Basit notification yönetimi")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Kullanıcının bildirimlerini listele
     */
    @Operation(summary = "User Notifications", description = "Kullanıcının bildirimlerini listeler")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
            
            log.info("📋 User notifications retrieved: userId={}, count={}", userId, notifications.getTotalElements());
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            log.error("❌ Error retrieving user notifications: userId={}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Email tercihlerini güncelle
     */
    @Operation(summary = "Update Email Preference", description = "Email tercihlerini günceller")
    @PutMapping("/preferences/{userId}/email")
    public ResponseEntity<NotificationPreference> updateEmailPreference(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Email enabled") @RequestParam boolean enabled) {
        
        try {
            NotificationPreference preference = notificationService.updateEmailPreference(userId, enabled);
            
            log.info("📧 Email preference updated: userId={}, enabled={}", userId, enabled);
            return ResponseEntity.ok(preference);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ User not found: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("❌ Error updating email preference: userId={}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * User tercihlerini getir
     */
    @Operation(summary = "Get User Preferences", description = "Kullanıcının tercihlerini getirir")
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<NotificationPreference> getUserPreferences(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        try {
            NotificationPreference preferences = notificationService.getUserPreferences(userId);
            
            log.info("⚙️ User preferences retrieved: userId={}", userId);
            return ResponseEntity.ok(preferences);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ User not found: userId={}, error={}", userId, e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("❌ Error retrieving user preferences: userId={}", userId, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Manual notification gönder (Test için)
     */
    @Operation(summary = "Send Manual Notification", description = "Manuel bildirim gönderir (Test için)")
    @PostMapping("/send")
    public ResponseEntity<Notification> sendManualNotification(
            @RequestBody ManualNotificationRequest request) {
        
        try {
            log.info("📤 Manual notification request: userId={}, type={}, subject={}", 
                    request.getUserId(), request.getType(), request.getSubject());
            
            Notification notification = notificationService.sendManualNotification(
                    request.getUserId(), 
                    request.getType(), 
                    request.getSubject(), 
                    request.getContent()
            );
            
            log.info("✅ Manual notification sent: id={}", notification.getId());
            return ResponseEntity.ok(notification);
            
        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            log.error("❌ Error sending manual notification: {}", request, e);
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * Basit manual notification request DTO
     */
    public static class ManualNotificationRequest {
        private String userId;
        private String type;
        private String subject;
        private String content;
        
        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        @Override
        public String toString() {
            return "ManualNotificationRequest{userId='" + userId + "', type='" + type + "', subject='" + subject + "'}";
        }
    }
} 