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
    private String newStatus;   // Örn: DELIVERED, IN_TRANSIT vs.
    private String location;
    private String updatedBy;
    private LocalDateTime timestamp;   // status değişim zamanı
    private String receiverEmail;

    // TrackingEvent'e aşağıdaki alanları ekle
    private Long receiverUserId;
    private String receiverFullName;
    private Long senderUserId;
    private String senderFullName;
    private Long companyId;
    private Long carrierUserId;
    private String carrierFullName;



}
