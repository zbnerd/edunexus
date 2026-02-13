package com.edunexusobservability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;

/**
 * Business metrics for attendance service
 * Tracks attendance-specific metrics beyond standard HTTP metrics
 */
@Component
@RequiredArgsConstructor
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    /**
     * Record attendance event
     */
    public void recordAttendance(Long courseId, AttendanceStatus status) {
        Counter.builder("attendance.recorded")
                .tag("course_id", String.valueOf(courseId))
                .tag("status", status.name())
                .description("Number of attendance records created")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record check-in within time window
     */
    public void recordTimelyCheckIn(Long courseId) {
        Counter.builder("attendance.checkin.timely")
                .tag("course_id", String.valueOf(courseId))
                .description("Number of check-ins within time window")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record late check-in
     */
    public void recordLateCheckIn(Long courseId) {
        Counter.builder("attendance.checkin.late")
                .tag("course_id", String.valueOf(courseId))
                .description("Number of late check-ins")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record absence marking
     */
    public void recordAbsenceMarked(Long courseId) {
        Counter.builder("attendance.absence.marked")
                .tag("course_id", String.valueOf(courseId))
                .description("Number of automatic absence markings")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Start timer for session report generation
     */
    public Timer.Sample startReportTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Record session report generation time
     */
    public void recordReportGeneration(Long sessionId, Timer.Sample sample) {
        sample.stop(Timer.builder("attendance.report.generation")
                .tag("session_id", String.valueOf(sessionId))
                .description("Time taken to generate session attendance report")
                .register(meterRegistry));
    }
}
