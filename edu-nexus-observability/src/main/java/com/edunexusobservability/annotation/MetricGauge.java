package com.edunexusobservability.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for registering gauge metrics that track real-time values.
 *
 * Unlike @Timed and @Counted which are method-level, gauges track stateful values
 * that can go up or down (e.g., active users, cache size, queue depth).
 *
 * Usage:
 * <pre>
 * {@code
 * @MetricGauge(
 *     name = "active.users",
 *     description = "Current number of active user sessions",
 *     tags = {"region", "type"}
 * )
 * public AtomicInteger getActiveUsers() { return activeUsers; }
 * }
 * </pre>
 *
 * Metrics recorded:
 * - Current value (sampled at scrape time)
 * - Can be tagged for multi-dimensional analysis
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MetricGauge {

    /**
     * Metric name. Will be prefixed with "gauge." automatically.
     */
    String name();

    /**
     * Human-readable description of the metric.
     */
    String description() default "";

    /**
     * Tag keys to apply to the metric.
     * Values must be provided via tagValues() or runtime context.
     */
    String[] tags() default {};

    /**
     * Tag values corresponding to tag keys.
     * Must match length of tags() if specified.
     */
    String[] tagValues() default {};

    /**
     * Base unit for the gauge value (e.g., "bytes", "items", "sessions").
     */
    String baseUnit() default "";
}
