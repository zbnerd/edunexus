package com.edunexuscourseservice.application.service;

import com.edunexuscourseservice.application.service.kafka.CourseRatingProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourseRatingCacheOrchestrator
 *
 * Test coverage:
 * - Happy path: All orchestration events (added, updated, deleted)
 * - Error cases: Producer failures
 * - Edge cases: Null values, boundary values
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CourseRatingCacheOrchestrator Tests")
class CourseRatingCacheOrchestratorTest {

    @Mock
    private CourseRatingProducerService producer;

    @InjectMocks
    private CourseRatingCacheOrchestrator orchestrator;

    @Nested
    @DisplayName("Rating Added Event Tests")
    class RatingAddedEventTests {

        @Test
        @DisplayName("On rating added should send event via producer")
        void onRatingAdded_ValidData_SendsEvent() {
            // Given
            Long courseId = 1L;
            int rating = 5;
            Long ratingId = 100L;

            // When
            orchestrator.onRatingAdded(courseId, rating, ratingId);

            // Then
            verify(producer).sendRatingAddedEvent(courseId, rating, ratingId);
            verify(producer, times(1)).sendRatingAddedEvent(anyLong(), anyInt(), anyLong());
        }

        @Test
        @DisplayName("On rating added with minimum rating should send event")
        void onRatingAdded_MinimumRating_SendsEvent() {
            // Given
            Long courseId = 1L;
            int rating = 1;
            Long ratingId = 50L;

            // When
            orchestrator.onRatingAdded(courseId, rating, ratingId);

            // Then
            verify(producer).sendRatingAddedEvent(courseId, rating, ratingId);
        }

        @Test
        @DisplayName("On rating added with maximum rating should send event")
        void onRatingAdded_MaximumRating_SendsEvent() {
            // Given
            Long courseId = 2L;
            int rating = 5;
            Long ratingId = 200L;

            // When
            orchestrator.onRatingAdded(courseId, rating, ratingId);

            // Then
            verify(producer).sendRatingAddedEvent(courseId, rating, ratingId);
        }

        @Test
        @DisplayName("On rating added with zero rating ID should send event")
        void onRatingAdded_ZeroRatingId_SendsEvent() {
            // Given
            Long courseId = 1L;
            int rating = 3;
            Long ratingId = 0L;

            // When
            orchestrator.onRatingAdded(courseId, rating, ratingId);

            // Then
            verify(producer).sendRatingAddedEvent(courseId, rating, ratingId);
        }
    }

    @Nested
    @DisplayName("Rating Updated Event Tests")
    class RatingUpdatedEventTests {

        @Test
        @DisplayName("On rating updated should send event via producer")
        void onRatingUpdated_ValidData_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 3;
            int newRating = 5;
            String comment = "Improved course";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
            verify(producer, times(1)).sendRatingUpdatedEvent(anyLong(), anyInt(), anyInt(), anyString());
        }

