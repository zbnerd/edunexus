package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.CourseRating;
import com.edunexuscourseservice.domain.course.entity.CourseSession;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.repository.CourseRatingRepository;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingService {

    private final CourseRatingRepository courseRatingRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public CourseRating addRatingToCourse(Long courseId, CourseRating courseRating) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseRating.setCourse(course);
        return courseRatingRepository.save(courseRating);
    }

    @Transactional
    public CourseRating updateRating(Long ratingId, CourseRating newCourseRating) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        courseRating.updateCourseRating(newCourseRating);
        return courseRating;
    }

    public Optional<CourseRating> getRating(Long ratingId) {
        return courseRatingRepository.findById(ratingId);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        courseRatingRepository.deleteById(ratingId);
    }

    public List<CourseRating> getAllRatingsByCourseId(Long courseId) {
        return courseRatingRepository.findByCourseId(courseId);
    }


}
