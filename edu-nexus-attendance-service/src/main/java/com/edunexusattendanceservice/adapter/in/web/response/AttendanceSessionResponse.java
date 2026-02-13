package com.edunexusattendanceservice.adapter.in.web.response;

import com.edunexusattendanceservice.adapter.out.persistence.entity.AttendanceSession;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttendanceSessionResponse {
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
