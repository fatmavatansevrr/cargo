package com.cargotracking.notification_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Basit NotificationPreference - Sadece temel ayarlar
 */
@Document(collection = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    
    @Id
    private String id;
    
    @Field("user_id")
    @Indexed(unique = true)
    @NotNull(message = "User ID gereklidir")
    private Long userId;
    
    @Field("email_enabled")
    private boolean emailEnabled = true;
    
    @Field("sms_enabled")
    private boolean smsEnabled = false;
    
    @Field("push_enabled")
    private boolean pushEnabled = false;
    
    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * Belirli kanal aktif mi kontrol et
     */
    public boolean isChannelEnabled(Notification.NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> emailEnabled;
            case SMS -> smsEnabled;
            case PUSH_NOTIFICATION -> pushEnabled;
            case IN_APP -> true; // In-app her zaman aktif
        };
    }
} 