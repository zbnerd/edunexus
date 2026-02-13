package com.edunexusobservability.annotation;

import io.micrometer.core.annotation.Timed;
import org.springframework.core.annotation.AliasFor;

/**
 * Custom @Timed annotation for recording method execution time metrics.
 *
 * Extends Micrometer's @Timed with sensible defaults for service-level metrics.
 *
 * Usage:
 * <pre>
 * {@code
 * @MetricTimed(value = "course.service", description = "Course service operation timing")
 * public Course getCourse(Long id) { ... }
 * }
 * </pre>
 *
 * Metrics recorded:
 * - method execution time (in seconds)
 * - percentiles (p50, p95, p99)
 * - count of invocations
 */

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom @Timed annotation for recording method execution time metrics.
 *
 * Extends Micrometer's @Timed with sensible defaults for service-level metrics.
 *
 * Usage:
 * <pre>
 * {@code
 * @MetricTimed(value = "course.service", description = "Course service operation timing")
 * public Course getCourse(Long id) { ... }
 * }
 * </pre>
 *
 * Metrics recorded:
 * - method execution time (in seconds)
 * - percentiles (p50, p95, p99)
 * - count of invocations
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Timed
public @interface MetricTimed {

    /**
     * Metric name suffix. Will be prefixed with "method." automatically.
     */
    @AliasFor(annotation = Timed.class, attribute = "value")
    String value() default "";

    /**
     * Human-readable description of the metric.
     */
    @AliasFor(annotation = Timed.class, attribute = "description")
    String description() default "";

    /**
     * Percentiles to calculate for timing metrics.
     */
    @AliasFor(annotation = Timed.class, attribute = "percentiles")
    double[] percentiles() default {0.5, 0.95, 0.99};

    /**
     * Whether to record histogram (percentile) data.
     */
    @AliasFor(annotation = Timed.class, attribute = "histogram")
    boolean histogram() default true;
}