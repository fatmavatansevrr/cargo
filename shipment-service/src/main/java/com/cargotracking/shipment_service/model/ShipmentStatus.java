package com.cargotracking.shipment_service.model;

public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    EXCEPTION
}