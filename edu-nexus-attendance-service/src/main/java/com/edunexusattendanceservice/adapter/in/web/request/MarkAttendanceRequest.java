package com.edunexusattendanceservice.adapter.in.web.request;

import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkAttendanceRequest {
    private Long userId;
    private Long courseId;
    private Long sessionId;
    private AttendanceStatus status;
}
