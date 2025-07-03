package com.cargotracking.tracking_service.service;

import com.cargotracking.tracking_service.model.TrackingRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TrackingCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "tracking:";

    public TrackingCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verilen shipmentId için TrackingRecord bilgisini Redis'e cache'ler.
     */
    public void cacheTrackingInfo(String shipmentId, TrackingRecord record) {
        String key = PREFIX + shipmentId;
        redisTemplate.opsForValue().set(key, record, Duration.ofHours(1));
    }

    /**
     * Redis'ten ilgili shipmentId için TrackingRecord bilgisini getirir.
     */
    public TrackingRecord getTrackingInfo(String shipmentId) {
        String key = PREFIX + shipmentId;
        Object result = redisTemplate.opsForValue().get(key);
        return (result instanceof TrackingRecord) ? (TrackingRecord) result : null;
    }

    /**
     * Redis'ten ilgili shipmentId'ye ait cache verisini temizler.
     */
    public void evictTrackingInfo(String shipmentId) {
        String key = PREFIX + shipmentId;
        redisTemplate.delete(key);
    }
}
