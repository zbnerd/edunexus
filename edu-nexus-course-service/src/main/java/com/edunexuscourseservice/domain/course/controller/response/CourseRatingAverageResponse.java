package com.edunexuscourseservice.domain.course.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRatingAverageResponse {
    private Long courseId;
    private Double averageRating;
}
