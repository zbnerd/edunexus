package com.edunexuscourseservice.adapter.out.persistence.repository;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    /**
     * Find all sessions by course ID with pagination to avoid loading entire collections.
     * Use this method instead of findByCourseId() for better performance.
     */
    Page<CourseSession> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Find all sessions by course ID (without pagination).
     * Consider using the paginated version for better performance.
     *
     * @deprecated Use findByCourseId(Long courseId, Pageable pageable) instead
     */
    @Deprecated
    List<CourseSession> findByCourseId(Long courseId);

    /**
     * Find session by ID and course ID in a single query with JOIN FETCH to avoid N+1.
     * This eagerly fetches the associated course to prevent lazy loading issues.
     */
    @Query("SELECT cs FROM CourseSession cs JOIN FETCH cs.course WHERE cs.id = :sessionId AND cs.course.id = :courseId")
    Optional<CourseSession> findByIdAndCourseId(@Param("sessionId") Long sessionId, @Param("courseId") Long courseId);
}
