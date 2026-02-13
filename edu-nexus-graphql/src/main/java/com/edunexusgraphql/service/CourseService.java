package com.edunexusgraphql.service;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.port.client.CourseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for course-related operations.
 * Delegates HTTP client operations to CourseClient interface.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService {

    private final CourseClient courseClient;

    public Course createCourse(String title, String description, Long instructorId) {
        return courseClient.createCourse(title, description, instructorId);
    }

    public Course updateCourse(Long id, String title, String description) {
        return courseClient.updateCourse(id, title, description);
    }

    public Optional<Course> findCourseById(Long courseId) {
        return courseClient.findCourseById(courseId);
    }

    public List<Course> findCoursesByIds(List<Long> courseIds) {
        return courseClient.findCoursesByIds(courseIds);
    }

    public List<Course> findAllCourses(String title, String description, Integer page) {
        return courseClient.findAllCourses(title, description, page);
    }

    public CourseSession addSessionToCourse(Long courseId, String title) {
        return courseClient.addSessionToCourse(courseId, title);
    }

    public Optional<CourseSession> findSessionById(Long courseId, Long sessionId) {
        return courseClient.findSessionById(courseId, sessionId);
    }

    public List<CourseSession> findAllSessionsByCourseId(Long courseId) {
        return courseClient.findAllSessionsByCourseId(courseId);
    }

    public CourseRating addRatingToCourse(Long userId, Long courseId, Integer rating, String comment) {
        return courseClient.addRatingToCourse(userId, courseId, rating, comment);
    }
}
