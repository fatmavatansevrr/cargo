package com.cargotracking.tracking_service.dto;

import com.cargotracking.tracking_service.model.TrackingState;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class StatusUpdateRequest {
    @Schema(
            description = "Gönderinin ID'si (örneğin: SHIP12345)",
            example = "SHIP12345",
            required = true
    )
    private String shipmentId;

    @Schema(
            description = "Yeni statü bilgisi",
            example = "DELIVERED",
            required = true,
            implementation = TrackingState.class
    )
    private TrackingState status;

    @Schema(
            description = "Güncellemenin yapıldığı konum",
            example = "İzmir Transfer Merkezi"
    )
    private String location;

    @Schema(
            description = "Statü güncellemesini yapan sistem ya da kullanıcı",
            example = "SYSTEM"
    )
    private String updatedBy;

    private String recipientEmail;

}
