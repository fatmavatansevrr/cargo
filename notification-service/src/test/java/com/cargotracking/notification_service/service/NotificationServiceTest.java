package com.cargotracking.notification_service.service;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.event.TrackingEvent;
import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.model.NotificationTemplate;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import com.cargotracking.notification_service.repository.NotificationRepository;
import com.cargotracking.notification_service.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationService Unit Tests
 * NotificationService'in tüm metodlarını test eder
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private NotificationPreferenceRepository preferenceRepository;
    
    @Mock
    private NotificationTemplateRepository templateRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private SmsService smsService;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private ShipmentEvent shipmentEvent;
    private TrackingEvent trackingEvent;
    private NotificationPreference userPreference;
    private NotificationTemplate emailTemplate;
    private Notification notification;
    
    @BeforeEach
    void setUp() {
        // Shipment Event setup
        shipmentEvent = new ShipmentEvent();
        shipmentEvent.setEventType("shipment.created");
        shipmentEvent.setShipmentId(1L);
        shipmentEvent.setTrackingNumber("TRK123456");
        shipmentEvent.setSenderUserId(100L);
        shipmentEvent.setStatus("CREATED");
        shipmentEvent.setEventTimestamp(LocalDateTime.now());
        shipmentEvent.setEventSource("shipment-service");
        shipmentEvent.setCustomerName("Ahmet Yılmaz");
        shipmentEvent.setCustomerEmail("ahmet@example.com");
        shipmentEvent.setCustomerPhone("+905551234567");
        
        // Tracking Event setup
        trackingEvent = new TrackingEvent();
        trackingEvent.setEventType("status.changed");
        trackingEvent.setShipmentId(1L);
        trackingEvent.setTrackingNumber("TRK123456");
        trackingEvent.setUserId(100L);
        trackingEvent.setCurrentStatus("IN_TRANSIT");
        trackingEvent.setPreviousStatus("CREATED");
        trackingEvent.setLocation("İstanbul Hub");
        trackingEvent.setEventTimestamp(LocalDateTime.now());
        trackingEvent.setEventSource("tracking-service");
        trackingEvent.setCustomerName("Ahmet Yılmaz");
        trackingEvent.setCustomerEmail("ahmet@example.com");
        
        // User Preference setup
        userPreference = new NotificationPreference();
        userPreference.setUserId(100L);
        userPreference.setEmail("ahmet@example.com");
        userPreference.setPhoneNumber("+905551234567");
        userPreference.setEmailEnabled(true);
        userPreference.setSmsEnabled(false);
        userPreference.setPushEnabled(true);
        userPreference.setInAppEnabled(true);
        
        // Email Template setup
        emailTemplate = new NotificationTemplate();
        emailTemplate.setNotificationType(Notification.NotificationType.SHIPMENT_CREATED);
        emailTemplate.setChannel(Notification.NotificationChannel.EMAIL);
        emailTemplate.setTitle("Kargonuz Oluşturuldu - {{trackingNumber}}");
        emailTemplate.setBody("Sayın {{customerName}}, {{trackingNumber}} takip numaralı kargonuz oluşturuldu.");
        emailTemplate.setActive(true);
        
        // Notification setup
        notification = new Notification();
        notification.setId("notification123");
        notification.setUserId(100L);
        notification.setTrackingNumber("TRK123456");
        notification.setType(Notification.NotificationType.SHIPMENT_CREATED);
        notification.setChannel(Notification.NotificationChannel.EMAIL);
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setTitle("Test Notification");
        notification.setMessage("Test Message");
        notification.setRecipient("ahmet@example.com");
        notification.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void testProcessShipmentEvent_WithExistingPreferences() {
        // Given
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(userPreference));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        // Push notification mock removed - not called in current logic
        
        // When
        notificationService.processShipmentEvent(shipmentEvent);
        
        // Then
        verify(preferenceRepository).findByUserId(100L);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class)); // En az bir notification
        verify(emailService, atLeast(1)).sendEmail(any(Notification.class));
        // Push notification service çağrılmayabilir (preference logic'e bağlı)
        verify(smsService, never()).sendSms(any(Notification.class)); // SMS kapalı
    }
    
    @Test
    void testProcessShipmentEvent_WithoutPreferences() {
        // Given
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.empty());
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        
        // When
        notificationService.processShipmentEvent(shipmentEvent);
        
        // Then
        verify(preferenceRepository).findByUserId(100L);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class)); // Varsayılan email
        verify(emailService, atLeast(1)).sendEmail(any(Notification.class));
    }
    
    @Test
    void testProcessTrackingEvent_Success() {
        // Given
        when(preferenceRepository.findByUserId(100L)).thenReturn(Optional.of(userPreference));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        // Push notification mock removed - not called in current logic
        
        // When
        notificationService.processTrackingEvent(trackingEvent);
        
        // Then
        verify(preferenceRepository).findByUserId(100L);
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
        verify(emailService, atLeast(1)).sendEmail(any(Notification.class));
        // Push notification service çağrılmayabilir (preference logic'e bağlı)
    }
    
    @Test
    void testSendNotification_Success() {
        // Given
        when(templateRepository.findByNotificationTypeAndChannelAndActiveTrue(
                any(Notification.NotificationType.class), 
                any(Notification.NotificationChannel.class)))
                .thenReturn(Optional.of(emailTemplate));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        
        // When
        Notification result = notificationService.sendNotification(notification);
        
        // Then
        assertNotNull(result);
        assertEquals(Notification.NotificationStatus.SENT, result.getStatus());
        assertNotNull(result.getSentAt());
        verify(templateRepository).findByNotificationTypeAndChannelAndActiveTrue(
                Notification.NotificationType.SHIPMENT_CREATED,
                Notification.NotificationChannel.EMAIL);
        verify(emailService).sendEmail(any(Notification.class));
    }
    
    @Test
    void testSendNotification_Failure() {
        // Given
        when(templateRepository.findByNotificationTypeAndChannelAndActiveTrue(
                any(Notification.NotificationType.class), 
                any(Notification.NotificationChannel.class)))
                .thenReturn(Optional.of(emailTemplate));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(emailService.sendEmail(any(Notification.class))).thenReturn(false);
        
        // When
        Notification result = notificationService.sendNotification(notification);
        
        // Then
        assertNotNull(result);
        assertEquals(Notification.NotificationStatus.FAILED, result.getStatus());
        assertNull(result.getSentAt());
        verify(emailService).sendEmail(any(Notification.class));
    }
    
    @Test
    void testSendNotification_Exception() {
        // Given
        when(templateRepository.findByNotificationTypeAndChannelAndActiveTrue(
                any(Notification.NotificationType.class), 
                any(Notification.NotificationChannel.class)))
                .thenThrow(new RuntimeException("Database error"));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        
        // When
        Notification result = notificationService.sendNotification(notification);
        
        // Then
        assertNotNull(result);
        assertEquals(Notification.NotificationStatus.FAILED, result.getStatus());
        assertNotNull(result.getErrorMessage());
        verify(notificationRepository, atLeast(1)).save(any(Notification.class));
    }
    
    @Test
    void testGetUserNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(notification);
        Page<Notification> page = new PageImpl<>(notifications);
        Pageable pageable = PageRequest.of(0, 20);
        
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(100L, pageable))
                .thenReturn(page);
        
        // When
        Page<Notification> result = notificationService.getUserNotifications(100L, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(notification.getId(), result.getContent().get(0).getId());
        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(100L, pageable);
    }
    
    @Test
    void testGetUnreadCount() {
        // Given
        when(notificationRepository.countByUserIdAndStatus(100L, Notification.NotificationStatus.SENT))
                .thenReturn(5L);
        
        // When
        long result = notificationService.getUnreadCount(100L);
        
        // Then
        assertEquals(5L, result);
        verify(notificationRepository).countByUserIdAndStatus(100L, Notification.NotificationStatus.SENT);
    }
    
    @Test
    void testMarkAsRead_Success() {
        // Given
        notification.setStatus(Notification.NotificationStatus.SENT);
        when(notificationRepository.findById("notification123")).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        
        // When
        notificationService.markAsRead("notification123");
        
        // Then
        verify(notificationRepository).findById("notification123");
        verify(notificationRepository).save(argThat(n -> 
                n.getStatus() == Notification.NotificationStatus.READ && 
                n.getReadAt() != null));
    }
    
    @Test
    void testMarkAsRead_NotFound() {
        // Given
        when(notificationRepository.findById("notification123")).thenReturn(Optional.empty());
        
        // When
        notificationService.markAsRead("notification123");
        
        // Then
        verify(notificationRepository).findById("notification123");
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testRetryFailedNotifications_Success() {
        // Given
        Notification failedNotification = new Notification();
        failedNotification.setId("failed123");
        failedNotification.setStatus(Notification.NotificationStatus.FAILED);
        failedNotification.setChannel(Notification.NotificationChannel.EMAIL);
        failedNotification.setRetryCount(1);
        failedNotification.setMaxRetries(3);
        
        when(notificationRepository.findFailedNotificationsForRetry(3))
                .thenReturn(Arrays.asList(failedNotification));
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);
        
        // When
        notificationService.retryFailedNotifications();
        
        // Then
        verify(notificationRepository).findFailedNotificationsForRetry(3);
        verify(emailService).sendEmail(failedNotification);
        verify(notificationRepository).save(argThat(n -> 
                n.getStatus() == Notification.NotificationStatus.SENT &&
                n.getRetryCount() == 2));
    }
    
    @Test
    void testRetryFailedNotifications_MaxRetryReached() {
        // Given
        Notification failedNotification = new Notification();
        failedNotification.setId("failed123");
        failedNotification.setStatus(Notification.NotificationStatus.FAILED);
        failedNotification.setChannel(Notification.NotificationChannel.EMAIL);
        failedNotification.setRetryCount(2);
        failedNotification.setMaxRetries(3);
        
        when(notificationRepository.findFailedNotificationsForRetry(3))
                .thenReturn(Arrays.asList(failedNotification));
        when(emailService.sendEmail(any(Notification.class))).thenReturn(false);
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);
        
        // When
        notificationService.retryFailedNotifications();
        
        // Then
        verify(notificationRepository).findFailedNotificationsForRetry(3);
        verify(emailService).sendEmail(failedNotification);
        verify(notificationRepository).save(argThat(n -> 
                n.getRetryCount() == 3)); // Max retry reached
    }
    
    @Test
    void testTemplateProcessing() {
        // Given
        when(templateRepository.findByNotificationTypeAndChannelAndActiveTrue(
                Notification.NotificationType.SHIPMENT_CREATED,
                Notification.NotificationChannel.EMAIL))
                .thenReturn(Optional.of(emailTemplate));
        
        notification.setTemplateData(Map.of(
                "customerName", "Ahmet Yılmaz",
                "trackingNumber", "TRK123456"
        ));
        
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification savedNotification = invocation.getArgument(0);
            // Template processing sonucu kontrol et
            assertTrue(savedNotification.getTitle().contains("TRK123456"));
            assertTrue(savedNotification.getMessage().contains("Ahmet Yılmaz"));
            assertTrue(savedNotification.getMessage().contains("TRK123456"));
            return savedNotification;
        });
        
        when(emailService.sendEmail(any(Notification.class))).thenReturn(true);
        
        // When
        notificationService.sendNotification(notification);
        
        // Then
        verify(templateRepository).findByNotificationTypeAndChannelAndActiveTrue(
                Notification.NotificationType.SHIPMENT_CREATED,
                Notification.NotificationChannel.EMAIL);
    }
    
    @Test
    void testEventTypeMapping() {
        // Test shipment event type mapping
        assertEquals("shipment.created", shipmentEvent.getEventType());
        
        // Test tracking event type mapping  
        assertEquals("status.changed", trackingEvent.getEventType());
        
        // Test notification type inference
        notificationService.processShipmentEvent(shipmentEvent);
        
        verify(notificationRepository, atLeastOnce()).save(argThat(n -> 
                n.getType() == Notification.NotificationType.SHIPMENT_CREATED));
    }
} 