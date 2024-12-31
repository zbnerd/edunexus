package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRatingAddEvent {
    private Long courseId;
    private int rating;
}