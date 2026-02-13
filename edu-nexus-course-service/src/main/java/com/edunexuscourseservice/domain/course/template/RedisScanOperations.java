package com.edunexuscourseservice.domain.course.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis SCAN operations
 * <p>
 * Non-blocking alternatives to KEYS command for Redis operations.
 * Uses SCAN to avoid blocking the Redis server during bulk operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScanOperations {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * SCAN keys matching pattern (non-blocking alternative to KEYS)
     *
     * @param pattern Key pattern to match
     * @return List of matching keys
     */
    public List<String> scan(String pattern) {
        List<String> keys = new ArrayList<>();
        try {
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions()
                                .match(pattern)
                                .count(100)
                                .build())) {
                    if (cursor != null) {
                        cursor.forEachRemaining(key -> keys.add(new String(key)));
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("SCAN failed for pattern: {}. Error: {}", pattern, e.getMessage());
        }
        return keys;
    }

    /**
     * Delete keys matching pattern using SCAN (non-blocking)
     *
     * @param pattern Key pattern to match and delete
     * @return Number of keys deleted
     */
    public long deleteByPattern(String pattern) {
        List<String> keys = scan(pattern);
        if (!keys.isEmpty()) {
            try {
                Long deleted = redisTemplate.delete(keys);
                log.info("Deleted {} keys matching pattern: {}", deleted, pattern);
                return deleted != null ? deleted : 0;
            } catch (Exception e) {
                log.warn("Failed to delete keys for pattern: {}. Error: {}", pattern, e.getMessage());
            }
        }
        return 0;
    }
}
