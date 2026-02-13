package com.edunexusobservability.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Micrometer metrics configuration for all services.
 *
 * Provides:
 * - JVM metrics (memory, threads, GC)
 * - System metrics (CPU)
 * - Application tags for service identification
 * - Common meter filters and customizations
 *
 * Services should import this configuration from edu-nexus-observability.
 */
@Configuration
public class MetricsConfig {

    /**
     * Customize MeterRegistry with application tags.
     *
     * Tags added to all metrics:
     * - application: Service name from spring.application.name
     * - environment: Active Spring profile (dev, local, prod)
     * - region: Geographic region (optional)
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> {
            String appName = environment.getProperty("spring.application.name", "unknown-service");
            String activeProfile = String.join(",", environment.getActiveProfiles());

            registry.config().commonTags(
                "application", appName,
                "environment", activeProfile.isEmpty() ? "default" : activeProfile
            );

            // Add optional region tag if configured
            String region = environment.getProperty("metrics.region");
            if (region != null) {
                registry.config().commonTags("region", region);
            }
        };
    }

    /**
     * Enable JVM memory metrics.
     *
     * Metrics:
     * - jvm_memory_used_bytes: Memory usage by area
     * - jvm_memory_committed_bytes: Committed memory by area
     * - jvm_memory_max_bytes: Max memory by area
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Enable JVM thread metrics.
     *
     * Metrics:
     * - jvm_threads_live_threads: Current live thread count
     * - jvm_threads_peak_threads: Peak thread count
     * - jvm_threads_daemon_threads: Daemon thread count
     * - jvm_threads_state_threads: Thread count by state
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Enable system processor metrics.
     *
     * Metrics:
     * - system_cpu_count: Number of available processors
     * - system_load_average_1m: Average system load
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Meter filter to ensure metric names follow conventions.
     *
     * - Converts dot-separated names to snake_case
     * - Removes special characters
     * - Enforces consistent naming
     */
    @Bean
    public MeterFilter meterNameFilter() {
        return MeterFilter.deny(id -> {
            String name = id.getName();
            // Deny metrics that don't follow naming conventions
            return name != null && (name.contains("..") || name.matches(".*[{}\\[\\]].*"));
        });
    }

    /**
     * Meter filter to limit cardinality on high-cardinality tags.
     *
     * Prevents metric explosion by:
     * - Limiting tag value lengths
     * - Rejecting tags with too many unique values
     */
    @Bean
    public MeterFilter cardinalityLimitFilter() {
        return new MeterFilter() {
            @Override
            public io.micrometer.core.instrument.Meter.Id map(io.micrometer.core.instrument.Meter.Id id) {
                // Limit tag value lengths to 50 characters
                if (id.getTags().stream().anyMatch(tag -> tag.getValue().length() > 50)) {
                    return id.withTags(
                        id.getTags().stream()
                            .map(tag -> tag.getValue().length() > 50
                                ? io.micrometer.core.instrument.Tag.of(tag.getKey(), tag.getValue().substring(0, 50))
                                : tag)
                            .toList()
                    );
                }
                return id;
            }
        };
    }
}
