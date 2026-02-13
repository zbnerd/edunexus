package com.edunexuscourseservice.adapter.in.web.request;

import com.edunexuscourseservice.adapter.out.persistence.entity.CourseRating;
import com.edunexuscourseservice.domain.course.dto.CourseRatingInfoDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CourseRatingCreateRequest {
    @jakarta.validation.constraints.NotNull(message = "User ID is required")
    private Long userId;

    @jakarta.validation.constraints.NotNull(message = "Rating is required")
    @jakarta.validation.constraints.Min(value = 1, message = "Rating must be at least 1")
    @jakarta.validation.constraints.Max(value = 5, message = "Rating must not exceed 5")
    private int rating;

    @jakarta.validation.constraints.Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String comment;

    public CourseRating toEntity() {
        CourseRating courseRating = new CourseRating();
        courseRating.setCourseRatingInfo(CourseRatingInfoDto.builder()
                .userId(userId)
                .rating(rating)
                .comment(comment)
                .build());
        return courseRating;
    }
}
