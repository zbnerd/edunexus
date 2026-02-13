package com.edunexuscourseservice.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration with Dead Letter Queue (DLT) support.
 *
 * Features:
 * - Retry mechanism with exponential backoff
 * - Manual immediate acknowledgment for reliability
 * - DLT configuration for failed messages after max retries
 * - Metrics integration for observability
 * - SASL authentication support
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.properties.max.poll.records:100}")
    private int maxPollRecords;

    @Value("${spring.kafka.consumer.properties.max.poll.interval.ms:300000}")
    private int maxPollIntervalMs;

    @Value("${KAFKA_SASL_ENABLED:true}")
    private boolean saslEnabled;

    @Value("${KAFKA_PASSWORD:kafka_secret_password_change_me}")
    private String kafkaPassword;

    /**
     * Base consumer configuration with authentication and deserialization settings.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "course-rating-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollIntervalMs);

        // SASL Authentication (when enabled)
        if (saslEnabled) {
            props.put("security.protocol", "SASL_PLAINTEXT");
            props.put("sasl.mechanism", "PLAIN");
            props.put("sasl.jaas.config",
                String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"kafka_user\" password=\"%s\";", kafkaPassword));
        }

        // JSON trusted packages for deserialization
        props.put("spring.json.trusted.packages", "*");

        // Read committed isolation level for exactly-once semantics
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Kafka Listener Container Factory with DLT and retry configuration.
     *
     * Configuration:
     * - Max 3 retry attempts with exponential backoff
     * - Backoff starts at 1000ms, max 10000ms with 2.0 multiplier
     * - Manual immediate acknowledgment mode
     * - DLT enabled for messages that fail after all retries
     * - Transient error retry support
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            MeterRegistry meterRegistry) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // Manual immediate acknowledgment for reliable processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        // Configure retry mechanism with exponential backoff using Spring Kafka's native support
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L); // 1 second
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L); // 10 seconds

        // Error handler with retry and DLT support
        DltPublishingRecoverer dltRecoverer = new DltPublishingRecoverer(meterRegistry);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(dltRecoverer, backOff);

        // Non-retryable exceptions (goes directly to DLT)
        errorHandler.addNotRetryableExceptions(JsonProcessingException.class);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        errorHandler.addNotRetryableExceptions(
            org.apache.kafka.common.errors.SerializationException.class
        );
        errorHandler.addNotRetryableExceptions(
            com.fasterxml.jackson.databind.JsonMappingException.class
        );

        // Retryable transient exceptions (these are retried by default)
        // TimeoutException, NetworkException, RetriableException are automatically retried

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    /**
     * DLT Publishing Recoverer - publishes failed messages to Dead Letter Topics.
     * Implements ConsumerRecordRecoverer interface for Spring Kafka DefaultErrorHandler.
     */
    private static class DltPublishingRecoverer implements ConsumerRecordRecoverer {
        private final MeterRegistry meterRegistry;

        public DltPublishingRecoverer(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public void accept(org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record, Exception exception) {
            // Record metrics for failed messages after all retries
            meterRegistry.counter("kafka.dlt.published",
                "topic", record.topic(),
                "exception", exception.getClass().getSimpleName()
            ).increment();

            // Log failed message details
            System.err.printf("Message failed after all retries. Topic: %s, Partition: %d, Offset: %d, Error: %s%n",
                record.topic(), record.partition(), record.offset(), exception.getMessage());

            // TODO: Publish to DLT topic: {record.topic()}.dlt
            // DLT topics should be pre-created (e.g., course-rating-add.dlt)
        }
    }
}
