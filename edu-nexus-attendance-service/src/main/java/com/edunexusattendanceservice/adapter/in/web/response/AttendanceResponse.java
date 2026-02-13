package com.edunexusattendanceservice.adapter.in.web.response;

import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceResponse {
    private Long id;
    private Long userId;
    private Long courseId;
    private Long sessionId;
    private String checkInTime;
    private String checkOutTime;
    private String status;
    private String createdAt;
    private String updatedAt;

    public static AttendanceResponse from(Attendance attendance) {
        return AttendanceResponse.builder()
                .id(attendance.getId())
                .userId(attendance.getUserId())
                .courseId(attendance.getCourseId())
                .sessionId(attendance.getSessionId())
                .checkInTime(attendance.getCheckInTime() != null
                        ? attendance.getCheckInTime().toString() : null)
                .checkOutTime(attendance.getCheckOutTime() != null
                        ? attendance.getCheckOutTime().toString() : null)
                .status(attendance.getStatus() != null
                        ? attendance.getStatus().name() : null)
                .createdAt(attendance.getCreatedAt() != null
                        ? attendance.getCreatedAt().toString() : null)
                .updatedAt(attendance.getUpdatedAt() != null
                        ? attendance.getUpdatedAt().toString() : null)
                .build();
    }
}
