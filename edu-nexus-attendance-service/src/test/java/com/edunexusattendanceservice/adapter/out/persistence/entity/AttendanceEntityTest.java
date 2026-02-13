package com.edunexusattendanceservice.adapter.out.persistence.entity;

import com.edunexusattendanceservice.domain.attendance.dto.AttendanceDto;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Attendance Entity Tests")
class AttendanceEntityTest {

    private Attendance attendance;

    @BeforeEach
    void setUp() {
        attendance = new Attendance();
        attendance.setId(1L);
        attendance.setUserId(1L);
        attendance.setCourseId(1L);
        attendance.setSessionId(1L);
        attendance.setStatus(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Should check in successfully")
    void checkIn_Success() {
        // When
        attendance.checkIn(AttendanceStatus.PRESENT);

        // Then
        assertThat(attendance.getCheckInTime()).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Should check in with LATE status")
    void checkIn_WithLateStatus_Success() {
        // When
        attendance.checkIn(AttendanceStatus.LATE);

        // Then
        assertThat(attendance.getCheckInTime()).isNotNull();
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("Should check out successfully")
    void checkOut_Success() {
        // Given
        attendance.checkIn(AttendanceStatus.PRESENT);

        // When
        attendance.checkOut();

        // Then
        assertThat(attendance.getCheckOutTime()).isNotNull();
        assertThat(attendance.getCheckOutTime()).isAfterOrEqualTo(attendance.getCheckInTime());
    }

    @Test
    @DisplayName("Should throw exception when checking out without checking in")
    void checkOut_WithoutCheckIn_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> attendance.checkOut())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot check out without checking in");
    }

    @Test
    @DisplayName("Should update status")
    void updateStatus_Success() {
        // When
        attendance.updateStatus(AttendanceStatus.ABSENT);

        // Then
        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("Should convert entity to DTO")
    void toDto_Success() {
        // Given
        attendance.checkIn(AttendanceStatus.PRESENT);
        attendance.checkOut();

        // When
        AttendanceDto dto = attendance.toDto();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getCourseId()).isEqualTo(1L);
        assertThat(dto.getSessionId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(dto.getCheckInTime()).isNotNull();
        assertThat(dto.getCheckOutTime()).isNotNull();
    }

    @Test
    @DisplayName("Should create entity from DTO")
    void fromDto_Success() {
        // Given
        AttendanceDto dto = AttendanceDto.builder()
                .userId(2L)
                .courseId(3L)
                .sessionId(4L)
                .checkInTime(LocalDateTime.now())
                .status(AttendanceStatus.LATE)
                .build();

        // When
        Attendance result = Attendance.fromDto(dto);

        // Then
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getCourseId()).isEqualTo(3L);
        assertThat(result.getSessionId()).isEqualTo(4L);
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
    }

    @Test
    @DisplayName("Should default to PRESENT status when DTO status is null")
    void fromDto_WithNullStatus_DefaultsToPresent() {
        // Given
        AttendanceDto dto = AttendanceDto.builder()
                .userId(1L)
                .courseId(1L)
                .sessionId(1L)
                .status(null)
                .build();

        // When
        Attendance result = Attendance.fromDto(dto);

        // Then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("Should set timestamps on pre-persist")
    void prePersist_SetsTimestamps() {
        // When
        attendance.prePersist();

        // Then
        assertThat(attendance.getCreatedAt()).isNotNull();
        assertThat(attendance.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update timestamp on pre-update")
    void preUpdate_UpdatesTimestamp() {
        // Given
        attendance.prePersist();
        LocalDateTime originalCreatedAt = attendance.getCreatedAt();

        // When
        attendance.preUpdate();

        // Then
        assertThat(attendance.getUpdatedAt()).isNotNull();
        assertThat(attendance.getCreatedAt()).isEqualTo(originalCreatedAt);
    }
}
