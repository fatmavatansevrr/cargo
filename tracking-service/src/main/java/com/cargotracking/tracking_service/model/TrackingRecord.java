package com.cargotracking.tracking_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "tracking_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingRecord {

    @Id
    private String id;

    private String shipmentId;

    private TrackingState status;

    private String location;

    private String updatedBy;

    private LocalDateTime updatedAt;

    private String receiverEmail;
}
