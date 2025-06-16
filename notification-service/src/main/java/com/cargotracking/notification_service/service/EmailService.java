package com.cargotracking.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Email gönderme servisi
 * FR-NT-003: Email bildirim desteği
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender javaMailSender;
    
    @Value("${spring.mail.from:noreply@cargotracking.com}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    /**
     * Basit email gönderir
     */
    @Async
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String content) {
        try {
            if (!emailEnabled) {
                log.warn("Email gönderimi devre dışı. Email: {}", to);
                return CompletableFuture.completedFuture(false);
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            javaMailSender.send(message);
            
            log.info("Email başarıyla gönderildi: {} -> {}", to, subject);
            return CompletableFuture.completedFuture(true);
            
        } catch (Exception e) {
            log.error("Email gönderme hatası: {} -> {}, Hata: {}", to, subject, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }
    
    /**
     * Shipment status değişikliği için email template'i oluşturur
     */
    public String createShipmentStatusEmailContent(String trackingNumber, String status, 
                                                  String customerName, String location) {
        return String.format("""
            Sayın %s,
            
            Takip numarası %s olan kargonuzun durumu güncellendi.
            
            Yeni Durum: %s
            Lokasyon: %s
            Güncelleme Zamanı: %s
            
            Kargonuzu takip etmek için: https://cargotracking.com/track/%s
            
            Bu bilgilendirme otomatik olarak gönderilmiştir.
            
            Saygılarımızla,
            Kargo Takip Sistemi
            """, 
            customerName != null ? customerName : "Müşteri",
            trackingNumber,
            getStatusDisplayName(status),
            location != null ? location : "Bilinmiyor",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            trackingNumber
        );
    }
    
    /**
     * Shipment oluşturulması için email template'i oluşturur
     */
    public String createShipmentCreatedEmailContent(String trackingNumber, String customerName, 
                                                   String originAddress, String destinationAddress) {
        return String.format("""
            Sayın %s,
            
            Kargonuz başarıyla sisteme kaydedildi.
            
            Takip Numarası: %s
            Çıkış Adresi: %s
            Varış Adresi: %s
            Oluşturma Zamanı: %s
            
            Kargonuzu takip etmek için: https://cargotracking.com/track/%s
            
            Bu bilgilendirme otomatik olarak gönderilmiştir.
            
            Saygılarımızla,
            Kargo Takip Sistemi
            """,
            customerName != null ? customerName : "Müşteri",
            trackingNumber,
            originAddress != null ? originAddress : "Bilinmiyor",
            destinationAddress != null ? destinationAddress : "Bilinmiyor",
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),
            trackingNumber
        );
    }
    
    /**
     * Status code'unu kullanıcı dostu isim haline getirir
     */
    private String getStatusDisplayName(String status) {
        return switch (status != null ? status : "") {
            case "CREATED" -> "Oluşturuldu";
            case "PACKAGE_RECEIVED" -> "Kargoya Verildi";
            case "IN_TRANSIT" -> "Yolda";
            case "OUT_FOR_DELIVERY" -> "Dağıtıma Çıktı";
            case "DELIVERED" -> "Teslim Edildi";
            case "DELIVERY_FAILED" -> "Teslimat Başarısız";
            case "RETURNED_TO_SENDER" -> "Gönderene İade Edildi";
            case "CANCELED" -> "İptal Edildi";
            default -> status != null ? status : "Bilinmiyor";
        };
    }
    
    /**
     * Email subject'i oluşturur
     */
    public String createEmailSubject(String trackingNumber, String status) {
        return String.format("Kargo Durumu Güncellendi - %s (%s)", 
                           trackingNumber, getStatusDisplayName(status));
    }
} 