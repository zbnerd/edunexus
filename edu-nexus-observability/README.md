# EduNexus Observability Module

A shared module for common observability components across all EduNexus microservices.

## Components

### 1. MDC Filter (`filter/MdcFilter.java`)
- Provides structured logging with correlation IDs
- Extracts trace IDs from incoming requests (B3 and W3C Trace Context)
- Generates correlation IDs when not provided
- Cleans up MDC to prevent memory leaks
- Adds correlation ID to response headers

### 2. Metrics Annotations (`annotation/MetricTimed.java`, `annotation/MetricCounted.java`)
- `@MetricTimed`: Records method execution time with percentiles
- `@MetricCounted`: Records method invocation counts
- Extends Micrometer annotations with sensible defaults
- Consistent naming conventions across services

### 3. Logging Configuration (`config/LoggingConfig.java`)
- Structured logging with correlation ID support
- Console and file output for production
- Rolling file rotation with size and time-based policies
- Service-specific configuration

### 4. Tracing Configuration (`config/TracingConfig.java`)
- Common observation predicate for all services
- Excludes health checks and actuator endpoints
- Enables distributed tracing integration

## Usage

### 1. Add Dependency to Your Service

```gradle
dependencies {
    implementation project(':edu-nexus-observability')
}
```

### 2. Use the MDC Filter

```java
// The filter is automatically applied if using Spring Boot auto-configuration
// No additional configuration needed
```

### 3. Use Metrics Annotations

```java
@Service
public class CourseService {

    @MetricTimed(value = "course.get", description = "Get course by ID")
    public Course getCourse(Long id) {
        // Method implementation
    }

    @MetricCounted(value = "course.create", description = "Course creation attempts")
    public Course createCourse(CreateCourseRequest request) {
        // Method implementation
    }
}
```

### 4. Configure Logging

```java
@Configuration
public class AppConfig {

    @PostConstruct
    public void configureLogging() {
        new LoggingConfig().configureLogging("my-service",
            !env.acceptsProfiles("local"));
    }
}
```

## Benefits

1. **Consistency**: All services use the same observability patterns
2. **Reduced Duplication**: Common code is centralized
3. **Maintenance**: Updates only need to be made in one place
4. **Standardization**: Uniform metrics and logging across the system
5. **Observability**: Better correlation and tracing across service boundaries

## Implementation Status

✅ **Common Module Created**
- `edu-nexus-observability` module created with all components
- Build configuration completed
- Lombok dependencies added

✅ **Services Updated**
- Course service: Updated to use common module
- GraphQL service: Updated to use common module
- Gateway service: Updated to use common module
- All other services: Updated MDC filter aliases

✅ **Duplicated Code Removed**
- 8 duplicate MdcFilter implementations consolidated
- Metric annotations moved from course service to common module
- Tracing configuration shared across services

## Integration

All services now inherit observability capabilities automatically when including the dependency:
- Course service: ✅ Integrated
- GraphQL service: ✅ Integrated
- Gateway service: ✅ Integrated
- Enrollment service: ✅ Updated MDC filter
- File manage service: ✅ Updated MDC filter
- User service: ✅ Updated MDC filter
- Playback service: ✅ Updated MDC filter
- Coupon service: ✅ Updated MDC filter
- Attendance service: ✅ Updated MDC filter