package com.edunexusobservability.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Predefined business metrics for EduNexus domain operations.
 *
 * Provides standardized metrics for common business operations:
 * Course management, Enrollment, User sessions, Ratings, etc.
 *
 * Usage:
 * <pre>
 * {@code
 * @Autowired
 * private BusinessMetrics businessMetrics;
 *
 * public void enrollUser(Long userId, Long courseId) {
 *     businessMetrics.enrollmentCreated().increment();
 *     businessMetrics.enrollmentTimer().record(() -> {
 *         // enrollment logic
 *     });
 * }
 * }
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    // ==================== Course Metrics ====================

    /**
     * Counter for course creation attempts.
     */
    public Counter courseCreationCounter() {
        return Counter.builder("course.creation.count")
                .description("Total course creation attempts")
                .tag("type", "total")
                .register(meterRegistry);
    }

    /**
     * Counter for successful course creations.
     */
    public Counter courseCreationSuccessCounter() {
        return Counter.builder("course.creation.count")
                .description("Successful course creations")
                .tag("type", "success")
                .register(meterRegistry);
    }

    /**
     * Counter for failed course creations.
     */
    public Counter courseCreationFailureCounter() {
        return Counter.builder("course.creation.count")
                .description("Failed course creations")
                .tag("type", "failure")
                .register(meterRegistry);
    }

    /**
     * Timer for course retrieval operations.
     */
    public Timer courseRetrievalTimer() {
        return Timer.builder("course.retrieval.duration")
                .description("Course retrieval operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== Enrollment Metrics ====================

    /**
     * Counter for enrollment creation events.
     */
    public Counter enrollmentCreatedCounter() {
        return Counter.builder("enrollment.count")
                .description("Total enrollment creation events")
                .tag("action", "created")
                .register(meterRegistry);
    }

    /**
     * Counter for enrollment cancellation events.
     */
    public Counter enrollmentCancelledCounter() {
        return Counter.builder("enrollment.count")
                .description("Total enrollment cancellation events")
                .tag("action", "cancelled")
                .register(meterRegistry);
    }

    /**
     * Timer for enrollment operations.
     */
    public Timer enrollmentOperationTimer() {
        return Timer.builder("enrollment.operation.duration")
                .description("Enrollment operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== User Metrics ====================

    /**
     * Gauge for active user sessions.
     */
    public Gauge activeUserSessionsGauge(AtomicLong activeSessions) {
        return Gauge.builder("user.sessions.active", activeSessions, AtomicLong::get)
                .description("Current number of active user sessions")
                .register(meterRegistry);
    }

    /**
     * Counter for user registration events.
     */
    public Counter userRegistrationCounter() {
        return Counter.builder("user.registration.count")
                .description("Total user registration events")
                .register(meterRegistry);
    }

    /**
     * Counter for user login events.
     */
    public Counter userLoginCounter() {
        return Counter.builder("user.login.count")
                .description("Total user login events")
                .register(meterRegistry);
    }

    /**
     * Counter for user logout events.
     */
    public Counter userLogoutCounter() {
        return Counter.builder("user.logout.count")
                .description("Total user logout events")
                .register(meterRegistry);
    }

    // ==================== Rating Metrics ====================

    /**
     * Counter for rating creation events.
     */
    public Counter ratingCreatedCounter() {
        return Counter.builder("rating.count")
                .description("Total rating creation events")
                .tag("action", "created")
                .register(meterRegistry);
    }

    /**
     * Counter for rating update events.
     */
    public Counter ratingUpdatedCounter() {
        return Counter.builder("rating.count")
                .description("Total rating update events")
                .tag("action", "updated")
                .register(meterRegistry);
    }

    /**
     * Counter for rating deletion events.
     */
    public Counter ratingDeletedCounter() {
        return Counter.builder("rating.count")
                .description("Total rating deletion events")
                .tag("action", "deleted")
                .register(meterRegistry);
    }

    /**
     * Timer for rating operations.
     */
    public Timer ratingOperationTimer() {
        return Timer.builder("rating.operation.duration")
                .description("Rating operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== Cache Metrics ====================

    /**
     * Counter for cache hit events.
     */
    public Counter cacheHitCounter() {
        return Counter.builder("cache.access.count")
                .description("Cache access hits")
                .tag("result", "hit")
                .register(meterRegistry);
    }

    /**
     * Counter for cache miss events.
     */
    public Counter cacheMissCounter() {
        return Counter.builder("cache.access.count")
                .description("Cache access misses")
                .tag("result", "miss")
                .register(meterRegistry);
    }

    /**
     * Counter for cache eviction events.
     */
    public Counter cacheEvictionCounter() {
        return Counter.builder("cache.eviction.count")
                .description("Cache eviction events")
                .register(meterRegistry);
    }

    // ==================== Kafka Metrics ====================

    /**
     * Counter for Kafka message publishing events.
     */
    public Counter kafkaPublishCounter() {
        return Counter.builder("kafka.publish.count")
                .description("Kafka message publish events")
                .register(meterRegistry);
    }

    /**
     * Counter for Kafka message consumption events.
     */
    public Counter kafkaConsumeCounter() {
        return Counter.builder("kafka.consume.count")
                .description("Kafka message consume events")
                .register(meterRegistry);
    }

    /**
     * Counter for Kafka message processing failures.
     */
    public Counter kafkaProcessingFailureCounter() {
        return Counter.builder("kafka.processing.failure.count")
                .description("Kafka message processing failures")
                .register(meterRegistry);
    }

    /**
     * Timer for Kafka message processing duration.
     */
    public Timer kafkaProcessingTimer() {
        return Timer.builder("kafka.processing.duration")
                .description("Kafka message processing duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }
}
