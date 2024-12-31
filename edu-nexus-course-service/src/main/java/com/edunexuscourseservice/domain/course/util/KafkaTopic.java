package com.edunexuscourseservice.domain.course.util;

import lombok.Getter;

@Getter
public enum KafkaTopic {
    COURSE_RATING_ADD("course-rating-add"),
    COURSE_RATING_UPDATE("course-rating-update"),
    COURSE_RATING_DELETE("course-rating-delete");

    private String topic;

    KafkaTopic(String s) {
        this.topic = s;
    }
}
