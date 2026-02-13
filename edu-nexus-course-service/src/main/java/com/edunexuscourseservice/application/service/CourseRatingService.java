package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Course Rating Service
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

    private final CourseRatingRepository courseRatingRepository;
    private final CourseRepository courseRepository;
    private final CourseRatingRedisRepository courseRatingRedisRepository;
    private final CourseRatingProducerService courseRatingProducerService;

    @Transactional
    public CourseRating addRatingToCourse(Long courseId, CourseRating courseRating) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseRating.setCourse(course);
        CourseRating savedCourseRating = courseRatingRepository.save(courseRating);

        // Fire-and-forget cache update (async via Kafka)
        courseRatingProducerService.sendRatingAddedEvent(courseId, courseRating.getRating(), savedCourseRating.getId());
        return savedCourseRating;
    }

    @Transactional
    public CourseRating updateRating(Long ratingId, CourseRating newCourseRating) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        int oldCourseRating = courseRating.getRating();

        courseRating.updateCourseRating(newCourseRating);
        int newCourseRatings = courseRating.getRating();

        // Fire-and-forget cache update (async via Kafka)
        courseRatingProducerService.sendRatingUpdatedEvent(courseRating.getCourse().getId(), oldCourseRating, newCourseRatings, courseRating.getComment());
        return courseRating;
    }

    public Optional<CourseRating> getRating(Long ratingId) {
        return courseRatingRepository.findById(ratingId);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        // Fire-and-forget cache update (async via Kafka)
        courseRatingProducerService.sendRatingDeletedEvent(courseRating.getCourse().getId(), courseRating.getRating());
        courseRatingRepository.deleteById(ratingId);
    }

    public List<CourseRating> getAllRatingsByCourseId(Long courseId) {
        return courseRatingRepository.findByCourseId(courseId);
    }

    /**
     * Get average rating with Cache-Aside pattern
     *
     * If cache miss, returns 0.0. Cache will be populated asynchronously via Kafka events.
     * For initial cache population, data flows from DB -> Kafka -> Redis on first write.
     *
     * @param courseId Course ID
     * @return Average rating from cache, or 0.0 if not cached yet
     */
    public Double getAverageRatingByCourseId(Long courseId) {
        try {
            return courseRatingRedisRepository.getAverageReviewRating(courseId);
        } catch (Exception e) {
            log.warn("Failed to get average rating from cache for course {}, returning 0.0. Error: {}",
                    courseId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Batch fetch average ratings for multiple courses to avoid N+1 queries.
     * Returns a map of courseId to average rating (0.0 if not found in cache).
     *
     * @param courseIds List of course IDs
     * @return Map of courseId to average rating
     */
    public Map<Long, Double> getAverageRatingsByCourseIds(List<Long> courseIds) {
        Map<Long, Double> ratings = new HashMap<>();

        for (Long courseId : courseIds) {
            try {
                Double avgRating = courseRatingRedisRepository.getAverageReviewRating(courseId);
                ratings.put(courseId, avgRating);
            } catch (Exception e) {
                log.warn("Failed to get average rating from cache for course {}, returning 0.0. Error: {}",
                        courseId, e.getMessage());
                ratings.put(courseId, 0.0);
            }
        }

        return ratings;
    }

}
