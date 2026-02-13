package com.edunexusgraphql.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private Long paymentId;
    private Long userId;
    private Long courseId; // Null for SUBSCRIPTION type

    public static PaymentConfirmedEvent create(Long paymentId, Long userId, Long courseId) {
        return new PaymentConfirmedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            paymentId,
            userId,
            courseId
        );
    }
}
