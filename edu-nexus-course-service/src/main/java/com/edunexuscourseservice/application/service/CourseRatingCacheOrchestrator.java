package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Course Rating Cache Orchestrator
 *
 * Handles Kafka event coordination for cache updates.
 * Single Responsibility: Orchestrate asynchronous cache updates via Kafka events.
 *
 * Implements Fire-and-Forget pattern per ADR-000:
 * - No response events sent back
 * - Cache updates are asynchronous
 * - Cache failures don't trigger compensating transactions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingCacheOrchestrator {

    private final CourseRatingProducerService producer;

    /**
     * Handle rating added event - trigger cache update.
     *
     * @param courseId Course ID
     * @param rating Rating value
     * @param ratingId Rating entity ID
     */
    public void onRatingAdded(Long courseId, int rating, Long ratingId) {
        producer.sendRatingAddedEvent(courseId, rating, ratingId);
        log.debug("Orchestrated cache update for rating added to course {}", courseId);
    }

    /**
     * Handle rating updated event - trigger cache update.
     *
     * @param courseId Course ID
     * @param oldRating Previous rating value
     * @param newRating New rating value
     * @param comment Rating comment
     */
    public void onRatingUpdated(Long courseId, int oldRating, int newRating, String comment) {
        producer.sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        log.debug("Orchestrated cache update for rating updated on course {}", courseId);
    }

    /**
     * Handle rating deleted event - trigger cache update.
     *
     * @param courseId Course ID
     * @param oldRating Deleted rating value
     */
    public void onRatingDeleted(Long courseId, int oldRating) {
        producer.sendRatingDeletedEvent(courseId, oldRating);
        log.debug("Orchestrated cache update for rating deleted from course {}", courseId);
    }
}
