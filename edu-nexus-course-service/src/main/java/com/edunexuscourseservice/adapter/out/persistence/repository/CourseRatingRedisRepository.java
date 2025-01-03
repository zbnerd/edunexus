package com.edunexuscourseservice.adapter.out.persistence.repository;

public interface CourseRatingRedisRepository {

    void cacheReviewRating(Long courseId, int rating);
    void updateReviewRating(Long courseId, int originalRating, int updatedRating);
    void deleteReviewRating(Long courseId, int originalRating);
    double getAverageReviewRating(Long courseId);
    void initializeRating(Long courseId, int total, int count);
}
