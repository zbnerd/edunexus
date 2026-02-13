package com.edunexusgraphql.port.client;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.CourseSession;

import java.util.List;
import java.util.Optional;

/**
 * Client interface for course service operations.
 * Abstracts HTTP/RestTemplate concerns from the application layer.
 */
public interface CourseClient {

    /**
     * Create a new course.
     *
     * @param title Course title
     * @param description Course description
     * @param instructorId Instructor user ID
     * @return Created course
     */
    Course createCourse(String title, String description, Long instructorId);

    /**
     * Update an existing course.
     *
     * @param id Course ID
     * @param title New title
     * @param description New description
     * @return Updated course
     */
    Course updateCourse(Long id, String title, String description);

    /**
     * Find a course by ID.
     *
     * @param courseId Course ID
     * @return Optional course
     */
    Optional<Course> findCourseById(Long courseId);

    /**
     * Find multiple courses by their IDs.
     *
     * @param courseIds List of course IDs
     * @return List of courses
     */
    List<Course> findCoursesByIds(List<Long> courseIds);

    /**
     * Find all courses with optional filters.
     *
     * @param title Title filter (optional)
     * @param description Description filter (optional)
     * @param page Page number (optional)
     * @return List of courses
     */
    List<Course> findAllCourses(String title, String description, Integer page);

    /**
     * Add a session to a course.
     *
     * @param courseId Course ID
     * @param title Session title
     * @return Created session
     */
    CourseSession addSessionToCourse(Long courseId, String title);

    /**
     * Find a session by ID.
     *
     * @param courseId Course ID
     * @param sessionId Session ID
     * @return Optional session
     */
    Optional<CourseSession> findSessionById(Long courseId, Long sessionId);

    /**
     * Find all sessions for a course.
     *
     * @param courseId Course ID
     * @return List of sessions
     */
    List<CourseSession> findAllSessionsByCourseId(Long courseId);

    /**
     * Add a rating to a course.
     *
     * @param userId User ID
     * @param courseId Course ID
     * @param rating Rating value (1-5)
     * @param comment Rating comment
     * @return Created rating
     */
    CourseRating addRatingToCourse(Long userId, Long courseId, Integer rating, String comment);
}
