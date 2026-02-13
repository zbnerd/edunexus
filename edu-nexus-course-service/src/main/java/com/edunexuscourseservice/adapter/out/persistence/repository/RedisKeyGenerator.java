package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.domain.course.util.RedisKey;

/**
 * Generates Redis keys for course rating operations.
 * <p>
 * Centralizes key generation logic to avoid duplication.
 */
public class RedisKeyGenerator {

    /**
     * Generate the Redis key for rating total.
     *
     * @param courseId The course ID
     * @return The Redis key for total ratings
     */
    public static String generateRatingTotalKey(Long courseId) {
        return RedisKey.COURSE_RATING_TOTAL.getKey(courseId);
    }

    /**
     * Generate the Redis key for rating count.
     *
     * @param courseId The course ID
     * @return The Redis key for rating count
     */
    public static String generateRatingCountKey(Long courseId) {
        return RedisKey.COURSE_RATING_COUNT.getKey(courseId);
    }
}
