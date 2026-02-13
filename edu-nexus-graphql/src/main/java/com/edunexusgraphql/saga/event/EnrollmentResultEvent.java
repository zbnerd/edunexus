package com.edunexusgraphql.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResultEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private Long paymentId;
    private Long userId;
    private Long enrollmentId;
    private String status; // SUCCESS or FAILED
    private String errorMessage; // Null if successful

    public static EnrollmentResultEvent success(Long paymentId, Long userId, Long enrollmentId) {
        return new EnrollmentResultEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            paymentId,
            userId,
            enrollmentId,
            "SUCCESS",
            null
        );
    }

    public static EnrollmentResultEvent failure(Long paymentId, Long userId, String errorMessage) {
        return new EnrollmentResultEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            paymentId,
            userId,
            null,
            "FAILED",
            errorMessage
        );
    }
}
