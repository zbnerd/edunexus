package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseSessionRepository;
import com.edunexuscourseservice.port.in.CourseSessionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseSessionService implements CourseSessionUseCase {

    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;

    /**
     * Adds a new session to an existing course.
     *
     * @param courseId the ID of the course to add the session to
     * @param courseSession the session entity to add
     * @return the saved course session with generated ID
     * @throws NotFoundException if the course with specified ID doesn't exist
     * @throws IllegalArgumentException if courseSession is null
     */
    @Transactional
    public CourseSession addSessionToCourse(Long courseId, CourseSession courseSession) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseSession.setCourse(course);
        return courseSessionRepository.save(courseSession);
    }

    /**
     * Updates an existing course session with new information.
     *
     * @param sessionId the ID of the session to update
     * @param newCourseSession the new session information
     * @return the updated course session
     * @throws NotFoundException if session with specified ID doesn't exist
     * @throws IllegalArgumentException if newCourseSession is null
     */
    @Transactional
    public CourseSession updateSession(Long sessionId, CourseSession newCourseSession) {
        CourseSession courseSession = getSession(sessionId)
                .orElseThrow(() -> new NotFoundException("CourseSession not found with id = " + sessionId));

        courseSession.updateCourseSession(newCourseSession);
        return courseSession;
    }

    /**
     * Retrieves a course session by its ID.
     *
     * @param sessionId the unique identifier of the session
     * @return Optional containing the session if found, empty otherwise
     */
    public Optional<CourseSession> getSession(Long sessionId) {
        return courseSessionRepository.findById(sessionId);
    }

    /**
     * Retrieves all sessions for a specific course.
     * Consider using {@link #getSessionsByCourseIdPaged(Long, Pageable)} for better
     * performance with large datasets.
     *
     * @param courseId the ID of the course
     * @return list of all sessions for the course
     * @throws IllegalArgumentException if courseId is null
     */
    public List<CourseSession> getAllSessionsByCourseId(Long courseId) {
        return courseSessionRepository.findByCourseId(courseId);
    }

    /**
     * Retrieves paginated sessions for a specific course.
     * Recommended over {@link #getAllSessionsByCourseId(Long)} to avoid loading
     * entire collections for large datasets.
     *
     * @param courseId the ID of the course
     * @param pageable pagination and sorting parameters
     * @return page of course sessions
     * @throws IllegalArgumentException if courseId or pageable is null
     */
    public Page<CourseSession> getSessionsByCourseIdPaged(Long courseId, Pageable pageable) {
        return courseSessionRepository.findByCourseId(courseId, pageable);
    }
}
