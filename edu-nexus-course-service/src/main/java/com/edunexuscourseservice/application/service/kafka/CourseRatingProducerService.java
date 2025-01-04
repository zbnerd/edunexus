package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.domain.course.util.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendRatingAddedEvent(Long courseId, int rating, Long courseRatingId) {
        String message = String.format("{\"courseId\": %d, \"rating\": %d, \"courseRatingId\": %d}", courseId, rating, courseRatingId);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_ADD.getTopic(), message);
    }

    public void sendRatingUpdatedEvent(Long courseId, int oldRating, int newRating, String comment) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d, \"newRating\": %d, \"comment\": %s}", courseId, oldRating, newRating, comment);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_UPDATE.getTopic(), message);
    }

    public void sendRatingDeletedEvent(Long courseId, int oldRating) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d}", courseId, oldRating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_DELETE.getTopic(), message);
    }

    public void sendRatingRedisAddingResponseEvent(String response, String method, Long courseId, int rating, Long courseRatingId) {
        String message = String.format("{\"response\": \"%s\",\"method\": \"%s\", \"courseId\": %d, \"rating\": %d, \"courseRatingId\": %d}",response, method, courseId, rating, courseRatingId);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_REDIS_ADD_RESPONSE.getTopic(), message);
    }

    public void sendRatingRedisUpdatingResponseEvent(String response, String method, Long courseId, int oldRating, int newRating, String comment) {
        String message = String.format("{\"response\": \"%s\",\"method\": \"%s\", \"courseId\": %d, \"oldRating\": %d, \"newRating\": %d, \"comment\": \"%s\"}",response, method, courseId, oldRating, newRating, comment);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_REDIS_UPDATE_RESPONSE.getTopic(), message);
    }

    public void sendRatingRedisDeletingResponseEvent(String response, String method, Long courseId, int oldRating) {
        String message = String.format("{\"response\": \"%s\",\"method\": \"%s\", \"courseId\": %d, \"oldRating\": %d}",response, method, courseId, oldRating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_REDIS_DELETE_RESPONSE.getTopic(), message);
    }

}
