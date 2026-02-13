package com.edunexuscourseservice.application.event;

import com.edunexuscourseservice.adapter.out.persistence.entity.redis.RCourse;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Transactional event listener for cache invalidation.
 *
 * Listens for course update events AFTER_COMMIT phase to ensure:
 * 1. Database transaction has successfully committed
 * 2. Cache operations are separated from DB transaction
 * 3. Cache failures don't cause DB transaction rollback
 *
 * This implements the Cache-Aside pattern with proper transaction boundaries.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationListener {

    private final CourseRedisRepository courseRedisRepository;

    /**
     * Handle course update event AFTER database transaction commits.
     *
     * Phase: AFTER_COMMIT ensures this only runs after successful DB commit
     * Async: Cache operations run asynchronously to not block the main thread
     *
     * @param event The course updated event
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseUpdatedEvent(CourseUpdatedEvent event) {
        log.info("Processing cache invalidation for courseId: {}, eventType: {}",
                event.getCourseId(), event.getEventType());

        try {
            invalidateCache(event.getCourseId());
            log.info("Cache invalidation completed for courseId: {}", event.getCourseId());
        } catch (Exception e) {
            log.error("Cache invalidation failed for courseId: {} (non-fatal, cache will auto-heal on next read)",
                    event.getCourseId(), e);
            // Cache failures are non-fatal - cache will be refreshed on next read
            // This separates cache concerns from transactional data integrity
        }
    }

    /**
     * Invalidate the cache entry for a specific course.
     *
     * @param courseId The course ID to invalidate
     */
    private void invalidateCache(Long courseId) {
        log.debug("Invalidating Redis cache for courseId: {}", courseId);

        try {
            // Check if cache entry exists
            if (courseRedisRepository.existsById(courseId)) {
                courseRedisRepository.deleteById(courseId);
                log.debug("Cache entry deleted for courseId: {}", courseId);
            } else {
                log.debug("No cache entry found for courseId: {} (nothing to invalidate)", courseId);
            }
        } catch (Exception e) {
            log.error("Failed to invalidate cache for courseId: {}", courseId, e);
            throw e; // Re-throw for logging in handler
        }
    }
}
