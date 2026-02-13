package com.edunexusobservability.annotation;

import io.micrometer.core.annotation.Counted;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom @Counted annotation for recording method invocation counts.
 *
 * Extends Micrometer's @Counted with sensible defaults for service-level metrics.
 *
 * Usage:
 * <pre>
 * {@code
 * @MetricCounted(value = "course.creation", description = "Course creation attempts")
 * public Course createCourse(CreateCourseRequest request) { ... }
 * }
 * </pre>
 *
 * Metrics recorded:
 * - total count of invocations
 * - can be split by result (success/failure) when used with exception handling
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Counted
public @interface MetricCounted {

    /**
     * Metric name suffix. Will be prefixed with "method." automatically.
     */
    @AliasFor(annotation = Counted.class, attribute = "value")
    String value() default "";

    /**
     * Human-readable description of the metric.
     */
    @AliasFor(annotation = Counted.class, attribute = "description")
    String description() default "";

    /**
     * Whether to record failures separately.
     */
    @AliasFor(annotation = Counted.class, attribute = "recordFailuresOnly")
    boolean recordFailuresOnly() default false;
}