package com.edunexuscourseservice.config;

import com.edunexusobservability.metrics.MetricsRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Course service specific metrics configuration.
 *
 * Extends the base observability configuration with course-specific tags and behaviors.
 */
@Configuration
public class MetricsConfiguration {

    /**
     * Customize metrics registry with course service specific tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> {
            String appName = environment.getProperty("spring.application.name", "course-service");
            String activeProfile = String.join(",", environment.getActiveProfiles());

            registry.config().commonTags(
                "application", appName,
                "environment", activeProfile.isEmpty() ? "default" : activeProfile,
                "service", "course"
            );
        };
    }
}
