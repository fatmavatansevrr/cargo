package com.cargotracking.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Basit SmsService - Sadece temel SMS gönderim işlevleri
 */
@Service
@Slf4j
public class SmsService {
    
    @Value("${notification.channels.sms.enabled:false}")
    private boolean smsEnabled;
    
    /**
     * Basit SMS gönderimi (mock implementation)
     */
    public void sendSms(String to, String message) {
        if (!smsEnabled) {
            log.info("📱 SMS servisi deaktif");
            return;
        }
        
        try {
            // Mock SMS gönderimi - gerçek SMS servisi entegrasyonu için bu kısım geliştirilecek
            log.info("📱 SMS gönderiliyor: {} -> {}", to, message);
            
            // Burada gerçek SMS provider API'si çağrılacak
            // Örneğin: Twilio, AWS SNS, vs.
            Thread.sleep(100); // Simulate API call
            
            log.info("📱 SMS başarıyla gönderildi: {}", to);
            
        } catch (Exception e) {
            log.error("❌ SMS gönderim hatası: {}", to, e);
            throw new RuntimeException("SMS gönderilemedi", e);
        }
    }
    
    /**
     * SMS servisi durumu kontrolü
     */
    public boolean isSmsServiceHealthy() {
        try {
            return smsEnabled;
        } catch (Exception e) {
            log.error("❌ SMS servisi sağlık kontrolü başarısız", e);
            return false;
        }
    }
} 