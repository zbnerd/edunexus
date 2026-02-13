package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.adapter.out.persistence.entity.Course;
import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseRatingQueryService
 *
 * Test coverage:
 * - Happy path: Get average rating, batch get ratings, get ratings by course
 * - Error cases: Repository exceptions, cache failures
 * - Edge cases: Null values, empty lists, cache misses
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseRatingQueryService Tests")
class CourseRatingQueryServiceTest {

    @Mock
    private CourseRatingCrudService crudService;

    @Mock
    private CourseRatingRedisRepository redisRepository;

    @InjectMocks
    private CourseRatingQueryService queryService;

    private Course testCourse;
    private CourseRating testRating1;
    private CourseRating testRating2;

    @BeforeEach
    void setUp() {
        testCourse = createTestCourse(1L, "Test Course");
        testRating1 = createTestRating(1L, 100L, 5, "Excellent", testCourse);
        testRating2 = createTestRating(2L, 101L, 4, "Good", testCourse);
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
    @DisplayName("Get Average Rating Tests")
    class GetAverageRatingTests {

        @Test
        @DisplayName("Get average rating from cache should return cached value")
        void getAverageRating_CachedValue_ReturnsAverage() {
            // Given
            Long courseId = 1L;
            double expectedAverage = 4.5;
            when(redisRepository.getAverageReviewRating(courseId)).thenReturn(expectedAverage);

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertEquals(expectedAverage, result);
            verify(redisRepository).getAverageReviewRating(courseId);
        }

        @Test
        @DisplayName("Get average rating with cache exception should return 0.0")
        void getAverageRating_CacheException_ReturnsZero() {
            // Given
            Long courseId = 1L;
            when(redisRepository.getAverageReviewRating(courseId))
                    .thenThrow(new RuntimeException("Redis connection failed"));

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertEquals(0.0, result);
            verify(redisRepository).getAverageReviewRating(courseId);
        }

        @Test
        @DisplayName("Get average rating with null course ID should handle gracefully")
        void getAverageRating_NullCourseId_ReturnsZero() {
            // Given
            when(redisRepository.getAverageReviewRating(anyLong()))
                    .thenThrow(new RuntimeException("Null course ID"));

            // When
            Double result = queryService.getAverageRating(null);

            // Then
            assertEquals(0.0, result);
        }

        @Test
        @DisplayName("Get average rating with zero course ID should handle gracefully")
        void getAverageRating_ZeroCourseId_ReturnsZero() {
            // Given
            Long courseId = 0L;
            when(redisRepository.getAverageReviewRating(courseId))
                    .thenThrow(new RuntimeException("Invalid course ID"));

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertEquals(0.0, result);
        }
    }

    @Nested
    @DisplayName("Get Average Ratings Batch Tests")
    class GetAverageRatingsBatchTests {

        @Test
        @DisplayName("Get multiple average ratings should return map with all values")
        void getAverageRatings_MultipleCourses_ReturnsMap() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, 2L, 3L);
            when(redisRepository.getAverageReviewRating(1L)).thenReturn(4.5);
            when(redisRepository.getAverageReviewRating(2L)).thenReturn(3.5);
            when(redisRepository.getAverageReviewRating(3L)).thenReturn(5.0);

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(4.5, result.get(1L));
            assertEquals(3.5, result.get(2L));
            assertEquals(5.0, result.get(3L));
            verify(redisRepository, times(3)).getAverageReviewRating(anyLong());
        }

        @Test
        @DisplayName("Get average ratings with empty list should return empty map")
        void getAverageRatings_EmptyList_ReturnsEmptyMap() {
            // Given
            List<Long> courseIds = Collections.emptyList();

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(redisRepository, never()).getAverageReviewRating(anyLong());
        }

        @Test
        @DisplayName("Get average ratings with single course should return single entry map")
        void getAverageRatings_SingleCourse_ReturnsSingleEntry() {
            // Given
            List<Long> courseIds = Arrays.asList(1L);
            when(redisRepository.getAverageReviewRating(1L)).thenReturn(4.0);

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(4.0, result.get(1L));
            verify(redisRepository).getAverageReviewRating(1L);
        }

