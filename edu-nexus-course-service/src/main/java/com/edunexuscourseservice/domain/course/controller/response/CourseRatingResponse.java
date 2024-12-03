package com.edunexuscourseservice.domain.course.controller.response;

import com.edunexuscourseservice.domain.course.entity.CourseRating;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRatingResponse {
    private Long id;
    private Long userId;
    private int rating;
    private String comment;

    public static CourseRatingResponse from(CourseRating courseRating) {
        return CourseRatingResponse.builder()
                .id(courseRating.getId())
                .userId(courseRating.getUserId())
                .rating(courseRating.getRating())
                .comment(courseRating.getComment())
                .build();
    }
}
