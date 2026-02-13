package com.edunexusgraphql.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private Long paymentId;
    private Long userId;
    private String reason; // Failure reason

    public static PaymentFailedEvent create(Long paymentId, Long userId, String reason) {
        return new PaymentFailedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            paymentId,
            userId,
            reason
        );
    }
}
