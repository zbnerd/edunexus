package com.edunexusattendanceservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling
 * Required for scheduled absence marking
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
