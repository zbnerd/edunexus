package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.CourseSession;
import com.edunexuscourseservice.domain.course.exception.NotFoundException;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import com.edunexuscourseservice.domain.course.repository.CourseSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseSessionService {

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

    public List<CourseSession> getAllSessionsByCourseId(Long courseId) {
        return courseSessionRepository.findByCourseId(courseId);
    }
}
