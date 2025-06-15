package com.cargotracking.shipment_service.service;


import com.cargotracking.shipment_service.event.ShipmentCreatedEvent;
import com.cargotracking.shipment_service.event.ShipmentStatusChangedEvent;

public interface EventPublisherService {
    void publishShipmentCreatedEvent(ShipmentCreatedEvent event);
    void publishShipmentStatusChangedEvent(ShipmentStatusChangedEvent event);
}
