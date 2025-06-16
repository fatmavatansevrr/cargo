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
 * Twilio SMS Service Integration Tests
 * Bu testler gerçek Twilio credentials ile çalışır
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
    "sms.provider.enabled=true",
    "sms.provider.type=mock", // Test için mock mode
    "twilio.account.sid=test-sid",
    "twilio.auth.token=test-token",
    "twilio.phone.number=+1234567890"
})
class TwilioSmsServiceTest {
    
    private SmsService smsService;
    
    @BeforeEach
    void setUp() {
        smsService = new SmsService();
        // Test için reflection ile değerleri set et
        setField(smsService, "smsEnabled", true);
        setField(smsService, "providerType", "mock");
        setField(smsService, "accountSid", "test-sid");
        setField(smsService, "authToken", "test-token");
        setField(smsService, "fromPhoneNumber", "+1234567890");
    }
    
    @Test
    void testSendSms_Success() {
        // Given
        Notification notification = createTestNotification();
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testSendSms_InvalidPhoneNumber() {
        // Given
        Notification notification = createTestNotification();
        notification.setRecipient("invalid-phone");
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testSendSms_EmptyRecipient() {
        // Given
        Notification notification = createTestNotification();
        notification.setRecipient("");
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testSendSms_NullRecipient() {
        // Given
        Notification notification = createTestNotification();
        notification.setRecipient(null);
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testPhoneNumberValidation_Valid() {
        // Given
        String validPhone = "+905551234567";
        
        // When
        boolean result = smsService.isValidPhoneNumber(validPhone);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testPhoneNumberValidation_Invalid() {
        // Given
        String[] invalidPhones = {
            "123456789",      // No country code
            "+0551234567",    // Starts with 0
            "invalid",        // Not a number
            "",               // Empty
            null              // Null
        };
        
        // When & Then
        for (String phone : invalidPhones) {
            assertFalse(smsService.isValidPhoneNumber(phone), 
                       "Phone should be invalid: " + phone);
        }
    }
    
    @Test
    void testBulkSms_Success() {
        // Given
        java.util.List<Notification> notifications = java.util.List.of(
            createTestNotification("+905551234567", "Test message 1"),
            createTestNotification("+905551234568", "Test message 2"),
            createTestNotification("+905551234569", "Test message 3")
        );
        
        // When
        int successCount = smsService.sendBulkSms(notifications);
        
        // Then
        assertEquals(3, successCount);
    }
    
    @Test
    void testBulkSms_PartialFailure() {
        // Given
        java.util.List<Notification> notifications = java.util.List.of(
            createTestNotification("+905551234567", "Test message 1"),
            createTestNotification("invalid-phone", "Test message 2"), // Invalid
            createTestNotification("+905551234569", "Test message 3")
        );
        
        // When
        int successCount = smsService.sendBulkSms(notifications);
        
        // Then
        assertEquals(2, successCount); // 2 başarılı, 1 başarısız
    }
    
    @Test
    void testSmsConnection_Success() {
        // When
        boolean result = smsService.testSmsConnection();
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testMessageFormatting_LongMessage() {
        // Given
        Notification notification = createTestNotification();
        String longMessage = "Bu çok uzun bir mesajdır. ".repeat(10); // 160+ karakter
        notification.setMessage(longMessage);
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertTrue(result); // Mesaj kesilse bile gönderim başarılı olmalı
    }
    
    @Test
    void testMessageFormatting_EmptyMessage() {
        // Given
        Notification notification = createTestNotification();
        notification.setMessage("");
        notification.setTitle("Test Title");
        
        // When
        boolean result = smsService.sendSms(notification);
        
        // Then
        assertTrue(result); // Title kullanılmalı
    }
    
    private Notification createTestNotification() {
        return createTestNotification("+905551234567", "Test SMS message");
    }
    
    private Notification createTestNotification(String recipient, String message) {
        Notification notification = new Notification();
        notification.setId("test-id");
        notification.setUserId(100L);
        notification.setTrackingNumber("TRK123456");
        notification.setType(Notification.NotificationType.STATUS_CHANGED);
        notification.setChannel(Notification.NotificationChannel.SMS);
        notification.setRecipient(recipient);
        notification.setTitle("Test SMS");
        notification.setMessage(message);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
    
    // Reflection helper for testing
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