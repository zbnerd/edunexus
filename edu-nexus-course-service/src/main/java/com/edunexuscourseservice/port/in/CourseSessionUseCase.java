package com.edunexuscourseservice.port.in;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CourseSessionUseCase {
    CourseSession addSessionToCourse(Long courseId, CourseSession courseSession);
    CourseSession updateSession(Long sessionId, CourseSession newCourseSession);
    Optional<CourseSession> getSession(Long sessionId);
    List<CourseSession> getAllSessionsByCourseId(Long courseId);

    /**
     * Get paginated sessions by course ID to avoid loading entire collections.
     * Recommended over getAllSessionsByCourseId() for better performance.
     *
     * @param courseId Course ID
     * @param pageable Pagination parameters
     * @return Page of course sessions
     */
    Page<CourseSession> getSessionsByCourseIdPaged(Long courseId, Pageable pageable);
}
