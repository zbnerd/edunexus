package com.edunexuscourseservice.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a course is updated.
 * Triggers cache invalidation AFTER_COMMIT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdatedEvent {
    private Instant timestamp;
    private Long courseId;
    private String eventType; // CREATE, UPDATE, DELETE
    private String operationId; // For tracking

    public static CourseUpdatedEvent create(Long courseId, String eventType) {
        return CourseUpdatedEvent.builder()
                .timestamp(Instant.now())
                .courseId(courseId)
                .eventType(eventType)
                .operationId(java.util.UUID.randomUUID().toString())
                .build();
    }
}
