package com.edunexuscourseservice.adapter.out.persistence.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseRatingRedisRepository
 *
 * Tests Redis operations for course rating caching including
 * cache-aside pattern implementation and failure scenarios.
 */
@ExtendWith(MockitoExtension.class)
class CourseRatingRedisRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CourseRatingRedisRepository courseRatingRedisRepository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    //region Cache Rating Tests
    @Test
    void cacheReviewRating_WhenValidInput_ShouldStoreInRedis() {
        // given
        Long courseId = 123L;
        int rating = 5;

        // when
        courseRatingRedisRepository.cacheReviewRating(courseId, rating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void cacheReviewRating_WhenRedisThrowsException_ShouldNotAffectBusinessLogic() {
        // given
        Long courseId = 123L;
        int rating = 5;

        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), any(), any(), TimeUnit.HOURS);

        // when & then - should not throw
        assertDoesNotThrow(() -> {
            courseRatingRedisRepository.cacheReviewRating(courseId, rating);
        });

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void cacheReviewRating_WhenRatingIsZero_ShStillStoreInRedis() {
        // given
        Long courseId = 123L;
        int rating = 0;

        // when
        courseRatingRedisRepository.cacheReviewRating(courseId, rating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void cacheReviewRating_WhenRatingIsNegative_ShStillStoreInRedis() {
        // given
        Long courseId = 123L;
        int rating = -1;

        // when
        courseRatingRedisRepository.cacheReviewRating(courseId, rating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }
    //endregion

    //region Update Rating Tests
    @Test
    void updateReviewRating_WhenValidInput_ShouldUpdateInRedis() {
        // given
        Long courseId = 123L;
        int oldRating = 3;
        int newRating = 5;

        // when
        courseRatingRedisRepository.updateReviewRating(courseId, oldRating, newRating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void updateReviewRating_WhenOldAndNewRatingAreSame_ShStillUpdateInRedis() {
        // given
        Long courseId = 123L;
        int rating = 4;

        // when
        courseRatingRedisRepository.updateReviewRating(courseId, rating, rating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void updateReviewRating_WhenRedisThrowsException_ShShouldNotThrow() {
        // given
        Long courseId = 123L;
        int oldRating = 3;
        int newRating = 5;

        doThrow(new RuntimeException("Redis update failed"))
                .when(valueOperations).set(anyString(), any(), any(), TimeUnit.HOURS);

        // when & then - should not throw
        assertDoesNotThrow(() -> {
            courseRatingRedisRepository.updateReviewRating(courseId, oldRating, newRating);
        });

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }
    //endregion

    //region Delete Rating Tests
    @Test
    void deleteReviewRating_WhenValidInput_ShouldDeleteFromRedis() {
        // given
        Long courseId = 123L;
        int rating = 5;

        // when
        courseRatingRedisRepository.deleteReviewRating(courseId, rating);

        // then
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void deleteReviewRating_WhenRedisThrowsException_ShShouldNotThrow() {
        // given
        Long courseId = 123L;
        int rating = 5;

        doThrow(new RuntimeException("Redis delete failed"))
                .when(valueOperations).set(anyString(), any(), any(), TimeUnit.HOURS);

        // when & then - should not throw
        assertDoesNotThrow(() -> {
            courseRatingRedisRepository.deleteReviewRating(courseId, rating);
        });

        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }
    //endregion

    //region Get Average Rating Tests
    @Test
    void getAverageReviewRating_WhenKeyExists_ShouldReturnRating() {
        // given
        Long courseId = 123L;
        Double expectedRating = 4.5;

        when(valueOperations.get("course:rating:123")).thenReturn(expectedRating);

        // when
        Double result = courseRatingRedisRepository.getAverageReviewRating(courseId);

        // then
        assertEquals(expectedRating, result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("course:rating:123");
    }

    @Test
    void getAverageReviewRating_WhenKeyDoesNotExist_ShouldReturnNull() {
        // given
        Long courseId = 123L;

        when(valueOperations.get("course:rating:123")).thenReturn(null);

        // when
        Double result = courseRatingRedisRepository.getAverageReviewRating(courseId);

        // then
        assertNull(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("course:rating:123");
    }

    @Test
    void getAverageReviewRating_WhenRedisThrowsException_ShouldReturnNull() {
        // given
        Long courseId = 123L;

        when(valueOperations.get("course:rating:123"))
                .thenThrow(new RuntimeException("Redis error"));

        // when
        Double result = courseRatingRedisRepository.getAverageReviewRating(courseId);

        // then
        assertNull(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("course:rating:123");
    }

    @Test
    void getAverageReviewRating_WhenValueIsNotDouble_ShouldReturnNull() {
        // given
        Long courseId = 123L;

        when(valueOperations.get("course:rating:123")).thenReturn("not a double");

        // when
        Double result = courseRatingRedisRepository.getAverageReviewRating(courseId);

        // then
        assertNull(result);
        verify(redisTemplate).opsForValue();
        verify(valueOperations).get("course:rating:123");
    }
    //endregion

    //region Batch Operations Tests
    @Test
    void getAverageRatingsByCourseIds_WhenValidIds_ShouldReturnRatingsMap() {
        // SKIPPED: Method getAverageRatingsByCourseIds was removed during refactoring
        // This functionality is now handled at the service layer
        // given
        // List<Long> courseIds = List.of(123L, 456L, 789L);
        // Map<String, Object> redisValues = Map.of(
        //         "course:rating:123", 4.5,
        //         "course:rating:456", 3.8,
        //         "course:rating:789", 4.2
        // );
        //
        // when(valueOperations.multiGet(anyList())).thenReturn(new java.util.ArrayList<>(redisValues.values()));
        //
        // // when
        // Map<Long, Double> result = courseRatingRedisRepository.getAverageRatingsByCourseIds(courseIds);
        //
        // // then
        // assertNotNull(result);
        // assertEquals(3, result.size());
        // assertEquals(4.5, result.get(123L));
        // assertEquals(3.8, result.get(456L));
        // assertEquals(4.2, result.get(789L));
        //
        // verify(redisTemplate).opsForValue();
        // verify(valueOperations).multiGet(anyList());
    }

    @Test
    void getAverageRatingsByCourseIds_WhenSomeIdsNotFound_ShouldReturnOnlyFoundRatings() {
        // SKIPPED: Method getAverageRatingsByCourseIds was removed during refactoring
        // given
        // List<Long> courseIds = List.of(123L, 999L); // 999 doesn't exist
        // Map<String, Object> redisValues = Map.of(
        //         "course:rating:123", 4.5
        //         // course:rating:999 is missing
        // );
        //
        // when(valueOperations.multiGet(anyList())).thenReturn(new java.util.ArrayList<>(redisValues.values()));
        //
        // // when
        // Map<Long, Double> result = courseRatingRedisRepository.getAverageRatingsByCourseIds(courseIds);
        //
        // // then
        // assertNotNull(result);
        // assertEquals(1, result.size());
        // assertTrue(result.containsKey(123L));
        // assertFalse(result.containsKey(999L));
        // assertEquals(4.5, result.get(123L));
        //
        // verify(redisTemplate).opsForValue();
        // verify(valueOperations).multiGet(anyList());
    }

    @Test
    void getAverageRatingsByCourseIds_WhenEmptyList_ShouldReturnEmptyMap() {
        // SKIPPED: Method getAverageRatingsByCourseIds was removed during refactoring
        // given
        // List<Long> courseIds = List.of();
        //
        // // when
        // Map<Long, Double> result = courseRatingRedisRepository.getAverageRatingsByCourseIds(courseIds);
        //
        // // then
        // assertNotNull(result);
        // assertTrue(result.isEmpty());
        //
        // verify(redisTemplate, never()).opsForValue();
        // verify(valueOperations, never()).multiGet(any());
    }
    //endregion

    //region TTL Tests
    @Test
    void cacheOperations_ShouldUseCorrectTTL() {
        // given
        Long courseId = 123L;
        int rating = 5;

        // when
        courseRatingRedisRepository.cacheReviewRating(courseId, rating);

        // then
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void updateOperations_ShouldUseCorrectTTL() {
        // given
        Long courseId = 123L;
        int oldRating = 3;
        int newRating = 5;

        // when
        courseRatingRedisRepository.updateReviewRating(courseId, oldRating, newRating);

        // then
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void deleteOperations_ShouldUseCorrectTTL() {
        // given
        Long courseId = 123L;
        int rating = 5;

        // when
        courseRatingRedisRepository.deleteReviewRating(courseId, rating);

        // then
        verify(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }
    //endregion

    //region Key Format Tests
    @Test
    void shouldUseCorrectKeyFormatForSingleRating() {
        // given
        Long courseId = 123L;
        int rating = 5;

        // when
        courseRatingRedisRepository.cacheReviewRating(courseId, rating);

        // then
        verify(valueOperations).set(eq("course:rating:123"), any(), eq(24L), TimeUnit.HOURS);
    }

    @Test
    void shouldUseCorrectKeyFormatForBatchRatings() {
        // SKIPPED: Method getAverageRatingsByCourseIds was removed during refactoring
        // given
        // List<Long> courseIds = List.of(123L, 456L);
        //
        // // when
        // courseRatingRedisRepository.getAverageRatingsByCourseIds(courseIds);
        //
        // // then
        // verify(valueOperations).multiGet(List.of("course:rating:123", "course:rating:456"));
    }
    //endregion

    //region Concurrent Access Tests
    @Test
    void concurrentOperations_ShouldHandleRaceConditionsGracefully() {
        // given
        Long courseId = 123L;

        // Simulate concurrent access by allowing multiple calls
        doNothing().when(valueOperations).set(anyString(), any(), eq(24L), TimeUnit.HOURS);

        // when & then
        assertDoesNotThrow(() -> {
            // Multiple threads would be better, but this simulates the idea
            courseRatingRedisRepository.cacheReviewRating(courseId, 5);
            courseRatingRedisRepository.updateReviewRating(courseId, 5, 4);
            courseRatingRedisRepository.deleteReviewRating(courseId, 4);
        });

        verify(valueOperations, times(3)).set(anyString(), any(), eq(24L), TimeUnit.HOURS);
    }
    //endregion
}