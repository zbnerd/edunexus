package com.edunexusattendanceservice.domain.attendance.dto;

import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO for attendance data transfer
 */
@Getter
@Builder
public class AttendanceDto {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long sessionId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
