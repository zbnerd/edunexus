package com.edunexuscourseservice.domain.course.service;

import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import com.edunexuscourseservice.port.in.CourseUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

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
    private CourseUseCase courseUseCase;

    @Test
    void testSaveCourse() {
        // given
        Course course = new Course();

        // when
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        // then
        Course result = courseUseCase.saveCourse(course);
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
        Course result = courseUseCase.updateCourse(1L, updatedDetails);
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
        Optional<Course> result = courseUseCase.getCourseById(1L);
        assertTrue(result.isPresent());
        assertSame(course, result.get());
        verify(courseRepository).findById(1L);
    }

    @Test
    void testGetAllCourses() {
        // given
        List<Course> courses = Arrays.asList(new Course(), new Course());
        CourseSearchCondition condition = new CourseSearchCondition();

        PageRequest page = PageRequest.of(0, 20);

        // when
        when(courseRepository.findAll(condition, page)).thenReturn(courses);


        // then
        List<Course> result = courseUseCase.getAllCourses(condition, page);

        assertNotNull(result);
        assertEquals(courses.size(), result.size());
        verify(courseRepository).findAll(condition, page);
    }

    private void setId(Object target, Long id) throws Exception {
        Field field = target.getClass().getDeclaredField("id");
        field.setAccessible(true); // private 필드 접근 허용
        field.set(target, id);
    }
}