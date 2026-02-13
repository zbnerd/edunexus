package com.edunexuscourseservice.application.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingAddEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp
    private Long sequenceNumber; // Sequence number for ordering

    private Long courseId;
    private int rating;
    private Long courseRatingId;

    public static CourseRatingAddEvent create(Long courseId, int rating, Long courseRatingId) {
        return new CourseRatingAddEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            System.currentTimeMillis(), // Use timestamp as sequence number
            courseId,
            rating,
            courseRatingId
        );
    }
}