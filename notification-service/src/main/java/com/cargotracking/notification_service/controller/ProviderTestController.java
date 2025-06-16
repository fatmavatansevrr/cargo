package com.cargotracking.notification_service.controller;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.service.EmailService;
import com.cargotracking.notification_service.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider Test Controller
 * Email ve SMS provider'larını manuel test etmek için
 */
@RestController
@RequestMapping("/api/test/providers")
@RequiredArgsConstructor
@Slf4j
public class ProviderTestController {
    
    private final EmailService emailService;
    private final SmsService smsService;
    
    /**
     * Email provider test
     */
    @PostMapping("/email/send")
    public ResponseEntity<Map<String, Object>> testEmailSend(@RequestBody EmailTestRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notification notification = createEmailNotification(request);
            boolean success = emailService.sendEmail(notification);
            
            response.put("success", success);
            response.put("message", success ? "📧 Email sent successfully" : "❌ Email sending failed");
            response.put("recipient", request.getRecipient());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Email test failed", e);
            response.put("success", false);
            response.put("message", "❌ Email test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * SMS provider test
     */
    @PostMapping("/sms/send")
    public ResponseEntity<Map<String, Object>> testSmsSend(@RequestBody SmsTestRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Notification notification = createSmsNotification(request);
            boolean success = smsService.sendSms(notification);
            
            response.put("success", success);
            response.put("message", success ? "📱 SMS sent successfully" : "❌ SMS sending failed");
            response.put("recipient", request.getRecipient());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SMS test failed", e);
            response.put("success", false);
            response.put("message", "❌ SMS test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Bulk email test
     */
    @PostMapping("/email/bulk")
    public ResponseEntity<Map<String, Object>> testBulkEmail(@RequestBody BulkEmailTestRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Notification> notifications = request.getRecipients().stream()
                .map(email -> createEmailNotification(new EmailTestRequest(email, request.getSubject(), request.getMessage())))
                .toList();
            
            int successCount = emailService.sendBulkEmailAdvanced(notifications);
            
            response.put("success", successCount > 0);
            response.put("totalSent", successCount);
            response.put("totalRequested", request.getRecipients().size());
            response.put("successRate", (double) successCount / request.getRecipients().size() * 100);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Bulk email test failed", e);
            response.put("success", false);
            response.put("message", "Bulk email test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Bulk SMS test
     */
    @PostMapping("/sms/bulk")
    public ResponseEntity<Map<String, Object>> testBulkSms(@RequestBody BulkSmsTestRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Notification> notifications = request.getRecipients().stream()
                .map(phone -> createSmsNotification(new SmsTestRequest(phone, request.getMessage())))
                .toList();
            
            int successCount = smsService.sendBulkSms(notifications);
            
            response.put("success", successCount > 0);
            response.put("totalSent", successCount);
            response.put("totalRequested", request.getRecipients().size());
            response.put("successRate", (double) successCount / request.getRecipients().size() * 100);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Bulk SMS test failed", e);
            response.put("success", false);
            response.put("message", "Bulk SMS test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Email connection test
     */
    @GetMapping("/email/connection")
    public ResponseEntity<Map<String, Object>> testEmailConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = emailService.testEmailConnection();
            
            response.put("success", success);
            response.put("message", success ? "Email connection successful" : "Email connection failed");
            response.put("provider", "Gmail SMTP");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Email connection test failed", e);
            response.put("success", false);
            response.put("message", "Email connection test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * SMS connection test
     */
    @GetMapping("/sms/connection")
    public ResponseEntity<Map<String, Object>> testSmsConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = smsService.testSmsConnection();
            
            response.put("success", success);
            response.put("message", success ? "SMS connection successful" : "SMS connection failed");
            response.put("provider", "Twilio");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("SMS connection test failed", e);
            response.put("success", false);
            response.put("message", "SMS connection test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Phone number validation test
     */
    @PostMapping("/sms/validate")
    public ResponseEntity<Map<String, Object>> validatePhoneNumber(@RequestBody PhoneValidationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isValid = smsService.isValidPhoneNumber(request.getPhoneNumber());
        
        response.put("phoneNumber", request.getPhoneNumber());
        response.put("isValid", isValid);
        response.put("format", isValid ? "E.164" : "Invalid");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Provider status check
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProviderStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // Email provider status
        Map<String, Object> emailStatus = new HashMap<>();
        try {
            boolean emailConnected = emailService.testEmailConnection();
            emailStatus.put("connected", emailConnected);
            emailStatus.put("provider", "Gmail SMTP");
            emailStatus.put("status", emailConnected ? "✅ ACTIVE" : "❌ INACTIVE");
        } catch (Exception e) {
            emailStatus.put("connected", false);
            emailStatus.put("status", "❌ ERROR");
            emailStatus.put("error", e.getMessage());
        }
        
        // SMS provider status
        Map<String, Object> smsStatus = new HashMap<>();
        try {
            boolean smsConnected = smsService.testSmsConnection();
            smsStatus.put("connected", smsConnected);
            smsStatus.put("provider", "Twilio");
            smsStatus.put("status", smsConnected ? "✅ ACTIVE" : "❌ INACTIVE");
        } catch (Exception e) {
            smsStatus.put("connected", false);
            smsStatus.put("status", "❌ ERROR");
            smsStatus.put("error", e.getMessage());
        }
        
        response.put("email", emailStatus);
        response.put("sms", smsStatus);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    // Helper methods
    private Notification createEmailNotification(EmailTestRequest request) {
        Notification notification = new Notification();
        notification.setId("test-email-" + System.currentTimeMillis());
        notification.setUserId(999L);
        notification.setType(Notification.NotificationType.SHIPMENT_CREATED);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setRecipient(request.getRecipient());
        notification.setTitle(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    private Notification createSmsNotification(SmsTestRequest request) {
        Notification notification = new Notification();
        notification.setId("test-sms-" + System.currentTimeMillis());
        notification.setUserId(999L);
        notification.setType(Notification.NotificationType.STATUS_CHANGED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient(request.getRecipient());
        notification.setTitle("Test SMS");
        notification.setMessage(request.getMessage());
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    // Request DTOs
    public static class EmailTestRequest {
        private String recipient;
        private String subject;
        private String message;
        
        public EmailTestRequest() {}
        
        public EmailTestRequest(String recipient, String subject, String message) {
            this.recipient = recipient;
            this.subject = subject;
            this.message = message;
        }
        
        // Getters and setters
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class SmsTestRequest {
        private String recipient;
        private String message;
        
        public SmsTestRequest() {}
        
        public SmsTestRequest(String recipient, String message) {
            this.recipient = recipient;
            this.message = message;
        }
        
        // Getters and setters
        public String getRecipient() { return recipient; }
        public void setRecipient(String recipient) { this.recipient = recipient; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class BulkEmailTestRequest {
        private List<String> recipients;
        private String subject;
        private String message;
        
        // Getters and setters
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class BulkSmsTestRequest {
        private List<String> recipients;
        private String message;
        
        // Getters and setters
        public List<String> getRecipients() { return recipients; }
        public void setRecipients(List<String> recipients) { this.recipients = recipients; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class PhoneValidationRequest {
        private String phoneNumber;
        
        // Getters and setters
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
} 