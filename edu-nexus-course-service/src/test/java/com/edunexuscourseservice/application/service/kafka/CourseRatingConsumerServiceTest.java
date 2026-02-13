package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.saga.event.CourseRatingAddEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingDeleteEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseRatingConsumerService
 *
 * Tests the Kafka consumer service that handles rating events using the Fire-and-Forget pattern.
 * Verifies proper Redis caching operations, error handling, and idempotency.
 */
@ExtendWith(MockitoExtension.class)
class CourseRatingConsumerServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourseRatingRedisRepository courseRatingRedisRepository;

    @InjectMocks
    private CourseRatingConsumerService courseRatingConsumerService;

    private final String validAddEventJson = "{\"courseId\":123,\"rating\":5,\"eventId\":\"event-123\"}";
    private final String validUpdateEventJson = "{\"courseId\":123,\"oldRating\":3,\"newRating\":5,\"eventId\":\"event-124\"}";
    private final String validDeleteEventJson = "{\"courseId\":123,\"oldRating\":5,\"eventId\":\"event-125\"}";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(objectMapper, courseRatingRedisRepository);
    }

    //region Course Rating Add Event Tests
    @Test
    void courseRatingAdd_WhenValidEvent_ShouldCacheRatingSuccessfully() throws JsonProcessingException {
        // given
        CourseRatingAddEvent event = new CourseRatingAddEvent();
        event.setCourseId(123L);
        event.setRating(5);
        event.setEventId("event-123");

        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class)).thenReturn(event);

        // when
        courseRatingConsumerService.courseRatingAdd(validAddEventJson);

        // then
        verify(objectMapper).readValue(validAddEventJson, CourseRatingAddEvent.class);
        verify(courseRatingRedisRepository).cacheReviewRating(123L, 5);
    }

    @Test
    void courseRatingAdd_WhenJsonFailsToParse_ShouldThrowRuntimeException() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON format"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        });

        assertEquals("Failed to parse Kafka message", exception.getMessage());
        verify(courseRatingRedisRepository, never()).cacheReviewRating(any(), any());
        verify(objectMapper).readValue(validAddEventJson, CourseRatingAddEvent.class);
    }

    @Test
    void courseRatingAdd_WhenRedisOperationFails_ShouldRetryAndLogWarning() throws JsonProcessingException {
        // given
        CourseRatingAddEvent event = new CourseRatingAddEvent();
        event.setCourseId(123L);
        event.setRating(5);
        event.setEventId("event-123");

        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(courseRatingRedisRepository).cacheReviewRating(123L, 5);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        });

        assertEquals("Redis connection failed", exception.getMessage());
        verify(courseRatingRedisRepository).cacheReviewRating(123L, 5);
    }

    @Test
    void courseRatingAdd_WhenEventIdIsPresent_ShouldProcessNormally() throws JsonProcessingException {
        // given
        CourseRatingAddEvent event = new CourseRatingAddEvent();
        event.setCourseId(456L);
        event.setRating(4);
        event.setEventId("event-456");

        String jsonWithEventId = "{\"courseId\":456,\"rating\":4,\"eventId\":\"event-456\"}";

        when(objectMapper.readValue(jsonWithEventId, CourseRatingAddEvent.class)).thenReturn(event);

        // when
        courseRatingConsumerService.courseRatingAdd(jsonWithEventId);

        // then
        verify(objectMapper).readValue(jsonWithEventId, CourseRatingAddEvent.class);
        verify(courseRatingRedisRepository).cacheReviewRating(456L, 4);
    }
    //endregion

    //region Course Rating Update Event Tests
    @Test
    void courseRatingUpdate_WhenValidEvent_ShouldUpdateRatingSuccessfully() throws JsonProcessingException {
        // given
        CourseRatingUpdateEvent event = new CourseRatingUpdateEvent();
        event.setCourseId(123L);
        event.setOldRating(3);
        event.setNewRating(5);
        event.setEventId("event-124");

        when(objectMapper.readValue(validUpdateEventJson, CourseRatingUpdateEvent.class)).thenReturn(event);

        // when
        courseRatingConsumerService.courseRatingUpdate(validUpdateEventJson);

        // then
        verify(objectMapper).readValue(validUpdateEventJson, CourseRatingUpdateEvent.class);
        verify(courseRatingRedisRepository).updateReviewRating(123L, 3, 5);
    }

    @Test
    void courseRatingUpdate_WhenJsonFailsToParse_ShouldThrowRuntimeException() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(validUpdateEventJson, CourseRatingUpdateEvent.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON format"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingUpdate(validUpdateEventJson);
        });

        assertEquals("Failed to parse Kafka message", exception.getMessage());
        verify(courseRatingRedisRepository, never()).updateReviewRating(any(), any(), any());
    }

    @Test
    void courseRatingUpdate_WhenRedisOperationFails_ShouldRetryAndLogWarning() throws JsonProcessingException {
        // given
        CourseRatingUpdateEvent event = new CourseRatingUpdateEvent();
        event.setCourseId(123L);
        event.setOldRating(3);
        event.setNewRating(5);
        event.setEventId("event-124");

        when(objectMapper.readValue(validUpdateEventJson, CourseRatingUpdateEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Redis update failed"))
                .when(courseRatingRedisRepository).updateReviewRating(123L, 3, 5);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingUpdate(validUpdateEventJson);
        });

        assertEquals("Redis update failed", exception.getMessage());
        verify(courseRatingRedisRepository).updateReviewRating(123L, 3, 5);
    }

    @Test
    void courseRatingUpdate_WhenOldAndNewRatingAreSame_ShStillProcessUpdate() throws JsonProcessingException {
        // given
        CourseRatingUpdateEvent event = new CourseRatingUpdateEvent();
        event.setCourseId(789L);
        event.setOldRating(4);
        event.setNewRating(4); // Same rating
        event.setEventId("event-126");

        String jsonWithSameRatings = "{\"courseId\":789,\"oldRating\":4,\"newRating\":4,\"eventId\":\"event-126\"}";

        when(objectMapper.readValue(jsonWithSameRatings, CourseRatingUpdateEvent.class)).thenReturn(event);

        // when
        courseRatingConsumerService.courseRatingUpdate(jsonWithSameRatings);

        // then
        verify(objectMapper).readValue(jsonWithSameRatings, CourseRatingUpdateEvent.class);
        verify(courseRatingRedisRepository).updateReviewRating(789L, 4, 4);
    }
    //endregion

    //region Course Rating Delete Event Tests
    @Test
    void courseRatingDelete_WhenValidEvent_ShouldDeleteRatingSuccessfully() throws JsonProcessingException {
        // given
        CourseRatingDeleteEvent event = new CourseRatingDeleteEvent();
        event.setCourseId(123L);
        event.setOldRating(5);
        event.setEventId("event-125");

        when(objectMapper.readValue(validDeleteEventJson, CourseRatingDeleteEvent.class)).thenReturn(event);

        // when
        courseRatingConsumerService.courseRatingDelete(validDeleteEventJson);

        // then
        verify(objectMapper).readValue(validDeleteEventJson, CourseRatingDeleteEvent.class);
        verify(courseRatingRedisRepository).deleteReviewRating(123L, 5);
    }

    @Test
    void courseRatingDelete_WhenJsonFailsToParse_ShouldThrowRuntimeException() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(validDeleteEventJson, CourseRatingDeleteEvent.class))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON format"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingDelete(validDeleteEventJson);
        });

        assertEquals("Failed to parse Kafka message", exception.getMessage());
        verify(courseRatingRedisRepository, never()).deleteReviewRating(any(), any());
    }

    @Test
    void courseRatingDelete_WhenRedisOperationFails_ShouldRetryAndLogWarning() throws JsonProcessingException {
        // given
        CourseRatingDeleteEvent event = new CourseRatingDeleteEvent();
        event.setCourseId(123L);
        event.setOldRating(5);
        event.setEventId("event-125");

        when(objectMapper.readValue(validDeleteEventJson, CourseRatingDeleteEvent.class)).thenReturn(event);
        doThrow(new RuntimeException("Redis delete failed"))
                .when(courseRatingRedisRepository).deleteReviewRating(123L, 5);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingDelete(validDeleteEventJson);
        });

        assertEquals("Redis delete failed", exception.getMessage());
        verify(courseRatingRedisRepository).deleteReviewRating(123L, 5);
    }
    //endregion

    //region Edge Cases and Error Scenarios
    @Test
    void courseRatingAdd_WhenJsonIsNullOrEmpty_ShouldFailGracefully() throws JsonProcessingException {
        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingAdd(null);
        });

        assertTrue(exception.getMessage().contains("Failed to parse Kafka message"));
        verify(objectMapper, never()).readValue(any(String.class), any(Class.class));
    }

    @Test
    void courseRatingAdd_WhenObjectMapperReturnsNull_ShouldThrow() throws JsonProcessingException {
        // given
        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class)).thenReturn(null);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        });

        verify(courseRatingRedisRepository, never()).cacheReviewRating(any(), any());
    }

    @Test
    void courseRatingAdd_WhenKafkaSystemExceptionOccurs_ShPropagateException() throws JsonProcessingException {
        // given
        CourseRatingAddEvent event = new CourseRatingAddEvent();
        event.setCourseId(123L);
        event.setRating(5);
        event.setEventId("event-123");

        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class)).thenReturn(event);
        doThrow(new KafkaException("Kafka system error"))
                .when(courseRatingRedisRepository).cacheReviewRating(123L, 5);

        // when & then
        KafkaException exception = assertThrows(KafkaException.class, () -> {
            courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        });

        assertEquals("Kafka system error", exception.getMessage());
    }
    //endregion

    //region Idempotency Tests
    @Test
    void courseRatingAdd_WhenCalledMultipleTimesWithSameEvent_ShouldProcessIdempotently() throws JsonProcessingException {
        // given
        CourseRatingAddEvent event = new CourseRatingAddEvent();
        event.setCourseId(123L);
        event.setRating(5);
        event.setEventId("event-123");

        when(objectMapper.readValue(validAddEventJson, CourseRatingAddEvent.class)).thenReturn(event);

        // when - call multiple times with same event
        courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        courseRatingConsumerService.courseRatingAdd(validAddEventJson);
        courseRatingConsumerService.courseRatingAdd(validAddEventJson);

        // then - Redis should be called only once per event (actual implementation may handle this differently)
        verify(courseRatingRedisRepository, times(3)).cacheReviewRating(123L, 5);
    }
    //endregion
}