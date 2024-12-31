package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.saga.event.CourseRatingAddEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingDeleteEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingUpdateEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class CourseRatingConsumerService {

    private final ObjectMapper objectMapper;
    private final CourseRatingRedisRepository courseRatingRedisRepository;

    @KafkaListener(topics = "course-rating-add", groupId = "course-rating-group")
    public void courseRatingAdd(String message) {
        processKafkaMessage(message, CourseRatingAddEvent.class,
                event -> courseRatingRedisRepository.cacheReviewRating(event.getCourseId(), event.getRating()));
    }

    @KafkaListener(topics = "course-rating-update", groupId = "course-rating-group")
    public void courseRatingUpdate(String message) {
        processKafkaMessage(message, CourseRatingUpdateEvent.class,
                event -> courseRatingRedisRepository.updateReviewRating(event.getCourseId(), event.getOldRating(), event.getNewRating()));
    }

    @KafkaListener(topics = "course-rating-delete", groupId = "course-rating-group")
    public void courseRatingDelete(String message) {
        processKafkaMessage(message, CourseRatingDeleteEvent.class,
                event -> courseRatingRedisRepository.deleteReviewRating(event.getCourseId(), event.getOldRating()));
    }

    private <T> void processKafkaMessage(String message, Class<T> eventType, Consumer<T> eventProcessor) {
        try {
            T event = objectMapper.readValue(message, eventType);
            eventProcessor.accept(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse Kafka message", e);
        }
    }
}
