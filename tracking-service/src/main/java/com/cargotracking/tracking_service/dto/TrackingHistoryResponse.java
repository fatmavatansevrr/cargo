package com.cargotracking.tracking_service.dto;

import com.cargotracking.tracking_service.model.TrackingState;
import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TrackingHistoryResponse {

    @Schema(description = "Kargonun kimliği", example = "12345")
    private String shipmentId;

    @Schema(description = "Kargonun mevcut statüsü", example = "DELIVERED")
    private TrackingState status;

    @Schema(description = "Güncellemenin yapıldığı konum", example = "Ankara Dağıtım Merkezi")
    private String location;

    @Schema(description = "Statü güncellemesini yapan kişi veya sistem", example = "SYSTEM")
    private String updatedBy;

    @Schema(description = "Statünün güncellendiği zaman", example = "2024-06-15T14:30:00")
    private LocalDateTime updatedAt;

    private String recipientEmail;

    @Schema(description = "Alıcı ad soyad", example = "Ahmet Yılmaz")
    private String receiverFullName;

    @Schema(description = "Gönderici user ID", example = "32001")
    private Long senderUserId;

    @Schema(description = "Şirket ID", example = "42")
    private Long companyId;

    @Schema(description = "Kurye (carrier) user ID", example = "42001")
    private Long carrierUserId;

}
