package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Notification preference business logic service
 * FR-NT-004: Kullanıcı bildirim tercihleri yönetimi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {
    
    private final NotificationPreferenceRepository preferenceRepository;
    
    /**
     * Kullanıcının bildirim tercihlerini getirir, yoksa default oluşturur
     */
    public NotificationPreference getUserPreferences(Long userId) {
        log.debug("Kullanıcı bildirim tercihleri getiriliyor: userId={}", userId);
        
        return preferenceRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.info("Kullanıcı için default bildirim tercihleri oluşturuluyor: userId={}", userId);
                return createDefaultPreferencesForUser(userId);
            });
    }
    
    /**
     * Email ile bildirim tercihlerini getirir
     */
    public NotificationPreference getPreferencesByEmail(String email) {
        log.debug("Email ile bildirim tercihleri getiriliyor: email={}", email);
        
        return preferenceRepository.findByUserEmail(email)
            .orElseGet(() -> {
                log.info("Email için default bildirim tercihleri oluşturuluyor: email={}", email);
                return createDefaultPreferencesForEmail(email);
            });
    }
    
    /**
     * Yeni bildirim tercihleri oluşturur
     */
    public NotificationPreference createPreferences(NotificationPreference preference) {
        log.info("Yeni bildirim tercihleri oluşturuluyor: userId={}, email={}", 
                preference.getUserId(), preference.getUserEmail());
        
        // Var olan tercihleri kontrol et
        if (preference.getUserId() != null && 
            preferenceRepository.existsByUserId(preference.getUserId())) {
            log.warn("Kullanıcı için bildirim tercihleri zaten mevcut: userId={}", preference.getUserId());
            return getUserPreferences(preference.getUserId());
        }
        
        // Timestamps ayarla
        LocalDateTime now = LocalDateTime.now();
        preference.setCreatedAt(now);
        preference.setUpdatedAt(now);
        
        // Default değerleri ayarla (eğer null ise)
        setDefaultValues(preference);
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Mevcut bildirim tercihlerini günceller
     */
    public NotificationPreference updatePreferences(NotificationPreference preference) {
        log.info("Bildirim tercihleri güncelleniyor: userId={}, email={}", 
                preference.getUserId(), preference.getUserEmail());
        
        NotificationPreference existingPreference;
        
        // Mevcut tercihleri bul
        if (preference.getUserId() != null) {
            existingPreference = preferenceRepository.findByUserId(preference.getUserId())
                .orElseThrow(() -> new RuntimeException("Kullanıcı bildirim tercihleri bulunamadı: " + preference.getUserId()));
        } else if (preference.getUserEmail() != null) {
            existingPreference = preferenceRepository.findByUserEmail(preference.getUserEmail())
                .orElseThrow(() -> new RuntimeException("Email bildirim tercihleri bulunamadı: " + preference.getUserEmail()));
        } else {
            throw new RuntimeException("UserId veya email belirtilmeli");
        }
        
        // Güncelleme zamanını ayarla
        preference.setUpdatedAt(LocalDateTime.now());
        preference.setCreatedAt(existingPreference.getCreatedAt()); // Preserve original creation time
        preference.setId(existingPreference.getId()); // Preserve ID
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Email bildirimlerini aç/kapat
     */
    public NotificationPreference toggleEmailNotifications(Long userId, boolean enabled) {
        log.info("Email bildirimleri güncelleniyor: userId={}, enabled={}", userId, enabled);
        
        NotificationPreference preference = getUserPreferences(userId);
        preference.setEmailEnabled(enabled);
        preference.setUpdatedAt(LocalDateTime.now());
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * SMS bildirimlerini aç/kapat
     */
    public NotificationPreference toggleSmsNotifications(Long userId, boolean enabled) {
        log.info("SMS bildirimleri güncelleniyor: userId={}, enabled={}", userId, enabled);
        
        NotificationPreference preference = getUserPreferences(userId);
        preference.setSmsEnabled(enabled);
        preference.setUpdatedAt(LocalDateTime.now());
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Belirli bir status için bildirimleri aç/kapat
     */
    public NotificationPreference toggleStatusNotification(Long userId, String status, boolean enabled) {
        log.info("Status bildirimi güncelleniyor: userId={}, status={}, enabled={}", 
                userId, status, enabled);
        
        NotificationPreference preference = getUserPreferences(userId);
        
        Set<String> enabledStatuses = new HashSet<>(preference.getEnabledStatusNotifications());
        
        if (enabled) {
            enabledStatuses.add(status);
        } else {
            enabledStatuses.remove(status);
        }
        
        preference.setEnabledStatusNotifications(enabledStatuses);
        preference.setUpdatedAt(LocalDateTime.now());
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Kullanıcı bildirim tercihlerini siler
     */
    public void deletePreferences(Long userId) {
        log.info("Bildirim tercihleri siliniyor: userId={}", userId);
        
        if (preferenceRepository.existsByUserId(userId)) {
            preferenceRepository.deleteByUserId(userId);
            log.info("Bildirim tercihleri başarıyla silindi: userId={}", userId);
        } else {
            log.warn("Silinecek bildirim tercihleri bulunamadı: userId={}", userId);
        }
    }
    
    /**
     * Kullanıcı için default bildirim tercihleri oluşturur
     */
    private NotificationPreference createDefaultPreferencesForUser(Long userId) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserId(userId);
        
        LocalDateTime now = LocalDateTime.now();
        preference.setCreatedAt(now);
        preference.setUpdatedAt(now);
        
        setDefaultValues(preference);
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Email için default bildirim tercihleri oluşturur
     */
    private NotificationPreference createDefaultPreferencesForEmail(String email) {
        NotificationPreference preference = new NotificationPreference();
        preference.setUserEmail(email);
        
        LocalDateTime now = LocalDateTime.now();
        preference.setCreatedAt(now);
        preference.setUpdatedAt(now);
        
        setDefaultValues(preference);
        
        return preferenceRepository.save(preference);
    }
    
    /**
     * Default değerleri ayarlar
     */
    private void setDefaultValues(NotificationPreference preference) {
        // Eğer null ise default değerleri ayarla
        if (preference.getEnabledStatusNotifications() == null || preference.getEnabledStatusNotifications().isEmpty()) {
            preference.setEnabledStatusNotifications(Set.of(
                "PACKAGE_RECEIVED",     // Kargoya Verildi
                "IN_TRANSIT",          // Yolda
                "OUT_FOR_DELIVERY",    // Dağıtıma Çıktı
                "DELIVERED",           // Teslim Edildi
                "DELIVERY_FAILED"      // Teslimat Başarısız
            ));
        }
        
        if (preference.getEnabledEventTypes() == null || preference.getEnabledEventTypes().isEmpty()) {
            preference.setEnabledEventTypes(Set.of(
                "shipment.created",
                "shipment.updated",
                "status.updated"
            ));
        }
        
        // Default kanal ayarları (eğer henüz ayarlanmamışsa)
        // emailEnabled default true olarak zaten ayarlı (model'de)
        // smsEnabled default false olarak zaten ayarlı (model'de)
        // pushNotificationEnabled default false olarak zaten ayarlı (model'de)
    }
} 