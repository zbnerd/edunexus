package com.edunexusattendanceservice.domain.attendance.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AttendanceStatus Enum Tests")
class AttendanceStatusTest {

    @Test
    @DisplayName("Should have PRESENT status with correct description")
    void presentStatus_HasCorrectDescription() {
        assertThat(AttendanceStatus.PRESENT.name()).isEqualTo("PRESENT");
        assertThat(AttendanceStatus.PRESENT.getDescription())
                .isEqualTo("Student attended on time");
    }

    @Test
    @DisplayName("Should have LATE status with correct description")
    void lateStatus_HasCorrectDescription() {
        assertThat(AttendanceStatus.LATE.name()).isEqualTo("LATE");
        assertThat(AttendanceStatus.LATE.getDescription())
                .isEqualTo("Student arrived late");
    }

    @Test
    @DisplayName("Should have ABSENT status with correct description")
    void absentStatus_HasCorrectDescription() {
        assertThat(AttendanceStatus.ABSENT.name()).isEqualTo("ABSENT");
        assertThat(AttendanceStatus.ABSENT.getDescription())
                .isEqualTo("Student was absent");
    }
}