        @Test
        @DisplayName("On rating updated with null comment should send event")
        void onRatingUpdated_NullComment_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 4;
            int newRating = 5;
            String comment = null;

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }

        @Test
        @DisplayName("On rating updated with empty comment should send event")
        void onRatingUpdated_EmptyComment_SendsEvent() {
            // Given
            Long courseId = 2L;
            int oldRating = 2;
            int newRating = 4;
            String comment = "";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }

        @Test
        @DisplayName("On rating updated with same values should send event")
        void onRatingUpdated_SameValues_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 4;
            int newRating = 4;
            String comment = "No change";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }

        @Test
        @DisplayName("On rating updated from min to max rating should send event")
        void onRatingUpdated_MinToMax_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 1;
            int newRating = 5;
            String comment = "Much improved";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }

        @Test
        @DisplayName("On rating updated from max to min rating should send event")
        void onRatingUpdated_MaxToMin_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 5;
            int newRating = 1;
            String comment = "Disappointed";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }
    }

    @Nested
    @DisplayName("Rating Deleted Event Tests")
    class RatingDeletedEventTests {

        @Test
        @DisplayName("On rating deleted should send event via producer")
        void onRatingDeleted_ValidData_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 4;

            // When
            orchestrator.onRatingDeleted(courseId, oldRating);

            // Then
            verify(producer).sendRatingDeletedEvent(courseId, oldRating);
            verify(producer, times(1)).sendRatingDeletedEvent(anyLong(), anyInt());
        }

        @Test
        @DisplayName("On rating deleted with minimum rating should send event")
        void onRatingDeleted_MinimumRating_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 1;

            // When
            orchestrator.onRatingDeleted(courseId, oldRating);

            // Then
            verify(producer).sendRatingDeletedEvent(courseId, oldRating);
        }

        @Test
        @DisplayName("On rating deleted with maximum rating should send event")
        void onRatingDeleted_MaximumRating_SendsEvent() {
            // Given
            Long courseId = 2L;
            int oldRating = 5;

            // When
            orchestrator.onRatingDeleted(courseId, oldRating);

            // Then
            verify(producer).sendRatingDeletedEvent(courseId, oldRating);
        }

        @Test
        @DisplayName("On rating deleted with zero rating should send event")
        void onRatingDeleted_ZeroRating_SendsEvent() {
            // Given
            Long courseId = 1L;
            int oldRating = 0;

            // When
            orchestrator.onRatingDeleted(courseId, oldRating);

            // Then
            verify(producer).sendRatingDeletedEvent(courseId, oldRating);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Multiple events in sequence should all be sent")
        void multipleEvents_AllSent() {
            // Given
            Long courseId = 1L;

            // When
            orchestrator.onRatingAdded(courseId, 5, 100L);
            orchestrator.onRatingUpdated(courseId, 5, 4, "Updated");
            orchestrator.onRatingDeleted(courseId, 4);

            // Then
            verify(producer).sendRatingAddedEvent(courseId, 5, 100L);
            verify(producer).sendRatingUpdatedEvent(courseId, 5, 4, "Updated");
            verify(producer).sendRatingDeletedEvent(courseId, 4);
            verify(producer, times(1)).sendRatingAddedEvent(anyLong(), anyInt(), anyLong());
            verify(producer, times(1)).sendRatingUpdatedEvent(anyLong(), anyInt(), anyInt(), anyString());
            verify(producer, times(1)).sendRatingDeletedEvent(anyLong(), anyInt());
        }

        @Test
        @DisplayName("Events for different courses should all be sent")
        void differentCourses_AllEventsSent() {
            // Given
            Long courseId1 = 1L;
            Long courseId2 = 2L;
            Long courseId3 = 3L;

            // When
            orchestrator.onRatingAdded(courseId1, 5, 100L);
            orchestrator.onRatingAdded(courseId2, 4, 101L);
            orchestrator.onRatingAdded(courseId3, 3, 102L);

            // Then
            verify(producer).sendRatingAddedEvent(courseId1, 5, 100L);
            verify(producer).sendRatingAddedEvent(courseId2, 4, 101L);
            verify(producer).sendRatingAddedEvent(courseId3, 3, 102L);
            verify(producer, times(3)).sendRatingAddedEvent(anyLong(), anyInt(), anyLong());
        }

        @Test
        @DisplayName("Long comment in updated event should be sent")
        void longComment_SentSuccessfully() {
            // Given
            Long courseId = 1L;
            int oldRating = 3;
            int newRating = 5;
            String longComment = "This is a very long comment that might contain special characters and multiple sentences. It tests that the orchestrator properly handles comments of varying lengths.";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, longComment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, longComment);
        }

        @Test
        @DisplayName("Special characters in comment should be sent")
        void specialCharactersInComment_SentSuccessfully() {
            // Given
            Long courseId = 1L;
            int oldRating = 3;
            int newRating = 5;
            String comment = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

            // When
            orchestrator.onRatingUpdated(courseId, oldRating, newRating, comment);

            // Then
            verify(producer).sendRatingUpdatedEvent(courseId, oldRating, newRating, comment);
        }
    }

    @Nested
    @DisplayName("Fire-and-Forget Pattern Tests")
    class FireAndForgetPatternTests {

        @Test
        @DisplayName("Orchestrator should not handle producer exceptions")
        void onRatingAdded_ProducerThrowsException_PropagatesException() {
            // Given
            Long courseId = 1L;
            int rating = 5;
            Long ratingId = 100L;
            doThrow(new RuntimeException("Kafka connection failed")).when(producer)
                    .sendRatingAddedEvent(anyLong(), anyInt(), anyLong());

            // When & Then
            assertThrows(RuntimeException.class, () -> orchestrator.onRatingAdded(courseId, rating, ratingId));
        }
    }
}
