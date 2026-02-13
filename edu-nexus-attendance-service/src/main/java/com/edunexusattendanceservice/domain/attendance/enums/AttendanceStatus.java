package com.edunexusattendanceservice.domain.attendance.enums;

import lombok.Getter;

/**
 * Attendance status enumeration
 */
@Getter
public enum AttendanceStatus {
    PRESENT("Student attended on time"),
    LATE("Student arrived late"),
    ABSENT("Student was absent");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }
}
