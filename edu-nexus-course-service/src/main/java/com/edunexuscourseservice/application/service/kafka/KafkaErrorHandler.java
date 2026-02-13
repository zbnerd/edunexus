package com.edunexuscourseservice.application.service.kafka;

import com.edunexuscourseservice.application.service.kafka.dlt.ConsumerRecordExtractor;
import com.edunexuscourseservice.application.service.kafka.dlt.DltPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Custom Kafka error handler that publishes failed messages to Dead Letter Topics.
 * <p>
 * Refactored to delegate specialized concerns:
 * - ConsumerRecord extraction delegated to ConsumerRecordExtractor
 * - DLT publishing delegated to DltPublisher
 * - Error handler focused on error handling flow and metrics
 *
 * Records metrics for monitoring and provides structured logging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

    private final MeterRegistry meterRegistry;
    private final ConsumerRecordExtractor recordExtractor;
    private final DltPublisher dltPublisher;

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        // Extract record from the failed message
        ConsumerRecord<?, ?> record = recordExtractor.extractRecord(message);

        // Record error metric
        if (record != null) {
            meterRegistry.counter("kafka.consumer.errors",
                    "topic", record.topic(),
                    "exception", exception.getClass().getSimpleName())
                    .increment();

            // Log with full context
            log.error("Kafka consumer error: topic={}, partition={}, offset={}, key={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    exception);

            // Publish to DLT
            dltPublisher.publishToDlt(record, exception);
        } else {
            log.error("Kafka consumer error (no record available): {}", exception.getMessage(), exception);
        }

        // Return null to acknowledge the message (prevents infinite retry)
        return null;
    }
}
