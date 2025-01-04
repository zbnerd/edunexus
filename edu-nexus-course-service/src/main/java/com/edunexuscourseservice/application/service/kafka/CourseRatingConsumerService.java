package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.adapter.out.persistence.repository.CourseRatingRedisRepository;
import com.edunexuscourseservice.application.saga.event.CourseRatingAddEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingDeleteEvent;
import com.edunexuscourseservice.application.saga.event.CourseRatingUpdateEvent;
import com.edunexuscourseservice.domain.course.util.KafkaCourseRatingResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingConsumerService {

    private final ObjectMapper objectMapper;
    private final CourseRatingRedisRepository courseRatingRedisRepository;
    private final CourseRatingProducerService producerService;

    @KafkaListener(topics = "course-rating-add", groupId = "course-rating-group")
    public void courseRatingAdd(String message) throws JsonProcessingException {

        CourseRatingAddEvent event = objectMapper.readValue(message, CourseRatingAddEvent.class);

        try {
            courseRatingRedisRepository.cacheReviewRating(event.getCourseId(), event.getRating());
            producerService.sendRatingRedisAddingResponseEvent(
                    KafkaCourseRatingResponse.SUCCESS.getResponse(),
                    "CourseRatingAddEvent", event.getCourseId(),
                    event.getRating(),
                    event.getCourseRatingId());
        } catch (Exception e) {
            producerService.sendRatingRedisAddingResponseEvent(
                    KafkaCourseRatingResponse.FAIL.getResponse(),
                    "CourseRatingAddEvent", event.getCourseId(),
                    event.getRating(),
                    event.getCourseRatingId());
            throw new RuntimeException("Failed to parse Kafka message", e);
        }

    }

    @KafkaListener(topics = "course-rating-update", groupId = "course-rating-group")
    public void courseRatingUpdate(String message) throws JsonProcessingException {

        CourseRatingUpdateEvent event = objectMapper.readValue(message, CourseRatingUpdateEvent.class);

        try {
            courseRatingRedisRepository.updateReviewRating(event.getCourseId(), event.getOldRating(), event.getNewRating());
            producerService.sendRatingRedisUpdatingResponseEvent("success", "CourseRatingUpdateEvent", event.getCourseId(), event.getOldRating(), event.getNewRating(), event.getComment());
        } catch (Exception e) {
            producerService.sendRatingRedisUpdatingResponseEvent("fail", "CourseRatingUpdateEvent", event.getCourseId(), event.getOldRating(), event.getNewRating(), event.getComment());
            throw new RuntimeException("Failed to parse Kafka message", e);
        }
    }

    @KafkaListener(topics = "course-rating-delete", groupId = "course-rating-group")
    public void courseRatingDelete(String message) throws JsonProcessingException {
        CourseRatingDeleteEvent event = objectMapper.readValue(message, CourseRatingDeleteEvent.class);
        try {
            courseRatingRedisRepository.deleteReviewRating(event.getCourseId(), event.getOldRating());
            producerService.sendRatingRedisDeletingResponseEvent("success", "CourseRatingDeleteEvent", event.getCourseId(), event.getOldRating());
        } catch (Exception e) {
            producerService.sendRatingRedisDeletingResponseEvent("fail", "CourseRatingDeleteEvent", event.getCourseId(), event.getOldRating());
            throw new RuntimeException("Failed to parse Kafka message", e);
        }
    }

}
