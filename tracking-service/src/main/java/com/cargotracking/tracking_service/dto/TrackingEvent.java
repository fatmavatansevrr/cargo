package com.cargotracking.tracking_service.dto;

import com.cargotracking.tracking_service.model.TrackingState;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingEvent {
    private String trackingNumber;
    private TrackingState newStatus;
    private String location;
    private String updatedBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private String recipientEmail;
    private String receiverFullName;

    private Long senderUserId;

    private Long companyId;
    private Long carrierUserId;

}
