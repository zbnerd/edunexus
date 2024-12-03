package com.edunexuscourseservice.domain.course.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRatingInfoDto {
    private Long userId;
    private int rating;
    private String comment;
}