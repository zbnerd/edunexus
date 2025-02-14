package com.edunexuscourseservice.adapter.in.web.response;

import com.edunexuscourseservice.domain.course.util.RoundUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRatingAverageResponse {
    private Long courseId;
    private Double averageRating;

    public static CourseRatingAverageResponse from(Long courseId, Double averageRating) {
        return CourseRatingAverageResponse.builder()
                .courseId(courseId)
                .averageRating(RoundUtils.roundToNDecimals(averageRating,2))
                .build();
    }
}