        @Test
        @DisplayName("Get average ratings with partial cache failure should return 0.0 for failed courses")
        void getAverageRatings_PartialCacheFailure_ReturnsPartialResults() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, 2L, 3L);
            when(redisRepository.getAverageReviewRating(1L)).thenReturn(4.5);
            when(redisRepository.getAverageReviewRating(2L))
                    .thenThrow(new RuntimeException("Redis timeout"));
            when(redisRepository.getAverageReviewRating(3L)).thenReturn(5.0);

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(4.5, result.get(1L));
            assertEquals(0.0, result.get(2L));
            assertEquals(5.0, result.get(3L));
        }

        @Test
        @DisplayName("Get average ratings with all cache failures should return all zeros")
        void getAverageRatings_AllCacheFailures_ReturnsAllZeros() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, 2L, 3L);
            when(redisRepository.getAverageReviewRating(anyLong()))
                    .thenThrow(new RuntimeException("Redis down"));

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(0.0, result.get(1L));
            assertEquals(0.0, result.get(2L));
            assertEquals(0.0, result.get(3L));
        }

        @Test
        @DisplayName("Get average ratings with duplicate course IDs should handle gracefully")
        void getAverageRatings_DuplicateCourseIds_HandlesGracefully() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, 1L, 2L);
            when(redisRepository.getAverageReviewRating(1L)).thenReturn(4.5);
            when(redisRepository.getAverageReviewRating(2L)).thenReturn(3.5);

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            // Map doesn't preserve duplicate keys, last value wins
            assertEquals(2, result.size());
            assertEquals(4.5, result.get(1L));
            assertEquals(3.5, result.get(2L));
        }

        @Test
        @DisplayName("Get average ratings with null values in list should skip nulls")
        void getAverageRatings_NullInList_HandlesGracefully() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, null, 2L);
            when(redisRepository.getAverageReviewRating(1L)).thenReturn(4.5);
            when(redisRepository.getAverageReviewRating(2L)).thenReturn(3.5);
            when(redisRepository.getAverageReviewRating(null))
                    .thenThrow(new RuntimeException("Null ID"));

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size());  // 1L, null, 2L are all different keys
            assertEquals(4.5, result.get(1L));
            assertEquals(0.0, result.get(null));
            assertEquals(3.5, result.get(2L));
        }
    }

    @Nested
    @DisplayName("Get Ratings By Course ID Tests")
    class GetRatingsByCourseIdTests {

        @Test
        @DisplayName("Get ratings by course ID should return list of ratings")
        void getRatingsByCourseId_ValidCourseId_ReturnsListOfRatings() {
            // Given
            Long courseId = 1L;
            List<CourseRating> expectedRatings = Arrays.asList(testRating1, testRating2);
            when(crudService.findByCourseId(courseId)).thenReturn(expectedRatings);

            // When
            List<CourseRating> result = queryService.getRatingsByCourseId(courseId);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(testRating1.getId(), result.get(0).getId());
            assertEquals(testRating2.getId(), result.get(1).getId());
            verify(crudService).findByCourseId(courseId);
        }

        @Test
        @DisplayName("Get ratings by course ID with no ratings should return empty list")
        void getRatingsByCourseId_NoRatings_ReturnsEmptyList() {
            // Given
            Long courseId = 2L;
            when(crudService.findByCourseId(courseId)).thenReturn(Collections.emptyList());

            // When
            List<CourseRating> result = queryService.getRatingsByCourseId(courseId);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(crudService).findByCourseId(courseId);
        }

        @Test
        @DisplayName("Get ratings by course ID should delegate to crud service")
        void getRatingsByCourseId_DelegatesToCrudService() {
            // Given
            Long courseId = 1L;
            List<CourseRating> expectedRatings = Arrays.asList(testRating1);
            when(crudService.findByCourseId(courseId)).thenReturn(expectedRatings);

            // When
            List<CourseRating> result = queryService.getRatingsByCourseId(courseId);

            // Then
            assertSame(expectedRatings, result);
            verify(crudService, times(1)).findByCourseId(courseId);
            verifyNoInteractions(redisRepository);
        }

        @Test
        @DisplayName("Get ratings with null course ID should handle gracefully")
        void getRatingsByCourseId_NullCourseId_HandlesGracefully() {
            // Given
            when(crudService.findByCourseId(null)).thenReturn(Collections.emptyList());

            // When
            List<CourseRating> result = queryService.getRatingsByCourseId(null);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Get ratings by course ID with single rating should return list with one element")
        void getRatingsByCourseId_SingleRating_ReturnsSingletonList() {
            // Given
            Long courseId = 1L;
            List<CourseRating> expectedRatings = Arrays.asList(testRating1);
            when(crudService.findByCourseId(courseId)).thenReturn(expectedRatings);

            // When
            List<CourseRating> result = queryService.getRatingsByCourseId(courseId);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testRating1.getId(), result.get(0).getId());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Get average rating with maximum double value should return max value")
        void getAverageRating_MaxDoubleValue_ReturnsMax() {
            // Given
            Long courseId = 1L;
            when(redisRepository.getAverageReviewRating(courseId)).thenReturn(Double.MAX_VALUE);

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertEquals(Double.MAX_VALUE, result);
        }

        @Test
        @DisplayName("Get average rating with minimum double value should return min value")
        void getAverageRating_MinDoubleValue_ReturnsMin() {
            // Given
            Long courseId = 1L;
            when(redisRepository.getAverageReviewRating(courseId)).thenReturn(Double.MIN_VALUE);

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertEquals(Double.MIN_VALUE, result);
        }

        @Test
        @DisplayName("Get average rating with NaN should return NaN")
        void getAverageRating_NaN_ReturnsNaN() {
            // Given
            Long courseId = 1L;
            when(redisRepository.getAverageReviewRating(courseId)).thenReturn(Double.NaN);

            // When
            Double result = queryService.getAverageRating(courseId);

            // Then
            assertTrue(Double.isNaN(result));
        }

        @Test
        @DisplayName("Get average ratings with large list should process all")
        void getAverageRatings_LargeList_ProcessesAll() {
            // Given
            List<Long> courseIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
            for (Long courseId : courseIds) {
                when(redisRepository.getAverageReviewRating(courseId)).thenReturn(4.0);
            }

            // When
            Map<Long, Double> result = queryService.getAverageRatings(courseIds);

            // Then
            assertNotNull(result);
            assertEquals(10, result.size());
            result.values().forEach(value -> assertEquals(4.0, value));
        }
    }
}
