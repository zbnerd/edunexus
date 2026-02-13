package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.domain.course.util.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer for Course Rating Events
 *
 * Implements Fire-and-Forget pattern per ADR-000:
 * - No response events sent back
 * - Cache updates are asynchronous
 * - Cache failures don't trigger compensating transactions
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRatingProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Send rating added event (fire-and-forget)
     */
    public void sendRatingAddedEvent(Long courseId, int rating, Long courseRatingId) {
        String message = String.format("{\"courseId\": %d, \"rating\": %d, \"courseRatingId\": %d}", courseId, rating, courseRatingId);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_ADD.getTopic(), message);
        log.debug("Sent rating added event for course {}", courseId);
    }

    /**
     * Send rating updated event (fire-and-forget)
     */
    public void sendRatingUpdatedEvent(Long courseId, int oldRating, int newRating, String comment) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d, \"newRating\": %d, \"comment\": %s}", courseId, oldRating, newRating, comment);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_UPDATE.getTopic(), message);
        log.debug("Sent rating updated event for course {}", courseId);
    }

    /**
     * Send rating deleted event (fire-and-forget)
     */
    public void sendRatingDeletedEvent(Long courseId, int oldRating) {
        String message = String.format("{\"courseId\": %d, \"oldRating\": %d}", courseId, oldRating);
        kafkaTemplate.send(KafkaTopic.COURSE_RATING_DELETE.getTopic(), message);
        log.debug("Sent rating deleted event for course {}", courseId);
    }

}
