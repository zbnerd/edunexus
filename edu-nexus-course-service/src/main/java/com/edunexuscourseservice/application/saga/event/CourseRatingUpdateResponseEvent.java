package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingUpdateResponseEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private String response;
    private String method;
    private Long courseId;
    private int oldRating;
    private int newRating;
    private String comment;
}