package com.edunexusattendanceservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for the attendance service
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
