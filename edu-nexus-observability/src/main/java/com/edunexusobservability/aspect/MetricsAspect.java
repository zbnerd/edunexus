package com.edunexusobservability.aspect;

import com.edunexusobservability.metrics.MetricsRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect for intercepting methods annotated with custom metrics annotations.
 *
 * Handles:
 * - @MetricTimed: Records method execution time
 * - @MetricCounted: Records method invocation count
 * - Automatic tagging with method signature metadata
 *
 * Only active when metrics.aspect.enabled=true (default: false for safety)
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "metrics.aspect",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class MetricsAspect {

    private final MetricsRegistry metricsRegistry;

    /**
     * Intercept methods annotated with @MetricTimed.
     *
     * Records execution time with method metadata as tags:
     * - class: Fully qualified class name
     * - method: Method name
     * - return-type: Return type
     */
    @Around("@annotation(com.edunexusobservability.annotation.MetricTimed)")
    public Object timedAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String metricName = String.format("%s.%s", className, methodName);

        Timer.Sample sample = Timer.start(metricsRegistry.getMeterRegistry());

        try {
            Object result = joinPoint.proceed();

            // Record successful execution
            metricsRegistry.timer(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .record(sample);

            return result;
        } catch (Throwable ex) {
            // Record failed execution
            metricsRegistry.timer(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("error", ex.getClass().getSimpleName())
                    .record(sample);

            throw ex;
        }
    }

    /**
     * Intercept methods annotated with @MetricCounted.
     *
     * Records invocation count with method metadata as tags.
     * Separates success and failure counts.
     */
    @Around("@annotation(com.edunexusobservability.annotation.MetricCounted)")
    public Object countedAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        String metricName = String.format("%s.%s", className, methodName);

        try {
            Object result = joinPoint.proceed();

            // Record success
            metricsRegistry.counter(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "success")
                    .increment();

            return result;
        } catch (Throwable ex) {
            // Record failure
            metricsRegistry.counter(metricName)
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", "error")
                    .tag("error", ex.getClass().getSimpleName())
                    .increment();

            throw ex;
        }
    }
}
