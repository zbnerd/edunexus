package com.edunexuscourseservice.domain.course.repository;

import com.edunexuscourseservice.domain.course.entity.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
    List<CourseSession> findByCourseId(Long courseId);
}
