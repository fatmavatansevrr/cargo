package com.cargotracking.notification_service.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingEvent {
    private String trackingNumber;
    private String previousStatus;  // null olabilir, ilk status ise
    private String currentStatus;   // Örn: DELIVERED, IN_TRANSIT vs.
    private String location;
    private String updatedBy;
    private LocalDateTime timestamp;   // status değişim zamanı
    private String receiverEmail;

    // StatusChangedEvent'ten dönüştürme için static method
    public static TrackingEvent fromStatusChangedEvent(String jsonPayload, com.fasterxml.jackson.databind.ObjectMapper mapper) throws Exception {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> eventMap = mapper.readValue(jsonPayload, java.util.Map.class);

        TrackingEvent trackingEvent = new TrackingEvent();
        trackingEvent.setTrackingNumber((String) eventMap.get("shipmentId"));
        trackingEvent.setPreviousStatus((String) eventMap.get("previousStatus")); // opsiyonel, genelde yok
        trackingEvent.setCurrentStatus(eventMap.get("newStatus") != null ? eventMap.get("newStatus").toString() : null);
        trackingEvent.setLocation((String) eventMap.get("location"));
        trackingEvent.setUpdatedBy((String) eventMap.get("updatedBy"));

        String updatedAtStr = (String) eventMap.get("updatedAt");
        if (updatedAtStr != null) {
            trackingEvent.setTimestamp(LocalDateTime.parse(updatedAtStr));
        }

        trackingEvent.setReceiverEmail((String) eventMap.get("receiverEmail"));
        return trackingEvent;
    }
}
