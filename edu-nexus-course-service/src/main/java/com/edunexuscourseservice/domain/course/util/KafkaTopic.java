package com.edunexuscourseservice.domain.course.util;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    COURSE_RATING_ADD("course-rating-add"),
    COURSE_RATING_UPDATE("course-rating-update"),
    COURSE_RATING_DELETE("course-rating-delete"),

    COURSE_RATING_REDIS_ADD_RESPONSE("course-rating-redis-add-response"),
    COURSE_RATING_REDIS_UPDATE_RESPONSE("course-rating-redis-update-response"),
    COURSE_RATING_REDIS_DELETE_RESPONSE("course-rating-redis-delete-response");

    private final String topic;

    KafkaTopic(String s) {
        this.topic = s;
    }
}
