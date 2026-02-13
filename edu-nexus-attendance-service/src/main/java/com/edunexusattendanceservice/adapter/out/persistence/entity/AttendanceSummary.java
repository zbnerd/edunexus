package com.edunexusattendanceservice.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity representing pre-calculated attendance summary
 * Provides quick access to attendance statistics without complex queries
 */
@Getter
@Setter
@Entity
@Table(name = "attendance_summaries")
public class AttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions;

    @Column(name = "attended_sessions", nullable = false)
    private Integer attendedSessions;

    @Column(name = "late_sessions", nullable = false)
    private Integer lateSessions;

    @Column(name = "absent_sessions", nullable = false)
    private Integer absentSessions;

    @Column(name = "attendance_rate", nullable = false)
    private Double attendanceRate;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdatedAt = LocalDateTime.now();
    }

    /**
     * Calculate and update attendance rate
     */
    public void recalculateAttendanceRate() {
        if (totalSessions > 0) {
            this.attendanceRate = ((double) (attendedSessions + lateSessions) / totalSessions) * 100;
        } else {
            this.attendanceRate = 0.0;
        }
    }

    /**
     * Increment attended sessions count
     */
    public void incrementAttendedSessions() {
        this.attendedSessions++;
        recalculateAttendanceRate();
    }

    /**
     * Increment late sessions count
     */
    public void incrementLateSessions() {
        this.lateSessions++;
        recalculateAttendanceRate();
    }

    /**
     * Increment absent sessions count
     */
    public void incrementAbsentSessions() {
        this.absentSessions++;
        recalculateAttendanceRate();
    }

    /**
     * Update summary from raw counts
     */
    public void updateFromCounts(int total, int attended, int late, int absent) {
        this.totalSessions = total;
        this.attendedSessions = attended;
        this.lateSessions = late;
        this.absentSessions = absent;
        recalculateAttendanceRate();
    }

    /**
     * Get percentage as formatted string
     */
    public String getAttendancePercentage() {
        return String.format("%.2f%%", attendanceRate);
    }

    /**
     * Check if student meets minimum attendance requirement
     */
    public boolean meetsMinimumRequirement(double minimumPercentage) {
        return attendanceRate >= minimumPercentage;
    }
}
