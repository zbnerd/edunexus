package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.config.course.metrics.CourseMetrics;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Course Rating Service Facade
 *
 * Implements CourseRatingUseCase by delegating to specialized services:
 * - CourseRatingCrudService: Database operations
 * - CourseRatingCacheOrchestrator: Kafka event coordination
 * - CourseRatingQueryService: Read operations
 *
 * This facade maintains the existing public interface while separating concerns internally.
 * No client code changes required.
 *
 * Implements Cache-Aside pattern per ADR-000:
 * - DB is source of truth
 * - Cache is updated asynchronously via Kafka
 * - Cache failures don't trigger DB rollbacks
 * - Lazy loading: Cache populated on first access
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingService implements CourseRatingUseCase {

    private final CourseRatingCrudService crudService;
    private final CourseRatingCacheOrchestrator cacheOrchestrator;
    private final CourseRatingQueryService queryService;
    private final CourseMetrics courseMetrics;

    @Transactional
    @Override
    public CourseRating addRatingToCourse(Long courseId, CourseRating courseRating) {
        Timer.Sample sample = courseMetrics.startCourseRetrieval();
        try {
            CourseRating savedRating = crudService.save(courseId, courseRating);

            // Fire-and-forget cache update (async via Kafka)
            cacheOrchestrator.onRatingAdded(courseId, courseRating.getRating(), savedRating.getId());

            courseMetrics.recordRatingCreated();
            courseMetrics.stopCourseRetrieval(sample, "addRating");

            return savedRating;
        } catch (Exception e) {
            log.error("Failed to add rating for course {}", courseId, e);
            throw e;
        }
    }

    @Transactional
    @Override
    public CourseRating updateRating(Long ratingId, CourseRating newCourseRating) {
        // Get old rating before update
        Optional<CourseRating> beforeUpdate = crudService.findById(ratingId);
        int oldRating = beforeUpdate.map(CourseRating::getRating).orElse(0);

        // Update in database
        CourseRating updatedRating = crudService.update(ratingId, newCourseRating);

        // Fire-and-forget cache update (async via Kafka)
        cacheOrchestrator.onRatingUpdated(
                updatedRating.getCourse().getId(),
                oldRating,
                updatedRating.getRating(),
                updatedRating.getComment()
        );

        return updatedRating;
    }

    @Override
    public Optional<CourseRating> getRating(Long ratingId) {
        return crudService.findById(ratingId);
    }

    @Transactional
    @Override
    public void deleteRating(Long ratingId) {
        // Get rating before delete for cache update
        Optional<CourseRating> beforeDelete = crudService.findById(ratingId);
        if (beforeDelete.isEmpty()) {
            throw new NotFoundException(
                    "CourseRating not found with id = " + ratingId);
        }

        CourseRating rating = beforeDelete.get();

        // Fire-and-forget cache update (async via Kafka)
        cacheOrchestrator.onRatingDeleted(rating.getCourse().getId(), rating.getRating());

        // Delete from database
        crudService.delete(ratingId);
    }

    @Override
    public List<CourseRating> getAllRatingsByCourseId(Long courseId) {
        return queryService.getRatingsByCourseId(courseId);
    }

    @Override
    public Double getAverageRatingByCourseId(Long courseId) {
        Timer.Sample sample = courseMetrics.startCourseRetrieval();
        try {
            Double average = queryService.getAverageRating(courseId);
            courseMetrics.stopCourseRetrieval(sample, "getAverageRating");
            return average;
        } finally {
            // Sample is stopped in try block
        }
    }

    @Override
    public Map<Long, Double> getAverageRatingsByCourseIds(List<Long> courseIds) {
        return queryService.getAverageRatings(courseIds);
    }
}
