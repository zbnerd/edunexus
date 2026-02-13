package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.saga.event.CourseRatingAddEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingDeleteEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


/**
 * Kafka Consumer for Course Rating Events
 *
 * Implements Fire-and-Forget pattern per ADR-000:
 * - No response events sent
 * - Cache failures are logged but don't trigger compensating transactions
 * - Idempotent operations (increment/decrement are naturally idempotent-safe with proper design)
 * - DLT (Dead Letter Topic) for failed events
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingConsumerService {

    private final ObjectMapper objectMapper;
    private final CourseRatingRedisRepository courseRatingRedisRepository;

    @KafkaListener(topics = "course-rating-add", groupId = "course-rating-group")
    public void courseRatingAdd(String message) {
        try {
            CourseRatingAddEvent event = objectMapper.readValue(message, CourseRatingAddEvent.class);
            courseRatingRedisRepository.cacheReviewRating(event.getCourseId(), event.getRating());
            log.debug("Processed rating add event for course {}", event.getCourseId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse rating add message: {}", message, e);
            throw new RuntimeException("Failed to parse Kafka message", e);
        } catch (Exception e) {
            log.warn("Failed to process rating add event (will retry): {}", message, e);
            throw e; // Let Kafka retry mechanism handle it
        }
    }

    @KafkaListener(topics = "course-rating-update", groupId = "course-rating-group")
    public void courseRatingUpdate(String message) {
        try {
            CourseRatingUpdateEvent event = objectMapper.readValue(message, CourseRatingUpdateEvent.class);
            courseRatingRedisRepository.updateReviewRating(
                    event.getCourseId(),
                    event.getOldRating(),
                    event.getNewRating()
            );
            log.debug("Processed rating update event for course {}", event.getCourseId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse rating update message: {}", message, e);
            throw new RuntimeException("Failed to parse Kafka message", e);
        } catch (Exception e) {
            log.warn("Failed to process rating update event (will retry): {}", message, e);
            throw e; // Let Kafka retry mechanism handle it
        }
    }

    @KafkaListener(topics = "course-rating-delete", groupId = "course-rating-group")
    public void courseRatingDelete(String message) {
        try {
            CourseRatingDeleteEvent event = objectMapper.readValue(message, CourseRatingDeleteEvent.class);
            courseRatingRedisRepository.deleteReviewRating(event.getCourseId(), event.getOldRating());
            log.debug("Processed rating delete event for course {}", event.getCourseId());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse rating delete message: {}", message, e);
            throw new RuntimeException("Failed to parse Kafka message", e);
        } catch (Exception e) {
            log.warn("Failed to process rating delete event (will retry): {}", message, e);
            throw e; // Let Kafka retry mechanism handle it
        }
    }

}
