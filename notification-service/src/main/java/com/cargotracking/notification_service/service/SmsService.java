package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.model.Notification;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * SMS Service - Twilio entegrasyonu ile SMS gönderimi
 */
@Service
@Slf4j
public class SmsService {
    
    @Value("${sms.provider.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${sms.provider.type:mock}")
    private String providerType;
    
    @Value("${twilio.account.sid:}")
    private String accountSid;
    
    @Value("${twilio.auth.token:}")
    private String authToken;
    
    @Value("${twilio.phone.number:}")
    private String fromPhoneNumber;
    
    @PostConstruct
    public void initializeTwilio() {
        if (smsEnabled && "twilio".equals(providerType) && 
            !accountSid.isEmpty() && !authToken.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("✅ Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("❌ Failed to initialize Twilio SMS service", e);
                smsEnabled = false;
            }
        } else {
            log.info("📱 SMS service running in mock mode");
        }
    }
    
    /**
     * SMS gönderimi - Twilio veya Mock
     */
    public boolean sendSms(Notification notification) {
        if (!smsEnabled || notification.getRecipient() == null || notification.getRecipient().trim().isEmpty()) {
            log.warn("📱 Cannot send SMS: service disabled or invalid recipient");
            return false;
        }
        
        try {
            if ("twilio".equals(providerType) && !accountSid.isEmpty()) {
                return sendTwilioSms(notification);
            } else {
                return sendMockSms(notification);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to: {}", notification.getRecipient(), e);
            return false;
        }
    }
    
    /**
     * Twilio ile gerçek SMS gönderimi
     */
    private boolean sendTwilioSms(Notification notification) {
        try {
            String messageText = formatSmsMessage(notification);
            
            Message message = Message.creator(
                new PhoneNumber(notification.getRecipient()), // To
                new PhoneNumber(fromPhoneNumber), // From
                messageText // Body
            ).create();
            
            log.info("📱 Twilio SMS sent successfully - SID: {} to: {}", 
                    message.getSid(), notification.getRecipient());
            return true;
            
        } catch (Exception e) {
            log.error("❌ Twilio SMS failed to: {}", notification.getRecipient(), e);
            return false;
        }
    }
    
    /**
     * Mock SMS gönderimi
     */
    private boolean sendMockSms(Notification notification) {
        String messageText = formatSmsMessage(notification);
        log.info("📱 [MOCK] SMS sent successfully to: {} - Message: {}", 
                notification.getRecipient(), messageText);
        return true;
    }
    
    /**
     * Bulk SMS gönderimi
     */
    public int sendBulkSms(List<Notification> notifications) {
        int successCount = 0;
        for (Notification notification : notifications) {
            if (sendSms(notification)) {
                successCount++;
            }
            
            // Rate limiting için kısa bekleme
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("📱 Bulk SMS completed: {}/{} successful", successCount, notifications.size());
        return successCount;
    }
    
    /**
     * SMS bağlantı testi
     */
    public boolean testSmsConnection() {
        try {
            if ("twilio".equals(providerType) && !accountSid.isEmpty()) {
                // Twilio hesap bilgilerini test et
                com.twilio.rest.api.v2010.Account account = com.twilio.rest.api.v2010.Account.fetcher(accountSid).fetch();
                log.info("✅ Twilio connection test successful - Account: {}", account.getFriendlyName());
                return true;
            } else {
                log.info("✅ Mock SMS connection test successful");
                return true;
            }
        } catch (Exception e) {
            log.error("❌ SMS connection test failed", e);
            return false;
        }
    }
    
    /**
     * Phone number validation (E.164 format)
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // E.164 format validation
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");
        return cleaned.matches("^\\+[1-9]\\d{1,14}$");
    }
    
    /**
     * SMS mesajını formatla (160 karakter limiti)
     */
    private String formatSmsMessage(Notification notification) {
        String message = notification.getMessage();
        
        if (message == null || message.trim().isEmpty()) {
            message = notification.getTitle();
        }
        
        if (message == null) {
            message = "CargoTrack bildirimi";
        }
        
        // SMS karakter limiti (160 karakter)
        if (message.length() > 160) {
            message = message.substring(0, 157) + "...";
        }
        
        return message;
    }
} 