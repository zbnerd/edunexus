package com.edunexusattendanceservice.adapter.out.persistence.repository;

import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.adapter.out.persistence.entity.condition.AttendanceSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Attendance entity
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Find all attendances for a specific user with pagination
     */
    Page<Attendance> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all attendances for a specific user (non-paginated)
     */
    List<Attendance> findAllByUserId(Long userId);

    /**
     * Find all attendances for a specific user in a course
     */
    List<Attendance> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Find all attendances for a specific session with pagination
     */
    Page<Attendance> findBySessionId(Long sessionId, Pageable pageable);

    /**
     * Find all attendances for a specific session (non-paginated)
     */
    List<Attendance> findAllBySessionId(Long sessionId);

    /**
     * Find attendance by user and session
     */
    Optional<Attendance> findByUserIdAndSessionId(Long userId, Long sessionId);

    /**
     * Check if user is already checked in to a session
     */
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.fetchSize", value = "50"),
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    })
    boolean existsByUserIdAndSessionIdAndCheckOutTimeIsNull(Long userId, Long sessionId);

    /**
     * Count total sessions for a course that a user attended (including absents)
     */
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    @Query("SELECT COUNT(DISTINCT a.sessionId) FROM Attendance a WHERE a.userId = :userId AND a.courseId = :courseId")
    long countTotalSessionsForUserInCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * Count attended sessions (PRESENT or LATE) for a user in a course
     */
    @QueryHints(value = {
        @QueryHint(name = "org.hibernate.cacheable", value = "true"),
        @QueryHint(name = "org.hibernate.fetchSize", value = "100")
    })
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.userId = :userId AND a.courseId = :courseId AND (a.status = 'PRESENT' OR a.status = 'LATE')")
    long countAttendedSessionsForUserInCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    /**
     * Find active check-ins (not checked out) for a user with pagination
     */
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId AND a.checkOutTime IS NULL")
    Page<Attendance> findActiveAttendancesByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find active check-ins (not checked out) for a user (non-paginated)
     */
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId AND a.checkOutTime IS NULL")
    List<Attendance> findAllActiveAttendancesByUser(@Param("userId") Long userId);

    /**
     * Find attendances within a date range with pagination
     */
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId AND a.checkInTime BETWEEN :startDate AND :endDate")
    Page<Attendance> findByUserIdAndCheckInTimeBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * Find attendances within a date range (non-paginated)
     */
    @Query("SELECT a FROM Attendance a WHERE a.userId = :userId AND a.checkInTime BETWEEN :startDate AND :endDate")
    List<Attendance> findAllByUserIdAndCheckInTimeBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
