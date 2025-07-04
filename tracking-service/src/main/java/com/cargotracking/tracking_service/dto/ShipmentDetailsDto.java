package com.cargotracking.tracking_service.dto;

import com.cargotracking.tracking_service.model.TrackingState;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShipmentDetailsDto {
    private Long id;
    private String trackingNumber;
    private Long senderCustomerId;
    private Long assignedCarrierId;
    private Long shipmentCompanyId;
    private AddressDto recipientAddress;
    private AddressDto senderAddress;
    private String status;
    private String serviceType;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;

    @Data
    public static class AddressDto {
        private String fullName;
        private String email;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
