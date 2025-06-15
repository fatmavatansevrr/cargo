package com.cargotracking.shipment_service.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String address;
    private String city;
    private String country;
    private String postalCode;
}
