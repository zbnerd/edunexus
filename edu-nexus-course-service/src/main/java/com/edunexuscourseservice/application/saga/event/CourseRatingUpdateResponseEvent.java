package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingUpdateResponseEvent {
    private String response;
    private String method;
    private Long courseId;
    private int oldRating;
    private int newRating;
    private String comment;
}