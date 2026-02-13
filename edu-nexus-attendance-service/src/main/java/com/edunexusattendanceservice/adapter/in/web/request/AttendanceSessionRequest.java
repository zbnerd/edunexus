package com.edunexusattendanceservice.adapter.in.web.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceSessionRequest {
    private Long courseId;
    private Long sessionId;
    private String scheduledStart;
    private String scheduledEnd;
    private Integer attendanceWindowMinutes;
    private Boolean autoMarkAbsent;
}
