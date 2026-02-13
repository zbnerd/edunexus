package com.edunexusattendanceservice.adapter.in.web;

import com.edunexus.common.exception.NotFoundException;
import com.edunexus.common.exception.ErrorCode;
import com.edunexusattendanceservice.adapter.in.web.AttendanceController.AttendanceResponse;
import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSession;
import com.edunexusattendanceservice.application.service.AttendanceService;
import com.edunexusattendanceservice.application.service.AttendanceSessionService;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceRateResponse;
import com.edunexusattendanceservice.domain.attendance.dto.CheckInRequest;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin REST controller for attendance management
 * Provides endpoints for administrators and instructors
 */
@Slf4j
@RestController
@RequestMapping("/attendances/admin")
@RequiredArgsConstructor
@Timed
public class AttendanceAdminController {

    private final AttendanceService attendanceService;
    private final AttendanceSessionService attendanceSessionService;

    /**
     * Create attendance session configuration
     * POST /attendances/admin/sessions
     */
    @PostMapping("/sessions")
    @Counted(value = "attendance.session.creation", description = "Attendance session creation attempts")
    public ResponseEntity<AttendanceSessionResponse> createSession(@Valid @RequestBody AttendanceSessionRequest request) {
        log.info("Creating attendance session for course {} and session {}",
                request.getCourseId(), request.getSessionId());

        AttendanceSession session = new AttendanceSession();
        session.setCourseId(request.getCourseId());
        session.setSessionId(request.getSessionId());
        session.setScheduledStart(parseDateTime(request.getScheduledStart()));
        session.setScheduledEnd(parseDateTime(request.getScheduledEnd()));
        session.setAttendanceWindowMinutes(request.getAttendanceWindowMinutes());
        session.setAutoMarkAbsent(request.getAutoMarkAbsent() != null ? request.getAutoMarkAbsent() : true);

        AttendanceSession saved = attendanceSessionService.createAttendanceSession(session);
        return ResponseEntity.created(URI.create("/attendances/admin/sessions/" + saved.getId()))
                .body(AttendanceSessionResponse.from(saved));
    }

