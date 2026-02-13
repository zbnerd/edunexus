package com.edunexuscourseservice.config.init;

import com.edunexuscourseservice.domain.course.template.CacheAsideTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Redis Cache Initialization Component
 *
 * Removed dangerous patterns:
 * - No more @PostConstruct blocking on startup
 * - No more KEYS(*) command (blocking operation)
 * - No more full data loading on startup
 *
 * Cache-Aside Pattern:
 * - Lazy loading: Cache loads on first access
 * - Cache is ephemeral: Can be wiped and rebuilt from DB
 * - TTL-based expiration ensures data freshness
 *
 * Optional manual cache warmup available via:
 * - Set property redis.cache.warmup.enabled=true to enable on startup
 * - Or call a dedicated warmup endpoint if implemented
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "redis.cache.warmup.enabled", havingValue = "true", matchIfMissing = false)
public class RedisInit {

    private final CacheAsideTemplate cacheAsideTemplate;

    /**
     * Optional cache warmup on startup (disabled by default)
     * Uses SCAN instead of KEYS to avoid blocking
     */
    @jakarta.annotation.PostConstruct
    public void warmupCache() {
        log.info("Redis cache warmup enabled - scanning for existing keys...");
        long keyCount = cacheAsideTemplate.deleteByPattern("edu-nexus-course:*");
        log.info("Cache warmup complete - cleaned up {} old keys", keyCount);
        log.info("Cache will be populated lazily on first access (Cache-Aside pattern)");
    }

}
