package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.entity.CourseRating;
import com.edunexuscourseservice.domain.course.repository.CourseRatingRepository;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseRatingServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseRatingRepository courseRatingRepository;

    @InjectMocks
    private CourseRatingService courseRatingService;
    
    @Test
    void testAddRatingToCourse() throws Exception  {
        // given
        CourseRating courseRating = new CourseRating();
        Course course = new Course();
        setId(course, 1L);
        setId(courseRating, 1L);

        courseRating.setCourse(course);

        // when
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(courseRating);
        
        // then
        CourseRating result = courseRatingService.addRatingToCourse(1L, courseRating);
        assertNotNull(result);
        verify(courseRatingRepository).save(courseRating);
        assertEquals(1L, result.getId());

    }

    @Test
    void testUpdateRating() throws Exception {
        // given
        CourseRating existingRating = new CourseRating();
        setId(existingRating, 1L);
        existingRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(5)
                        .comment("Greate course!")
                        .build()
        );

        CourseRating updateRating = new CourseRating();
        updateRating.setCourseRatingInfo(
                CourseRatingInfoDto.builder()
                        .rating(4)
                        .comment("Good course!")
                        .build()
        );

        // when
        when(courseRatingRepository.findById(1L)).thenReturn(Optional.of(existingRating));

        // then
        CourseRating result = courseRatingService.updateRating(1L, updateRating);
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals("Good course!", result.getComment());
    }

    @Test
    void testDeleteRating() {
        doNothing().when(courseRatingRepository).deleteById(1L);

        courseRatingService.deleteRating(1L);

        verify(courseRatingRepository).deleteById(1L);

    }

    @Test
    void testGetAllRatingsByCourseId() {
        List<CourseRating> ratings = Arrays.asList(new CourseRating(), new CourseRating());

        when(courseRatingRepository.findByCourseId(1L)).thenReturn(ratings);

        List<CourseRating> allCourses = courseRatingService.getAllRatingsByCourseId(1L);
        assertNotNull(allCourses);
        assertEquals(ratings.size(), allCourses.size());
        verify(courseRatingRepository).findByCourseId(1L);
    }

    @Test
    void testGetRatingById() {
        // given
        CourseRating courseRating = new CourseRating();

        // when
        when(courseRatingRepository.findById(1L)).thenReturn(Optional.of(courseRating));

        // then
        Optional<CourseRating> result = courseRatingService.getRating(1L);
        assertTrue(result.isPresent());
        assertEquals(courseRating, result.get());
        verify(courseRatingRepository).findById(1L);
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true); // private 필드 접근 허용
        field.set(target, id);
    }
}