package com.edunexuscourseservice.application.service.kafka.dlt;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher for Dead Letter Topic (DLT) messages.
 * <p>
 * Handles publishing failed messages to DLT with:
 * - Metrics recording for monitoring
 * - Async publishing with callback handling
 * - Error logging and recovery
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DltPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;
    private final DltTopicMapper topicMapper;
    private final DltMessageBuilder messageBuilder;

    /**
     * Publish a failed message to its corresponding DLT.
     *
     * @param failedRecord The failed Kafka record
     * @param exception The exception that caused the failure
     */
    public void publishToDlt(ConsumerRecord<?, ?> failedRecord, Exception exception) {
        String dltTopic = topicMapper.getDltTopic(failedRecord.topic());

        if (dltTopic == null) {
            log.warn("No DLT configured for topic: {}", failedRecord.topic());
            return;
        }

        String dltPayload = messageBuilder.buildDltMessage(failedRecord, exception);

        kafkaTemplate.send(dltTopic, String.valueOf(failedRecord.key()), dltPayload)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published failed message to DLT: topic={}, partition={}, offset={}",
                                failedRecord.topic(), failedRecord.partition(), failedRecord.offset());
                        meterRegistry.counter("kafka.dlt.published", "dltTopic", dltTopic).increment();
                    } else {
                        log.error("Failed to publish message to DLT: dltTopic={}, error={}",
                                dltTopic, ex.getMessage(), ex);
                        meterRegistry.counter("kafka.dlt.publish.failed", "dltTopic", dltTopic).increment();
                    }
                });
    }

    /**
     * Record a DLT publishing error.
     */
    public void recordPublishError() {
        meterRegistry.counter("kafka.dlt.error").increment();
    }
}
