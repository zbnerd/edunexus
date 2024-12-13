package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.CourseRating;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.repository.CourseRatingRedisRepository;
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
    private final CourseRatingRedisRepository courseRatingRedisRepository;

    @Transactional
    public CourseRating addRatingToCourse(Long courseId, CourseRating courseRating) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseRating.setCourse(course);
        CourseRating savedCourseRating = courseRatingRepository.save(courseRating);

        courseRatingRedisRepository.saveReviewRating(courseId, courseRating.getRating());

        return savedCourseRating;
    }

    @Transactional
    public CourseRating updateRating(Long ratingId, CourseRating newCourseRating) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        int oldCourseRating = courseRating.getRating();

        courseRating.updateCourseRating(newCourseRating);
        int newCourseRatings = courseRating.getRating();

        courseRatingRedisRepository.updateReviewRating(courseRating.getCourse().getId(), oldCourseRating, newCourseRatings);
        return courseRating;
    }

    public Optional<CourseRating> getRating(Long ratingId) {
        return courseRatingRepository.findById(ratingId);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        courseRatingRedisRepository.deleteReviewRating(courseRating.getCourse().getId(), courseRating.getRating());
        courseRatingRepository.deleteById(ratingId);
    }

    public List<CourseRating> getAllRatingsByCourseId(Long courseId) {
        return courseRatingRepository.findByCourseId(courseId);
    }

    public Double getAverageRatingByCourseId(Long courseId) {
        return courseRatingRedisRepository.getAverageReviewRating(courseId);
    }

    public void initCourseRatings() {
        List<Course> courseList = courseRepository.findAll();

        for (Course course : courseList) {
            List<CourseRating> ratingList = courseRatingRepository.findByCourseId(course.getId());
            List<Integer> ratings = ratingList.stream()
                    .map(CourseRating::getRating)
                    .toList();

            int totalRating = ratings.stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            courseRatingRedisRepository.initializeRating(course.getId(), totalRating, ratingList.size());
        }
    }

}
