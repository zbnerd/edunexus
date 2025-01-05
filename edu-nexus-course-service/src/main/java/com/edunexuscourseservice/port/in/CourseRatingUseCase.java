package com.edunexuscourseservice.port.in;


import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;

import java.util.List;
import java.util.Optional;

public interface CourseRatingUseCase {
    CourseRating addRatingToCourse(Long courseId, CourseRating courseRating);
    CourseRating updateRating(Long ratingId, CourseRating newCourseRating);
    Optional<CourseRating> getRating(Long ratingId);
    void deleteRating(Long ratingId);
    List<CourseRating> getAllRatingsByCourseId(Long courseId);
    Double getAverageRatingByCourseId(Long courseId);
    void initCourseRatings();
}
