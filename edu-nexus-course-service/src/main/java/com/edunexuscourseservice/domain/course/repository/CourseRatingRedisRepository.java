package com.edunexuscourseservice.domain.course.repository;

public interface CourseRatingRedisRepository {

    void saveReviewRating(Long courseId, int rating);
    void updateReviewRating(Long courseId, int originalRating, int updatedRating);
    void deleteReviewRating(Long courseId, int originalRating);
    double getAverageReviewRating(Long courseId);
}
