package com.cargotracking.shipment_service.dto;



import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.cargotracking.shipment_service.model.Priority;
import com.cargotracking.shipment_service.model.ShipmentStatus;
import com.cargotracking.shipment_service.model.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateShipmentRequest {
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String senderPhone;

    @Email(message = "Invalid email format")
    private String senderEmail;

    @Valid
    private AddressDTO senderAddress;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String receiverPhone;

    @Email(message = "Invalid email format")
    private String receiverEmail;

    @Valid
    private AddressDTO receiverAddress;

    private ShipmentStatus status;
    private ShipmentType type;
    private Priority priority;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight must not exceed 1000 kg")
    private Double weight;

    @DecimalMin(value = "0.01", message = "Cost must be positive")
    private BigDecimal cost;

    private LocalDateTime estimatedDeliveryDate;

    @Size(max = 255, message = "Delivery preferences must not exceed 255 characters")
    private String deliveryPreferences;

    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;
}
