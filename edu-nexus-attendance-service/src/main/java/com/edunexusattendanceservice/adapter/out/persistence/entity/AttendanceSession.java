package com.edunexusattendanceservice.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity representing attendance session configuration
 * Manages time windows for check-in and automatic absence marking
 */
@Getter
@Setter
@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(name = "attendance_window_minutes", nullable = false)
    private Integer attendanceWindowMinutes;

    @Column(name = "auto_mark_absent", nullable = false)
    private Boolean autoMarkAbsent = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculate the check-in window end time
     */
    public LocalDateTime getCheckInWindowEnd() {
        return scheduledStart.plusMinutes(attendanceWindowMinutes);
    }

    /**
     * Check if current time is within check-in window
     */
    public boolean isWithinCheckInWindow() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(scheduledStart) && !now.isAfter(getCheckInWindowEnd());
    }

    /**
     * Check if check-in is allowed at given time
     */
    public boolean isCheckInAllowed(LocalDateTime checkInTime) {
        return !checkInTime.isBefore(scheduledStart) && !checkInTime.isAfter(getCheckInWindowEnd());
    }

    /**
     * Determine if check-in should be marked as LATE
     */
    public boolean isLateCheckIn(LocalDateTime checkInTime) {
        return checkInTime.isAfter(scheduledStart);
    }

    /**
     * Check if session has ended (for auto-marking absent)
     */
    public boolean hasSessionEnded() {
        return LocalDateTime.now().isAfter(scheduledEnd);
    }

    /**
     * Calculate session duration in minutes
     */
    public long getDurationMinutes() {
        return java.time.Duration.between(scheduledStart, scheduledEnd).toMinutes();
    }
}
