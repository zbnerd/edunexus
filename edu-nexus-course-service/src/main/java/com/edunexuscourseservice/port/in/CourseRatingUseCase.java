package com.edunexuscourseservice.port.in;


import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Use case interface for Course Rating operations
 *
 * Per ADR-000 (Cache-Aside pattern):
 * - Removed initCourseRatings() - no more full loading on startup
 * - Cache is populated lazily on first access
 */
public interface CourseRatingUseCase {
    CourseRating addRatingToCourse(Long courseId, CourseRating courseRating);
    CourseRating updateRating(Long ratingId, CourseRating newCourseRating);
    Optional<CourseRating> getRating(Long ratingId);
    void deleteRating(Long ratingId);
    List<CourseRating> getAllRatingsByCourseId(Long courseId);
    Double getAverageRatingByCourseId(Long courseId);

    /**
     * Batch fetch average ratings for multiple courses to avoid N+1 queries.
     * Returns a map of courseId to average rating (0.0 if not found in cache).
     *
     * @param courseIds List of course IDs
     * @return Map of courseId to average rating
     */
    Map<Long, Double> getAverageRatingsByCourseIds(List<Long> courseIds);
}
