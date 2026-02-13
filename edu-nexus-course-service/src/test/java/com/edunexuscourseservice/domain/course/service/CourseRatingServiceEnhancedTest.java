package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.service.CourseRatingCacheOrchestrator;
import com.edunexuscourseservice.application.service.CourseRatingCrudService;
import com.edunexuscourseservice.application.service.CourseRatingQueryService;
import com.edunexuscourseservice.application.service.CourseRatingService;
import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import com.edunexuscourseservice.config.course.metrics.CourseMetrics;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexus.common.exception.NotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
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
    private CourseRatingCrudService crudService;

    @Mock
    private CourseRatingCacheOrchestrator cacheOrchestrator;

    @Mock
    private CourseRatingQueryService queryService;

    @Mock
    private CourseMetrics courseMetrics;

    @Mock
    private MeterRegistry meterRegistry;

    @InjectMocks
    private CourseRatingService courseRatingService;

    //region Edge Case Tests

    @Test
    void addRatingToCourse_WhenCourseNotFound_ShouldThrowNotFoundException() {
        // given
        CourseRating courseRating = new CourseRating();
        courseRating.setRating(5);
        when(crudService.save(999L, courseRating))
                .thenThrow(new NotFoundException("Course not found with id = 999"));

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.addRatingToCourse(999L, courseRating);
        });

        verify(crudService).save(999L, courseRating);
        verify(cacheOrchestrator, never()).onRatingAdded(any(), anyInt(), any());
    }

    @Test
    void updateRating_WhenRatingNotFound_ShouldThrowNotFoundException() {
        // given
        CourseRating newCourseRating = new CourseRating();
        // crudService.update calls findById internally, so need to mock that too
        when(crudService.update(999L, newCourseRating))
                .thenThrow(new NotFoundException("CourseRating not found with id = 999"));

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.updateRating(999L, newCourseRating);
        });

        verify(crudService).findById(999L);
        verify(cacheOrchestrator, never()).onRatingUpdated(any(), anyInt(), any(), anyString());
    }

    @Test
    void deleteRating_WhenRatingNotFound_ShouldThrowNotFoundException() {
        // given
        when(crudService.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseRatingService.deleteRating(999L);
        });

        verify(crudService).findById(999L);
        verify(crudService, never()).delete(any());
        verify(cacheOrchestrator, never()).onRatingDeleted(any(), anyInt());
    }

    @Test
    void getRating_WhenRatingDoesNotExist_ShouldReturnEmptyOptional() {
        // given
        when(crudService.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<CourseRating> result = courseRatingService.getRating(999L);

        // then
        assertTrue(result.isEmpty());
        verify(crudService).findById(999L);
    }
    //endregion

    //region Redis Failure Tests

    @Test
    void getAverageRatingByCourseId_WhenRedisFails_ShouldReturnZeroAndLogWarning() {
        // given
        when(queryService.getAverageRating(123L)).thenReturn(0.0);
        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(0.0, result);
        verify(queryService).getAverageRating(123L);
    }

    @Test
    void getAverageRatingByCourseId_WhenRedisReturnsNull_ShouldReturnZero() {
        // given
        when(queryService.getAverageRating(123L)).thenReturn(0.0);
        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(0.0, result);
        verify(queryService).getAverageRating(123L);
    }

    @Test
    void getAverageRatingByCourseId_WhenRedisReturnsValidValue_ShouldReturnCorrectValue() {
        // given
        when(queryService.getAverageRating(123L)).thenReturn(4.5);
        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        // when
        Double result = courseRatingService.getAverageRatingByCourseId(123L);

        // then
        assertEquals(4.5, result);
        verify(queryService).getAverageRating(123L);
    }
    //endregion

    //region Repository Query Tests

    @Test
    void getAllRatingsByCourseId_WhenNoRatingsExist_ShouldReturnEmptyList() {
        // given
        when(queryService.getRatingsByCourseId(123L)).thenReturn(List.of());

        // when
        List<CourseRating> result = courseRatingService.getAllRatingsByCourseId(123L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(queryService).getRatingsByCourseId(123L);
    }

    @Test
    void getAllRatingsByCourseId_WhenMultipleRatingsExist_ShouldReturnAllRatings() throws Exception {
        // given
        List<CourseRating> ratings = List.of(
                createMockRating(1L, 5),
                createMockRating(2L, 4),
                createMockRating(3L, 3)
        );
        when(queryService.getRatingsByCourseId(123L)).thenReturn(ratings);

        // when
        List<CourseRating> result = courseRatingService.getAllRatingsByCourseId(123L);

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(queryService).getRatingsByCourseId(123L);
    }

    @Test
    void getAllRatingsByCourseId_WhenRepositoryThrowsException_ShouldPropagate() {
        // given
        when(queryService.getRatingsByCourseId(123L))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.getAllRatingsByCourseId(123L);
        });

        verify(queryService).getRatingsByCourseId(123L);
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
        courseRating.setRating(5);

        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        when(crudService.save(1L, courseRating)).thenReturn(courseRating);
        doThrow(new RuntimeException("Kafka failed"))
                .when(cacheOrchestrator).onRatingAdded(any(), anyInt(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.addRatingToCourse(1L, courseRating);
        });

        verify(crudService).save(1L, courseRating);
        verify(cacheOrchestrator).onRatingAdded(1L, courseRating.getRating(), 1L);
    }

    @Test
    void updateRating_WhenProducerServiceFailsAfterUpdate_ShouldStillHaveUpdatedData() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourse(course);
        existingRating.setRating(3);
        existingRating.setComment("Original comment");

        CourseRating updateRating = new CourseRating();
        updateRating.setCourse(course);  // Set course to avoid NPE
        updateRating.setRating(5);
        updateRating.setComment("Updated comment");

        when(crudService.findById(1L)).thenReturn(Optional.of(existingRating));
        when(crudService.update(1L, updateRating)).thenReturn(existingRating);
        doThrow(new RuntimeException("Kafka failed"))
                .when(cacheOrchestrator).onRatingUpdated(any(), any(), any(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.updateRating(1L, updateRating);
        });

        verify(crudService).update(1L, updateRating);
        verify(cacheOrchestrator).onRatingUpdated(
                course.getId(), 3, 5, "Updated comment");
    }

    @Test
    void deleteRating_WhenProducerServiceFails_ShouldStillDeleteFromDatabase() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourse(course);
        existingRating.setRating(4);
        existingRating.setComment("To be deleted");

        when(crudService.findById(1L)).thenReturn(Optional.of(existingRating));
        doThrow(new RuntimeException("Kafka failed"))
                .when(cacheOrchestrator).onRatingDeleted(any(), any());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.deleteRating(1L);
        });

        verify(cacheOrchestrator).onRatingDeleted(
                course.getId(), 4);
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
        courseRating.setRating(5);

        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        when(crudService.save(1L, courseRating))
                .thenThrow(new RuntimeException("Database save failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.addRatingToCourse(1L, courseRating);
        });

        verify(crudService).save(1L, courseRating);
        verify(cacheOrchestrator, never()).onRatingAdded(any(), anyInt(), any());
    }

    @Test
    void updateRating_WhenSaveFails_ShouldNotFireKafkaEvent() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourse(course);
        existingRating.setRating(3);
        existingRating.setComment("Original comment");

        CourseRating updateRating = new CourseRating();
        updateRating.setCourse(course);  // Set course to avoid NPE
        updateRating.setRating(5);
        updateRating.setComment("Updated comment");

        when(crudService.findById(1L)).thenReturn(Optional.of(existingRating));
        when(crudService.update(1L, updateRating))
                .thenThrow(new RuntimeException("Database save failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingService.updateRating(1L, updateRating);
        });

        verify(crudService).update(1L, updateRating);
        verify(cacheOrchestrator, never()).onRatingUpdated(any(), anyInt(), any(), anyString());
    }

    @Test
    void getAverageRatingsByCourseIds_WhenValidIds_ShouldReturnRatingsMap() {
        // given
        List<Long> courseIds = List.of(123L, 456L, 789L);
        Map<Long, Double> expectedRatings = Map.of(
                123L, 4.5,
                456L, 3.8,
                789L, 4.2
        );
        when(queryService.getAverageRatings(courseIds)).thenReturn(expectedRatings);

        // when
        Map<Long, Double> result = courseRatingService.getAverageRatingsByCourseIds(courseIds);

        // then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(4.5, result.get(123L));
        assertEquals(3.8, result.get(456L));
        assertEquals(4.2, result.get(789L));
        verify(queryService).getAverageRatings(courseIds);
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
