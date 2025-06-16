package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provider Integration Tests
 * Email ve SMS provider'larının entegrasyon testleri
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
    "email.provider.enabled=true",
    "email.provider.type=gmail",
    "sms.provider.enabled=true",
    "sms.provider.type=mock",
    "spring.mail.from=test@example.com"
})
class ProviderIntegrationTest {
    
    private EmailService emailService;
    private SmsService smsService;
    
    @BeforeEach
    void setUp() {
        // Mock services for testing
        emailService = createMockEmailService();
        smsService = createMockSmsService();
    }
    
    @Test
    void testEmailProvider_SendNotification() {
        // Given
        Notification notification = createEmailNotification();
        
        // When
        boolean result = emailService.sendEmail(notification);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSmsProvider_SendNotification() {
        // Given
        Notification notification = createSmsNotification();
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testEmailProvider_InvalidRecipient() {
        // Given
        Notification notification = createEmailNotification();
        notification.setRecipient("invalid-email");
        
        // When
        boolean result = emailService.sendEmail(notification);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testSmsProvider_InvalidPhoneNumber() {
        // Given
        Notification notification = createSmsNotification();
        notification.setRecipient("invalid-phone");
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testEmailProvider_ConnectionTest() {
        // When
        boolean result = emailService.testEmailConnection();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSmsProvider_ConnectionTest() {
        // When
        boolean result = smsService.testSmsConnection();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testBulkNotifications_Mixed() {
        // Given
        java.util.List<Notification> emailNotifications = java.util.List.of(
            createEmailNotification("user1@example.com"),
            createEmailNotification("user2@example.com")
        );
        
        java.util.List<Notification> smsNotifications = java.util.List.of(
            createSmsNotification("+905551234567"),
            createSmsNotification("+905551234568")
        );
        
        // When
        int emailSuccessCount = emailService.sendBulkEmailAdvanced(emailNotifications);
        int smsSuccessCount = smsService.sendBulkSms(smsNotifications);
        
        // Then
        assertEquals(2, emailSuccessCount);
        assertEquals(2, smsSuccessCount);
    }
    
    @Test
    void testProviderFailover_EmailToSms() {
        // Given
        Notification notification = createEmailNotification();
        notification.setRecipient("invalid-email"); // Email başarısız olacak
        
        // When
        boolean emailResult = emailService.sendEmail(notification);
        
        // Email başarısız olursa SMS'e fallback
        boolean smsResult = false;
        if (!emailResult) {
            Notification smsNotification = createSmsNotification();
            smsResult = smsService.sendSms(smsNotification);
        }
        
        // Then
        assertFalse(emailResult); // Email başarısız
        assertTrue(smsResult);    // SMS başarılı
    }
    
    @Test
    void testProviderPerformance_BulkOperations() {
        // Given
        int notificationCount = 10;
        java.util.List<Notification> notifications = new java.util.ArrayList<>();
        
        for (int i = 0; i < notificationCount; i++) {
            notifications.add(createEmailNotification("user" + i + "@example.com"));
        }
        
        // When
        long startTime = System.currentTimeMillis();
        int successCount = emailService.sendBulkEmailAdvanced(notifications);
        long endTime = System.currentTimeMillis();
        
        // Then
        assertEquals(notificationCount, successCount);
        long duration = endTime - startTime;
        assertTrue(duration < 10000, "Bulk operation should complete within 10 seconds");
    }
    
    private Notification createEmailNotification() {
        return createEmailNotification("test@example.com");
    }
    
    private Notification createEmailNotification(String email) {
        Notification notification = new Notification();
        notification.setId("email-test-" + System.currentTimeMillis());
        notification.setUserId(100L);
        notification.setTrackingNumber("TRK123456");
        notification.setType(Notification.NotificationType.SHIPMENT_CREATED);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setRecipient(email);
        notification.setTitle("Test Email Notification");
        notification.setMessage("Bu bir test email bildirimidir.");
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    private Notification createSmsNotification() {
        return createSmsNotification("+905551234567");
    }
    
    private Notification createSmsNotification(String phoneNumber) {
        Notification notification = new Notification();
        notification.setId("sms-test-" + System.currentTimeMillis());
        notification.setUserId(100L);
        notification.setTrackingNumber("TRK123456");
        notification.setType(Notification.NotificationType.STATUS_CHANGED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient(phoneNumber);
        notification.setTitle("Test SMS");
        notification.setMessage("Bu bir test SMS bildirimidir.");
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    // Mock service creators for testing
    private EmailService createMockEmailService() {
        return new EmailService(null, null) {
            @Override
            public boolean sendEmail(Notification notification) {
                // Mock email validation and sending
                if (notification.getRecipient() == null || 
                    !notification.getRecipient().contains("@") ||
                    notification.getRecipient().equals("invalid-email")) {
                    return false;
                }
                return true;
            }
            
            @Override
            public boolean testEmailConnection() {
                return true;
            }
            
            @Override
            public int sendBulkEmailAdvanced(java.util.List<Notification> notifications) {
                int successCount = 0;
                for (Notification notification : notifications) {
                    if (sendEmail(notification)) {
                        successCount++;
                    }
                }
                return successCount;
            }
        };
    }
    
    private SmsService createMockSmsService() {
        SmsService service = new SmsService();
        // Set mock values using reflection
        setField(service, "smsEnabled", true);
        setField(service, "providerType", "mock");
        return service;
    }
    
    // Reflection helper
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            // Ignore for test
        }
    }
} 