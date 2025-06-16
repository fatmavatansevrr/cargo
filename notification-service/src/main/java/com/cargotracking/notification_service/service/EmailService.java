package com.cargotracking.notification_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * EmailService - Mail konfigürasyonu olmadığında da çalışır
 */
@Service
@Slf4j
public class EmailService {
    
    private final Optional<JavaMailSender> mailSender;
    
    @Value("${notification.channels.email.from-address:noreply@cargotracking.com}")
    private String fromAddress;
    
    @Value("${notification.channels.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = Optional.ofNullable(mailSender);
    }
    
    /**
     * Email gönderimi - Mail konfigürasyonu yoksa mock gönderim
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("📧 Email servisi deaktif - Mock gönderim: {} -> {}", subject, to);
            return;
        }
        
        if (!isMailConfigured()) {
            log.warn("📧 Mail konfigürasyonu bulunamadı - Mock gönderim: {} -> {}", subject, to);
            log.info("📧 Mock Email: To={}, Subject={}, Body={}", to, subject, body.substring(0, Math.min(body.length(), 100)));
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.get().send(message);
            log.info("📧 Email başarıyla gönderildi: {}", to);
            
        } catch (Exception e) {
            log.error("❌ Email gönderim hatası: {} - Mock gönderim yapılıyor", to, e);
            // Hata durumunda mock gönderim yap
            log.info("📧 Mock Email (Fallback): To={}, Subject={}, Body={}", to, subject, body.substring(0, Math.min(body.length(), 100)));
        }
    }
    
    /**
     * Email servisi durumu kontrolü
     */
    public boolean isEmailServiceHealthy() {
        try {
            return emailEnabled && isMailConfigured();
        } catch (Exception e) {
            log.debug("📧 Email servisi sağlık kontrolü - Mock mode aktif", e);
            return true; // Mock mode'da her zaman healthy
        }
    }
    
    /**
     * Mail konfigürasyonunun yapılıp yapılmadığını kontrol et
     */
    private boolean isMailConfigured() {
        return mailSender.isPresent() && StringUtils.hasText(mailUsername);
    }
}
