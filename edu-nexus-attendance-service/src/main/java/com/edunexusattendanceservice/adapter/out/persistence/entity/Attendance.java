package com.edunexusattendanceservice.adapter.out.persistence.entity;

import com.edunexusattendanceservice.domain.attendance.dto.AttendanceDto;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA Entity representing student attendance records
 */
@Getter
@Setter
@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

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
     * Mark student as checked in
     */
    public void checkIn(AttendanceStatus attendanceStatus) {
        this.checkInTime = LocalDateTime.now();
        this.status = attendanceStatus;
    }

    /**
     * Mark student as checked out
     */
    public void checkOut() {
        if (this.checkInTime == null) {
            throw new IllegalStateException("Cannot check out without checking in");
        }
        this.checkOutTime = LocalDateTime.now();
    }

    /**
     * Update attendance status
     */
    public void updateStatus(AttendanceStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * Convert entity to DTO
     */
    public AttendanceDto toDto() {
        return AttendanceDto.builder()
                .id(this.id)
                .userId(this.userId)
                .courseId(this.courseId)
                .sessionId(this.sessionId)
                .checkInTime(this.checkInTime)
                .checkOutTime(this.checkOutTime)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

    /**
     * Create new attendance from DTO
     */
    public static Attendance fromDto(AttendanceDto dto) {
        Attendance attendance = new Attendance();
        attendance.setUserId(dto.getUserId());
        attendance.setCourseId(dto.getCourseId());
        attendance.setSessionId(dto.getSessionId());
        attendance.setCheckInTime(dto.getCheckInTime());
        attendance.setCheckOutTime(dto.getCheckOutTime());
        attendance.setStatus(dto.getStatus() != null ? dto.getStatus() : AttendanceStatus.PRESENT);
        return attendance;
    }
}
