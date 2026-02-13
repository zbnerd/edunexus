package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingUpdateEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private Long courseId;
    private int oldRating;
    private int newRating;
    private String comment;

    public static CourseRatingUpdateEvent create(Long courseId, int oldRating, int newRating, String comment) {
        return new CourseRatingUpdateEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            courseId,
            oldRating,
            newRating,
            comment
        );
    }
}