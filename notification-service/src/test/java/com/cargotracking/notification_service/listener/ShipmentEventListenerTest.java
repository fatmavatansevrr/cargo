package com.cargotracking.notification_service.listener;

import com.cargotracking.notification_service.event.ShipmentEvent;
import com.cargotracking.notification_service.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ShipmentEventListener Unit Tests
 * Kafka event processing testleri
 */
@ExtendWith(MockitoExtension.class)
class ShipmentEventListenerTest {
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private Acknowledgment acknowledgment;
    
    @InjectMocks
    private ShipmentEventListener shipmentEventListener;
    
    private String validShipmentEventJson;
    private String invalidJson;
    
    @BeforeEach
    void setUp() {
        validShipmentEventJson = """
            {
                "eventType": "shipment.created",
                "shipmentId": 1,
                "trackingNumber": "TRK123456",
                "senderUserId": 100,
                "status": "CREATED",
                "eventTimestamp": "2024-01-01T10:00:00",
                "eventSource": "shipment-service",
                "customerName": "Ahmet Yılmaz",
                "customerEmail": "ahmet@example.com"
            }
            """;
            
        invalidJson = "{ invalid json }";
    }
    
    @Test
    void testHandleShipmentCreated_Success() throws Exception {
        // Given
        ShipmentEvent mockEvent = createMockShipmentEvent();
        when(objectMapper.readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class)))
                .thenReturn(mockEvent);
        
        // When
        shipmentEventListener.handleShipmentCreated(
                validShipmentEventJson, "shipment.created", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class));
        verify(notificationService).processShipmentEvent(any(ShipmentEvent.class));
        verify(acknowledgment).acknowledge();
    }
    
    @Test
    void testHandleShipmentCreated_JsonParsingError() throws Exception {
        // Given
        when(objectMapper.readValue(eq(invalidJson), eq(ShipmentEvent.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));
        
        // When
        shipmentEventListener.handleShipmentCreated(
                invalidJson, "shipment.created", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(invalidJson), eq(ShipmentEvent.class));
        verify(notificationService, never()).processShipmentEvent(any(ShipmentEvent.class));
        verify(acknowledgment, never()).acknowledge(); // Retry için acknowledge etme
    }
    
    @Test
    void testHandleShipmentCreated_ServiceError() throws Exception {
        // Given
        ShipmentEvent mockEvent = createMockShipmentEvent();
        when(objectMapper.readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class)))
                .thenReturn(mockEvent);
        doThrow(new RuntimeException("Service error"))
                .when(notificationService).processShipmentEvent(any(ShipmentEvent.class));
        
        // When
        shipmentEventListener.handleShipmentCreated(
                validShipmentEventJson, "shipment.created", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class));
        verify(notificationService).processShipmentEvent(any(ShipmentEvent.class));
        verify(acknowledgment, never()).acknowledge(); // Retry için acknowledge etme
    }
    
        @Test
    void testHandleShipmentUpdated_Success() throws Exception {
        // Given
        ShipmentEvent mockEvent = createMockShipmentEvent();
        when(objectMapper.readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class)))
                .thenReturn(mockEvent);
        
        // When
        shipmentEventListener.handleShipmentUpdated(
                validShipmentEventJson, "shipment.updated", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class));
        verify(notificationService).processShipmentEvent(any(ShipmentEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    void testHandleShipmentCanceled_Success() throws Exception {
        // Given
        ShipmentEvent mockEvent = createMockShipmentEvent();
        when(objectMapper.readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class)))
                .thenReturn(mockEvent);
        
        // When
        shipmentEventListener.handleShipmentCanceled(
                validShipmentEventJson, "shipment.canceled", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class));
        verify(notificationService).processShipmentEvent(any(ShipmentEvent.class));
        verify(acknowledgment).acknowledge();
    }
    
        @Test
    void testHandleAllShipmentEvents_Success() throws Exception {
        // Given
        ShipmentEvent mockEvent = createMockShipmentEvent();
        when(objectMapper.readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class)))
                .thenReturn(mockEvent);
        
        // When
        shipmentEventListener.handleAllShipmentEvents(
                validShipmentEventJson, "shipment.updated", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(validShipmentEventJson), eq(ShipmentEvent.class));
        verify(acknowledgment).acknowledge();
        // Bu generic listener'da notification service çağrılmaz, sadece loglama
        verify(notificationService, never()).processShipmentEvent(any(ShipmentEvent.class));
    }

    @Test
    void testHandleAllShipmentEvents_Error() throws Exception {
        // Given
        when(objectMapper.readValue(eq(invalidJson), eq(ShipmentEvent.class)))
                .thenThrow(new RuntimeException("JSON parsing error"));
        
        // When
        shipmentEventListener.handleAllShipmentEvents(
                invalidJson, "shipment.updated", 0, 1000L, acknowledgment);
        
        // Then
        verify(objectMapper).readValue(eq(invalidJson), eq(ShipmentEvent.class));
        // Generic listener hatalarda da acknowledge eder (monitoring için)
        verify(acknowledgment, never()).acknowledge();
    }
    
    private ShipmentEvent createMockShipmentEvent() {
        ShipmentEvent event = new ShipmentEvent();
        event.setEventType("shipment.created");
        event.setShipmentId(1L);
        event.setTrackingNumber("TRK123456");
        event.setSenderUserId(100L);
        event.setStatus("CREATED");
        event.setEventTimestamp(LocalDateTime.now());
        event.setEventSource("shipment-service");
        event.setCustomerName("Ahmet Yılmaz");
        event.setCustomerEmail("ahmet@example.com");
        return event;
    }
} 