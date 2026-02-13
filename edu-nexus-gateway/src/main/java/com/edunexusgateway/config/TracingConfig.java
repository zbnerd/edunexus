package com.edunexusgateway.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Tracing configuration for the gateway service.
 *
 * This class extends the common TracingConfig.
 */
@Configuration
public class TracingConfig extends com.edunexusobservability.config.TracingConfig {
    // All functionality inherited from the common implementation
}
