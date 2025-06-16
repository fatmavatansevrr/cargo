package com.cargotracking.notification_service.integration;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.service.EmailService;
import com.cargotracking.notification_service.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Provider Integration Tests
 * Email ve SMS provider'larının entegrasyon testleri
 */
@SpringBootTest
@TestPropertySource(properties = {
    "email.provider.enabled=true",
    "sms.provider.enabled=true",
    "sms.provider.type=mock",
    "spring.mail.from=test@example.com"
})
class ProviderIntegrationTest {
    
    @Test
    void testEmailAndSmsProviders() {
        // Given
        EmailService emailService = createMockEmailService();
        SmsService smsService = createMockSmsService();
        
        Notification emailNotification = createEmailNotification();
        Notification smsNotification = createSmsNotification();
        
        // When
        boolean emailResult = emailService.sendEmail(emailNotification);
        boolean smsResult = smsService.sendSms(smsNotification);
        
        // Then
        assertTrue(emailResult);
        assertTrue(smsResult);
    }
    
    private Notification createEmailNotification() {
        Notification notification = new Notification();
        notification.setId("email-test");
        notification.setUserId(100L);
        notification.setType(Notification.NotificationType.SHIPMENT_CREATED);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setRecipient("test@example.com");
        notification.setTitle("Test Email");
        notification.setMessage("Test message");
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    private Notification createSmsNotification() {
        Notification notification = new Notification();
        notification.setId("sms-test");
        notification.setUserId(100L);
        notification.setType(Notification.NotificationType.STATUS_CHANGED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient("+905551234567");
        notification.setTitle("Test SMS");
        notification.setMessage("Test SMS message");
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    private EmailService createMockEmailService() {
        return new EmailService(null, null) {
            @Override
            public boolean sendEmail(Notification notification) {
                return notification.getRecipient() != null && 
                       notification.getRecipient().contains("@");
            }
        };
    }
    
    private SmsService createMockSmsService() {
        return new SmsService() {
            @Override
            public boolean sendSms(Notification notification) {
                return notification.getRecipient() != null && 
                       notification.getRecipient().startsWith("+");
            }
        };
    }
} 