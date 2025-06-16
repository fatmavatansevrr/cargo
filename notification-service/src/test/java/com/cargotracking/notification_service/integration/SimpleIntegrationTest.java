package com.cargotracking.notification_service.integration;

import com.cargotracking.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simple Integration Test
 * Kafka event processing testleri - MongoDB mock'lanmış
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(
    partitions = 1,
    topics = {"shipment.created", "shipment.updated", "status.changed"},
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9093",
        "port=9093"
    }
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "eureka.client.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SimpleIntegrationTest {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @MockBean
    private NotificationService notificationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
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
        
        String eventJson = objectMapper.writeValueAsString(shipmentEventData);
        
        // When
        kafkaTemplate.send("shipment.created", eventJson);
        
        // Then - Kafka event'in işlendiğini doğrula
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, atLeast(1)).processShipmentEvent(any());
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
        
        String eventJson = objectMapper.writeValueAsString(trackingEventData);
        
        // When
        kafkaTemplate.send("status.changed", eventJson);
        
        // Then - Kafka event'in işlendiğini doğrula
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, atLeast(1)).processTrackingEvent(any());
        });
    }
    
    @Test
    void testMultipleEventsProcessing() throws Exception {
        // Given
        String shipmentEvent = createShipmentEventJson();
        String trackingEvent = createTrackingEventJson();
        
        // When - Birden fazla event gönder
        kafkaTemplate.send("shipment.created", shipmentEvent);
        kafkaTemplate.send("status.changed", trackingEvent);
        
        // Then - Her iki event'in de işlendiğini doğrula
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(notificationService, atLeast(1)).processShipmentEvent(any());
            verify(notificationService, atLeast(1)).processTrackingEvent(any());
        });
    }
    
    private String createShipmentEventJson() throws Exception {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "shipment.created");
        eventData.put("shipmentId", 1L);
        eventData.put("trackingNumber", "TRK123456");
        eventData.put("senderUserId", 100L);
        eventData.put("status", "CREATED");
        eventData.put("eventTimestamp", LocalDateTime.now().toString());
        eventData.put("eventSource", "shipment-service");
        eventData.put("customerName", "Ahmet Yılmaz");
        eventData.put("customerEmail", "test@example.com");
        
        return objectMapper.writeValueAsString(eventData);
    }
    
    private String createTrackingEventJson() throws Exception {
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
        
        return objectMapper.writeValueAsString(eventData);
    }
} 