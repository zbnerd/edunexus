package com.edunexusattendanceservice.adapter.out.persistence.repository;

import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for AttendanceSummary entity
 */
@Repository
public interface AttendanceSummaryRepository extends JpaRepository<AttendanceSummary, Long> {

    /**
     * Find summary by user and course
     */
    Optional<AttendanceSummary> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Find all summaries for a user
     */
    List<AttendanceSummary> findByUserId(Long userId);

    /**
     * Find all summaries for a course
     */
    List<AttendanceSummary> findByCourseId(Long courseId);

    /**
     * Find summaries below minimum attendance rate
     */
    @Query("SELECT s FROM AttendanceSummary s WHERE s.courseId = :courseId AND s.attendanceRate < :minRate")
    List<AttendanceSummary> findByCourseIdWithLowAttendance(@Param("courseId") Long courseId, @Param("minRate") double minRate);

    /**
     * Find users at risk (attendance below threshold)
     */
    @Query("SELECT s FROM AttendanceSummary s WHERE s.attendanceRate < :threshold ORDER BY s.attendanceRate ASC")
    List<AttendanceSummary> findUsersAtRisk(@Param("threshold") double threshold);

    /**
     * Calculate average attendance rate for a course
     */
    @Query("SELECT AVG(s.attendanceRate) FROM AttendanceSummary s WHERE s.courseId = :courseId")
    Double calculateAverageAttendanceRateForCourse(@Param("courseId") Long courseId);

    /**
     * Delete summary by user and course
     */
    void deleteByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Check if summary exists
     */
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
