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

    public void sendRatingAddedEvent(Long courseId, int rating) {
        String message = String.format("{\"courseId\": %d, \"rating\": %d}", courseId, rating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_ADD.getTopic(), message);
    }

    public void sendRatingUpdatedEvent(Long courseId, int oldRating, int newRating) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d, \"newRating\": %d}", courseId, oldRating, newRating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_UPDATE.getTopic(), message);
    }

    public void sendRatingDeletedEvent(Long courseId, int oldRating) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d}", courseId, oldRating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_DELETE.getTopic(), message);
    }

}