    /**
     * Update attendance session configuration
     * PUT /attendances/admin/sessions/{id}
     */
    @PutMapping("/sessions/{id}")
    public ResponseEntity<AttendanceSessionResponse> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceSessionRequest request) {
        log.info("Updating attendance session {}", id);

        AttendanceSession session = new AttendanceSession();
        session.setCourseId(request.getCourseId());
        session.setSessionId(request.getSessionId());
        session.setScheduledStart(parseDateTime(request.getScheduledStart()));
        session.setScheduledEnd(parseDateTime(request.getScheduledEnd()));
        session.setAttendanceWindowMinutes(request.getAttendanceWindowMinutes());
        session.setAutoMarkAbsent(request.getAutoMarkAbsent());

        AttendanceSession updated = attendanceSessionService.updateAttendanceSession(id, session);
        return ResponseEntity.ok(AttendanceSessionResponse.from(updated));
    }

    /**
     * Get attendance session by ID
     * GET /attendances/admin/sessions/{id}
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<AttendanceSessionResponse> getSession(@PathVariable Long id) {
        AttendanceSession session = attendanceSessionService.getAttendanceSessionById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Attendance session not found with id: " + id));
        return ResponseEntity.ok(AttendanceSessionResponse.from(session));
    }

    /**
     * Get all sessions for a course
     * GET /attendances/admin/sessions/course/{courseId}
     */
    @GetMapping("/sessions/course/{courseId}")
    public ResponseEntity<List<AttendanceSessionResponse>> getSessionsByCourse(@PathVariable Long courseId) {
        List<AttendanceSession> sessions = attendanceSessionService.getAttendanceSessionsByCourseId(courseId);
        List<AttendanceSessionResponse> responses = sessions.stream()
                .map(AttendanceSessionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get session attendance report
     * GET /attendances/admin/reports/session/{sessionId}
     */
    @GetMapping("/reports/session/{sessionId}")
    @Timed(value = "attendance.report.session", extraTags = {"operation", "sessionReport"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<SessionReportResponse> getSessionReport(@PathVariable Long sessionId) {
        log.info("Generating attendance report for session {}", sessionId);

        List<Attendance> attendances = attendanceService.getAttendanceBySessionId(sessionId);

        long presentCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long lateCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long absentCount = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long totalCount = attendances.size();

        SessionReportResponse report = SessionReportResponse.builder()
                .sessionId(sessionId)
                .totalStudents(totalCount)
                .presentCount(presentCount)
                .lateCount(lateCount)
                .absentCount(absentCount)
                .attendanceRate(totalCount > 0 ? (double) (presentCount + lateCount) / totalCount * 100 : 0.0)
                .attendances(attendances.stream()
                        .map(AttendanceResponse::from)
                        .collect(Collectors.toList()))
                .build();

        return ResponseEntity.ok(report);
    }

    /**
     * Get course attendance report
     * GET /attendances/admin/reports/course/{courseId}
     */
    @GetMapping("/reports/course/{courseId}")
    @Timed(value = "attendance.report.course", extraTags = {"operation", "courseReport"}, percentiles = {0.5, 0.95, 0.99})
    public ResponseEntity<CourseReportResponse> getCourseReport(@PathVariable Long courseId) {
        log.info("Generating attendance report for course {}", courseId);

        // Get all sessions for this course
        List<AttendanceSession> sessions = attendanceSessionService.getAttendanceSessionsByCourseId(courseId);

        int totalSessions = sessions.size();
        long totalAttendanceRecords = 0;
        long totalPresent = 0;
        long totalLate = 0;
        long totalAbsent = 0;

        for (AttendanceSession session : sessions) {
            List<Attendance> attendances = attendanceService.getAttendanceBySessionId(session.getSessionId());
            totalAttendanceRecords += attendances.size();
            totalPresent += attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            totalLate += attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
            totalAbsent += attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        }

        double overallAttendanceRate = totalAttendanceRecords > 0
                ? (double) (totalPresent + totalLate) / totalAttendanceRecords * 100
                : 0.0;

        CourseReportResponse report = CourseReportResponse.builder()
                .courseId(courseId)
                .totalSessions(totalSessions)
                .totalAttendanceRecords(totalAttendanceRecords)
                .totalPresent(totalPresent)
                .totalLate(totalLate)
                .totalAbsent(totalAbsent)
                .overallAttendanceRate(Math.round(overallAttendanceRate * 100.0) / 100.0)
                .build();

        return ResponseEntity.ok(report);
    }

    /**
     * Mark attendance manually (admin override)
     * POST /attendances/admin/mark
     */
    @PostMapping("/mark")
    @Counted(value = "attendance.mark.manual", description = "Manual attendance marking attempts")
    public ResponseEntity<AttendanceResponse> markAttendance(@Valid @RequestBody MarkAttendanceRequest request) {
        log.info("Manual attendance marking for user {} in session {} with status {}",
                request.getUserId(), request.getSessionId(), request.getStatus());

        CheckInRequest checkInRequest = CheckInRequest.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .sessionId(request.getSessionId())
                .status(request.getStatus())
                .build();

        Attendance attendance = attendanceService.checkIn(checkInRequest);
        return ResponseEntity.ok(AttendanceResponse.from(attendance));
    }

    /**
     * Parse datetime string to LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr);
    }

    /**
     * Get users at risk (low attendance)
     * GET /attendances/admin/reports/at-risk?courseId={courseId}&threshold={threshold}
     */
    @GetMapping("/reports/at-risk")
    public ResponseEntity<List<AtRiskStudentResponse>> getAtRiskStudents(
            @RequestParam Long courseId,
            @RequestParam(defaultValue = "75.0") double threshold) {
        log.info("Finding students at risk in course {} with threshold {}%", courseId, threshold);

        // This would typically use AttendanceSummaryRepository
        // For now, we'll calculate on the fly
        List<AttendanceSession> sessions = attendanceSessionService.getAttendanceSessionsByCourseId(courseId);

        // Implementation would query attendance summaries and filter by threshold
        // This is a placeholder for the actual implementation

        return ResponseEntity.ok(List.of());
    }

    /**
     * Trigger absence marking for ended sessions
     * POST /attendances/admin/sessions/mark-absences
     */
    @PostMapping("/sessions/mark-absences")
    @Counted(value = "attendance.absence.marking", description = "Manual absence marking triggers")
    public ResponseEntity<Map<String, String>> markAbsences() {
        log.info("Manually triggering absence marking for ended sessions");
        attendanceSessionService.markAbsencesForEndedSessions();
        return ResponseEntity.ok(Map.of("message", "Absence marking completed"));
    }

    // Response DTOs

    @lombok.Getter
    @lombok.Builder
    public static class AttendanceSessionResponse {
        private Long id;
        private Long courseId;
        private Long sessionId;
        private String scheduledStart;
        private String scheduledEnd;
        private Integer attendanceWindowMinutes;
        private Boolean autoMarkAbsent;
        private String createdAt;
        private String updatedAt;

        public static AttendanceSessionResponse from(AttendanceSession session) {
            return AttendanceSessionResponse.builder()
                    .id(session.getId())
                    .courseId(session.getCourseId())
                    .sessionId(session.getSessionId())
                    .scheduledStart(session.getScheduledStart().toString())
                    .scheduledEnd(session.getScheduledEnd().toString())
                    .attendanceWindowMinutes(session.getAttendanceWindowMinutes())
                    .autoMarkAbsent(session.getAutoMarkAbsent())
                    .createdAt(session.getCreatedAt() != null ? session.getCreatedAt().toString() : null)
                    .updatedAt(session.getUpdatedAt() != null ? session.getUpdatedAt().toString() : null)
                    .build();
        }
    }

    @lombok.Getter
    @lombok.Builder
    public static class SessionReportResponse {
        private Long sessionId;
        private long totalStudents;
        private long presentCount;
        private long lateCount;
        private long absentCount;
        private double attendanceRate;
        private List<AttendanceResponse> attendances;
    }

    @lombok.Getter
    @lombok.Builder
    public static class CourseReportResponse {
        private Long courseId;
        private int totalSessions;
        private long totalAttendanceRecords;
        private long totalPresent;
        private long totalLate;
        private long totalAbsent;
        private double overallAttendanceRate;
    }

    @lombok.Getter
    @lombok.Builder
    public static class AtRiskStudentResponse {
        private Long userId;
        private Long courseId;
        private double attendanceRate;
        private int attendedSessions;
        private int totalSessions;
    }

    // Request DTOs

    @lombok.Getter
    @lombok.Setter
    public static class AttendanceSessionRequest {
        private Long courseId;
        private Long sessionId;
        private String scheduledStart;
        private String scheduledEnd;
        private Integer attendanceWindowMinutes;
        private Boolean autoMarkAbsent;
    }

    @lombok.Getter
    @lombok.Setter
    public static class MarkAttendanceRequest {
        private Long userId;
        private Long courseId;
        private Long sessionId;
        private AttendanceStatus status;
    }
}
