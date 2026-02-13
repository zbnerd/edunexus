package com.edunexusattendanceservice.port.in;

import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceDto;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceRateResponse;
import com.edunexusattendanceservice.domain.attendance.dto.CheckInRequest;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;

import java.util.List;
import java.util.Optional;

/**
 * Use case interface for attendance operations
 * Defines the contract for attendance management
 */
public interface AttendanceUseCase {

    /**
     * Check in a student to a session
     */
    Attendance checkIn(CheckInRequest request);

    /**
     * Check out a student from a session
     */
    Attendance checkOut(Long attendanceId);

    /**
     * Get attendance by ID
     */
    Optional<Attendance> getAttendanceById(Long attendanceId);

    /**
     * Get all attendance records for a specific user
     */
    List<Attendance> getAttendanceByUserId(Long userId);

    /**
     * Get all attendance records for a user in a specific course
     */
    List<Attendance> getAttendanceByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Get all attendance records for a specific session
     */
    List<Attendance> getAttendanceBySessionId(Long sessionId);

    /**
     * Get attendance record for a user in a session
     */
    Optional<Attendance> getAttendanceByUserIdAndSessionId(Long userId, Long sessionId);

    /**
     * Calculate attendance rate for a user in a course
     */
    AttendanceRateResponse calculateAttendanceRate(Long userId, Long courseId);

    /**
     * Update attendance status
     */
    Attendance updateAttendanceStatus(Long attendanceId, AttendanceStatus status);

    /**
     * Get all attendances (with optional filtering)
     */
    List<Attendance> getAllAttendances();
}
