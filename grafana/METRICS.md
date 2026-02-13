# EduNexus Metrics Reference

This document describes all Prometheus metrics exported by EduNexus services.

## Metric Naming Conventions

All metrics follow these naming patterns:
- `counter_*` - Monotonically increasing counters
- `timer_*` - Timing/duration measurements
- `gauge_*` - Point-in-time values (can go up or down)
- `summary_*` - Distribution summaries
- `jvm_*` - JVM built-in metrics
- `http_server_requests_*` - HTTP request metrics (Spring Boot actuator)

## Common Labels

All metrics include these labels:
- `application` - Service name (e.g., `edu-nexus-course-service`)
- `environment` - Environment (dev, local, prod)
- `service` - Service type (course, user, enrollment, graphql)

## HTTP Request Metrics

### `http_server_requests_seconds`
**Type**: Histogram (Timer)
**Description**: HTTP request duration
**Labels**:
- `method` - HTTP method (GET, POST, etc.)
- `uri` - Request URI (sanitized)
- `status` - HTTP status code
- `outcome` - Request outcome (SUCCESS, CLIENT_ERROR, SERVER_ERROR)
- `exception` - Exception type (if error)

**Example PromQL**:
```promql
# P95 latency
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Request rate
rate(http_server_requests_seconds_count[5m])

# Error rate
rate(http_server_requests_seconds_count{status=~"5.."}[5m])
```

## Course Service Metrics

### `counter_course_creation_count`
**Type**: Counter
**Description**: Course creation operations
**Labels**:
- `type` - success/failure
- `reason` - Failure reason (if applicable)

### `timer_course_retrieval_duration`
**Type**: Timer
**Description**: Course retrieval operations
**Labels**:
- `operation` - Operation type (getCourse, getAllCourses, batchGetCourses)
- `status` - success/error

### `gauge_course_queries_active`
**Type**: Gauge
**Description**: Currently active course queries

### `counter_cache_access_count`
**Type**: Counter
**Description**: Cache access operations
**Labels**:
- `result` - hit/miss
- `cache` - Cache name

### `counter_rating_count`
**Type**: Counter
**Description**: Rating operations
**Labels**:
- `action` - created/updated/deleted

### `counter_cache_eviction_count`
**Type**: Counter
**Description**: Cache eviction events

### `timer_rating_operation_duration`
**Type**: Timer
**Description**: Rating operation duration

### `timer_course_retrieval`
**Type**: Timer
**Description**: Course-specific retrieval timing

## User Service Metrics

### `counter_user_registration_count`
**Type**: Counter
**Description**: User registration events

### `counter_user_login_count`
**Type**: Counter
**Description**: User login events

### `counter_user_logout_count`
**Type**: Counter
**Description**: User logout events

### `gauge_user_sessions_active`
**Type**: Gauge
**Description**: Current active user sessions

## Enrollment Service Metrics

### `counter_enrollment_count`
**Type**: Counter
**Description**: Enrollment operations
**Labels**:
- `action` - created/cancelled

### `timer_enrollment_operation_duration`
**Type**: Timer
**Description**: Enrollment operation duration

## Kafka Metrics

### `counter_kafka_publish_count`
**Type**: Counter
**Description**: Kafka message publish events
**Labels**:
- `topic` - Kafka topic name

### `counter_kafka_consume_count`
**Type**: Counter
**Description**: Kafka message consume events
**Labels**:
- `topic` - Kafka topic name

### `counter_kafka_processing_failure_count`
**Type**: Counter
**Description**: Kafka message processing failures

### `timer_kafka_processing_duration`
**Type**: Timer
**Description**: Kafka message processing duration

## JVM Metrics (Built-in)

### Memory
- `jvm_memory_used_bytes` - Current memory usage
- `jvm_memory_committed_bytes` - Committed memory
- `jvm_memory_max_bytes` - Maximum memory
**Labels**:
- `area` - heap/non-heap
- `id` - Memory pool name

### Threads
- `jvm_threads_live_threads` - Current live thread count
- `jvm_threads_peak_threads` - Peak thread count
- `jvm_threads_daemon_threads` - Daemon thread count
- `jvm_threads_state_threads` - Thread count by state

### GC
- `jvm_gc_pause_seconds` - GC pause duration
- `jvm_gc_pause_seconds_count` - GC pause count
**Labels**:
- `action` - GC action
- `cause` - GC cause

### CPU
- `system_cpu_usage` - System CPU usage (0-1)
- `process_cpu_usage` - Process CPU usage (0-1)

## Using Metrics in Code

### Using MetricsRegistry

```java
@Autowired
private MetricsRegistry metrics;

public void doWork() {
    // Counter
    metrics.counter("work.done")
        .tag("type", "success")
        .increment();

    // Timer
    metrics.timer("work.duration")
        .tag("operation", "doWork")
        .record(() -> {
            // work here
        });

    // Gauge for AtomicInteger
    AtomicInteger activeWorkers = metrics.registerAtomicGauge(
        "workers.active",
        new AtomicInteger(0)
    );
}
```

### Using Micrometer Annotations

```java
@Timed(value = "method.execution", percentiles = {0.5, 0.95, 0.99})
public void importantMethod() {
    // method implementation
}

@Counted(value = "method.calls", description = "Method invocation count")
public void countedMethod() {
    // method implementation
}
```

## Grafana Dashboards

Three dashboards are provided in `grafana/dashboards/`:

1. **edu-nexus-overview.json** - System-wide overview
   - Request rate
   - Error rate
   - P95 latency
   - JVM memory/threads/CPU

2. **service-performance.json** - Per-service performance
   - Request rate by service & endpoint
   - Response time percentiles
   - Error rate by service
   - Heap memory & GC

3. **business-metrics.json** - Business KPIs
   - Enrollments
   - Course creation
   - Rating operations
   - Active sessions
   - Cache performance
   - Kafka throughput

## Query Examples

### Find slow endpoints
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (application, uri, le))
```

### Error rate by service
```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
```

### Cache hit ratio
```promql
sum(rate(cache_access_count_total{result="hit"}[5m])) /
sum(rate(cache_access_count_total[5m]))
```

### Active sessions trend
```promql
gauge_user_sessions_active
```

## Alerting Rules

Recommended Prometheus alerting rules:

### High Error Rate
```yaml
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
  for: 5m
  annotations:
    summary: "High error rate on {{ $labels.application }}"
```

### High Latency
```yaml
- alert: HighLatency
  expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
  for: 5m
  annotations:
    summary: "High P95 latency on {{ $labels.application }}"
```

### Low Cache Hit Rate
```yaml
- alert: LowCacheHitRate
  expr: sum(rate(cache_access_count_total{result="hit"}[5m])) / sum(rate(cache_access_count_total[5m])) < 0.5
  for: 10m
  annotations:
    summary: "Low cache hit rate for {{ $labels.cache }}"
```
