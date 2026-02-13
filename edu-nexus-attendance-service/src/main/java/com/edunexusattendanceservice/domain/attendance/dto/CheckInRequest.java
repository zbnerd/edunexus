package com.edunexusattendanceservice.domain.attendance.dto;

import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Request DTO for checking in to a session
 */
@Getter
@Builder
public class CheckInRequest {
    private Long userId;
    private Long courseId;
    private Long sessionId;
    private AttendanceStatus status;

    /**
     * Check if check-in is late based on session start time
     */
    public boolean isLate(LocalDateTime sessionStartTime) {
        return LocalDateTime.now().isAfter(sessionStartTime.plusMinutes(15));
    }
}
