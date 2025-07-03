package com.cargotracking.analytics_service.listener;

import com.cargotracking.analytics_service.dto.AnalyticsDataEvent;
import com.cargotracking.analytics_service.mapper.AnalyticsMapper;
import com.cargotracking.analytics_service.model.ShipmentAnalytics;
import com.cargotracking.analytics_service.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;
    private final AnalyticsMapper analyticsMapper;

    @KafkaListener(topics = "analytics-topic", groupId = "analytics-group")
    public void handleAnalyticsEvent(AnalyticsDataEvent event) {
        log.info("Received analytics event for tracking number: {}", event.getTrackingNumber());
        try {
            ShipmentAnalytics shipmentAnalytics = analyticsMapper.toEntity(event);
            analyticsService.saveAnalytics(shipmentAnalytics);
            log.info("Successfully processed and saved analytics for tracking number: {}", event.getTrackingNumber());
        } catch (Exception e) {
            log.error("Error processing analytics event for tracking number: {}", event.getTrackingNumber(), e);
        }
    }
} 