package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * NotificationController - Notification REST API endpoints
 * Bildirim yönetimi ve kullanıcı işlemleri için REST endpoints
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * Kullanıcının bildirimlerini getir (sayfalı)
     * GET /api/notifications/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting notifications for user: {}, page: {}, size: {}", userId, page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
            
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            log.error("Error getting user notifications for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Okunmamış bildirim sayısını getir
     * GET /api/notifications/user/{userId}/unread-count
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        
        log.info("Getting unread notification count for user: {}", userId);
        
        try {
            long unreadCount = notificationService.getUnreadCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("unreadCount", unreadCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting unread count for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bildirimi okundu olarak işaretle
     * PUT /api/notifications/{notificationId}/mark-read
     */
    @PutMapping("/{notificationId}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId) {
        
        log.info("Marking notification as read: {}", notificationId);
        
        try {
            notificationService.markAsRead(notificationId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("notificationId", notificationId);
            response.put("status", "marked_as_read");
            response.put("message", "Bildirim okundu olarak işaretlendi");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", notificationId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Bildirim okundu olarak işaretlenemedi");
            errorResponse.put("notificationId", notificationId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Manuel bildirim gönder
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@Valid @RequestBody Notification notification) {
        
        log.info("Sending manual notification to user: {}, channel: {}", 
                notification.getUserId(), notification.getChannel());
        
        try {
            Notification sentNotification = notificationService.sendNotification(notification);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(sentNotification);
            
        } catch (Exception e) {
            log.error("Error sending manual notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Başarısız bildirimleri yeniden dene
     * POST /api/notifications/retry-failed
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<Map<String, String>> retryFailedNotifications() {
        
        log.info("Retrying failed notifications");
        
        try {
            notificationService.retryFailedNotifications();
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Başarısız bildirimler yeniden denenmeye alındı");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrying failed notifications", e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Başarısız bildirimler yeniden denenemedi");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Takip numarasına göre bildirimleri getir
     * GET /api/notifications/tracking/{trackingNumber}
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<Map<String, Object>> getNotificationsByTracking(@PathVariable String trackingNumber) {
        
        log.info("Getting notifications for tracking number: {}", trackingNumber);
        
        try {
            // Bu method NotificationService'e eklenecek
            // List<Notification> notifications = notificationService.getNotificationsByTracking(trackingNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("trackingNumber", trackingNumber);
            response.put("message", "Bu özellik henüz implement edilmemiş");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting notifications for tracking: {}", trackingNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Bildirim türlerine göre istatistik
     * GET /api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String period) {
        
        log.info("Getting notification statistics for user: {}, period: {}", userId, period);
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalNotifications", 0);
            stats.put("sentNotifications", 0);
            stats.put("failedNotifications", 0);
            stats.put("readNotifications", 0);
            stats.put("message", "İstatistikler henüz implement edilmemiş");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting notification statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Health check endpoint
     * GET /api/notifications/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "notification-service");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        return ResponseEntity.ok(health);
    }
} 