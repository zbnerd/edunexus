package com.edunexuscourseservice.config.course.metrics;

import com.edunexusobservability.metrics.MetricsRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Course service specific metrics.
 *
 * Provides metrics for:
 * - Course CRUD operations
 * - Course rating operations
 * - Cache operations
 * - Kafka message processing
 */
@Component
@RequiredArgsConstructor
public class CourseMetrics {

    private final MetricsRegistry metricsRegistry;

    // Course-specific counters
    private static final AtomicLong activeCourseQueries = new AtomicLong(0);

    /**
     * Record course creation event.
     */
    public void recordCourseCreated() {
        metricsRegistry.counter("course.creation")
                .tag("type", "success")
                .increment();
    }

    /**
     * Record course creation failure.
     */
    public void recordCourseCreationFailed(String reason) {
        metricsRegistry.counter("course.creation")
                .tag("type", "failure")
                .tag("reason", reason)
                .increment();
    }

    /**
     * Record course retrieval with timing.
     */
    public Timer.Sample startCourseRetrieval() {
        return Timer.start(metricsRegistry.getMeterRegistry());
    }

    /**
     * Stop course retrieval timer.
     */
    public void stopCourseRetrieval(Timer.Sample sample, String operation) {
        metricsRegistry.timer("course.retrieval")
                .tag("operation", operation)
                .record(sample);
    }

    /**
     * Increment active course queries gauge.
     */
    public void incrementActiveQueries() {
        activeCourseQueries.incrementAndGet();
    }

    /**
     * Decrement active course queries gauge.
     */
    public void decrementActiveQueries() {
        activeCourseQueries.decrementAndGet();
    }

    /**
     * Record cache hit.
     */
    public void recordCacheHit(String cacheName) {
        metricsRegistry.counter("cache.access")
                .tag("result", "hit")
                .tag("cache", cacheName)
                .increment();
    }

    /**
     * Record cache miss.
     */
    public void recordCacheMiss(String cacheName) {
        metricsRegistry.counter("cache.access")
                .tag("result", "miss")
                .tag("cache", cacheName)
                .increment();
    }

    /**
     * Record rating created.
     */
    public void recordRatingCreated() {
        metricsRegistry.counter("rating.count")
                .tag("action", "created")
                .increment();
    }

    /**
     * Record Kafka message published.
     */
    public void recordKafkaMessagePublished(String topic) {
        metricsRegistry.counter("kafka.publish")
                .tag("topic", topic)
                .increment();
    }

    /**
     * Record Kafka message consumed.
     */
    public void recordKafkaMessageConsumed(String topic) {
        metricsRegistry.counter("kafka.consume")
                .tag("topic", topic)
                .increment();
    }

    /**
     * Get active queries gauge.
     */
    public Gauge getActiveQueriesGauge() {
        return metricsRegistry.gauge("course.queries.active")
                .description("Current number of active course queries")
                .value(activeCourseQueries, (al) -> al.longValue())
                .register();
    }
}
