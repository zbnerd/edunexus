package com.edunexusattendanceservice.domain.attendance.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO for attendance rate calculation
 */
@Getter
@Builder
public class AttendanceRateResponse {
    private Long userId;
    private Long courseId;
    private Long totalSessions;
    private Long attendedSessions;
    private Double attendanceRate;
    private String percentage;
}
