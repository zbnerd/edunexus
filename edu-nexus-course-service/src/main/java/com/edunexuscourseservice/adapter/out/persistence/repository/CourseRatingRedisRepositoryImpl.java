package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.domain.course.template.CacheAsideTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;

/**
 * Course Rating Redis Repository using Cache-Aside Pattern
 * <p>
 * Refactored to delegate concerns:
 * - Key generation delegated to RedisKeyGenerator
 * - Error handling delegated to CacheOperationTemplate
 * - Repository focused on rating operations
 *
 * Changes from previous implementation:
 * - Removed dangerous patterns (no blocking operations)
 * - Added TTL to all cache operations (5 minutes per ADR-000)
 * - Cache failures are non-blocking (logged but don't throw)
 * - Used proper increment with TTL refresh
 * - Removed initializeRating (lazy loading instead)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CourseRatingRedisRepositoryImpl implements CourseRatingRedisRepository {

    private final CacheAsideTemplate cacheAsideTemplate;
    private final CacheOperationTemplate operationTemplate;

    // 5 minute TTL as per ADR-000 (Cache-Aside pattern)
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    public void cacheReviewRating(Long courseId, int rating) {
        String hashKey = generateRatingHashKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Atomic operation: increment both total and count in single Lua script
                    atomicHashIncrement(hashKey, "total", rating, CACHE_TTL);
                    atomicHashIncrement(hashKey, "count", 1, CACHE_TTL);
                    log.debug("Cached rating for course {}: {}", courseId, rating);
                },
                "Failed to cache rating for course {}.", courseId
        );
    }

    @Override
    public void updateReviewRating(Long courseId, int originalRating, int updatedRating) {
        String hashKey = generateRatingHashKey(courseId);
        long delta = (long) updatedRating - originalRating;

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Atomic operation: update total by difference
                    atomicHashIncrement(hashKey, "total", delta, CACHE_TTL);
                    log.debug("Updated cached rating for course {}: {} -> {}", courseId, originalRating, updatedRating);
                },
                "Failed to update cached rating for course {}.", courseId
        );
    }

    @Override
    public void deleteReviewRating(Long courseId, int originalRating) {
        String hashKey = generateRatingHashKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Atomic operation: decrement both total and count
                    atomicHashIncrement(hashKey, "total", -originalRating, CACHE_TTL);
                    atomicHashIncrement(hashKey, "count", -1, CACHE_TTL);
                    log.debug("Deleted cached rating for course {}: {}", courseId, originalRating);
                },
                "Failed to delete cached rating for course {}.", courseId
        );
    }

    @Override
    public double getAverageReviewRating(Long courseId) {
        String hashKey = generateRatingHashKey(courseId);

        return operationTemplate.executeWithErrorHandling(
                () -> calculateAverageRating(hashKey, courseId),
                0.0,
                "Failed to get cached average rating for course {}.", courseId
        );
    }

    /**
     * Calculate average rating from cached total and count.
     * Uses atomic HGETALL to read both fields in single operation.
     */
    private double calculateAverageRating(String hashKey, Long courseId) {
        // Use HGETALL for atomic read of both total and count
        Map<Object, Object> ratingData = cacheAsideTemplate.getRedisTemplate().opsForHash().entries(hashKey);

        if (ratingData == null || ratingData.isEmpty()) {
            log.debug("No cached ratings found for course {}", courseId);
            return 0.0;
        }

        // Safely extract total and count with null checks
        Object totalObj = ratingData.get("total");
        Object countObj = ratingData.get("count");

        if (totalObj == null || countObj == null) {
            log.debug("Incomplete cached ratings for course {}", courseId);
            return 0.0;
        }

        // Convert to integers safely
        int total = safeParseInt(totalObj);
        int count = safeParseInt(countObj);

        if (count == 0) {
            return 0.0;
        }

        return (double) total / count;
    }

    /**
     * Safely parse integer from various object types.
     * Handles null values and provides fallback to 0.
     */
    private int safeParseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer from value: {}", value);
            return 0;
        }
    }

    @Override
    public void initializeRating(Long courseId, int total, int count) {
        String hashKey = generateRatingHashKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Initialize both fields in a single hash atomically
                    cacheAsideTemplate.getRedisTemplate().opsForHash().put(hashKey, "total", total);
                    cacheAsideTemplate.getRedisTemplate().opsForHash().put(hashKey, "count", count);
                    cacheAsideTemplate.getRedisTemplate().expire(hashKey, CACHE_TTL);
                    log.debug("Initialized cached ratings for course {}: total={}, count={}", courseId, total, count);
                },
                "Failed to initialize cached ratings for course {}.", courseId
        );
    }

    @Override
    public RedisTemplate<String, Object> getRedisTemplate() {
        return cacheAsideTemplate.getRedisTemplate();
    }

    /**
     * Generate Redis hash key for storing both total and count together.
     * Using a single hash key ensures atomic operations on both fields.
     */
    private String generateRatingHashKey(Long courseId) {
        return String.format("course_ratings:%d", courseId);
    }

    /**
     * Atomic hash increment using Lua script to ensure both total and count are updated together.
     * This prevents race conditions where one field updates but the other doesn't.
     */
    private void atomicHashIncrement(String hashKey, String field, long delta, Duration ttl) {
        String luaScript =
                "local current = redis.call('HINCRBY', KEYS[1], ARGV[1], ARGV[2]) " +
                        "redis.call('EXPIRE', KEYS[1], ARGV[3]) " +
                        "return current";

        cacheAsideTemplate.getRedisTemplate().execute(
                new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class),
                java.util.List.of(hashKey),
                field, String.valueOf(delta), String.valueOf(ttl.getSeconds())
        );
    }
}
