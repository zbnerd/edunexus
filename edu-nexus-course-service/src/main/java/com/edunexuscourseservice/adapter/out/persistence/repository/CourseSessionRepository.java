package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
    List<CourseSession> findByCourseId(Long courseId);
}
