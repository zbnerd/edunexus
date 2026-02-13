package com.edunexusgraphql.saga.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedEvent {
    private String eventId; // UUID for idempotency
    private Instant occurredAt; // Event timestamp

    private Long paymentId;
    private Long userId;
    private String type; // COURSE or SUBSCRIPTION
    private Double amount;
    private String paymentMethod;
    private Long courseId; // Null for SUBSCRIPTION type

    public static PaymentCreatedEvent create(Long paymentId, Long userId, String type,
                                             Double amount, String paymentMethod, Long courseId) {
        return new PaymentCreatedEvent(
            UUID.randomUUID().toString(),
            Instant.now(),
            paymentId,
            userId,
            type,
            amount,
            paymentMethod,
            courseId
        );
    }
}
