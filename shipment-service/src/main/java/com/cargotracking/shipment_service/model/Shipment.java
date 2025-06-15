package com.cargotracking.shipment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long receiverId;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false)
    private String senderPhone;

    @Column(nullable = false)
    private String senderEmail;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "sender_address")),
            @AttributeOverride(name = "city", column = @Column(name = "sender_city")),
            @AttributeOverride(name = "country", column = @Column(name = "sender_country")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "sender_postal_code"))
    })
    private Address senderAddress;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverPhone;

    @Column(nullable = false)
    private String receiverEmail;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "address", column = @Column(name = "receiver_address")),
            @AttributeOverride(name = "city", column = @Column(name = "receiver_city")),
            @AttributeOverride(name = "country", column = @Column(name = "receiver_country")),
            @AttributeOverride(name = "postalCode", column = @Column(name = "receiver_postal_code"))
    })
    private Address receiverAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private String description;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private BigDecimal cost;

    private LocalDateTime estimatedDeliveryDate;

    private LocalDateTime actualDeliveryDate;

    @Column(nullable = false)
    private String deliveryPreferences;

    private String specialInstructions;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;
}