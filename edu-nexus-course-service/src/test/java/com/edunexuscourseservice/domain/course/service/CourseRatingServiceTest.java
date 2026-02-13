package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.application.service.CourseRatingCacheOrchestrator;
import com.edunexuscourseservice.application.service.CourseRatingCrudService;
import com.edunexuscourseservice.application.service.CourseRatingQueryService;
import com.edunexuscourseservice.application.service.CourseRatingService;
import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import com.edunexuscourseservice.config.course.metrics.CourseMetrics;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseRatingServiceTest {

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

    @Test
    void testAddRatingToCourse() throws Exception {
        // given
        CourseRating courseRating = new CourseRating();
        Course course = new Course();
        setId(course, 1L);
        setId(courseRating, 1L);
        courseRating.setCourse(course);

        Timer.Sample sample = mock(Timer.Sample.class);
        when(courseMetrics.startCourseRetrieval()).thenReturn(sample);

        when(crudService.save(1L, courseRating)).thenReturn(courseRating);
        doNothing().when(courseMetrics).recordRatingCreated();
        doNothing().when(courseMetrics).stopCourseRetrieval(any(), anyString());

        // when
        CourseRating result = courseRatingService.addRatingToCourse(1L, courseRating);

        // then
        assertNotNull(result);
        verify(crudService).save(1L, courseRating);
        verify(cacheOrchestrator).onRatingAdded(1L, courseRating.getRating(), 1L);
        verify(courseMetrics).recordRatingCreated();
        verify(courseMetrics).stopCourseRetrieval(sample, "addRating");
        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateRating() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourse(course);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(5)
                        .comment("Greate course!")
                        .build()
        );

        CourseRating updateRating = new CourseRating();
        updateRating.setRating(4);
        updateRating.setComment("Good course!");

        when(crudService.findById(1L)).thenReturn(Optional.of(existingRating));
        // Use thenAnswer to invoke the real updateCourseRating method
        when(crudService.update(1L, updateRating)).thenAnswer(invocation -> {
            existingRating.updateCourseRating(updateRating);
            return existingRating;
        });

        // when
        CourseRating result = courseRatingService.updateRating(1L, updateRating);

        // then
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Good course!", result.getComment());
        verify(cacheOrchestrator).onRatingUpdated(
                course.getId(), 5, 4, "Good course!");
    }

    @Test
    void testDeleteRating() throws Exception {
        // given
        Course course = new Course();
        setId(course, 1L);

        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourse(course);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(5)
                        .comment("Great course!")
                        .build()
        );
        when(crudService.findById(1L)).thenReturn(Optional.of(existingRating));
        doNothing().when(crudService).delete(1L);

        // when
        courseRatingService.deleteRating(1L);

        // then
        verify(crudService).delete(1L);
        verify(cacheOrchestrator).onRatingDeleted(
                course.getId(), 5);
    }

    @Test
    void testGetAllRatingsByCourseId() {
        List<CourseRating> ratings = Arrays.asList(new CourseRating(), new CourseRating());

        when(queryService.getRatingsByCourseId(1L)).thenReturn(ratings);

        List<CourseRating> allCourses = courseRatingService.getAllRatingsByCourseId(1L);
        assertNotNull(allCourses);
        assertEquals(ratings.size(), allCourses.size());
        verify(queryService).getRatingsByCourseId(1L);
    }

    @Test
    void testGetRatingById() {
        // given
        CourseRating courseRating = new CourseRating();

        // when
        when(crudService.findById(1L)).thenReturn(Optional.of(courseRating));

        // then
        Optional<CourseRating> result = courseRatingService.getRating(1L);
        assertTrue(result.isPresent());
        assertEquals(courseRating, result.get());
        verify(crudService).findById(1L);
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(target, id);
    }
}
