package com.edunexuscourseservice.application.service.kafka.dlt;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Extracts ConsumerRecord from Spring Kafka Message objects.
 * <p>
 * Handles various ways Kafka records may be wrapped in Spring messaging.
 */
@Component
public class ConsumerRecordExtractor {

    /**
     * Extract ConsumerRecord from a Spring Kafka Message.
     * <p>
     * Tries multiple extraction strategies:
     * 1. Direct payload extraction
     * 2. Header-based extraction
     *
     * @param message The Spring Kafka message
     * @return The extracted ConsumerRecord, or null if not found
     */
    public ConsumerRecord<?, ?> extractRecord(Message<?> message) {
        // Direct payload
        Object payload = message.getPayload();
        if (payload instanceof ConsumerRecord) {
            return (ConsumerRecord<?, ?>) payload;
        }

        // Check message headers
        Object record = message.getHeaders().get("kafka_consumerRecord");
        if (record instanceof ConsumerRecord) {
            return (ConsumerRecord<?, ?>) record;
        }

        return null;
    }
}
