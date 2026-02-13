package com.edunexusgraphql.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Provider for building Course Service API URLs.
 * Centralizes URL building logic to avoid string concatenation scattered across services.
 */
@Component
public class CourseApiUrlProvider {

    private static final String SERVICE_NAME = "edu-nexus-course-service";
    private static final String COURSES_PATH = "/courses";

    @Value("${course.service.base-url:https://}" + SERVICE_NAME + COURSES_PATH)
    private String baseUrl;

    /**
     * Get the base URL for course service.
     *
     * @return base URL string
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Build URI for course by ID.
     *
     * @param courseId the course ID
     * @return URI for specific course
     */
    public URI courseByIdUri(Long courseId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{" + "courseId" + "}")
                .buildAndExpand(courseId)
                .toUri();
    }

    /**
     * Build URI for batch courses endpoint.
     *
     * @return URI for batch query
     */
    public URI batchCoursesUri() {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/batch")
                .build().toUri();
    }

    /**
     * Build URI for courses query with pagination.
     *
     * @param title optional title filter
     * @param description optional description filter
     * @param page page number
     * @return URI for courses query
     */
    public URI coursesQueryUri(String title, String description, Integer page) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("title", title)
                .queryParam("description", description)
                .queryParam("page", page)
                .build().toUri();
    }

    /**
     * Build URI for course sessions.
     *
     * @param courseId the course ID
     * @return URI for course sessions
     */
    public URI courseSessionsUri(Long courseId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{" + "courseId" + "}/sessions")
                .buildAndExpand(courseId)
                .toUri();
    }

    /**
     * Build URI for specific session within a course.
     *
     * @param courseId the course ID
     * @param sessionId the session ID
     * @return URI for specific session
     */
    public URI courseSessionUri(Long courseId, Long sessionId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{" + "courseId" + "}/sessions/{" + "sessionId" + "}")
                .buildAndExpand(courseId, sessionId)
                .toUri();
    }

    /**
     * Build URI for course ratings.
     *
     * @param courseId the course ID
     * @return URI for course ratings
     */
    public URI courseRatingsUri(Long courseId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{" + "courseId" + "}/ratings")
                .buildAndExpand(courseId)
                .toUri();
    }

    /**
     * Build URI for specific rating within a course.
     *
     * @param courseId the course ID
     * @param ratingId the rating ID
     * @return URI for specific rating
     */
    public URI courseRatingUri(Long courseId, Long ratingId) {
        return UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{" + "courseId" + "}/ratings/{" + "ratingId" + "}")
                .buildAndExpand(courseId, ratingId)
                .toUri();
    }
}
