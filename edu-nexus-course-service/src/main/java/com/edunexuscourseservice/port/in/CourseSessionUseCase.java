package com.edunexuscourseservice.port.in;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;

import java.util.List;
import java.util.Optional;

public interface CourseSessionUseCase {
    CourseSession addSessionToCourse(Long courseId, CourseSession courseSession);
    CourseSession updateSession(Long sessionId, CourseSession newCourseSession);
    Optional<CourseSession> getSession(Long sessionId);
    List<CourseSession> getAllSessionsByCourseId(Long courseId);
}
