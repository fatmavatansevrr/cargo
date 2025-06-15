package com.cargotracking.shipment_service.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.cargotracking.shipment_service.model.Priority;
import com.cargotracking.shipment_service.model.ShipmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateShipmentRequest {
    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Sender name is required")
    @Size(max = 100, message = "Sender name must not exceed 100 characters")
    private String senderName;

    @NotBlank(message = "Sender phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String senderPhone;

    @NotBlank(message = "Sender email is required")
    @Email(message = "Invalid email format")
    private String senderEmail;

    @Valid
    @NotNull(message = "Sender address is required")
    private AddressDTO senderAddress;

    @NotBlank(message = "Receiver name is required")
    @Size(max = 100, message = "Receiver name must not exceed 100 characters")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String receiverPhone;

    @NotBlank(message = "Receiver email is required")
    @Email(message = "Invalid email format")
    private String receiverEmail;

    @Valid
    @NotNull(message = "Receiver address is required")
    private AddressDTO receiverAddress;

    @NotNull(message = "Shipment type is required")
    private ShipmentType type;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1 kg")
    @DecimalMax(value = "1000.0", message = "Weight must not exceed 1000 kg")
    private Double weight;

    @NotNull(message = "Cost is required")
    @DecimalMin(value = "0.01", message = "Cost must be positive")
    private BigDecimal cost;

    @Future(message = "Estimated delivery date must be in the future")
    private LocalDateTime estimatedDeliveryDate;

    @NotBlank(message = "Delivery preferences are required")
    @Size(max = 255, message = "Delivery preferences must not exceed 255 characters")
    private String deliveryPreferences;

    @Size(max = 500, message = "Special instructions must not exceed 500 characters")
    private String specialInstructions;
}
