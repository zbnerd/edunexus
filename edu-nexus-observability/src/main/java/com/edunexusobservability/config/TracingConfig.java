package com.edunexusobservability.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common tracing configuration for all services.
 *
 * Provides consistent tracing behavior:
 * - Excludes health checks and actuator endpoints
 * - Includes all other observations
 * - Enables distributed tracing integration
 */
@Configuration
public class TracingConfig {

    @Bean
    public ObservationPredicate observationPredicate() {
        return (name, context) -> {
            if (name == null) {
                return true;
            }
            // Exclude health checks and actuator endpoints
            if (name.contains("healthcheck") || name.contains("/actuator")) {
                return false;
            }
            // Include all other observations
            return true;
        };
    }
}