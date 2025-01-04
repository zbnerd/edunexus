package com.edunexuscourseservice.domain.course.util;

import lombok.Getter;

@Getter
public enum KafkaCourseRatingResponse {
    SUCCESS("success"), FAIL("fail");

    private final String response;

    KafkaCourseRatingResponse(String response) {
        this.response = response;
    }
}
