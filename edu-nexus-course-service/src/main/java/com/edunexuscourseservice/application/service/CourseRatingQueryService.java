package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Course Rating Query Service
 *
 * Handles read-only operations for course ratings.
 * Single Responsibility: Query ratings from cache and database.
 *
 * Implements Cache-Aside pattern per ADR-000:
 * - Cache is first source for average ratings
 * - Returns 0.0 on cache miss (will be populated asynchronously)
 * - Direct database access for individual ratings
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingQueryService {

    private final CourseRatingCrudService crudService;
    private final CourseRatingRedisRepository redisRepository;

    /**
     * Get average rating from cache.
     *
     * If cache miss, returns 0.0. Cache will be populated asynchronously via Kafka events.
     *
     * @param courseId Course ID
     * @return Average rating from cache, or 0.0 if not cached yet
     */
    public Double getAverageRating(Long courseId) {
        try {
            return redisRepository.getAverageReviewRating(courseId);
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
    public Map<Long, Double> getAverageRatings(List<Long> courseIds) {
        Map<Long, Double> ratings = new HashMap<>();

        for (Long courseId : courseIds) {
            try {
                Double avgRating = redisRepository.getAverageReviewRating(courseId);
                ratings.put(courseId, avgRating);
            } catch (Exception e) {
                log.warn("Failed to get average rating from cache for course {}, returning 0.0. Error: {}",
                        courseId, e.getMessage());
                ratings.put(courseId, 0.0);
            }
        }

        return ratings;
    }

    /**
     * Get all ratings for a specific course from database.
     *
     * @param courseId Course ID
     * @return List of ratings
     */
    public List<CourseRating> getRatingsByCourseId(Long courseId) {
        return crudService.findByCourseId(courseId);
    }
}
