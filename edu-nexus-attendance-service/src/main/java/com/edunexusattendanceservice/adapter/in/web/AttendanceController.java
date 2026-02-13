package com.edunexusattendanceservice.adapter.in.web;

import com.edunexus.common.exception.NotFoundException;
import com.edunexus.common.exception.ErrorCode;
import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.application.service.AttendanceService;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceDto;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceRateResponse;
import com.edunexusattendanceservice.domain.attendance.dto.CheckInRequest;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import com.edunexusattendanceservice.adapter.in.web.response.AttendanceResponse;
import com.edunexusobservability.annotation.MetricTimed;
import com.edunexusobservability.metrics.BusinessMetrics;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for attendance management
 * Provides endpoints for check-in, check-out, and attendance queries
 */
@Slf4j
@RestController
@RequestMapping("/attendances")
@RequiredArgsConstructor
@Timed
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final BusinessMetrics businessMetrics;

    /**
     * Check in a student to a session
     * POST /attendances/checkin
     */
    @PostMapping("/checkin")
    @Counted(value = "attendance.checkin", description = "Student check-in attempts")
    @MetricTimed
    public ResponseEntity<AttendanceResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
        log.info("Check-in request for user {} in session {}", request.getUserId(), request.getSessionId());
        Attendance attendance = attendanceService.checkIn(request);

        // Record business metric for successful check-in
        businessMetrics.recordAttendance(attendance.getCourseId(), attendance.getStatus());

        return ResponseEntity.created(URI.create("/attendances/" + attendance.getId()))
                .body(AttendanceResponse.from(attendance));
    }

    /**
     * Check out a student from a session
     * POST /attendances/checkout/{id}
     */
    @PostMapping("/checkout/{id}")
    @Counted(value = "attendance.checkout", description = "Student check-out attempts")
    @Timed(value = "attendance.checkout", percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<AttendanceResponse> checkOut(@PathVariable Long id) {
        log.info("Check-out request for attendance record {}", id);
        Attendance attendance = attendanceService.checkOut(id);
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }

    /**
     * Get attendance history for a specific user
     * GET /attendances/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @Timed(value = "attendance.retrieval", extraTags = {"operation", "getByUser"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByUserId(@PathVariable Long userId) {
        log.info("Fetching attendance history for user {}", userId);
        List<Attendance> attendances = attendanceService.getAttendanceByUserId(userId);
        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get attendance history for a specific user in a course
     * GET /attendances/user/{userId}/course/{courseId}
     */
    @GetMapping("/user/{userId}/course/{courseId}")
    @Timed(value = "attendance.retrieval", extraTags = {"operation", "getByUserAndCourse"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByUserIdAndCourseId(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        log.info("Fetching attendance history for user {} in course {}", userId, courseId);
        List<Attendance> attendances = attendanceService.getAttendanceByUserIdAndCourseId(userId, courseId);
        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all attendance records for a specific session
     * GET /attendances/session/{sessionId}
     */
    @GetMapping("/session/{sessionId}")
    @Timed(value = "attendance.retrieval", extraTags = {"operation", "getBySession"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<List<AttendanceResponse>> getAttendanceBySessionId(@PathVariable Long sessionId) {
        log.info("Fetching attendance records for session {}", sessionId);
        List<Attendance> attendances = attendanceService.getAttendanceBySessionId(sessionId);
        List<AttendanceResponse> responses = attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get attendance rate for a user in a course
     * GET /attendances/rate/{userId}/{courseId}
     */
    @GetMapping("/rate/{userId}/{courseId}")
    @Timed(value = "attendance.rate.calculation", percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<AttendanceRateResponse> getAttendanceRate(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        log.info("Calculating attendance rate for user {} in course {}", userId, courseId);
        AttendanceRateResponse rate = attendanceService.calculateAttendanceRate(userId, courseId);
        return ResponseEntity.ok(rate);
    }

    /**
     * Get specific attendance record by ID
     * GET /attendances/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        log.info("Fetching attendance record {}", id);
        Attendance attendance = attendanceService.getAttendanceById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance not found with id: " + id));
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }

    /**
     * Update attendance status
     * PUT /attendances/{id}/status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<AttendanceResponse> updateAttendanceStatus(
            @PathVariable Long id,
            @RequestParam AttendanceStatus status) {
        log.info("Updating status for attendance {} to {}", id, status);
        Attendance attendance = attendanceService.updateAttendanceStatus(id, status);
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }

    /**
     * Get attendance for a specific user and session
     * GET /attendances/user/{userId}/session/{sessionId}
     */
    @GetMapping("/user/{userId}/session/{sessionId}")
    public ResponseEntity<AttendanceResponse> getAttendanceByUserAndSession(
            @PathVariable Long userId,
            @PathVariable Long sessionId) {
        log.info("Fetching attendance for user {} in session {}", userId, sessionId);
        Attendance attendance = attendanceService.getAttendanceByUserIdAndSessionId(userId, sessionId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance not found for user " + userId + " in session " + sessionId));
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }
}
