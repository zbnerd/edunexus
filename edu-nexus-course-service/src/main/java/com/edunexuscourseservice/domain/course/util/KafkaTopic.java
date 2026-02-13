package com.edunexuscourseservice.domain.course.util;

import lombok.Getter;

/**
 * Kafka Topics for Course Rating Events
 *
 * Removed response topics as per ADR-000 (Cache-Aside pattern):
 * - No more compensating transactions
 * - Fire-and-forget cache updates
 * - Cache failures don't trigger DB rollbacks
 */
@Getter
public enum KafkaTopic {
    COURSE_RATING_ADD("course-rating-add"),
    COURSE_RATING_UPDATE("course-rating-update"),
    COURSE_RATING_DELETE("course-rating-delete"),

    // Dead Letter Topics for main rating events
    COURSE_RATING_ADD_DLT("course-rating-add-dlt"),
    COURSE_RATING_UPDATE_DLT("course-rating-update-dlt"),
    COURSE_RATING_DELETE_DLT("course-rating-delete-dlt");

    private final String topic;

    KafkaTopic(String s) {
        this.topic = s;
    }
}
