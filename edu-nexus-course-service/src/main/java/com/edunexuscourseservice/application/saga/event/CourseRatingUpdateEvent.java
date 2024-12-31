package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRatingUpdateEvent {
    private Long courseId;
    private int oldRating;
    private int newRating;
}