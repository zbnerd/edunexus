package com.edunexusgraphql.service.kafka;

import com.edunexusgraphql.saga.event.PaymentConfirmedEvent;
import com.edunexusgraphql.saga.event.PaymentCreatedEvent;
import com.edunexusgraphql.saga.event.PaymentFailedEvent;
import com.edunexusgraphql.util.PaymentKafkaTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka Producer for Payment Saga Events
 *
 * Publishes events for the enrollment saga:
 * - PaymentCreatedEvent: Triggers enrollment registration
 * - PaymentConfirmedEvent: Signals successful enrollment
 * - PaymentFailedEvent: Signals enrollment failure (triggers compensation)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Send payment created event (triggers enrollment)
     */
    public void sendPaymentCreatedEvent(PaymentCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PaymentKafkaTopic.PAYMENT_CREATED.getTopic(), message);
            log.info("Sent payment created event for paymentId={}, userId={}", event.getPaymentId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send payment created event for paymentId={}", event.getPaymentId(), e);
            throw new RuntimeException("Failed to publish PaymentCreatedEvent", e);
        }
    }

    /**
     * Send payment confirmed event (enrollment succeeded)
     */
    public void sendPaymentConfirmedEvent(PaymentConfirmedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PaymentKafkaTopic.PAYMENT_CONFIRMED.getTopic(), message);
            log.info("Sent payment confirmed event for paymentId={}, userId={}", event.getPaymentId(), event.getUserId());
        } catch (Exception e) {
            log.error("Failed to send payment confirmed event for paymentId={}", event.getPaymentId(), e);
            throw new RuntimeException("Failed to publish PaymentConfirmedEvent", e);
        }
    }

    /**
     * Send payment failed event (enrollment failed - triggers compensation)
     */
    public void sendPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PaymentKafkaTopic.PAYMENT_FAILED.getTopic(), message);
            log.warn("Sent payment failed event for paymentId={}, userId={}, reason={}",
                    event.getPaymentId(), event.getUserId(), event.getReason());
        } catch (Exception e) {
            log.error("Failed to send payment failed event for paymentId={}", event.getPaymentId(), e);
            throw new RuntimeException("Failed to publish PaymentFailedEvent", e);
        }
    }
}
