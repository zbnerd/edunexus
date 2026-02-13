package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexus.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Course Rating CRUD Service
 *
 * Handles database operations only.
 * Single Responsibility: Persist and retrieve CourseRating entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRatingCrudService {

    private final CourseRatingRepository courseRatingRepository;
    private final CourseRepository courseRepository;

    /**
     * Save a new rating for a course.
     * Sets the course relationship before saving.
     *
     * @param courseId Course ID
     * @param rating Rating to save
     * @return Saved rating with generated ID
     */
    @Transactional
    public CourseRating save(Long courseId, CourseRating rating) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        rating.setCourse(course);
        return courseRatingRepository.save(rating);
    }

    /**
     * Update an existing rating.
     *
     * @param ratingId Rating ID to update
     * @param newRating New rating data
     * @return Updated rating
     */
    @Transactional
    public CourseRating update(Long ratingId, CourseRating newRating) {
        CourseRating existingRating = findById(ratingId)
                .orElseThrow(() -> new NotFoundException("CourseRating not found with id = " + ratingId));

        existingRating.updateCourseRating(newRating);
        return existingRating;
    }

    /**
     * Delete a rating by ID.
     *
     * @param ratingId Rating ID to delete
     */
    @Transactional
    public void delete(Long ratingId) {
        if (!courseRatingRepository.existsById(ratingId)) {
            throw new NotFoundException("CourseRating not found with id = " + ratingId);
        }
        courseRatingRepository.deleteById(ratingId);
    }

    /**
     * Find a rating by ID.
     *
     * @param ratingId Rating ID
     * @return Optional rating
     */
    public Optional<CourseRating> findById(Long ratingId) {
        return courseRatingRepository.findById(ratingId);
    }

    /**
     * Find all ratings for a specific course.
     *
     * @param courseId Course ID
     * @return List of ratings
     */
    public List<CourseRating> findByCourseId(Long courseId) {
        return courseRatingRepository.findByCourseId(courseId);
    }
}
