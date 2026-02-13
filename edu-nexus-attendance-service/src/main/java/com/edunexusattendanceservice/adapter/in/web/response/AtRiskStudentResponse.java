package com.edunexusattendanceservice.adapter.in.web.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AtRiskStudentResponse {
    private Long userId;
    private Long courseId;
    private double attendanceRate;
    private int attendedSessions;
    private int totalSessions;
}
