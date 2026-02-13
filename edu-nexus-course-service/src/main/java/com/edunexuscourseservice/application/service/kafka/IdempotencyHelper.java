package com.edunexuscourseservice.application.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Helper class for implementing idempotent Kafka consumers.
 * Uses Redis to track processed events and prevent duplicate processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyHelper {

    private final StringRedisTemplate redisTemplate;

    private static final String IDEMPOTENCY_KEY_PREFIX = "kafka:idempotency:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    /**
     * Check if an event has already been processed.
     *
     * @param eventId The unique event identifier
     * @param eventType The type of event (e.g., "rating-add", "rating-update")
     * @return true if the event has been processed before, false otherwise
     */
    public boolean isDuplicate(String eventId, String eventType) {
        if (eventId == null || eventId.isBlank()) {
            log.warn("Received event with null or blank eventId, treating as duplicate");
            return true;
        }

        String key = buildKey(eventId, eventType);
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.info("Duplicate event detected: eventId={}, eventType={}", eventId, eventType);
            return true;
        }

        return false;
    }

    /**
     * Mark an event as processed.
     *
     * @param eventId The unique event identifier
     * @param eventType The type of event
     */
    public void markProcessed(String eventId, String eventType) {
        if (eventId == null || eventId.isBlank()) {
            log.warn("Cannot mark event with null or blank eventId as processed");
            return;
        }

        String key = buildKey(eventId, eventType);
        redisTemplate.opsForValue().set(key, "processed", DEFAULT_TTL);
        log.debug("Event marked as processed: eventId={}, eventType={}", eventId, eventType);
    }

    /**
     * Mark an event as processed with custom TTL.
     *
     * @param eventId The unique event identifier
     * @param eventType The type of event
     * @param ttl Time to live for the idempotency key
     */
    public void markProcessed(String eventId, String eventType, Duration ttl) {
        if (eventId == null || eventId.isBlank()) {
            log.warn("Cannot mark event with null or blank eventId as processed");
            return;
        }

        String key = buildKey(eventId, eventType);
        redisTemplate.opsForValue().set(key, "processed", ttl);
        log.debug("Event marked as processed with custom TTL: eventId={}, eventType={}, ttl={}", eventId, eventType, ttl);
    }

    private String buildKey(String eventId, String eventType) {
        return IDEMPOTENCY_KEY_PREFIX + eventType + ":" + eventId;
    }

    /**
     * Clear all idempotency keys for a specific event type.
     * Useful for testing purposes.
     *
     * @param eventType The type of event
     */
    public void clearEventType(String eventType) {
        String pattern = IDEMPOTENCY_KEY_PREFIX + eventType + ":*";
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("Cleared all idempotency keys for eventType: {}", eventType);
    }
}
