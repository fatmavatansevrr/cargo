package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.NotificationPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * NotificationPreference repository interface
 * MongoDB ile kullanıcı bildirim tercihlerini yönetir
 */
@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
    
    /**
     * Kullanıcı ID'sine göre bildirim tercihlerini bulur
     */
    Optional<NotificationPreference> findByUserId(Long userId);
    
    /**
     * Email adresine göre bildirim tercihlerini bulur
     */
    Optional<NotificationPreference> findByUserEmail(String userEmail);
    
    /**
     * Kullanıcı ID'sine göre bildirim tercihlerinin var olup olmadığını kontrol eder
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Kullanıcı ID'sine göre bildirim tercihlerini siler
     */
    void deleteByUserId(Long userId);
} 