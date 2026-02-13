package com.edunexuscourseservice.adapter.in.web;

import com.edunexuscourseservice.adapter.in.web.request.CourseCreateRequest;
import com.edunexuscourseservice.adapter.in.web.request.CourseUpdateRequest;
import com.edunexuscourseservice.adapter.in.web.response.CourseInfoResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseRatingAverageResponse;
import com.edunexuscourseservice.adapter.in.web.response.CourseResponse;
import com.edunexuscourseservice.domain.course.dto.CourseInfoDto;
import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.port.in.CourseUseCase;
import com.edunexuscourseservice.domain.course.util.RoundUtils;
import com.edunexuscourseservice.port.in.CourseRatingUseCase;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.condition.CourseSearchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseController
 *
 * Tests the course REST API endpoints including CRUD operations,
 * rating lookups, and batch functionality. Verifies proper HTTP responses
 * and service layer interactions.
 */
@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseUseCase courseUseCase;

    @Mock
    private CourseRatingUseCase courseRatingUseCase;

    @InjectMocks
    private CourseController courseController;

    private Course testCourse;

    @BeforeEach
    void setUp() throws Exception {
        testCourse = new Course();
        testCourse.setCourseInfo(CourseInfoDto.builder()
                .title("Test Course")
                .description("Test Description")
                .instructorId(1L)
                .build());
        // Set ID using reflection since Course doesn't have setId()
        java.lang.reflect.Field idField = Course.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testCourse, 1L);
    }

    //region Create Course Tests
    @Test
    void createCourse_WhenValidRequest_ShouldReturnCreatedStatusWithCourse() throws Exception {
        // given
        CourseCreateRequest request = new CourseCreateRequest();
        java.lang.reflect.Field titleField = CourseCreateRequest.class.getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(request, "New Course");

        java.lang.reflect.Field descField = CourseCreateRequest.class.getDeclaredField("description");
        descField.setAccessible(true);
        descField.set(request, "New Description");

        java.lang.reflect.Field instructorField = CourseCreateRequest.class.getDeclaredField("instructorId");
        instructorField.setAccessible(true);
        instructorField.set(request, 1L);

        when(courseUseCase.saveCourse(any(Course.class))).thenReturn(testCourse);

        // when
        ResponseEntity<CourseResponse> response = courseController.createCourse(request);

        // then
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testCourse.getId(), response.getBody().getId());
        assertEquals("Test Course", response.getBody().getTitle()); // testCourse has title "Test Course"

        // Check Location header
        assertNotNull(response.getHeaders().getLocation());
        assertTrue(response.getHeaders().getLocation().toString().contains("/courses/"));

        verify(courseUseCase).saveCourse(any(Course.class));
    }

    @Test
    void createCourse_WhenServiceThrowsException_ShouldPropagateException() throws Exception {
        // given
        CourseCreateRequest request = new CourseCreateRequest();
        java.lang.reflect.Field titleField = CourseCreateRequest.class.getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(request, "New Course");

        java.lang.reflect.Field descField = CourseCreateRequest.class.getDeclaredField("description");
        descField.setAccessible(true);
        descField.set(request, "New Description");

        java.lang.reflect.Field instructorField = CourseCreateRequest.class.getDeclaredField("instructorId");
        instructorField.setAccessible(true);
        instructorField.set(request, 1L);

        when(courseUseCase.saveCourse(any(Course.class)))
                .thenThrow(new RuntimeException("Course creation failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseController.createCourse(request);
        });

        verify(courseUseCase).saveCourse(any(Course.class));
    }
    //endregion

    //region Update Course Tests
    @Test
    void updateCourse_WhenValidRequest_ShouldReturnUpdatedCourse() throws Exception {
        // given
        Course existingCourse = new Course();
        existingCourse.setCourseInfo(CourseInfoDto.builder()
                .title("Old Title")
                .description("Old Description")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField1 = Course.class.getDeclaredField("id");
        idField1.setAccessible(true);
        idField1.set(existingCourse, 1L);

        // Use reflection to set private fields since CourseUpdateRequest doesn't have setters
        CourseUpdateRequest request = new CourseUpdateRequest();
        java.lang.reflect.Field titleField = CourseUpdateRequest.class.getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(request, "Updated Title");

        java.lang.reflect.Field descField = CourseUpdateRequest.class.getDeclaredField("description");
        descField.setAccessible(true);
        descField.set(request, "Updated Description");

        java.lang.reflect.Field instructorField = CourseUpdateRequest.class.getDeclaredField("instructorId");
        instructorField.setAccessible(true);
        instructorField.set(request, 1L);

        when(courseUseCase.getCourseById(eq(1L))).thenReturn(Optional.of(existingCourse));
        when(courseUseCase.updateCourse(eq(1L), any(Course.class))).thenReturn(testCourse);

        // when
        ResponseEntity<CourseResponse> response = courseController.updateCourse(1L, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testCourse.getId(), response.getBody().getId());
        assertEquals("Test Course", response.getBody().getTitle()); // testCourse has title "Test Course"

        verify(courseUseCase).getCourseById(eq(1L));
        verify(courseUseCase).updateCourse(eq(1L), any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseNotFound_ShouldThrowNotFoundException() throws Exception {
        // given
        CourseUpdateRequest request = new CourseUpdateRequest();
        java.lang.reflect.Field titleField = CourseUpdateRequest.class.getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(request, "Updated Title");

        java.lang.reflect.Field descField = CourseUpdateRequest.class.getDeclaredField("description");
        descField.setAccessible(true);
        descField.set(request, "Updated Description");

        java.lang.reflect.Field instructorField = CourseUpdateRequest.class.getDeclaredField("instructorId");
        instructorField.setAccessible(true);
        instructorField.set(request, 1L);

        when(courseUseCase.getCourseById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseController.updateCourse(999L, request);
        });

        verify(courseUseCase).getCourseById(999L);
        verify(courseUseCase, never()).updateCourse(any(), any());
    }
    //endregion

    //region Get Course Tests
    @Test
    void getCourse_WhenCourseExists_ShouldReturnCourseWithRating() {
        // given
        when(courseUseCase.getCourseById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRatingUseCase.getAverageRatingByCourseId(1L)).thenReturn(4.5);

        // when
        ResponseEntity<CourseInfoResponse> response = courseController.getCourse(1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testCourse.getId(), response.getBody().getId());
        assertEquals("Test Course", response.getBody().getTitle());
        assertEquals(4.5, response.getBody().getCourseRatingAvg());

        verify(courseUseCase).getCourseById(eq(1L));
        verify(courseRatingUseCase).getAverageRatingByCourseId(eq(1L));
    }

    @Test
    void getCourse_WhenCourseNotFound_ShouldThrowNotFoundException() {
        // given
        when(courseUseCase.getCourseById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> {
            courseController.getCourse(999L);
        });

        verify(courseUseCase).getCourseById(999L);
        verify(courseRatingUseCase, never()).getAverageRatingByCourseId(any());
    }

    @Test
    void getCourseRatingAverage_WhenCourseExists_ShouldReturnRating() {
        // given
        when(courseRatingUseCase.getAverageRatingByCourseId(1L)).thenReturn(4.7);

        // when
        ResponseEntity<CourseRatingAverageResponse> response = courseController.getCourseRatingAverage(1L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getCourseId());
        assertEquals(4.7, response.getBody().getAverageRating());

        verify(courseRatingUseCase).getAverageRatingByCourseId(eq(1L));
    }

    @Test
    void getCourseRatingAverage_WhenNoRatingExists_ShouldReturnZero() {
        // given
        when(courseRatingUseCase.getAverageRatingByCourseId(999L)).thenReturn(0.0);

        // when
        ResponseEntity<CourseRatingAverageResponse> response = courseController.getCourseRatingAverage(999L);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(999L, response.getBody().getCourseId());
        assertEquals(0.0, response.getBody().getAverageRating());

        verify(courseRatingUseCase).getAverageRatingByCourseId(999L);
    }
    //endregion

    //region Get All Courses Tests
    @Test
    void getAllCourses_WhenCoursesExist_ShouldReturnCourseListWithRatings() throws Exception {
        // given
        Course course1 = new Course();
        course1.setCourseInfo(CourseInfoDto.builder()
                .title("Course 1")
                .description("Description 1")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField1 = Course.class.getDeclaredField("id");
        idField1.setAccessible(true);
        idField1.set(course1, 1L);

        Course course2 = new Course();
        course2.setCourseInfo(CourseInfoDto.builder()
                .title("Course 2")
                .description("Description 2")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField2 = Course.class.getDeclaredField("id");
        idField2.setAccessible(true);
        idField2.set(course2, 2L);

        List<Course> courses = List.of(course1, course2);
        Pageable pageable = PageRequest.of(0, 10);

        when(courseUseCase.getAllCourses(any(CourseSearchCondition.class), any(Pageable.class)))
                .thenReturn(courses);
        when(courseRatingUseCase.getAverageRatingsByCourseIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, 4.5, 2L, 4.0));

        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getAllCourses(
                new CourseSearchCondition(), pageable);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        CourseInfoResponse response1 = response.getBody().get(0);
        assertEquals(1L, response1.getId());
        assertEquals("Course 1", response1.getTitle());
        assertEquals(4.5, response1.getCourseRatingAvg());

        CourseInfoResponse response2 = response.getBody().get(1);
        assertEquals(2L, response2.getId());
        assertEquals("Course 2", response2.getTitle());
        assertEquals(4.0, response2.getCourseRatingAvg());

        verify(courseUseCase).getAllCourses(any(CourseSearchCondition.class), any(Pageable.class));
        verify(courseRatingUseCase).getAverageRatingsByCourseIds(List.of(1L, 2L));
    }

    @Test
    void getAllCourses_WhenNoCoursesExist_ShouldReturnEmptyList() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(courseUseCase.getAllCourses(any(CourseSearchCondition.class), any(Pageable.class)))
                .thenReturn(List.of());

        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getAllCourses(
                new CourseSearchCondition(), pageable);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(courseUseCase).getAllCourses(any(CourseSearchCondition.class), any(Pageable.class));
        // Even with empty list, getAverageRatingsByCourseIds is called with empty list
        verify(courseRatingUseCase).getAverageRatingsByCourseIds(eq(List.of()));
    }
    //endregion

    //region Batch Course Tests
    @Test
    void getCoursesByIds_WhenValidIds_ShouldReturnCoursesWithRatings() throws Exception {
        // given
        Course course1 = new Course();
        course1.setCourseInfo(CourseInfoDto.builder()
                .title("Batch Course 1")
                .description("Batch Description 1")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField1 = Course.class.getDeclaredField("id");
        idField1.setAccessible(true);
        idField1.set(course1, 1L);

        Course course2 = new Course();
        course2.setCourseInfo(CourseInfoDto.builder()
                .title("Batch Course 2")
                .description("Batch Description 2")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField2 = Course.class.getDeclaredField("id");
        idField2.setAccessible(true);
        idField2.set(course2, 2L);

        List<Long> courseIds = List.of(1L, 2L);
        List<Course> courses = List.of(course1, course2);

        when(courseUseCase.getCoursesByIds(courseIds)).thenReturn(courses);
        when(courseRatingUseCase.getAverageRatingsByCourseIds(courseIds))
                .thenReturn(Map.of(1L, 4.8, 2L, 3.9));

        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getCoursesByIds(courseIds);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        CourseInfoResponse response1 = response.getBody().get(0);
        assertEquals(1L, response1.getId());
        assertEquals("Batch Course 1", response1.getTitle());
        assertEquals(4.8, response1.getCourseRatingAvg());

        CourseInfoResponse response2 = response.getBody().get(1);
        assertEquals(2L, response2.getId());
        assertEquals("Batch Course 2", response2.getTitle());
        assertEquals(3.9, response2.getCourseRatingAvg());

        verify(courseUseCase).getCoursesByIds(courseIds);
        verify(courseRatingUseCase).getAverageRatingsByCourseIds(courseIds);
    }

    @Test
    void getCoursesByIds_WhenEmptyList_ShouldReturnEmptyResponse() {
        // given
        List<Long> emptyIds = List.of();

        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getCoursesByIds(emptyIds);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(courseUseCase, never()).getCoursesByIds(any());
        verify(courseRatingUseCase, never()).getAverageRatingsByCourseIds(any());
    }

    @Test
    void getCoursesByIds_WhenSomeIdsNotFound_ShouldReturnOnlyFoundCourses() throws Exception {
        // given
        Course foundCourse = new Course();
        foundCourse.setCourseInfo(CourseInfoDto.builder()
                .title("Found Course")
                .description("Found Description")
                .instructorId(1L)
                .build());
        java.lang.reflect.Field idField = Course.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(foundCourse, 1L);

        List<Long> courseIds = List.of(1L, 999L); // 999 doesn't exist
        List<Course> courses = List.of(foundCourse); // Only return found courses

        when(courseUseCase.getCoursesByIds(courseIds)).thenReturn(courses);
        when(courseRatingUseCase.getAverageRatingsByCourseIds(courseIds))
                .thenReturn(Map.of(1L, 4.5));

        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getCoursesByIds(courseIds);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        CourseInfoResponse response1 = response.getBody().get(0);
        assertEquals(1L, response1.getId());
        assertEquals("Found Course", response1.getTitle());
        assertEquals(4.5, response1.getCourseRatingAvg());

        verify(courseUseCase).getCoursesByIds(courseIds);
        verify(courseRatingUseCase).getAverageRatingsByCourseIds(courseIds);
    }
    //endregion

    //region Edge Cases
    @Test
    void updateCourse_WhenRequestIsNull_ShouldThrowException() {
        // given - updateCourse calls getCourseById first before checking request
        when(courseUseCase.getCourseById(1L)).thenReturn(Optional.of(testCourse));

        // when & then - NullPointerException will be thrown when accessing request methods
        assertThrows(NullPointerException.class, () -> {
            courseController.updateCourse(1L, null);
        });

        // getCourseById IS called before the null check happens
        verify(courseUseCase).getCourseById(eq(1L));
    }

    @Test
    void getCoursesByIds_WhenRequestIsNull_ShouldReturnEmptyResponse() {
        // when
        ResponseEntity<List<CourseInfoResponse>> response = courseController.getCoursesByIds(null);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(courseUseCase, never()).getCoursesByIds(any());
        verify(courseRatingUseCase, never()).getAverageRatingsByCourseIds(any());
    }
    //endregion
}