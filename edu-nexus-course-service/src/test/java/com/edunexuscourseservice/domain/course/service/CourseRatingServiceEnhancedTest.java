package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.service.CourseRatingService;
import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexus.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * Enhanced unit tests for CourseRatingService
 *
 * Tests edge cases, error scenarios, and Redis failure handling
 * Verifies proper Kafka event firing and fallback behavior
 */
@ExtendWith(MockitoExtension.class)
class CourseRatingServiceEnhancedTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseRatingRepository courseRatingRepository;

    @Mock
    private CourseRatingRedisRepository courseRatingRedisRepository;

    @Mock
    private CourseRatingProducerService courseRatingProducerService;

    @InjectMocks
    private CourseRatingService courseRatingService;

    @BeforeEach
    void setUp() {
        reset(courseRepository, courseRatingRepository, courseRatingRedisRepository, courseRatingProducerService);
    }

    //region Edge Case Tests

    @Test
    void addRatingToCourse_WhenCourseNotFound_ShouldThrowNotFoundException() {
        // given
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.addRatingToCourse(999L, new CourseRating());
        });

        verify(courseRepository).findById(999L);
        verify(courseRatingRepository, never()).save(any());
        verify(courseRatingProducerService, never()).sendRatingAddedEvent(any(), any(), any());
    }

    @Test
    void updateRating_WhenRatingNotFound_ShouldThrowNotFoundException() {
        // given
        when(courseRatingRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.updateRating(999L, new CourseRating());
        });

        verify(courseRatingRepository).findById(999L);
        verify(courseRatingRepository, never()).save(any());
        verify(courseRatingProducerService, never()).sendRatingUpdatedEvent(any(), any(), any(), any());
    }

    @Test
    void deleteRating_WhenRatingNotFound_ShouldThrowNotFoundException() {
        // given
        when(courseRatingRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.deleteRating(999L);
        });

        verify(courseRatingRepository).findById(999L);
        verify(courseRatingRepository, never()).deleteById(any());
        verify(courseRatingProducerService, never()).sendRatingDeletedEvent(any(), any());
    }

    @Test
    void getRating_WhenRatingDoesNotExist_ShouldReturnEmptyOptional() {
        // given
        when(courseRatingRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<CourseRating> result = courseRatingService.getRating(999L);

        // then
        assertTrue(result.isEmpty());
        verify(courseRatingRepository).findById(999L);
    }
    //endregion

    //region Redis Failure Tests

    @Test
    void getAverageRatingByCourseId_WhenRedisFails_ShouldReturnZeroAndLogWarning() {
        // given
        when(courseRatingRedisRepository.getAverageReviewRating(123L))
                .thenThrow(new RuntimeException("Redis connection failed"));

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(0.0, result);
        verify(courseRatingRedisRepository).getAverageReviewRating(123L);
    }

    @Test
    void getAverageRatingByCourseId_WhenRedisReturnsNull_ShouldReturnZero() {
        // given
        when(courseRatingRedisRepository.getAverageReviewRating(123L)).thenReturn(null);

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(0.0, result);
        verify(courseRatingRedisRepository).getAverageReviewRating(123L);
    }

    @Test
    void getAverageRatingByCourseId_WhenRedisReturnsValidValue_ShouldReturnCorrectValue() {
        // given
        when(courseRatingRedisRepository.getAverageReviewRating(123L)).thenReturn(4.5);

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(4.5, result);
        verify(courseRatingRedisRepository).getAverageReviewRating(123L);
    }
    //endregion

    //region Repository Query Tests

    @Test
    void getAllRatingsByCourseId_WhenNoRatingsExist_ShouldReturnEmptyList() {
        // given
        when(courseRatingRepository.findByCourseId(123L)).thenReturn(List.of());

        // when
        List<CourseRating> result = courseRatingService.getAllRatingsByCourseId(123L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(courseRatingRepository).findByCourseId(123L);
    }

    @Test
    void getAllRatingsByCourseId_WhenMultipleRatingsExist_ShouldReturnAllRatings() throws Exception {
        // given
        List<CourseRating> ratings = List.of(
                createMockRating(1L, 5),
                createMockRating(2L, 4),
                createMockRating(3L, 3)
        );
        when(courseRatingRepository.findByCourseId(123L)).thenReturn(ratings);

        // when
        List<CourseRating> result = courseRatingService.getAllRatingsByCourseId(123L);

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(courseRatingRepository).findByCourseId(123L);
    }

    @Test
    void getAllRatingsByCourseId_WhenRepositoryThrowsException_ShouldPropagate() {
        // given
        when(courseRatingRepository.findByCourseId(123L))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.getAllRatingsByCourseId(123L);
        });

        verify(courseRatingRepository).findByCourseId(123L);
    }
    //endregion

    //region Kafka Producer Failure Tests

    @Test
    void addRatingToCourse_WhenProducerServiceFails_ShouldStillSaveToDatabase() throws Exception {
        // given
        CourseRating courseRating = new CourseRating();
        Course course = new Course();
        setId(course, 1L);
        setId(courseRating, 1L);
        courseRating.setCourse(course);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(courseRating);
        doThrow(new RuntimeException("Kafka failed"))
                .when(courseRatingProducerService).sendRatingAddedEvent(any(), any(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.addRatingToCourse(1L, courseRating);
        });

        verify(courseRatingRepository).save(courseRating);
        verify(courseRatingProducerService).sendRatingAddedEvent(1L, courseRating.getRating(), 1L);
    }

    @Test
    void updateRating_WhenProducerServiceFailsAfterUpdate_ShouldStillHaveUpdatedData() throws Exception {
        // given
        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(3)
                        .comment("Original comment")
                        .build()
        );

        CourseRating updateRating = new CourseRating();
        updateRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(5)
                        .comment("Updated comment")
                        .build()
        );

        when(courseRatingRepository.findById(1L)).thenReturn(Optional.of(existingRating));
        when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(existingRating);
        doThrow(new RuntimeException("Kafka failed"))
                .when(courseRatingProducerService).sendRatingUpdatedEvent(any(), any(), any(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.updateRating(1L, updateRating);
        });

        verify(courseRatingRepository).save(existingRating);
        verify(courseRatingProducerService).sendRatingUpdatedEvent(
                existingRating.getCourse().getId(), 3, 5, "Updated comment");
    }

    @Test
    void deleteRating_WhenProducerServiceFails_ShouldStillDeleteFromDatabase() throws Exception {
        // given
        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(4)
                        .comment("To be deleted")
                        .build()
        );

        when(courseRatingRepository.findById(1L)).thenReturn(Optional.of(existingRating));
        doNothing().when(courseRatingRepository).deleteById(1L);
        doThrow(new RuntimeException("Kafka failed"))
                .when(courseRatingProducerService).sendRatingDeletedEvent(any(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.deleteRating(1L);
        });

        verify(courseRatingRepository).deleteById(1L);
        verify(courseRatingProducerService).sendRatingDeletedEvent(
                existingRating.getCourse().getId(), 4);
    }
    //endregion

    //region Transaction Boundary Tests

    @Test
    void addRatingToCourse_WhenCourseSaveFails_ShouldNotFireKafkaEvent() throws Exception {
        // given
        CourseRating courseRating = new CourseRating();
        Course course = new Course();
        setId(course, 1L);
        setId(courseRating, 1L);
        courseRating.setCourse(course);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRatingRepository.save(any(CourseRating.class)))
                .thenThrow(new RuntimeException("Database save failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.addRatingToCourse(1L, courseRating);
        });

        verify(courseRepository).findById(1L);
        verify(courseRatingRepository).save(any(CourseRating.class));
        verify(courseRatingProducerService, never()).sendRatingAddedEvent(any(), any(), any());
    }

    @Test
    void updateRating_WhenSaveFails_ShouldNotFireKafkaEvent() throws Exception {
        // given
        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(3)
                        .comment("Original comment")
                        .build()
        );

        CourseRating updateRating = new CourseRating();
        updateRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(5)
                        .comment("Updated comment")
                        .build()
        );

        when(courseRatingRepository.findById(1L)).thenReturn(Optional.of(existingRating));
        when(courseRatingRepository.save(any(CourseRating.class)))
                .thenThrow(new RuntimeException("Database save failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.updateRating(1L, updateRating);
        });

        verify(courseRatingRepository).findById(1L);
        verify(courseRatingRepository).save(any(CourseRating.class));
        verify(courseRatingProducerService, never()).sendRatingUpdatedEvent(any(), any(), any(), any());
    }
    //endregion

    //region Helper Methods

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }

    private CourseRating createMockRating(Long id, int rating) throws Exception {
        CourseRating ratingObj = new CourseRating();
        setId(ratingObj, id);
        ratingObj.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(rating)
                        .comment("Comment " + id)
                        .build()
        );
        return ratingObj;
    }
    //endregion
}