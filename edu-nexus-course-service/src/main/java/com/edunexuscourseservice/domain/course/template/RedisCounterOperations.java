package com.edunexuscourseservice.domain.course.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis counter operations
 * <p>
 * Handles atomic increment/decrement operations with TTL management.
 * Used for rating totals, counts, and other numeric counters.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCounterOperations {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Increment counter atomically (for rating totals/counts)
     * <p>
     * Resets TTL on each increment to keep active data cached.
     *
     * @param key Counter key
     * @param delta Amount to increment
     * @param ttl Time to live (resets on each update)
     * @return New value
     * @throws Exception if increment fails (counters are business-critical)
     */
    public Long increment(String key, long delta, Duration ttl) {
        try {
            Long newValue = redisTemplate.opsForValue().increment(key, delta);
            // Reset TTL on increment to keep active data cached
            redisTemplate.expire(key, ttl);
            return newValue;
        } catch (Exception e) {
            log.warn("Failed to increment key: {}. Error: {}", key, e.getMessage());
            throw e; // Re-throw for counters as they're business-critical
        }
    }
}
