package com.edunexusattendanceservice.port.in;

import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Use case interface for attendance session management
 */
public interface AttendanceSessionUseCase {

    /**
     * Create a new attendance session
     */
    AttendanceSession createAttendanceSession(AttendanceSession session);

    /**
     * Update attendance session
     */
    AttendanceSession updateAttendanceSession(Long sessionId, AttendanceSession session);

    /**
     * Get attendance session by ID
     */
    Optional<AttendanceSession> getAttendanceSessionById(Long id);

    /**
     * Get attendance session by session ID
     */
    Optional<AttendanceSession> getAttendanceSessionBySessionId(Long sessionId);

    /**
     * Get all attendance sessions for a course
     */
    List<AttendanceSession> getAttendanceSessionsByCourseId(Long courseId);

    /**
     * Get upcoming sessions for a course
     */
    List<AttendanceSession> getUpcomingSessionsByCourseId(Long courseId);

    /**
     * Get past sessions for a course
     */
    List<AttendanceSession> getPastSessionsByCourseId(Long courseId);

    /**
     * Get active sessions (currently within check-in window)
     */
    List<AttendanceSession> getActiveSessions();

    /**
     * Delete attendance session
     */
    void deleteAttendanceSession(Long id);

    /**
     * Mark absences for ended sessions
     */
    void markAbsencesForEndedSessions();
}
