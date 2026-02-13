package com.edunexuscourseservice.adapter.out.persistence.repository;

import org.springframework.data.redis.core.RedisTemplate;

public interface CourseRatingRedisRepository {

    void cacheReviewRating(Long courseId, int rating);
    void updateReviewRating(Long courseId, int originalRating, int updatedRating);
    void deleteReviewRating(Long courseId, int originalRating);
    double getAverageReviewRating(Long courseId);
    void initializeRating(Long courseId, int total, int count);

    /**
     * Get the underlying RedisTemplate for advanced operations like pipelines.
     * Used for batch operations to avoid N+1 query problems.
     */
    RedisTemplate<String, Object> getRedisTemplate();
}
