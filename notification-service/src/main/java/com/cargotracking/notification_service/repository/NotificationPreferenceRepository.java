package com.cargotracking.notification_service.repository;

import com.cargotracking.notification_service.model.NotificationPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * NotificationPreferenceRepository - MongoDB operations for notification preferences
 */
@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
    
    Optional<NotificationPreference> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
} 