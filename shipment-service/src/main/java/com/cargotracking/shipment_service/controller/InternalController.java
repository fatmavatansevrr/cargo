package com.cargotracking.shipment_service.controller;

import com.cargotracking.shipment_service.dto.ShipmentResponse;
import com.cargotracking.shipment_service.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/shipments")
@RequiredArgsConstructor
@Slf4j
public class InternalController {
    private final ShipmentService shipmentService;


    /**
     * Tracking number'a göre recipient email'i döndür (Notification Service için)
     */
    @Operation(summary = "Get Recipient Email", description = "Tracking number'a göre recipient email'i döndürür")
    @GetMapping("/tracking/{trackingNumber}/recipient-email")
    public ResponseEntity<String> getRecipientEmailByTrackingNumber(
            @Parameter(description = "Tracking Number") @PathVariable String trackingNumber) {

        log.info("📧 Recipient email isteniyor: {}", trackingNumber);

        try {
            String email = shipmentService.getRecipientEmailByTrackingNumber(trackingNumber);

            if (email != null && !email.trim().isEmpty()) {
                log.info("✅ Recipient email bulundu: {} -> {}", trackingNumber, email);
                return ResponseEntity.ok(email);
            } else {
                log.warn("⚠️ Recipient email bulunamadı: {}", trackingNumber);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("❌ Recipient email alınırken hata: {}", trackingNumber, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Tracking number'a göre shipment detaylarını döndür (Public endpoint)
     */
    @Operation(summary = "Get Shipment by Tracking Number", description = "Tracking number'a göre shipment bilgilerini döndürür")
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @Parameter(description = "Tracking Number") @PathVariable String trackingNumber) {

        log.info("🔍 Shipment bilgileri isteniyor: {}", trackingNumber);

        try {
            ShipmentResponse shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);

            if (shipment != null) {
                log.info("✅ Shipment bilgileri bulundu: {}", trackingNumber);
                return ResponseEntity.ok(shipment);
            } else {
                log.warn("⚠️ Shipment bulunamadı: {}", trackingNumber);
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("❌ Shipment bilgileri alınırken hata: {}", trackingNumber, e);
            return ResponseEntity.status(500).build();
        }
    }

}
