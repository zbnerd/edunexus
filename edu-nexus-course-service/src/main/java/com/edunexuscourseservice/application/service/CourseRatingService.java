package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
//import com.edunexuscourseservice.adapter.out.persistence.repository.redis.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingService {

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

        courseRatingProducerService.sendRatingAddedEvent(courseId, courseRating.getRating());
        return savedCourseRating;
    }

    @Transactional
    public CourseRating updateRating(Long ratingId, CourseRating newCourseRating) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        int oldCourseRating = courseRating.getRating();

        courseRating.updateCourseRating(newCourseRating);
        int newCourseRatings = courseRating.getRating();

        courseRatingProducerService.sendRatingUpdatedEvent(courseRating.getCourse().getId(), oldCourseRating, newCourseRatings);
        return courseRating;
    }

    public Optional<CourseRating> getRating(Long ratingId) {
        return courseRatingRepository.findById(ratingId);
    }

    @Transactional
    public void deleteRating(Long ratingId) {
        CourseRating courseRating = getRating(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        courseRatingProducerService.sendRatingDeletedEvent(courseRating.getCourse().getId(), courseRating.getRating());
        courseRatingRepository.deleteById(ratingId);
    }

//    @LogExecutionTime
    public List<CourseRating> getAllRatingsByCourseId(Long courseId) {
        return courseRatingRepository.findByCourseId(courseId);
    }

//    @LogExecutionTime
    public Double getAverageRatingByCourseId(Long courseId) {
        return courseRatingRedisRepository.getAverageReviewRating(courseId);
    }

//    @LogExecutionTime
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
