package com.edunexuscourseservice.application.service;

import com.edunexus.common.exception.NotFoundException;
import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRepository;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseRatingCrudService
 *
 * Test coverage:
 * - Happy path: Save, update, delete, find operations
 * - Error cases: Course not found, rating not found
 * - Edge cases: Null values, empty collections
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseRatingCrudService Tests")
class CourseRatingCrudServiceTest {

    @Mock
    private CourseRatingRepository courseRatingRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseRatingCrudService crudService;

    private Course testCourse;
    private CourseRating testRating;

    @BeforeEach
    void setUp() {
        testCourse = createTestCourse(1L, "Test Course");
        testRating = createTestRating(1L, 100L, 5, "Excellent course", testCourse);
    }

    private Course createTestCourse(Long id, String title) {
        Course course = new Course();
        try {
            Field idField = Course.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(course, id);

            Field titleField = Course.class.getDeclaredField("title");
            titleField.setAccessible(true);
            titleField.set(course, title);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return course;
    }

    private CourseRating createTestRating(Long id, Long userId, int rating, String comment, Course course) {
        CourseRating courseRating = new CourseRating();
        try {
            Field idField = CourseRating.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(courseRating, id);

            Field userIdField = CourseRating.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(courseRating, userId);

            courseRating.setRating(rating);
            courseRating.setComment(comment);
            courseRating.setCourse(course);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return courseRating;
    }

    @Nested
    @DisplayName("Save Operation Tests")
    class SaveOperationTests {

        @Test
        @DisplayName("Save rating with valid course ID should return saved rating")
        void save_ValidCourseId_ReturnsSavedRating() {
            // Given
            Long courseId = 1L;
            CourseRating newRating = new CourseRating();
            setUserId(newRating, 100L);
            newRating.setRating(4);
            newRating.setComment("Good course");

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(testRating);

            // When
            CourseRating result = crudService.save(courseId, newRating);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(100L, result.getUserId());
            verify(courseRepository).findById(courseId);
            verify(courseRatingRepository).save(newRating);
        }

        @Test
        @DisplayName("Save rating with non-existent course should throw NotFoundException")
        void save_NonExistentCourse_ThrowsNotFoundException() {
            // Given
            Long courseId = 999L;
            CourseRating newRating = new CourseRating();
            setUserId(newRating, 100L);
            newRating.setRating(4);

            when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> crudService.save(courseId, newRating)
            );

            assertTrue(exception.getMessage().contains("Course not found"));
            verify(courseRepository).findById(courseId);
            verify(courseRatingRepository, never()).save(any());
        }

        @Test
        @DisplayName("Save rating should set course relationship")
        void save_SetsCourseRelationship() {
            // Given
            Long courseId = 1L;
            CourseRating newRating = new CourseRating();
            setUserId(newRating, 100L);
            newRating.setRating(5);

            CourseRating savedRating = createTestRating(2L, 100L, 5, null, testCourse);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(savedRating);

            // When
            CourseRating result = crudService.save(courseId, newRating);

            // Then
            assertNotNull(result.getCourse());
            assertEquals(testCourse.getId(), result.getCourse().getId());
        }
    }

    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateOperationTests {

        @Test
        @DisplayName("Update existing rating should return updated rating")
        void update_ExistingRating_ReturnsUpdatedRating() {
            // Given
            Long ratingId = 1L;
            CourseRating newRatingData = new CourseRating();
            newRatingData.setRating(3);
            newRatingData.setComment("Updated comment");

            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));

            // When
            CourseRating result = crudService.update(ratingId, newRatingData);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getRating());
            assertEquals("Updated comment", result.getComment());
            assertEquals(100L, result.getUserId()); // Original user ID preserved
            verify(courseRatingRepository).findById(ratingId);
        }

        @Test
        @DisplayName("Update non-existent rating should throw NotFoundException")
        void update_NonExistentRating_ThrowsNotFoundException() {
            // Given
            Long ratingId = 999L;
            CourseRating newRatingData = new CourseRating();
            newRatingData.setRating(4);

            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.empty());

            // When & Then
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> crudService.update(ratingId, newRatingData)
            );

            assertTrue(exception.getMessage().contains("CourseRating not found"));
            verify(courseRatingRepository).findById(ratingId);
        }

        @Test
        @DisplayName("Update with null comment should set comment to null")
        void update_NullComment_SetsCommentToNull() {
            // Given
            Long ratingId = 1L;
            CourseRating newRatingData = new CourseRating();
            newRatingData.setRating(2);
            newRatingData.setComment(null);

            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));

            // When
            CourseRating result = crudService.update(ratingId, newRatingData);

            // Then
            assertNull(result.getComment());
            assertEquals(2, result.getRating());
        }
    }

    @Nested
    @DisplayName("Delete Operation Tests")
    class DeleteOperationTests {

        @Test
        @DisplayName("Delete existing rating should succeed")
        void delete_ExistingRating_Success() {
            // Given
            Long ratingId = 1L;
            when(courseRatingRepository.existsById(ratingId)).thenReturn(true);
            doNothing().when(courseRatingRepository).deleteById(ratingId);

            // When
            crudService.delete(ratingId);

            // Then
            verify(courseRatingRepository).existsById(ratingId);
            verify(courseRatingRepository).deleteById(ratingId);
        }

        @Test
        @DisplayName("Delete non-existent rating should throw NotFoundException")
        void delete_NonExistentRating_ThrowsNotFoundException() {
            // Given
            Long ratingId = 999L;
            when(courseRatingRepository.existsById(ratingId)).thenReturn(false);

            // When & Then
            NotFoundException exception = assertThrows(
                    NotFoundException.class,
                    () -> crudService.delete(ratingId)
            );

            assertTrue(exception.getMessage().contains("CourseRating not found"));
            verify(courseRatingRepository).existsById(ratingId);
            verify(courseRatingRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("Find Operation Tests")
    class FindOperationTests {

        @Test
        @DisplayName("Find by existing ID should return rating")
        void findById_ExistingId_ReturnsRating() {
            // Given
            Long ratingId = 1L;
            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));

            // When
            Optional<CourseRating> result = crudService.findById(ratingId);

            // Then
            assertTrue(result.isPresent());
            assertEquals(ratingId, result.get().getId());
            verify(courseRatingRepository).findById(ratingId);
        }

        @Test
        @DisplayName("Find by non-existent ID should return empty optional")
        void findById_NonExistentId_ReturnsEmpty() {
            // Given
            Long ratingId = 999L;
            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.empty());

            // When
            Optional<CourseRating> result = crudService.findById(ratingId);

            // Then
            assertFalse(result.isPresent());
            verify(courseRatingRepository).findById(ratingId);
        }

        @Test
        @DisplayName("Find by course ID should return list of ratings")
        void findByCourseId_ValidCourseId_ReturnsListOfRatings() {
            // Given
            Long courseId = 1L;
            CourseRating rating2 = createTestRating(2L, 101L, 4, "Good", testCourse);

            List<CourseRating> ratings = Arrays.asList(testRating, rating2);
            when(courseRatingRepository.findByCourseId(courseId)).thenReturn(ratings);

            // When
            List<CourseRating> result = crudService.findByCourseId(courseId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(1L, result.get(0).getId());
            assertEquals(2L, result.get(1).getId());
            verify(courseRatingRepository).findByCourseId(courseId);
        }

        @Test
        @DisplayName("Find by course ID with no ratings should return empty list")
        void findByCourseId_NoRatings_ReturnsEmptyList() {
            // Given
            Long courseId = 2L;
            when(courseRatingRepository.findByCourseId(courseId)).thenReturn(Arrays.asList());

            // When
            List<CourseRating> result = crudService.findByCourseId(courseId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(courseRatingRepository).findByCourseId(courseId);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Save with minimal rating data should succeed")
        void save_WithMinimalData_Success() {
            // Given
            Long courseId = 1L;
            CourseRating minimalRating = new CourseRating();
            setUserId(minimalRating, 100L);
            minimalRating.setRating(1);
            minimalRating.setComment(null);

            CourseRating saved = createTestRating(3L, 100L, 1, null, testCourse);

            when(courseRepository.findById(courseId)).thenReturn(Optional.of(testCourse));
            when(courseRatingRepository.save(any(CourseRating.class))).thenReturn(saved);

            // When
            CourseRating result = crudService.save(courseId, minimalRating);

            // Then
            assertNotNull(result);
            assertNull(result.getComment());
            assertEquals(1, result.getRating());
        }

        @Test
        @DisplayName("Update rating should preserve original user ID")
        void update_PreservesOriginalUserId() {
            // Given
            Long ratingId = 1L;
            CourseRating newRatingData = new CourseRating();
            setUserId(newRatingData, 999L); // Different user ID
            newRatingData.setRating(5);

            when(courseRatingRepository.findById(ratingId)).thenReturn(Optional.of(testRating));

            // When
            CourseRating result = crudService.update(ratingId, newRatingData);

            // Then
            assertEquals(100L, result.getUserId()); // Original user ID preserved
        }

        @Test
        @DisplayName("Find by ID with null should return empty optional")
        void findById_NullId_ReturnsEmpty() {
            // Given
            when(courseRatingRepository.findById(null)).thenReturn(Optional.empty());

            // When
            Optional<CourseRating> result = crudService.findById(null);

            // Then
            assertFalse(result.isPresent());
        }
    }

    private void setUserId(CourseRating rating, Long userId) {
        try {
            Field userIdField = CourseRating.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(rating, userId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
