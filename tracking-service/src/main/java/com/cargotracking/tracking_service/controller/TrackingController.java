package com.cargotracking.tracking_service.controller;

import com.cargotracking.tracking_service.model.TrackingStatus;
import com.cargotracking.tracking_service.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/{shipmentId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable String shipmentId,
            @RequestParam String status) {
        trackingService.updateStatus(shipmentId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<List<TrackingStatus>> getHistory(@PathVariable String shipmentId) {
        return ResponseEntity.ok(trackingService.getHistory(shipmentId));
    }
}

