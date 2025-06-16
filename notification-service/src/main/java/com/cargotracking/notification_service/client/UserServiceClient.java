package com.cargotracking.notification_service.client;

import com.cargotracking.notification_service.config.ServiceClientConfig;
import com.cargotracking.notification_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * User service ile iletişim için client
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final RestTemplate restTemplate;
    private final ServiceClientConfig config;
    
    /**
     * User ID ile kullanıcı bilgilerini getir
     */
    public UserDto getUserById(Long userId) {
        try {
            String url = config.getUserServiceUrl() + "/api/users/" + userId;
            log.info("🔍 User service'den user bilgisi alınıyor: {}", userId);
            
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            
            if (user != null && user.isActive()) {
                log.info("✅ User bulundu: {} ({})", user.getUsername(), user.getEmail());
                return user;
            } else {
                log.warn("⚠️ User bulunamadı veya aktif değil: {}", userId);
                return null;
            }
            
        } catch (Exception e) {
            log.error("❌ User service bağlantı hatası - User ID: {}", userId, e);
            return null;
        }
    }
    
    /**
     * User'ın var olup olmadığını kontrol et
     */
    public boolean userExists(Long userId) {
        try {
            String url = config.getUserServiceUrl() + "/api/users/" + userId;
            log.debug("👤 Checking user existence: userId={}, url={}", userId, url);
            
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            boolean exists = response.getStatusCode() == HttpStatus.OK;
            
            log.debug("👤 User existence check result: userId={}, exists={}", userId, exists);
            return exists;
            
        } catch (RestClientException e) {
            log.warn("⚠️ User service unavailable or user not found: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * User'ın email adresini al
     */
    public String getUserEmail(Long userId) {
        try {
            UserDto user = getUserById(userId);
            if (user != null && user.getEmail() != null) {
                log.debug("📧 User email retrieved: userId={}, email={}", userId, user.getEmail());
                return user.getEmail();
            }
            
            log.warn("⚠️ User email not found: userId={}", userId);
            return null;
            
        } catch (Exception e) {
            log.warn("⚠️ Failed to get user email: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * User service'in sağlık durumunu kontrol et
     */
    public boolean isUserServiceHealthy() {
        try {
            String url = config.getUserServiceUrl() + "/actuator/health";
            String response = restTemplate.getForObject(url, String.class);
            return response != null && response.contains("UP");
        } catch (Exception e) {
            log.error("❌ User service sağlık kontrolü başarısız", e);
            return false;
        }
    }
} 