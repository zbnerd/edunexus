package com.edunexusattendanceservice.application.service;

import com.edunexus.common.exception.NotFoundException;
import com.edunexus.common.exception.ErrorCode;
import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.adapter.out.persistence.repository.AttendanceRepository;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceRateResponse;
import com.edunexusattendanceservice.domain.attendance.dto.CheckInRequest;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import com.edunexusattendanceservice.port.in.AttendanceUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation for attendance management
 * Handles business logic for student attendance tracking
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService implements AttendanceUseCase {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionService attendanceSessionService;

    @Override
    @Transactional
    public Attendance checkIn(CheckInRequest request) {
        log.info("Checking in user {} for session {} in course {}",
                request.getUserId(), request.getSessionId(), request.getCourseId());

        LocalDateTime checkInTime = LocalDateTime.now();

        // Validate check-in time window
        if (!attendanceSessionService.isCheckInAllowed(request.getSessionId(), checkInTime)) {
            log.warn("Check-in not allowed for user {} in session {} - outside time window",
                    request.getUserId(), request.getSessionId());
            throw new IllegalArgumentException(
                    "Check-in is not allowed at this time. Please check the session schedule.");
        }

        // Check if already checked in
        Optional<Attendance> existingAttendance = attendanceRepository
                .findByUserIdAndSessionId(request.getUserId(), request.getSessionId());

        if (existingAttendance.isPresent()) {
            Attendance attendance = existingAttendance.get();
            if (attendance.getCheckOutTime() == null) {
                log.warn("User {} already checked in to session {}",
                        request.getUserId(), request.getSessionId());
                return attendance;
            }
            // Previously checked out, create new record
        }

        Attendance attendance = new Attendance();
        attendance.setUserId(request.getUserId());
        attendance.setCourseId(request.getCourseId());
        attendance.setSessionId(request.getSessionId());

        // Determine status - use provided status or determine based on check-in time
        AttendanceStatus status = request.getStatus() != null
                ? request.getStatus()
                : attendanceSessionService.determineAttendanceStatus(request.getSessionId(), checkInTime);

        attendance.checkIn(status);

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Successfully checked in user {} for session {} with status {}",
                request.getUserId(), request.getSessionId(), status);
        return saved;
    }

    @Override
    @Transactional
    public Attendance checkOut(Long attendanceId) {
        log.info("Checking out attendance record {}", attendanceId);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance not found with id: " + attendanceId));

        if (attendance.getCheckOutTime() != null) {
            log.warn("Attendance record {} already checked out at {}",
                    attendanceId, attendance.getCheckOutTime());
            return attendance;
        }

        attendance.checkOut();
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Successfully checked out attendance record {}", attendanceId);
        return saved;
    }

    @Override
    public Optional<Attendance> getAttendanceById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId);
    }

    @Override
    public List<Attendance> getAttendanceByUserId(Long userId) {
        log.debug("Fetching all attendance records for user {}", userId);
        return attendanceRepository.findByUserId(userId);
    }

    @Override
    public List<Attendance> getAttendanceByUserIdAndCourseId(Long userId, Long courseId) {
        log.debug("Fetching attendance records for user {} in course {}", userId, courseId);
        return attendanceRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public List<Attendance> getAttendanceBySessionId(Long sessionId) {
        log.debug("Fetching all attendance records for session {}", sessionId);
        return attendanceRepository.findBySessionId(sessionId);
    }

    @Override
    public Optional<Attendance> getAttendanceByUserIdAndSessionId(Long userId, Long sessionId) {
        return attendanceRepository.findByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public AttendanceRateResponse calculateAttendanceRate(Long userId, Long courseId) {
        log.debug("Calculating attendance rate for user {} in course {}", userId, courseId);

        long totalSessions = attendanceRepository.countTotalSessionsForUserInCourse(userId, courseId);
        long attendedSessions = attendanceRepository.countAttendedSessionsForUserInCourse(userId, courseId);

        double attendanceRate = totalSessions > 0
                ? (double) attendedSessions / totalSessions * 100
                : 0.0;

        DecimalFormat df = new DecimalFormat("0.00");
        String percentage = df.format(attendanceRate) + "%";

        return AttendanceRateResponse.builder()
                .userId(userId)
                .courseId(courseId)
                .totalSessions(totalSessions)
                .attendedSessions(attendedSessions)
                .attendanceRate(Math.round(attendanceRate * 100.0) / 100.0)
                .percentage(percentage)
                .build();
    }

    @Override
    @Transactional
    public Attendance updateAttendanceStatus(Long attendanceId, AttendanceStatus status) {
        log.info("Updating status for attendance {} to {}", attendanceId, status);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance not found with id: " + attendanceId));

        attendance.updateStatus(status);
        return attendanceRepository.save(attendance);
    }

    @Override
    public List<Attendance> getAllAttendances() {
        return attendanceRepository.findAll();
    }
}
