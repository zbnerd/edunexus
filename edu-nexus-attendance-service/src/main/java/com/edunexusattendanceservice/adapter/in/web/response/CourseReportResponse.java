package com.edunexusattendanceservice.adapter.in.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseReportResponse {
    private Long courseId;
    private int totalSessions;
    private long totalAttendanceRecords;
    private long totalPresent;
    private long totalLate;
    private long totalAbsent;
    private double overallAttendanceRate;
}
