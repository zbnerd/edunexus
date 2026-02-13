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

    @Transactional
    public CourseSession addSessionToCourse(Long courseId, CourseSession courseSession) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found with id = " + courseId));

        courseSession.setCourse(course);
        return courseSessionRepository.save(courseSession);
    }

    @Transactional
    public CourseSession updateSession(Long sessionId, CourseSession newCourseSession) {
        CourseSession courseSession = getSession(sessionId)
                .orElseThrow(() -> new NotFoundException("CourseSession not found with id = " + sessionId));

        courseSession.updateCourseSession(newCourseSession);
        return courseSession;
    }

    public Optional<CourseSession> getSession(Long sessionId) {
        return courseSessionRepository.findById(sessionId);
    }

    /**
     * Get all sessions by course ID.
     * Consider using getSessionsByCourseIdPaged() for better performance with large datasets.
     */
    public List<CourseSession> getAllSessionsByCourseId(Long courseId) {
        return courseSessionRepository.findByCourseId(courseId);
    }

    /**
     * Get paginated sessions by course ID for better performance.
     * Recommended over getAllSessionsByCourseId() to avoid loading entire collections.
     */
    public Page<CourseSession> getSessionsByCourseIdPaged(Long courseId, Pageable pageable) {
        return courseSessionRepository.findByCourseId(courseId, pageable);
    }
}
