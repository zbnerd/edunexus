package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.domain.course.template.CacheAsideTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;

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
        String totalKey = RedisKeyGenerator.generateRatingTotalKey(courseId);
        String countKey = RedisKeyGenerator.generateRatingCountKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Initialize counters if not exists (with TTL)
                    cacheAsideTemplate.increment(totalKey, rating, CACHE_TTL);
                    cacheAsideTemplate.increment(countKey, 1, CACHE_TTL);
                    log.debug("Cached rating for course {}: {}", courseId, rating);
                },
                "Failed to cache rating for course {}.", courseId
        );
    }

    @Override
    public void updateReviewRating(Long courseId, int originalRating, int updatedRating) {
        String totalKey = RedisKeyGenerator.generateRatingTotalKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    // Decrement old rating, increment new rating
                    cacheAsideTemplate.increment(totalKey, (long) updatedRating - originalRating, CACHE_TTL);
                    log.debug("Updated cached rating for course {}: {} -> {}", courseId, originalRating, updatedRating);
                },
                "Failed to update cached rating for course {}.", courseId
        );
    }

    @Override
    public void deleteReviewRating(Long courseId, int originalRating) {
        String totalKey = RedisKeyGenerator.generateRatingTotalKey(courseId);
        String countKey = RedisKeyGenerator.generateRatingCountKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    cacheAsideTemplate.increment(totalKey, -originalRating, CACHE_TTL);
                    cacheAsideTemplate.increment(countKey, -1, CACHE_TTL);
                    log.debug("Deleted cached rating for course {}: {}", courseId, originalRating);
                },
                "Failed to delete cached rating for course {}.", courseId
        );
    }

    @Override
    public double getAverageReviewRating(Long courseId) {
        String totalKey = RedisKeyGenerator.generateRatingTotalKey(courseId);
        String countKey = RedisKeyGenerator.generateRatingCountKey(courseId);

        return operationTemplate.executeWithErrorHandling(
                () -> calculateAverageRating(totalKey, countKey, courseId),
                0.0,
                "Failed to get cached average rating for course {}.", courseId
        );
    }

    /**
     * Calculate average rating from cached total and count.
     */
    private double calculateAverageRating(String totalKey, String countKey, Long courseId) {
        Object totalObj = cacheAsideTemplate.getOrLoad(
                totalKey,
                () -> {
                    log.debug("Cache miss for total rating of course {}", courseId);
                    return null;
                },
                CACHE_TTL
        );

        Object countObj = cacheAsideTemplate.getOrLoad(
                countKey,
                () -> {
                    log.debug("Cache miss for rating count of course {}", courseId);
                    return null;
                },
                CACHE_TTL
        );

        if (totalObj == null || countObj == null) {
            log.debug("No cached ratings found for course {}", courseId);
            return 0.0;
        }

        int total = ((Number) totalObj).intValue();
        int count = ((Number) countObj).intValue();

        if (count == 0) {
            return 0.0;
        }

        return (double) total / count;
    }

    @Override
    public void initializeRating(Long courseId, int total, int count) {
        String totalKey = RedisKeyGenerator.generateRatingTotalKey(courseId);
        String countKey = RedisKeyGenerator.generateRatingCountKey(courseId);

        operationTemplate.executeWithErrorHandling(
                () -> {
                    cacheAsideTemplate.put(totalKey, total, CACHE_TTL);
                    cacheAsideTemplate.put(countKey, count, CACHE_TTL);
                    log.debug("Initialized cached ratings for course {}: total={}, count={}", courseId, total, count);
                },
                "Failed to initialize cached ratings for course {}.", courseId
        );
    }
}
