package com.edunexusgraphql.port.client.impl;

import com.edunexusgraphql.model.Course;
import com.edunexusgraphql.model.CourseRating;
import com.edunexusgraphql.model.CourseSession;
import com.edunexusgraphql.port.client.CourseClient;
import com.edunexusgraphql.provider.CourseApiUrlProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * RestTemplate-based implementation of CourseClient.
 * Handles HTTP communication with the course service.
 */
@Slf4j
@Component
public class CourseRestClient implements CourseClient {

    private final RestTemplate restTemplate;
    private final CourseApiUrlProvider urlProvider;

    public CourseRestClient(RestTemplate restTemplate, CourseApiUrlProvider urlProvider) {
        this.restTemplate = restTemplate;
        this.urlProvider = urlProvider;
    }

    @Override
    public Course createCourse(String title, String description, Long instructorId) {
        log.debug("Creating course: title={}, instructorId={}", title, instructorId);
        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setInstructorId(instructorId);

        return restTemplate.postForObject(urlProvider.getBaseUrl(), course, Course.class);
    }

    @Override
    public Course updateCourse(Long id, String title, String description) {
        log.debug("Updating course: id={}, title={}", id, title);
        Course course = new Course();
        course.setId(id);
        course.setTitle(title);
        course.setDescription(description);

        restTemplate.put(urlProvider.courseByIdUri(id), course);
        return course;
    }

    @Override
    @Cacheable(value = "course", key = "#courseId")
    public Optional<Course> findCourseById(Long courseId) {
        log.debug("Finding course by id: {}", courseId);
        Course course = restTemplate.getForObject(urlProvider.courseByIdUri(courseId), Course.class);
        if (course != null) {
            course.setId(courseId);
        }
        return Optional.ofNullable(course);
    }

    @Override
    @Cacheable(value = "courses", key = "#courseIds")
    public List<Course> findCoursesByIds(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return Collections.emptyList();
        }

        log.debug("Finding {} courses by ids", courseIds.size());
        Course[] courses = restTemplate.postForObject(urlProvider.batchCoursesUri(), courseIds, Course[].class);
        if (courses == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(courses);
    }

    @Override
    public List<Course> findAllCourses(String title, String description, Integer page) {
        log.debug("Finding all courses: title={}, page={}", title, page);
        String url = urlProvider.coursesQueryUri(title, description, page).toString();

        Course[] courses = restTemplate.getForObject(url, Course[].class);
        if (courses == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(courses);
    }

    @Override
    public CourseSession addSessionToCourse(Long courseId, String title) {
        log.debug("Adding session to course: courseId={}, title={}", courseId, title);
        CourseSession courseSession = new CourseSession();
        courseSession.setTitle(title);

        CourseSession addedSession = restTemplate.postForObject(
                urlProvider.courseSessionsUri(courseId), courseSession, CourseSession.class);

        if (addedSession != null) {
            addedSession.setCourseId(courseId);
        }

        return addedSession;
    }

    @Override
    public Optional<CourseSession> findSessionById(Long courseId, Long sessionId) {
        log.debug("Finding session: courseId={}, sessionId={}", courseId, sessionId);
        try {
            CourseSession retrievedSession = restTemplate.getForObject(
                    urlProvider.courseSessionUri(courseId, sessionId), CourseSession.class);
            if (retrievedSession != null) {
                retrievedSession.setCourseId(courseId);
            }

            return Optional.ofNullable(retrievedSession);
        } catch (Exception e) {
            log.warn("Failed to find session: courseId={}, sessionId={}", courseId, sessionId, e);
            return Optional.empty();
        }
    }

    @Override
    public List<CourseSession> findAllSessionsByCourseId(Long courseId) {
        log.debug("Finding all sessions for courseId={}", courseId);
        CourseSession[] sessions = restTemplate.getForObject(
                urlProvider.courseSessionsUri(courseId), CourseSession[].class);
        if (sessions == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(sessions)
                .peek(session -> session.setCourseId(courseId))
                .toList();
    }

    @Override
    public CourseRating addRatingToCourse(Long userId, Long courseId, Integer rating, String comment) {
        log.debug("Adding rating to course: userId={}, courseId={}, rating={}", userId, courseId, rating);
        CourseRating courseRating = new CourseRating();
        courseRating.setUserId(userId);
        courseRating.setCourseId(courseId);
        courseRating.setRating(rating);
        courseRating.setComment(comment);

        return restTemplate.postForObject(
                urlProvider.courseRatingsUri(courseId), courseRating, CourseRating.class);
    }
}
