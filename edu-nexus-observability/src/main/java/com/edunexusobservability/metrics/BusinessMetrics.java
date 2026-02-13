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

    // ==================== Purchase & Subscription Metrics ====================

    /**
     * Record a course purchase event.
     * Use this to track when a user purchases a course.
     *
     * @param userId The user ID making the purchase
     * @param courseId The course ID being purchased
     */
    public void recordCoursePurchase(String userId, String courseId) {
        Counter.builder("course.purchase.count")
                .description("Total course purchase events")
                .tag("user_id", sanitizeTag(userId))
                .tag("course_id", sanitizeTag(courseId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record a course purchase event with additional metadata.
     *
     * @param userId The user ID making the purchase
     * @param courseId The course ID being purchased
     * @param amount The purchase amount
     * @param currency The currency code (e.g., "USD")
     */
    public void recordCoursePurchase(String userId, String courseId, double amount, String currency) {
        Counter.builder("course.purchase.count")
                .description("Total course purchase events")
                .tag("user_id", sanitizeTag(userId))
                .tag("course_id", sanitizeTag(courseId))
                .tag("currency", currency)
                .register(meterRegistry)
                .increment();

        // Also track the purchase amount
        meterRegistry.counter("course.purchase.amount",
                "currency", currency
        ).increment(amount);
    }

    /**
     * Record a failed course purchase attempt.
     *
     * @param userId The user ID
     * @param courseId The course ID
     * @param reason The failure reason
     */
    public void recordCoursePurchaseFailure(String userId, String courseId, String reason) {
        Counter.builder("course.purchase.failure.count")
                .description("Failed course purchase attempts")
                .tag("user_id", sanitizeTag(userId))
                .tag("course_id", sanitizeTag(courseId))
                .tag("reason", sanitizeTag(reason))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Timer for course purchase operations.
     * Returns a timer that can be used to measure purchase operation duration.
     */
    public Timer coursePurchaseTimer() {
        return Timer.builder("course.purchase.duration")
                .description("Course purchase operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== Coupon Metrics ====================

    /**
     * Record a coupon application event.
     * Use this to track when a coupon is applied to a purchase.
     *
     * @param couponCode The coupon code that was applied
     */
    public void recordCouponApplied(String couponCode) {
        Counter.builder("coupon.applied.count")
                .description("Total coupon application events")
                .tag("coupon_code", sanitizeTag(couponCode))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record a coupon application with user and course context.
     *
     * @param couponCode The coupon code applied
     * @param userId The user applying the coupon
     * @param courseId The course being purchased
     */
    public void recordCouponApplied(String couponCode, String userId, String courseId) {
        Counter.builder("coupon.applied.count")
                .description("Total coupon application events")
                .tag("coupon_code", sanitizeTag(couponCode))
                .tag("user_id", sanitizeTag(userId))
                .tag("course_id", sanitizeTag(courseId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record a coupon validation failure.
     *
     * @param couponCode The coupon code that failed validation
     * @param reason The failure reason (e.g., "expired", "not_found", "usage_limit")
     */
    public void recordCouponValidationFailure(String couponCode, String reason) {
        Counter.builder("coupon.validation.failure.count")
                .description("Coupon validation failures")
                .tag("coupon_code", sanitizeTag(couponCode))
                .tag("reason", sanitizeTag(reason))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record discount amount applied by coupon.
     *
     * @param couponCode The coupon code
     * @param discountAmount The discount amount
     * @param currency The currency code
     */
    public void recordCouponDiscount(String couponCode, double discountAmount, String currency) {
        Counter.builder("coupon.discount.amount")
                .description("Total discount amount by coupons")
                .tag("coupon_code", sanitizeTag(couponCode))
                .tag("currency", currency)
                .register(meterRegistry)
                .increment(discountAmount);
    }

    // ==================== Attendance Metrics ====================

    /**
     * Record an attendance check-in event.
     * Use this to track when a user checks into a session.
     *
     * @param userId The user ID checking in
     * @param sessionId The session ID
     */
    public void recordAttendanceCheckIn(String userId, String sessionId) {
        Counter.builder("attendance.checkin.count")
                .description("Total attendance check-in events")
                .tag("user_id", sanitizeTag(userId))
                .tag("session_id", sanitizeTag(sessionId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record an attendance check-in with timestamp context.
     *
     * @param userId The user ID checking in
     * @param sessionId The session ID
     * @param onTime Whether the check-in was on time (true) or late (false)
     */
    public void recordAttendanceCheckIn(String userId, String sessionId, boolean onTime) {
        Counter.builder("attendance.checkin.count")
                .description("Total attendance check-in events")
                .tag("user_id", sanitizeTag(userId))
                .tag("session_id", sanitizeTag(sessionId))
                .tag("status", onTime ? "on_time" : "late")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record an attendance check-out event.
     *
     * @param userId The user ID checking out
     * @param sessionId The session ID
     */
    public void recordAttendanceCheckOut(String userId, String sessionId) {
        Counter.builder("attendance.checkout.count")
                .description("Total attendance check-out events")
                .tag("user_id", sanitizeTag(userId))
                .tag("session_id", sanitizeTag(sessionId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record attendance duration for a user in a session.
     *
     * @param userId The user ID
     * @param sessionId The session ID
     * @param durationMinutes The duration in minutes
     */
    public void recordAttendanceDuration(String userId, String sessionId, long durationMinutes) {
        Counter.builder("attendance.duration.minutes")
                .description("Total attendance duration in minutes")
                .tag("user_id", sanitizeTag(userId))
                .tag("session_id", sanitizeTag(sessionId))
                .register(meterRegistry)
                .increment(durationMinutes);
    }

    /**
     * Record an attendance absence (no-show).
     *
     * @param userId The user ID who was absent
     * @param sessionId The session ID
     */
    public void recordAttendanceAbsence(String userId, String sessionId) {
        Counter.builder("attendance.absence.count")
                .description("Total attendance absence events")
                .tag("user_id", sanitizeTag(userId))
                .tag("session_id", sanitizeTag(sessionId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Timer for attendance check-in operations.
     */
    public Timer attendanceCheckInTimer() {
        return Timer.builder("attendance.checkin.duration")
                .description("Attendance check-in operation duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== Saga/Orchestration Metrics ====================

    /**
     * Record the start of a saga orchestration.
     *
     * @param sagaType The type of saga (e.g., "enrollment", "purchase")
     * @param sagaId The unique saga ID
     */
    public void recordSagaStarted(String sagaType, String sagaId) {
        Counter.builder("saga.started.count")
                .description("Total saga started events")
                .tag("saga_type", sanitizeTag(sagaType))
                .tag("saga_id", sanitizeTag(sagaId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record successful saga completion.
     *
     * @param sagaType The type of saga
     * @param sagaId The unique saga ID
     */
    public void recordSagaCompleted(String sagaType, String sagaId) {
        Counter.builder("saga.completed.count")
                .description("Total saga completed events")
                .tag("saga_type", sanitizeTag(sagaType))
                .tag("saga_id", sanitizeTag(sagaId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record saga failure and compensation start.
     *
     * @param sagaType The type of saga
     * @param sagaId The unique saga ID
     * @param failureReason The reason for failure
     */
    public void recordSagaCompensationStarted(String sagaType, String sagaId, String failureReason) {
        Counter.builder("saga.compensation.started.count")
                .description("Total saga compensation started events")
                .tag("saga_type", sanitizeTag(sagaType))
                .tag("saga_id", sanitizeTag(sagaId))
                .tag("failure_reason", sanitizeTag(failureReason))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record successful saga compensation.
     *
     * @param sagaType The type of saga
     * @param sagaId The unique saga ID
     */
    public void recordSagaCompensationCompleted(String sagaType, String sagaId) {
        Counter.builder("saga.compensation.completed.count")
                .description("Total saga compensation completed events")
                .tag("saga_type", sanitizeTag(sagaType))
                .tag("saga_id", sanitizeTag(sagaId))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Timer for saga end-to-end duration.
     */
    public Timer sagaDurationTimer() {
        return Timer.builder("saga.duration")
                .description("Saga end-to-end duration")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    // ==================== Helper Methods ====================

    /**
     * Sanitize tag values to prevent metric naming issues.
     * Removes or replaces characters that could cause problems.
     *
     * @param value The value to sanitize
     * @return A sanitized string safe for use as a tag value
     */
    private String sanitizeTag(String value) {
        if (value == null || value.isEmpty()) {
            return "unknown";
        }
        // Limit length to prevent cardinality explosion
        if (value.length() > 50) {
            value = value.substring(0, 50);
        }
        // Replace problematic characters
        return value.replaceAll("[^a-zA-Z0-9:_-]", "_");
    }
}
