# EduNexus Monitoring Guide

This guide covers monitoring and observability for the EduNexus microservices architecture, including Prometheus metrics, Grafana dashboards, alerting, and troubleshooting.

## Table of Contents

1. [Overview](#overview)
2. [Prometheus Queries](#prometheus-queries)
3. [Dashboard Interpretation](#dashboard-interpretation)
4. [Alert Threshold Recommendations](#alert-threshold-recommendations)
5. [Troubleshooting Guide](#troubleshooting-guide)

---

## Overview

EduNexus uses a comprehensive observability stack:

- **Prometheus**: Metrics collection and storage
- **Grafana**: Visualization and dashboards
- **Micrometer**: Metrics instrumentation in Spring Boot
- **Custom Business Metrics**: Domain-specific metrics via `BusinessMetrics` class

### Available Dashboards

| Dashboard | Purpose | UID |
|-----------|---------|-----|
| Service Overview | Overall system health across all services | `edunexus-service-overview` |
| Course Service | Course-specific metrics including cache and Kafka | `edunexus-course-service` |
| Enrollment Saga | Saga flow monitoring and compensation tracking | `edunexus-enrollment-saga` |

---

## Prometheus Queries

### Service-Level Metrics

#### HTTP Request Rate

```promql
# Total request rate across all services
sum(rate(http_server_requests_seconds_count{uri!="/actuator/*"}[$__interval]))

# Request rate by service
sum(rate(http_server_requests_seconds_count{uri!="/actuator/*"}[$__interval])) by (application)

# Request rate by endpoint
sum(rate(http_server_requests_seconds_count{application="edu-nexus-course-service"}[$__interval])) by (uri, method)
```

#### Error Rate

```promql
# Overall 5xx error rate
sum(rate(http_server_requests_seconds_count{status=~"5.."}[$__interval])) /
sum(rate(http_server_requests_seconds_count[$__interval]))

# Error rate by service
sum(rate(http_server_requests_seconds_count{status=~"5.."}[$__interval])) by (application) /
sum(rate(http_server_requests_seconds_count) by (application)[$__interval])

# 4xx vs 5xx errors
sum(rate(http_server_requests_seconds_count{status=~"4.."}[$__interval]))
sum(rate(http_server_requests_seconds_count{status=~"5.."}[$__interval]))
```

#### Latency Percentiles

```promql
# P50, P95, P99 latency across all services
histogram_quantile(0.50, sum(rate(http_server_requests_seconds_bucket[$__interval])) by (le))
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[$__interval])) by (le))
histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[$__interval])) by (le))

# Per-service P95 latency
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application="edu-nexus-course-service"}[$__interval])) by (le))
```

### JVM Metrics

#### Memory Usage

```promql
# Heap memory usage percentage
sum(jvm_memory_used_bytes{area="heap"}) / sum(jvm_memory_max_bytes{area="heap"})

# Heap memory by service
sum(jvm_memory_used_bytes{area="heap"}) by (application)
sum(jvm_memory_max_bytes{area="heap"}) by (application)

# Non-heap memory (Metaspace, Code Cache, etc.)
jvm_memory_used_bytes{area="nonheap"}
```

#### Thread Count

```promql
# Live threads by service
jvm_threads_live_threads

# Thread states
jvm_threads_state_threads

# Peak vs current threads
jvm_threads_peak_threads
jvm_threads_live_threads
```

#### GC Metrics

```promql
# GC time spent
rate(jvm_gc_pause_seconds_sum[$__interval])

# GC count
rate(jvm_gc_pause_seconds_count[$__interval])

# GC time by service
rate(jvm_gc_pause_seconds_sum[$__interval]) by (application)
```

### Database Connection Pool

```promql
# Active connections
hikaricp_connections_active

# Idle connections
hikaricp_connections_idle

# Connection pool usage
hikaricp_connections_active / hikaricp_connections_max

# Waiting threads
hikaricp_connections_pending
```

### Cache Metrics (Redis)

```promql
# Cache hit ratio
sum(rate(cache_access_count{result="hit"}[$__interval])) /
(sum(rate(cache_access_count{result="hit"}[$__interval])) +
 sum(rate(cache_access_count{result="miss"}[$__interval])))

# Raw hits and misses
sum(rate(cache_access_count{result="hit"}[$__interval]))
sum(rate(cache_access_count{result="miss"}[$__interval]))

# Cache evictions
sum(rate(cache_eviction_count[$__interval]))
```

### Kafka Metrics

```promql
# Producer rate
sum(rate(kafka_publish_count[$__interval]))

# Consumer rate
sum(rate(kafka_consume_count[$__interval]))

# Consumer lag (records pending)
kafka_consumer_records_lag

# Processing failures
sum(rate(kafka_processing_failure_count[$__interval]))

# Consumer lag by topic
kafka_consumer_records_lag{topic="course-rating-add"}
```

### Business Metrics

#### Course Operations

```promql
# Course creation rate
sum(rate(course_creation_count[$__interval]))

# Course creation success vs failure
sum(rate(course_creation_count{type="success"}[$__interval]))
sum(rate(course_creation_count{type="failure"}[$__interval]))

# Course retrieval latency
histogram_quantile(0.95, sum(rate(course_retrieval_duration_seconds_bucket[$__interval])) by (le))
```

#### Enrollment Metrics

```promql
# Enrollment creation rate
sum(rate(enrollment_count{action="created"}[$__interval]))

# Enrollment cancellation rate
sum(rate(enrollment_count{action="cancelled"}[$__interval]))

# Enrollment operation duration
histogram_quantile(0.95, sum(rate(enrollment_operation_duration_seconds_bucket[$__interval])) by (le))
```

#### Rating Metrics

```promql
# Rating operations rate
sum(rate(rating_count{action="created"}[$__interval]))
sum(rate(rating_count{action="updated"}[$__interval]))
sum(rate(rating_count{action="deleted"}[$__interval]))

# Rating operation latency
histogram_quantile(0.95, sum(rate(rating_operation_duration_seconds_bucket[$__interval])) by (le))
```

### Saga/Orchestration Metrics

```promql
# Saga state distribution
sum(saga_state_count{state="pending"})
sum(saga_state_count{state="confirmed"})
sum(saga_state_count{state="failed"})
sum(saga_state_count{state="compensating"})

# Compensation transaction rate
sum(rate(saga_compensation_count[$__interval]))

# End-to-end saga duration
histogram_quantile(0.95, sum(rate(saga_duration_seconds_bucket[$__interval])) by (le))
histogram_quantile(0.99, sum(rate(saga_duration_seconds_bucket[$__interval])) by (le))

# Saga success rate
(sum(rate(enrollment_count{action="created"}[$__interval])) -
 sum(rate(saga_compensation_count[$__interval]))) /
sum(rate(enrollment_count{action="created"}[$__interval]))
```

---

## Dashboard Interpretation

### Service Overview Dashboard

This dashboard provides a bird's-eye view of all services in the system.

#### Key Panels

1. **Total Request Rate**: Shows overall system throughput. Monitor for:
   - Sudden drops indicating service issues
   - Spikes that may indicate DDoS or legitimate load changes

2. **Error Rate**: Percentage of 5xx responses
   - **< 0.1%**: Healthy
   - **0.1% - 1%**: Investigate
   - **> 1%**: Critical

3. **Request Latency (Percentiles)**: Response time distribution
   - **P50**: Median response time
   - **P95**: 95th percentile (95% of requests complete faster)
   - **P99**: Tail latency, affects user experience most

4. **JVM Heap Memory**: Memory usage by service
   - Consistently near max = potential memory leak
   - Sudden drops = GC events

5. **CPU Usage**: Per-service CPU consumption
   - Spikes may indicate inefficient code or GC pressure

6. **Database Connection Pool**: Connection usage
   - **Active near Max**: Pool exhaustion risk
   - **Many Idle**: May indicate pool over-provisioning

### Course Service Dashboard

Service-specific dashboard with focus on caching and Kafka integration.

#### Key Panels

1. **Course CRUD Request Rate**: REST API operation breakdown
   - Compare read vs write patterns
   - Monitor for unexpected endpoint usage

2. **Cache Hit Ratio**: Redis cache effectiveness
   - **> 80%**: Good cache performance
   - **50-80%**: Review cache strategy
   - **< 50%**: Cache may be hurting performance

3. **Cache Evictions**: How often items are removed from cache
   - High evictions + low hit ratio = cache too small
   - Consider increasing cache size or TTL

4. **Rating Operations Rate**: CRUD on course ratings
   - Monitor for unusual spikes
   - Correlate with course enrollment events

5. **Kafka Producer/Consumer Rate**: Message throughput
   - Publish vs consume gap indicates consumer lag
   - **Processing Failures** panel shows consumer health

### Enrollment Saga Dashboard

Monitors the distributed transaction/saga orchestration for course enrollment.

#### Key Panels

1. **Saga State Distribution**: Current state of all active sagas
   - **Pending**: Normal operation state
   - **Confirmed**: Successfully completed
   - **Failed**: Requires investigation
   - **Compensating**: Rolling back transaction

2. **Compensation Transaction Rate**: Rollback operations
   - Should be near zero in normal operation
   - Spikes indicate system issues or integration failures

3. **Compensation Ratio**: Percentage of transactions requiring rollback
   - **< 1%**: Acceptable
   - **1-5%**: Review integration dependencies
   - **> 5%**: Critical issue in saga flow

4. **Saga Success Rate**: End-to-end success percentage
   - **> 99%**: Target
   - **< 95%**: Significant issues requiring attention

5. **End-to-End Latency**: Total time for complete enrollment flow
   - Includes all microservice calls and compensation
   - Compare against SLA requirements

---

## Alert Threshold Recommendations

### Critical Alerts (Immediate Action Required)

| Metric | Threshold | Duration | Severity |
|--------|-----------|----------|----------|
| Service Down | `up{job=~".*"} == 0` | 30s | Critical |
| Error Rate | `> 5%` | 1m | Critical |
| P99 Latency | `> 5s` | 2m | Critical |
| DB Pool Exhaustion | `hikaricp_connections_active / hikaricp_connections_max > 0.9` | 1m | Critical |
| Heap Usage | `> 90%` | 3m | Critical |
| Saga Failure Rate | `> 10%` | 5m | Critical |

### Warning Alerts (Investigate Soon)

| Metric | Threshold | Duration | Severity |
|--------|-----------|----------|----------|
| Error Rate | `> 1%` | 5m | Warning |
| P95 Latency | `> 1s` | 5m | Warning |
| Cache Hit Ratio | `< 50%` | 10m | Warning |
| Consumer Lag | `> 1000` | 5m | Warning |
| CPU Usage | `> 80%` | 5m | Warning |
| Compensation Rate | `> 2%` | 10m | Warning |

### Info Alerts (Monitor)

| Metric | Threshold | Duration | Severity |
|--------|-----------|----------|----------|
| Request Rate Change | `> 50%` from baseline | 10m | Info |
| New 5xx Errors | `count > 0` where usually 0 | 1m | Info |
| GC Time Increase | `> 2x` baseline | 5m | Info |

### Example Prometheus Alert Rules

```yaml
groups:
  - name: edunexus-critical
    interval: 30s
    rules:
      # Service availability
      - alert: ServiceDown
        expr: up{job=~"edu-nexus-.*"} == 0
        for: 30s
        labels:
          severity: critical
        annotations:
          summary: "Service {{ $labels.job }} is down"
          description: "{{ $labels.job }} has been down for more than 30 seconds"

      # High error rate
      - alert: HighErrorRate
        expr: |
          sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) /
          sum(rate(http_server_requests_seconds_count[5m])) > 0.05
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "High error rate detected"
          description: "Error rate is {{ $value | humanizePercentage }}"

      # DB pool exhaustion
      - alert: DatabasePoolExhausted
        expr: |
          hikaricp_connections_active / hikaricp_connections_max > 0.9
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Database connection pool nearly exhausted"
          description: "{{ $labels.application }} pool usage at {{ $value | humanizePercentage }}"

      # Memory pressure
      - alert: HighMemoryUsage
        expr: |
          sum(jvm_memory_used_bytes{area="heap"}) /
          sum(jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 3m
        labels:
          severity: critical
        annotations:
          summary: "High heap memory usage"
          description: "{{ $labels.application }} heap usage at {{ $value | humanizePercentage }}"

  - name: edunexus-warnings
    interval: 30s
    rules:
      # Cache hit ratio warning
      - alert: LowCacheHitRatio
        expr: |
          sum(rate(cache_access_count{result="hit"}[5m])) /
          (sum(rate(cache_access_count{result="hit"}[5m])) +
           sum(rate(cache_access_count{result="miss"}[5m]))) < 0.5
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Low cache hit ratio"
          description: "{{ $labels.application }} cache hit ratio is {{ $value | humanizePercentage }}"

      # Kafka consumer lag
      - alert: KafkaConsumerLag
        expr: kafka_consumer_records_lag > 1000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumer lag detected"
          description: "{{ $labels.topic }} consumer lag is {{ $value }}"

      # High saga compensation rate
      - alert: HighCompensationRate
        expr: |
          sum(rate(saga_compensation_count[5m])) /
          sum(rate(enrollment_count{action="created"}[5m])) > 0.02
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "High saga compensation rate"
          description: "Compensation rate is {{ $value | humanizePercentage }}"
```

---

## Troubleshooting Guide

### High Error Rate

**Symptoms**: Spike in 5xx errors in Service Overview dashboard

**Investigation Steps**:

1. Identify the affected service:
   ```promql
   sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) by (application)
   ```

2. Check error breakdown by endpoint:
   ```promql
   sum(rate(http_server_requests_seconds_count{status=~"5..",application="affected-service"}[5m])) by (uri)
   ```

3. Check application logs for the affected service

4. Verify downstream dependencies:
   - Database connectivity (check connection pool metrics)
   - Other microservices (check their error rates)
   - External APIs (if applicable)

**Common Causes**:
- Database connection exhaustion
- Memory pressure causing OOM
-下游服务不可用
- Configuration errors
- Code bugs causing exceptions

### Slow Response Times

**Symptoms**: P95/P99 latency elevated

**Investigation Steps**:

1. Check JVM GC metrics:
   ```promql
   rate(jvm_gc_pause_seconds_sum[5m])
   rate(jvm_gc_pause_seconds_count[5m])
   ```
   High GC time often correlates with slow responses

2. Check database query performance:
   ```promql
   hikaricp_connections_active
   hikaricp_connections_pending
   ```
   Long wait times for connections indicate slow queries

3. Check for cache misses:
   ```promql
   sum(rate(cache_access_count{result="miss"}[5m]))
   ```
   High miss rates cause cache stampedes

**Common Fixes**:
- Add/fix database indexes
- Increase cache sizes/TTL
- Optimize N+1 queries
- Increase heap size
- Tune GC settings

### Memory Leaks

**Symptoms**: Heap usage steadily increases, frequent GC

**Investigation Steps**:

1. Monitor heap trend over hours:
   ```promql
   sum(jvm_memory_used_bytes{area="heap"}) by (application)
   ```

2. Check GC frequency:
   ```promql
   rate(jvm_gc_pause_seconds_count[5m])
   ```

3. Take a heap dump for analysis (via JVM tools)

**Common Causes**:
- Unbounded collections
- Caches without eviction
- Thread local leaks
- Class loader leaks in hot reload scenarios

### Cache Performance Issues

**Symptoms**: Low hit ratio, high eviction rate

**Investigation Steps**:

1. Check cache hit ratio:
   ```promql
   sum(rate(cache_access_count{result="hit"}[5m])) /
   sum(rate(cache_access_count[5m]))
   ```

2. Check eviction rate:
   ```promql
   sum(rate(cache_eviction_count[5m]))
   ```

3. Correlate with request rate to identify cache stampedes

**Common Fixes**:
- Increase cache size
- Adjust TTL values
- Implement cache warming
- Use cache-aside pattern properly
- Consider sharding for large datasets

### Kafka Consumer Lag

**Symptoms**: Consumer not keeping up with producers

**Investigation Steps**:

1. Check consumer lag:
   ```promql
   kafka_consumer_records_lag
   ```

2. Check consumer processing rate:
   ```promql
   sum(rate(kafka_consume_count[5m]))
   ```

3. Check processing failures:
   ```promql
   sum(rate(kafka_processing_failure_count[5m]))
   ```

**Common Fixes**:
- Increase consumer instances
- Optimize message processing logic
- Fix bugs causing consumer failures
- Increase partition count for parallelism

### Saga Failures

**Symptoms**: High compensation rate, failed sagas

**Investigation Steps**:

1. Check saga state distribution:
   ```promql
   sum(saga_state_count) by (state)
   ```

2. Identify failing saga steps:
   - Check logs for specific compensation triggers
   - Verify downstream service health

3. Check timeout settings

**Common Fixes**:
- Increase timeout for slow services
- Fix failing downstream operations
- Improve saga orchestration logic
- Add retry with exponential backoff

### Database Connection Pool Exhaustion

**Symptoms**: Active connections near max, pending threads

**Investigation Steps**:

1. Check pool metrics:
   ```promql
   hikaricp_connections_active
   hikaricp_connections_max
   hikaricp_connections_pending
   ```

2. Correlate with slow query logs

3. Check for connection leaks (connections not returned)

**Common Fixes**:
- Fix slow queries (indexing, query optimization)
- Increase max pool size
- Reduce connection timeout
- Fix connection leaks in code
- Enable connection validation

### Dashboard Shows No Data

**Symptoms**: Panels showing "No data" or flatlines

**Investigation Steps**:

1. Check Prometheus targets:
   - Visit http://localhost:9090/targets
   - Verify all services are "UP"

2. Check service health:
   ```promql
   up{job=~"edu-nexus-.*"}
   ```

3. Verify actuator endpoints are exposed:
   - http://localhost:8001/actuator/prometheus
   - http://localhost:8002/actuator/prometheus

**Common Fixes**:
- Restart Prometheus
- Restart affected service
- Check network connectivity
- Verify Prometheus configuration
- Check actuator security settings

---

## Monitoring Best Practices

1. **Set meaningful baselines**: Understand normal behavior before alerting
2. **Use SLO-based alerting**: Alert on user impact, not just metrics
3. **Reduce noise**: Tune thresholds to avoid alert fatigue
4. **Document runbooks**: Create SOPs for common incidents
5. **Review dashboards weekly**: Ensure they remain relevant
6. **Test alerts**: Verify alert rules work during maintenance windows
7. **Correlate metrics**: Use dashboard links to jump between related metrics
8. **Monitor the monitoring**: Ensure Prometheus/Grafana stay healthy

---

## Quick Reference

### Dashboard Access

- **Grafana**: http://localhost:3000 (default credentials: admin/admin)
- **Prometheus**: http://localhost:9090
- **Service Actuator**: http://localhost:8001/actuator

### Important Ports

| Service | Port |
|---------|------|
| Discovery | 8000 |
| Course Service | 8001 |
| Enrollment Service | 8002 |
| File Service | 8003 |
| User Service | 8004 |
| Playback Service | 8005 |
| GraphQL Gateway | 8006 |
| Kafka UI | 8089 |
| Prometheus | 9090 |
| Grafana | 3000 |

### Restart Commands

```bash
# Restart Prometheus
docker-compose restart prometheus

# Restart Grafana
docker-compose restart grafana

# Restart all infrastructure
cd infrastructure && docker-compose restart
```

### Health Check Quick Queries

```bash
# All services up?
curl -s http://localhost:9090/api/v1/query?query=up | jq

# Current error rate?
curl -s 'http://localhost:9090/api/v1/query?query=sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))' | jq

# Total request rate?
curl -s 'http://localhost:9090/api/v1/query?query=sum(rate(http_server_requests_seconds_count[5m]))' | jq
```
