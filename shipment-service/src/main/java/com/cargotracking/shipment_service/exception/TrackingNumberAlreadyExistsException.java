package com.cargotracking.shipment_service.exception;

public class TrackingNumberAlreadyExistsException extends RuntimeException {
    public TrackingNumberAlreadyExistsException(String message) {
        super(message);
    }
}
