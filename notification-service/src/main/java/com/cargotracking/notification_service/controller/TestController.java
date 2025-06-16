package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.service.NotificationService;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test Controller - Notification service'i test etmek için
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    
    private final NotificationService notificationService;
    private final NotificationPreferenceRepository preferenceRepository;
    
    /**
     * Test: Email Notification
     */
    @PostMapping("/email")
    public String testEmailNotification(@RequestParam(defaultValue = "test@example.com") String email) {
        try {
            Long userId = 999L;
            createTestUserPreference(userId, email);
            
            ShipmentEvent event = new ShipmentEvent();
            event.setEventType("shipment.created");
            event.setShipmentId(12345L);
            event.setTrackingNumber("TRK" + System.currentTimeMillis());
            event.setSenderUserId(userId);
            event.setStatus("CREATED");
            event.setEventTimestamp(LocalDateTime.now());
            event.setEventSource("email-test");
            event.setCustomerName("Test Kullanıcı");
            event.setCustomerEmail(email);
            
            notificationService.processShipmentEvent(event);
            
            return "✅ Email notification sent to: " + email;
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    /**
     * Test: SMS Notification
     */
    @PostMapping("/sms")
    public String testSmsNotification(@RequestParam(defaultValue = "+905551234567") String phoneNumber) {
        try {
            Long userId = 998L;
            
            NotificationPreference preference = new NotificationPreference();
            preference.setUserId(userId);
            preference.setEmailEnabled(false);
            preference.setSmsEnabled(true);
            preference.setPushEnabled(false);
            preference.setPhoneNumber(phoneNumber);
                         // Notification types preferences map'i oluştur
             preference.setPreferences(Map.of(
                 Notification.NotificationType.SHIPMENT_CREATED, 
                 Set.of(Notification.NotificationChannel.SMS)
             ));
            preferenceRepository.save(preference);
            
            ShipmentEvent event = new ShipmentEvent();
            event.setEventType("shipment.created");
            event.setShipmentId(12345L);
            event.setTrackingNumber("TRK" + System.currentTimeMillis());
            event.setSenderUserId(userId);
            event.setStatus("CREATED");
            event.setEventTimestamp(LocalDateTime.now());
            event.setEventSource("sms-test");
            event.setCustomerName("SMS Test");
            event.setCustomerEmail("sms@example.com");
            
            notificationService.processShipmentEvent(event);
            
            return "✅ SMS notification sent to: " + phoneNumber + " (Mock SMS)";
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    /**
     * Test: Status Change Notification
     */
    @PostMapping("/status-change")
    public String testStatusChange(@RequestParam(defaultValue = "test@example.com") String email,
                                 @RequestParam(defaultValue = "IN_TRANSIT") String status) {
        try {
            Long userId = 997L;
            createTestUserPreference(userId, email);
            
            TrackingEvent event = new TrackingEvent();
            event.setEventType("status.changed");
            event.setShipmentId(12345L);
            event.setTrackingNumber("TRK" + System.currentTimeMillis());
            event.setUserId(userId);
            event.setCurrentStatus(status);
            event.setPreviousStatus("CREATED");
            event.setLocation("İstanbul");
            event.setDescription("Status changed to " + status);
            event.setEventTimestamp(LocalDateTime.now());
            event.setEventSource("status-test");
            
            notificationService.processTrackingEvent(event);
            
            return "✅ Status change notification sent: " + status;
            
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
    
    private void createTestUserPreference(Long userId, String email) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        preference.setEmailEnabled(true);
        preference.setSmsEnabled(false);
        preference.setPushEnabled(false);
        preference.setEmail(email);
        // Notification types preferences map'i oluştur
        preference.setPreferences(Map.of(
            Notification.NotificationType.SHIPMENT_CREATED, Set.of(Notification.NotificationChannel.EMAIL),
            Notification.NotificationType.STATUS_CHANGED, Set.of(Notification.NotificationChannel.EMAIL),
            Notification.NotificationType.DELIVERY_COMPLETED, Set.of(Notification.NotificationChannel.EMAIL)
        ));
        preferenceRepository.save(preference);
    }
} 