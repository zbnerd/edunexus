package com.edunexusattendanceservice.domain.attendance.dto;

import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Request DTO for checking in to a session
 */
@Getter
@Builder
public class CheckInRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Session ID is required")
    private Long sessionId;

    @NotNull(message = "Attendance status is required")
    private AttendanceStatus status;

    /**
     * Check if check-in is late based on session start time
     */
    public boolean isLate(LocalDateTime sessionStartTime) {
        return LocalDateTime.now().isAfter(sessionStartTime.plusMinutes(15));
    }
}
