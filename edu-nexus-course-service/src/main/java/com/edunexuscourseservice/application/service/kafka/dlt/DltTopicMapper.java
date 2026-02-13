package com.edunexuscourseservice.application.service.kafka.dlt;

import com.edunexuscourseservice.domain.course.util.KafkaTopic;
import org.springframework.stereotype.Component;

/**
 * Maps original Kafka topics to their Dead Letter Topic (DLT) equivalents.
 * <p>
 * Provides centralized topic mapping for DLT routing.
 */
@Component
public class DltTopicMapper {

    /**
     * Get the DLT topic name for a given original topic.
     *
     * @param originalTopic The original topic name
     * @return The corresponding DLT topic name, or null if not mapped
     */
    public String getDltTopic(String originalTopic) {
        return switch (originalTopic) {
            case "course-rating-add" -> KafkaTopic.COURSE_RATING_ADD_DLT.getTopic();
            case "course-rating-update" -> KafkaTopic.COURSE_RATING_UPDATE_DLT.getTopic();
            case "course-rating-delete" -> KafkaTopic.COURSE_RATING_DELETE_DLT.getTopic();
            default -> null;
        };
    }
}
