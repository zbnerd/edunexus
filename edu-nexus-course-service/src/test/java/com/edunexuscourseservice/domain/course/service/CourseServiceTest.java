package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.domain.course.entity.Course;
import com.edunexuscourseservice.domain.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void testSaveCourse() {
        // given
        Course course = new Course();

        // when
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // then
        Course result = courseService.saveCourse(course);
        assertNotNull(result);
        verify(courseRepository).save(course);
    }
    
    @Test
    void testUpdateCourse() throws Exception {
        // given
        Course existingCourse = new Course();
        setId(existingCourse, 1L); // 리플렉션으로 ID 설정
        existingCourse.setCourseInfo(
                CourseInfoDto.builder()
                        .title("original title")
                        .description("original description")
                        .instructorId(100L)
                        .build()
        );

        Course updatedDetails = new Course();
        setId(updatedDetails, 1L); // 동일 ID로 설정
        updatedDetails.setCourseInfo(
                CourseInfoDto.builder()
                        .title("updated title")
                        .description("updated description")
                        .instructorId(101L)
                        .build()
        );

        // when
        when(courseRepository.findById(1L)).thenReturn(Optional.of(existingCourse));

        // then
        Course result = courseService.updateCourse(1L, updatedDetails);
        assertNotNull(result);
        assertEquals("updated title", result.getTitle());
        assertEquals("updated description", result.getDescription());
        assertEquals(101L, result.getInstructorId());
    }

    @Test
    void testGetCourseById() {
        // given
        Course course = new Course();

        // when
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        // then
        Optional<Course> result = courseService.getCourseById(1L);
        assertTrue(result.isPresent());
        assertSame(course, result.get());
        verify(courseRepository).findById(1L);
    }

    @Test
    void testGetAllCourses() {
        // given
        List<Course> courses = Arrays.asList(new Course(), new Course());

        // when
        when(courseRepository.findAll()).thenReturn(courses);

        // then
        List<Course> result = courseService.getAllCourses();

        assertNotNull(result);
        assertEquals(courses.size(), result.size());
        verify(courseRepository).findAll();
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true); // private 필드 접근 허용
        field.set(target, id);
    }
}