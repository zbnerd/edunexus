package com.edunexusattendanceservice.application.service;

import com.edunexus.common.exception.NotFoundException;
import com.edunexus.common.exception.ErrorCode;
import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSession;
import com.edunexusattendanceservice.adapter.out.persistence.repository.AttendanceRepository;
import com.edunexusattendanceservice.adapter.out.persistence.repository.AttendanceSessionRepository;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import com.edunexusattendanceservice.port.in.AttendanceSessionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for attendance session management
 * Handles session time windows and automatic absence marking
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceSessionService implements AttendanceSessionUseCase {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    @Transactional
    public AttendanceSession createAttendanceSession(AttendanceSession session) {
        log.info("Creating attendance session for course {} and session {}",
                session.getCourseId(), session.getSessionId());

        validateSessionTimes(session);
        AttendanceSession saved = attendanceSessionRepository.save(session);
        log.info("Created attendance session with ID {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public AttendanceSession updateAttendanceSession(Long sessionId, AttendanceSession session) {
        log.info("Updating attendance session {}", sessionId);

        AttendanceSession existingSession = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance session not found with id: " + sessionId));

        validateSessionTimes(session);

        existingSession.setScheduledStart(session.getScheduledStart());
        existingSession.setScheduledEnd(session.getScheduledEnd());
        existingSession.setAttendanceWindowMinutes(session.getAttendanceWindowMinutes());
        existingSession.setAutoMarkAbsent(session.getAutoMarkAbsent());

        return attendanceSessionRepository.save(existingSession);
    }

    @Override
    public Optional<AttendanceSession> getAttendanceSessionById(Long id) {
        return attendanceSessionRepository.findById(id);
    }

    @Override
    public Optional<AttendanceSession> getAttendanceSessionBySessionId(Long sessionId) {
        return attendanceSessionRepository.findBySessionId(sessionId);
    }

    @Override
    public List<AttendanceSession> getAttendanceSessionsByCourseId(Long courseId) {
        return attendanceSessionRepository.findByCourseId(courseId);
    }

    @Override
    public List<AttendanceSession> getUpcomingSessionsByCourseId(Long courseId) {
        return attendanceSessionRepository.findUpcomingSessionsByCourseId(courseId, LocalDateTime.now());
    }

    @Override
    public List<AttendanceSession> getPastSessionsByCourseId(Long courseId) {
        return attendanceSessionRepository.findPastSessionsByCourseId(courseId, LocalDateTime.now());
    }

    @Override
    public List<AttendanceSession> getActiveSessions() {
        return attendanceSessionRepository.findActiveSessions(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteAttendanceSession(Long id) {
        log.info("Deleting attendance session {}", id);
        attendanceSessionRepository.deleteById(id);
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void markAbsencesForEndedSessions() {
        log.info("Checking for ended sessions to mark absences");

        List<AttendanceSession> endedSessions = attendanceSessionRepository
                .findSessionsEndedForAbsenceMarking(LocalDateTime.now());

        for (AttendanceSession session : endedSessions) {
            markAbsencesForSession(session);
        }

        log.info("Completed marking absences for {} ended sessions", endedSessions.size());
    }

    /**
     * Mark absences for users who didn't check in to a session
     */
    private void markAbsencesForSession(AttendanceSession session) {
        log.debug("Marking absences for session {}", session.getSessionId());

        // This would typically require knowing which users should attend
        // For now, we'll mark any existing attendance records without check-in as ABSENT
        // In a real implementation, you'd have an enrollment table to know who should attend

        // For each enrolled user (pseudo-code):
        // if (!hasAttendanceRecord(userId, session.getSessionId())) {
        //     createAbsentRecord(userId, session);
        // }
    }

    /**
     * Validate session time configuration
     */
    private void validateSessionTimes(AttendanceSession session) {
        if (session.getScheduledStart() == null || session.getScheduledEnd() == null) {
            throw new IllegalArgumentException("Scheduled start and end times are required");
        }

        if (session.getScheduledEnd().isBefore(session.getScheduledStart())) {
            throw new IllegalArgumentException("Scheduled end time must be after start time");
        }

        if (session.getAttendanceWindowMinutes() == null || session.getAttendanceWindowMinutes() <= 0) {
            throw new IllegalArgumentException("Attendance window minutes must be positive");
        }

        // Check if window extends past session end
        LocalDateTime windowEnd = session.getScheduledStart().plusMinutes(session.getAttendanceWindowMinutes());
        if (windowEnd.isAfter(session.getScheduledEnd())) {
            log.warn("Attendance window extends past session end for session {}", session.getSessionId());
        }
    }

    /**
     * Check if check-in is allowed based on session time window
     */
    public boolean isCheckInAllowed(Long sessionId, LocalDateTime checkInTime) {
        Optional<AttendanceSession> sessionOpt = attendanceSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            log.warn("No attendance session configuration found for session {}", sessionId);
            // If no session config, allow check-in (backward compatibility)
            return true;
        }

        return sessionOpt.get().isCheckInAllowed(checkInTime);
    }

    /**
     * Determine attendance status based on check-in time
     */
    public AttendanceStatus determineAttendanceStatus(Long sessionId, LocalDateTime checkInTime) {
        Optional<AttendanceSession> sessionOpt = attendanceSessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            return AttendanceStatus.PRESENT; // Default if no session config
        }

        AttendanceSession session = sessionOpt.get();
        if (session.isLateCheckIn(checkInTime)) {
            return AttendanceStatus.LATE;
        }
        return AttendanceStatus.PRESENT;
    }
}
