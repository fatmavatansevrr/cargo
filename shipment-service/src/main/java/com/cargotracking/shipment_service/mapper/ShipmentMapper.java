package com.cargotracking.shipment_service.mapper;


import com.cargotracking.shipment_service.dto.AddressDTO;
import com.cargotracking.shipment_service.dto.CreateShipmentRequest;
import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.dto.UpdateShipmentRequest;
import com.cargotracking.shipment_service.event.ShipmentCreatedEvent;
import com.cargotracking.shipment_service.model.Address;
import com.cargotracking.shipment_service.model.Shipment;
import org.springframework.web.bind.annotation.Mapping;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Shipment toEntity(CreateShipmentRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "receiverId", ignore = true)
    @Mapping(target = "senderName", ignore = true)
    @Mapping(target = "receiverName", ignore = true)
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntityFromRequest(UpdateShipmentRequest request, @MappingTarget Shipment shipment);

    ShipmentResponse toResponse(Shipment shipment);

    ShipmentCreatedEvent toCreatedEvent(Shipment shipment);

    AddressDTO toAddressDTO(Address address);

    Address toAddress(AddressDTO addressDTO);
}