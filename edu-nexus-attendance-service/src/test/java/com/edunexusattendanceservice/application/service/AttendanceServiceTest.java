package com.edunexusattendanceservice.application.service;

import com.edunexus.common.exception.NotFoundException;
import com.edunexusattendanceservice.adapter.out.persistence.entity.Attendance;
import com.edunexusattendanceservice.adapter.out.persistence.repository.AttendanceRepository;
import com.edunexusattendanceservice.domain.attendance.dto.AttendanceRateResponse;
import com.edunexusattendanceservice.domain.attendance.dto.CheckInRequest;
import com.edunexusattendanceservice.domain.attendance.enums.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Attendance Service Tests")
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    private Attendance testAttendance;
    private CheckInRequest checkInRequest;

    @BeforeEach
    void setUp() {
        testAttendance = new Attendance();
        testAttendance.setId(1L);
        testAttendance.setUserId(1L);
        testAttendance.setCourseId(1L);
        testAttendance.setSessionId(1L);
        testAttendance.setStatus(AttendanceStatus.PRESENT);
        testAttendance.setCheckInTime(LocalDateTime.now());
        testAttendance.setCreatedAt(LocalDateTime.now());
        testAttendance.setUpdatedAt(LocalDateTime.now());

        checkInRequest = CheckInRequest.builder()
                .userId(1L)
                .courseId(1L)
                .sessionId(1L)
                .status(AttendanceStatus.PRESENT)
                .build();
    }

    @Test
    @DisplayName("Should successfully check in a student")
    void checkIn_Success() {
        // Given
        when(attendanceRepository.findByUserIdAndSessionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // When
        Attendance result = attendanceService.checkIn(checkInRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getSessionId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(result.getCheckInTime()).isNotNull();

        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should return existing attendance if already checked in")
    void checkIn_AlreadyCheckedIn_ReturnsExisting() {
        // Given
        when(attendanceRepository.findByUserIdAndSessionId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testAttendance));

        // When
        Attendance result = attendanceService.checkIn(checkInRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should successfully check out a student")
    void checkOut_Success() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // When
        Attendance result = attendanceService.checkOut(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCheckOutTime()).isNotNull();
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    @DisplayName("Should throw exception when checking out non-existent attendance")
    void checkOut_NotFound_ThrowsException() {
        // Given
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> attendanceService.checkOut(999L))
                .isInstanceOf(NotFoundException.class);

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should return attendance by ID")
    void getAttendanceById_Found() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));

        // When
        Optional<Attendance> result = attendanceService.getAttendanceById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty when attendance not found by ID")
    void getAttendanceById_NotFound() {
        // Given
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Attendance> result = attendanceService.getAttendanceById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all attendances for a user")
    void getAttendanceByUserId_Success() {
        // Given
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setUserId(1L);
        attendance2.setCourseId(1L);
        attendance2.setSessionId(2L);

        when(attendanceRepository.findByUserId(1L))
                .thenReturn(Arrays.asList(testAttendance, attendance2));

        // When
        List<Attendance> result = attendanceService.getAttendanceByUserId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getUserId().equals(1L));
    }

    @Test
    @DisplayName("Should return all attendances for a user in a course")
    void getAttendanceByUserIdAndCourseId_Success() {
        // Given
        when(attendanceRepository.findByUserIdAndCourseId(1L, 1L))
                .thenReturn(Arrays.asList(testAttendance));

        // When
        List<Attendance> result = attendanceService.getAttendanceByUserIdAndCourseId(1L, 1L);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getCourseId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return all attendances for a session")
    void getAttendanceBySessionId_Success() {
        // Given
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setUserId(2L);
        attendance2.setCourseId(1L);
        attendance2.setSessionId(1L);

        when(attendanceRepository.findBySessionId(1L))
                .thenReturn(Arrays.asList(testAttendance, attendance2));

        // When
        List<Attendance> result = attendanceService.getAttendanceBySessionId(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(a -> a.getSessionId().equals(1L));
    }

    @Test
    @DisplayName("Should calculate attendance rate correctly")
    void calculateAttendanceRate_Success() {
        // Given
        long totalSessions = 10L;
        long attendedSessions = 8L;

        when(attendanceRepository.countTotalSessionsForUserInCourse(1L, 1L))
                .thenReturn(totalSessions);
        when(attendanceRepository.countAttendedSessionsForUserInCourse(1L, 1L))
                .thenReturn(attendedSessions);

        // When
        AttendanceRateResponse result = attendanceService.calculateAttendanceRate(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getCourseId()).isEqualTo(1L);
        assertThat(result.getTotalSessions()).isEqualTo(totalSessions);
        assertThat(result.getAttendedSessions()).isEqualTo(attendedSessions);
        assertThat(result.getAttendanceRate()).isEqualTo(80.0);
        assertThat(result.getPercentage()).isEqualTo("80.00%");
    }

    @Test
    @DisplayName("Should return zero attendance rate when no sessions exist")
    void calculateAttendanceRate_NoSessions_ReturnsZero() {
        // Given
        when(attendanceRepository.countTotalSessionsForUserInCourse(1L, 1L)).thenReturn(0L);
        when(attendanceRepository.countAttendedSessionsForUserInCourse(1L, 1L)).thenReturn(0L);

        // When
        AttendanceRateResponse result = attendanceService.calculateAttendanceRate(1L, 1L);

        // Then
        assertThat(result.getAttendanceRate()).isEqualTo(0.0);
        assertThat(result.getPercentage()).isEqualTo("0.00%");
    }

    @Test
    @DisplayName("Should update attendance status")
    void updateAttendanceStatus_Success() {
        // Given
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        // When
        Attendance result = attendanceService.updateAttendanceStatus(1L, AttendanceStatus.LATE);

        // Then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
        verify(attendanceRepository, times(1)).save(testAttendance);
    }

    @Test
    @DisplayName("Should throw exception when updating status of non-existent attendance")
    void updateAttendanceStatus_NotFound_ThrowsException() {
        // Given
        when(attendanceRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() ->
                attendanceService.updateAttendanceStatus(999L, AttendanceStatus.LATE))
                .isInstanceOf(NotFoundException.class);

        verify(attendanceRepository, never()).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should get attendance by user and session")
    void getAttendanceByUserIdAndSessionId_Found() {
        // Given
        when(attendanceRepository.findByUserIdAndSessionId(1L, 1L))
                .thenReturn(Optional.of(testAttendance));

        // When
        Optional<Attendance> result = attendanceService.getAttendanceByUserIdAndSessionId(1L, 1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
        assertThat(result.get().getSessionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty when attendance not found by user and session")
    void getAttendanceByUserIdAndSessionId_NotFound() {
        // Given
        when(attendanceRepository.findByUserIdAndSessionId(999L, 999L))
                .thenReturn(Optional.empty());

        // When
        Optional<Attendance> result = attendanceService.getAttendanceByUserIdAndSessionId(999L, 999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all attendances")
    void getAllAttendances_Success() {
        // Given
        Attendance attendance2 = new Attendance();
        attendance2.setId(2L);
        attendance2.setUserId(2L);

        when(attendanceRepository.findAll())
                .thenReturn(Arrays.asList(testAttendance, attendance2));

        // When
        List<Attendance> result = attendanceService.getAllAttendances();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Should check in with LATE status")
    void checkIn_WithLateStatus_Success() {
        // Given
        CheckInRequest lateRequest = CheckInRequest.builder()
                .userId(1L)
                .courseId(1L)
                .sessionId(2L)
                .status(AttendanceStatus.LATE)
                .build();

        when(attendanceRepository.findByUserIdAndSessionId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> {
            Attendance a = invocation.getArgument(0);
            a.setId(2L);
            return a;
        });

        // When
        Attendance result = attendanceService.checkIn(lateRequest);

        // Then
        assertThat(result.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(result.getCheckInTime()).isNotNull();
    }
}
