package com.edunexusattendanceservice.adapter.in.web.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SessionReportResponse {
    private Long sessionId;
    private long totalStudents;
    private long presentCount;
    private long lateCount;
    private long absentCount;
    private double attendanceRate;
    private List<AttendanceResponse> attendances;
}
