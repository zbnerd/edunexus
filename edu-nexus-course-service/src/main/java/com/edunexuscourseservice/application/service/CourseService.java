package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.application.event.CourseUpdatedEvent;
import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.adapter.out.persistence.entity.redis.RCourse;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRedisRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.port.in.CourseUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService implements CourseUseCase {

    private final CourseRepository courseRepository;
    private final CourseRedisRepository courseRedisRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Saves a new course to the database.
     *
     * @param course the course entity to save
     * @return the saved course with generated ID
     * @throws IllegalArgumentException if course is null
     */
    @Transactional
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * Updates an existing course with new information.
     * Publishes event for cache invalidation AFTER transaction commits.
     * This separates cache operations from DB transaction boundary.
     *
     * @param courseId the ID of the course to update
     * @param newCourse the new course information
     * @return the updated course entity
     * @throws NotFoundException if course with specified ID doesn't exist
     * @throws IllegalArgumentException if newCourse is null
     */
    @Transactional
    public Course updateCourse(Long courseId, Course newCourse) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        Course course = courseOptional.orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        course.updateCourse(newCourse);

        // Publish event for cache invalidation AFTER transaction commits
        // CacheInvalidationListener will handle this in AFTER_COMMIT phase
        eventPublisher.publishEvent(CourseUpdatedEvent.create(courseId, "UPDATE"));

        return course;
    }

    /**
     * Retrieves a course by its ID.
     * Implements cache-aside pattern: checks Redis cache first, falls back to database.
     *
     * @param courseId the unique identifier of the course
     * @return Optional containing the course if found
     * @throws NotFoundException if course with specified ID doesn't exist
     */
    public Optional<Course> getCourseById(Long courseId) {

        Optional<Course> cachedCourse = getCachedCourse(courseId);
        if (cachedCourse.isPresent()) {
            return cachedCourse;
        }

        Optional<Course> courseOptional = courseRepository.findById(courseId);
        Course course = courseOptional.orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseRedisRepository.save(new RCourse(course));
        return courseOptional;
    }

    private Optional<Course> getCachedCourse(Long courseId) {
        Optional<RCourse> rCourseOptional = courseRedisRepository.findById(courseId);

        if(rCourseOptional.isPresent()) {
            RCourse rCourse = rCourseOptional.get();
            Course course = new Course();
            course.setCourseInfo(
                    CourseInfoDto.builder()
                            .title(rCourse.getTitle())
                            .description(rCourse.getDescription())
                            .instructorId(rCourse.getInstructorId())
                            .build()
            );

            return Optional.of(course);
        }

        return Optional.empty();

    }

    /**
     * Retrieves a list of courses matching the search criteria with pagination.
     *
     * @param condition the search conditions to filter courses (title, description, etc.)
     * @param pageable pagination and sorting parameters
     * @return list of courses matching the criteria
     * @throws IllegalArgumentException if condition or pageable is null
     */
    public List<Course> getAllCourses(CourseSearchCondition condition, Pageable pageable) {
        return courseRepository.findAll(condition, pageable);
    }

    /**
     * Batch fetch courses by IDs to avoid N+1 queries.
     * Optimized for GraphQL batch loading and other scenarios requiring multiple courses.
     */
    public List<Course> getCoursesByIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }

        // Use findAllById() which is optimized by JPA to fetch in batches
        // This is much more efficient than individual findById() calls
        return courseRepository.findAllById(courseIds);
    }

}
