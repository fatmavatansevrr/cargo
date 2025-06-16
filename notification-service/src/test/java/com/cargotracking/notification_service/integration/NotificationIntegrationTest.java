package com.cargotracking.notification_service.integration;

import com.cargotracking.notification_service.model.Notification;
import com.cargotracking.notification_service.model.NotificationPreference;
import com.cargotracking.notification_service.repository.NotificationPreferenceRepository;
import com.cargotracking.notification_service.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Notification Service Integration Tests
 * End-to-end Kafka event processing ve notification gönderimi testleri
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    topics = {"shipment.created", "shipment.updated", "status.changed", "delivery.completed"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092",
        "port=9092"
    }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "eureka.client.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class NotificationIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private NotificationPreference testUserPreference;
    
    @BeforeEach
    void setUp() {
        // Test verilerini temizle
        notificationRepository.deleteAll();
        preferenceRepository.deleteAll();
        
        // Test kullanıcısı tercihlerini oluştur
        testUserPreference = new NotificationPreference();
        testUserPreference.setUserId(100L);
        testUserPreference.setEmail("test@example.com");
        testUserPreference.setPhoneNumber("+905551234567");
        testUserPreference.setEmailEnabled(true);
        testUserPreference.setSmsEnabled(false);
        testUserPreference.setPushEnabled(true);
        testUserPreference.setInAppEnabled(true);
        testUserPreference.setCreatedAt(LocalDateTime.now());
        testUserPreference.setUpdatedAt(LocalDateTime.now());
        
        preferenceRepository.save(testUserPreference);
    }
    
    @Test
    void testShipmentCreatedEventProcessing() throws Exception {
        // Given
        Map<String, Object> shipmentEventData = new HashMap<>();
        shipmentEventData.put("eventType", "shipment.created");
        shipmentEventData.put("shipmentId", 1L);
        shipmentEventData.put("trackingNumber", "TRK123456");
        shipmentEventData.put("senderUserId", 100L);
        shipmentEventData.put("status", "CREATED");
        shipmentEventData.put("eventTimestamp", LocalDateTime.now().toString());
        shipmentEventData.put("eventSource", "shipment-service");
        shipmentEventData.put("customerName", "Ahmet Yılmaz");
        shipmentEventData.put("customerEmail", "test@example.com");
        shipmentEventData.put("customerPhone", "+905551234567");
        
        String eventJson = objectMapper.writeValueAsString(shipmentEventData);
        
        // When
        kafkaTemplate.send("shipment.created", eventJson);
        
        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                100L, Notification.NotificationStatus.PENDING);
            
            assertFalse(notifications.isEmpty());
            
            // Email notification kontrolü
            Notification emailNotification = notifications.stream()
                .filter(n -> n.getChannel() == Notification.NotificationChannel.EMAIL)
                .findFirst()
                .orElse(null);
            
            assertNotNull(emailNotification);
            assertEquals(Notification.NotificationType.SHIPMENT_CREATED, emailNotification.getType());
            assertEquals("TRK123456", emailNotification.getTrackingNumber());
            assertEquals(100L, emailNotification.getUserId());
            assertEquals("test@example.com", emailNotification.getRecipient());
            
            // In-app notification kontrolü
            Notification inAppNotification = notifications.stream()
                .filter(n -> n.getChannel() == Notification.NotificationChannel.IN_APP)
                .findFirst()
                .orElse(null);
            
            assertNotNull(inAppNotification);
            assertEquals(Notification.NotificationType.SHIPMENT_CREATED, inAppNotification.getType());
        });
    }
    
    @Test
    void testStatusChangedEventProcessing() throws Exception {
        // Given
        Map<String, Object> trackingEventData = new HashMap<>();
        trackingEventData.put("eventType", "status.changed");
        trackingEventData.put("shipmentId", 1L);
        trackingEventData.put("trackingNumber", "TRK123456");
        trackingEventData.put("userId", 100L);
        trackingEventData.put("currentStatus", "IN_TRANSIT");
        trackingEventData.put("previousStatus", "CREATED");
        trackingEventData.put("location", "İstanbul Hub");
        trackingEventData.put("eventTimestamp", LocalDateTime.now().toString());
        trackingEventData.put("eventSource", "tracking-service");
        trackingEventData.put("customerName", "Ahmet Yılmaz");
        trackingEventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(trackingEventData);
        
        // When
        kafkaTemplate.send("status.changed", eventJson);
        
        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByTrackingNumberOrderByCreatedAtDesc("TRK123456");
            
            assertFalse(notifications.isEmpty());
            
            Notification statusNotification = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.STATUS_CHANGED)
                .findFirst()
                .orElse(null);
            
            assertNotNull(statusNotification);
            assertEquals("status.changed", statusNotification.getEventType());
            assertEquals("tracking-service", statusNotification.getEventSource());
            assertTrue(statusNotification.getTemplateData().containsKey("currentStatus"));
            assertEquals("IN_TRANSIT", statusNotification.getTemplateData().get("currentStatus"));
        });
    }
    
    @Test
    void testDeliveryCompletedEventProcessing() throws Exception {
        // Given
        Map<String, Object> deliveryEventData = new HashMap<>();
        deliveryEventData.put("eventType", "delivery.completed");
        deliveryEventData.put("shipmentId", 1L);
        deliveryEventData.put("trackingNumber", "TRK123456");
        deliveryEventData.put("userId", 100L);
        deliveryEventData.put("currentStatus", "DELIVERED");
        deliveryEventData.put("location", "Ankara Merkez");
        deliveryEventData.put("recipientName", "Ahmet Yılmaz");
        deliveryEventData.put("eventTimestamp", LocalDateTime.now().toString());
        deliveryEventData.put("eventSource", "tracking-service");
        deliveryEventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(deliveryEventData);
        
        // When
        kafkaTemplate.send("delivery.completed", eventJson);
        
        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByTrackingNumberOrderByCreatedAtDesc("TRK123456");
            
            assertFalse(notifications.isEmpty());
            
            Notification deliveryNotification = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.DELIVERY_COMPLETED)
                .findFirst()
                .orElse(null);
            
            assertNotNull(deliveryNotification);
            assertEquals("delivery.completed", deliveryNotification.getEventType());
            assertTrue(deliveryNotification.getTemplateData().containsKey("location"));
            assertEquals("Ankara Merkez", deliveryNotification.getTemplateData().get("location"));
        });
    }
    
    @Test
    void testMultipleEventsWithUserPreferences() throws Exception {
        // Given - Kullanıcı sadece email bildirimleri almak istiyor
        testUserPreference.setSmsEnabled(false);
        testUserPreference.setPushEnabled(false);
        testUserPreference.setInAppEnabled(false);
        testUserPreference.setEmailEnabled(true);
        preferenceRepository.save(testUserPreference);
        
        // When - Birden fazla event gönder
        sendShipmentCreatedEvent();
        Thread.sleep(1000);
        sendStatusChangedEvent();
        Thread.sleep(1000);
        sendDeliveryCompletedEvent();
        
        // Then
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> allNotifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(100L, Notification.NotificationStatus.PENDING);
            
            assertFalse(allNotifications.isEmpty());
            assertTrue(allNotifications.size() >= 3); // En az 3 event için notification
            
            // Sadece email notifications olmalı
            long emailCount = allNotifications.stream()
                .filter(n -> n.getChannel() == Notification.NotificationChannel.EMAIL)
                .count();
            
            long nonEmailCount = allNotifications.stream()
                .filter(n -> n.getChannel() != Notification.NotificationChannel.EMAIL)
                .count();
            
            assertTrue(emailCount >= 3);
            assertEquals(0, nonEmailCount);
        });
    }
    
    @Test
    void testEventProcessingWithoutUserPreferences() throws Exception {
        // Given - Kullanıcı tercihlerini sil (varsayılan davranış test et)
        preferenceRepository.deleteByUserId(100L);
        
        // When
        sendShipmentCreatedEvent();
        
        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                100L, Notification.NotificationStatus.PENDING);
            
            assertFalse(notifications.isEmpty());
            
            // Varsayılan olarak sadece email gönderilmeli
            Notification defaultNotification = notifications.get(0);
            assertEquals(Notification.NotificationChannel.EMAIL, defaultNotification.getChannel());
        });
    }
    
    @Test
    void testInvalidEventHandling() throws Exception {
        // Given - Geçersiz JSON
        String invalidJson = "{ invalid json structure }";
        
        // When
        kafkaTemplate.send("shipment.created", invalidJson);
        
        // Then - Uygulama çökmemeli, invalid event handle edilmeli
        Thread.sleep(5000); // Event processing için bekle
        
        List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
            100L, Notification.NotificationStatus.PENDING);
        
        // Invalid event için notification oluşturulmamalı
        assertTrue(notifications.isEmpty());
    }
    
    @Test
    void testEventRetryMechanism() throws Exception {
        // Bu test gerçek email service mock'lanarak yapılabilir
        // Şimdilik basic structure'ı test ediyoruz
        
        // Given
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "shipment.created");
        eventData.put("shipmentId", 1L);
        eventData.put("trackingNumber", "TRK123456");
        eventData.put("senderUserId", 100L);
        eventData.put("status", "CREATED");
        eventData.put("eventTimestamp", LocalDateTime.now().toString());
        eventData.put("eventSource", "shipment-service");
        eventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(eventData);
        
        // When
        kafkaTemplate.send("shipment.created", eventJson);
        
        // Then
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                100L, Notification.NotificationStatus.PENDING);
            
            assertFalse(notifications.isEmpty());
            
            // Retry count başlangıçta 0 olmalı
            Notification notification = notifications.get(0);
            assertEquals(0, notification.getRetryCount());
            assertTrue(notification.getMaxRetries() >= 3);
        });
    }
    
    // Helper methods
    private void sendShipmentCreatedEvent() throws Exception {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "shipment.created");
        eventData.put("shipmentId", 1L);
        eventData.put("trackingNumber", "TRK123456");
        eventData.put("senderUserId", 100L);
        eventData.put("status", "CREATED");
        eventData.put("eventTimestamp", LocalDateTime.now().toString());
        eventData.put("eventSource", "shipment-service");
        eventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(eventData);
        kafkaTemplate.send("shipment.created", eventJson);
    }
    
    private void sendStatusChangedEvent() throws Exception {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "status.changed");
        eventData.put("shipmentId", 1L);
        eventData.put("trackingNumber", "TRK123456");
        eventData.put("userId", 100L);
        eventData.put("currentStatus", "IN_TRANSIT");
        eventData.put("previousStatus", "CREATED");
        eventData.put("location", "İstanbul Hub");
        eventData.put("eventTimestamp", LocalDateTime.now().toString());
        eventData.put("eventSource", "tracking-service");
        eventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(eventData);
        kafkaTemplate.send("status.changed", eventJson);
    }
    
    private void sendDeliveryCompletedEvent() throws Exception {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "delivery.completed");
        eventData.put("shipmentId", 1L);
        eventData.put("trackingNumber", "TRK123456");
        eventData.put("userId", 100L);
        eventData.put("currentStatus", "DELIVERED");
        eventData.put("location", "Ankara Merkez");
        eventData.put("recipientName", "Ahmet Yılmaz");
        eventData.put("eventTimestamp", LocalDateTime.now().toString());
        eventData.put("eventSource", "tracking-service");
        eventData.put("customerEmail", "test@example.com");
        
        String eventJson = objectMapper.writeValueAsString(eventData);
        kafkaTemplate.send("delivery.completed", eventJson);
    }
} 