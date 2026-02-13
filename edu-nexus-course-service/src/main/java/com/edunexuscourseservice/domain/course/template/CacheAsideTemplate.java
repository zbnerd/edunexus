package com.edunexuscourseservice.domain.course.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Cache-Aside Template implementation
 *
 * Implements proper Cache-Aside pattern:
 * - Lazy loading on cache miss
 * - TTL-based expiration
 * - Non-blocking cache failures (cache is optimization, not SOT)
 * - Cache statistics tracking
 *
 * Refactored to delegate specialized operations:
 * - SCAN operations delegated to RedisScanOperations
 * - Counter operations delegated to RedisCounterOperations
 * - Metrics tracking delegated to CacheMetrics
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheAsideTemplate {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMetrics metrics;
    private final RedisScanOperations scanOperations;
    private final RedisCounterOperations counterOperations;

    /**
     * Get value from cache, or load from source on miss
     *
     * @param key Cache key
     * @param loader Function to load value on cache miss
     * @param ttl Time to live for cached value
     * @param <T> Type of cached value
     * @return Cached or loaded value
     */
    @SuppressWarnings("unchecked")
    public <T> T getOrLoad(String key, Supplier<T> loader, Duration ttl) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                metrics.recordHit();
                log.debug("Cache HIT for key: {}", key);
                return (T) cached;
            }
        } catch (Exception e) {
            log.warn("Cache GET failed for key: {}, treating as miss. Error: {}", key, e.getMessage());
        }

        metrics.recordMiss();
        log.debug("Cache MISS for key: {}, loading from source", key);

        // Load from source
        T value = loader.get();

        // Cache asynchronously (don't fail if cache is down)
        if (value != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl);
                log.debug("Cached key: {} with TTL: {}", key, ttl);
            } catch (Exception e) {
                log.warn("Failed to cache key: {}. Error: {}", key, e.getMessage());
                // Don't throw - cache is optional
            }
        }

        return value;
    }

    /**
     * Put value in cache with TTL
     *
     * @param key Cache key
     * @param value Value to cache
     * @param ttl Time to live
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached key: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.warn("Failed to cache key: {}. Error: {}", key, e.getMessage());
            // Don't throw - cache is optional
        }
    }

    /**
     * Invalidate cache entry
     *
     * @param key Cache key to invalidate
     */
    public void evict(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Evicted cache key: {}", key);
        } catch (Exception e) {
            log.warn("Failed to evict cache key: {}. Error: {}", key, e.getMessage());
        }
    }

    /**
     * SCAN keys matching pattern (non-blocking alternative to KEYS)
     *
     * @param pattern Key pattern to match
     * @return List of matching keys
     */
    public List<String> scan(String pattern) {
        return scanOperations.scan(pattern);
    }

    /**
     * Delete keys matching pattern using SCAN (non-blocking)
     *
     * @param pattern Key pattern to match and delete
     * @return Number of keys deleted
     */
    public long deleteByPattern(String pattern) {
        return scanOperations.deleteByPattern(pattern);
    }

    /**
     * Increment counter atomically (for rating totals/counts)
     *
     * @param key Counter key
     * @param delta Amount to increment
     * @param ttl Time to live (resets on each update)
     * @return New value
     */
    public Long increment(String key, long delta, Duration ttl) {
        return counterOperations.increment(key, delta, ttl);
    }

    /**
     * Get current cache metrics
     *
     * @return Cache metrics snapshot
     */
    public CacheMetrics getMetrics() {
        return metrics;
    }

    /**
     * Reset cache metrics
     */
    public void resetMetrics() {
        metrics.reset();
    }
}
