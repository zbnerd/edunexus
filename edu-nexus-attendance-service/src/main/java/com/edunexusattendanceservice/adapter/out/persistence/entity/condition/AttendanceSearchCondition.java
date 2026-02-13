package com.edunexusattendanceservice.adapter.out.persistence.entity.condition;

import lombok.Builder;
import lombok.Getter;

/**
 * Search condition for attendance queries
 */
@Getter
@Builder
public class AttendanceSearchCondition {
    private Long userId;
    private Long courseId;
    private Long sessionId;
}
