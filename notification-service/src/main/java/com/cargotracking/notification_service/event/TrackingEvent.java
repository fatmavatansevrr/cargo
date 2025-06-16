package com.cargotracking.notification_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TrackingEvent - Kafka'dan gelen tracking olayları
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    
    private String eventType; // status.changed, delivery.completed, delivery.failed
    private Long shipmentId;
    private String trackingNumber;
    private Long userId;
    
    private String currentStatus;
    private String previousStatus;
    private String location;
    private String description;
    
    private LocalDateTime eventTimestamp;
    private String eventSource;
    
    // Customer information
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Tracking details
    private String carrierName;
    private String deliveryDate;
    private String deliveryTime;
    private String recipientName;
    private String deliveryLocation;
    
    // Additional metadata
    private String notes;
    private String updateSource; // GPS, Manual, Scanner, etc.
    
    // Explicit getter methods (in addition to Lombok @Data)
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public Long getShipmentId() {
        return shipmentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public String getPreviousStatus() {
        return previousStatus;
    }
    
    public String getLocation() {
        return location;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public String getCarrierName() {
        return carrierName;
    }
    
    public String getEventSource() {
        return eventSource;
    }
} 