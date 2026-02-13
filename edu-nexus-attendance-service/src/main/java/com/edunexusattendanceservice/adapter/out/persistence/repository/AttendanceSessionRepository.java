package com.edunexusattendanceservice.adapter.out.persistence.repository;

import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for AttendanceSession entity
 */
@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    /**
     * Find attendance session by session ID
     */
    Optional<AttendanceSession> findBySessionId(Long sessionId);

    /**
     * Find all attendance sessions for a course
     */
    List<AttendanceSession> findByCourseId(Long courseId);

    /**
     * Find sessions that need to be marked as absent
     * (sessions that have ended but users haven't checked in)
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.scheduledEnd < :now AND s.autoMarkAbsent = true")
    List<AttendanceSession> findSessionsEndedForAbsenceMarking(@Param("now") LocalDateTime now);

    /**
     * Find active sessions (currently within check-in window)
     * Note: This is a simplified query - the actual time window calculation should be done in the service layer
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.scheduledStart <= :now AND s.scheduledEnd >= :now")
    List<AttendanceSession> findActiveSessions(@Param("now") LocalDateTime now);

    /**
     * Find upcoming sessions for a course
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.courseId = :courseId AND s.scheduledStart > :now ORDER BY s.scheduledStart")
    List<AttendanceSession> findUpcomingSessionsByCourseId(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);

    /**
     * Find past sessions for a course
     */
    @Query("SELECT s FROM AttendanceSession s WHERE s.courseId = :courseId AND s.scheduledEnd < :now ORDER BY s.scheduledStart DESC")
    List<AttendanceSession> findPastSessionsByCourseId(@Param("courseId") Long courseId, @Param("now") LocalDateTime now);

    /**
     * Check if session exists
     */
    boolean existsBySessionId(Long sessionId);

    /**
     * Delete sessions by course ID
     */
    void deleteByCourseId(Long courseId);
}
