package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingDeleteEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp
    private Long sequenceNumber; // Sequence number for ordering

    private Long courseId;
    private int oldRating;

    public static CourseRatingDeleteEvent create(Long courseId, int oldRating) {
        return new CourseRatingDeleteEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            System.currentTimeMillis(), // Use timestamp as sequence number
            courseId,
            oldRating
        );
    }
}