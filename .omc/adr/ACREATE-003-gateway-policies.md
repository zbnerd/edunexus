# ACREATE-003: Gateway Policies and Resilience Patterns

## Status
**PROPOSED** - Pending Review

## Context
The Spring Cloud Gateway currently has minimal configuration, lacking critical resilience patterns for production use.

### Current Configuration Issues
```yaml
# application-local.yml - minimal configuration
spring:
  cloud:
    gateway:
      routes:
        - id: auth_route
          uri: lb://edu-nexus-user-service
          predicates:
            - Path=/auth/**
```

**Missing:**
- No timeout configuration
- No circuit breaker
- No retry logic
- No global error handling
- Incomplete distributed tracing
- No rate limiting
- No request/response size limits

### Current Gateway Class
```java
@SpringBootApplication
@EnableDiscoveryClient
public class EduNexusGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(EduNexusGatewayApplication.class, args);
    }
}
```
Completely empty - no custom configuration beans.

## Decision
**Implement Comprehensive Gateway Resilience Policies**

### Required Policies

#### 1. Timeout Configuration
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        connect-timeout: 3000      # 3 seconds
        response-timeout: 30s      # 30 seconds
        pool:
          type: elastic
          max-connections: 500
          max-idle-time: 20s
          max-life-time: 60s
          acquire-timeout: 30000
```

#### 2. Circuit Breaker (Resilience4j)
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        sliding-window-size: 50
        failure-rate-threshold: 50
        wait-duration-in-open-state: 20s
        permitted-number-of-calls-in-half-open-state: 10
  retry:
    instances:
      user-service:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
```

#### 3. Route Configuration with Fallback
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://edu-nexus-user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: user-service
                fallbackUri: forward:/fallback/users
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,SERVICE_UNAVAILABLE
                methods: GET,POST
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
```

#### 4. Global Error Handler
```java
@Component
public class GlobalGatewayExceptionHandler
        implements WebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // Set headers
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.setStatusCode(determineStatusCode(ex));

        // Standard error format
        Map<String, Object> error = Map.of(
            "timestamp", Instant.now(),
            "path", exchange.getRequest().getPath().value(),
            "status", determineStatusCode(ex).value(),
            "error", determineStatusCode(ex).getReasonPhrase(),
            "message", getUserMessage(ex),
            "traceId", getTraceId(exchange)
        );

        return response.writeWith(Mono.just(
            response.bufferFactory().wrap(
                objectMapper.writeValueAsBytes(error)
            )
        ));
    }
}
```

#### 5. Distributed Tracing Configuration
```java
@Configuration
public class TracingConfig {

    @Bean
    public ObservationPredicate observationPredicate() {
        return (name, context) -> {
            // Exclude health checks
            if (name != null && name.contains("healthcheck")) {
                return false;
            }
            return true;
        };
    }

    @Bean
    public HandlerFilterFunction<ServerResponse, ServerResponse> tracingFilter() {
        return (request, next) -> {
            Observation observation = Observation.createNotStarted(
                "gateway.request",
                observationRegistry
            );
            observation.contextualName(request.request().path() + " " + request.request().methodName());
            return observation.observe(() -> next.handle(request));
        };
    }
}
```

#### 6. Rate Limiting
```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 100    # requests/sec
            redis-rate-limiter.burstCapacity: 200    # burst capacity
            key-resolver: "#{@ipKeyResolver}"       # by IP address
```

### Gateway Architecture
```
                    ┌─────────────────────────────────────┐
                    │         Client Request              │
                    └─────────────────┬───────────────────┘
                                      │
                    ┌─────────────────▼───────────────────┐
                    │        Rate Limiter                 │
                    │    (Per IP/Endpoint)                │
                    └─────────────────┬───────────────────┘
                                      │
                    ┌─────────────────▼───────────────────┐
                    │        Security Filter              │
                    │    (Auth/CORS)                      │
                    └─────────────────┬───────────────────┘
                                      │
                    ┌─────────────────▼───────────────────┐
                    │        Circuit Breaker Check        │
                    │    (Is service healthy?)            │
                    └─────────────────┬───────────────────┘
                                      │
                         ┌────────────┴────────────┐
                         │                         │
                    Open Circuit              Closed
                         │                         │
                    ┌────▼────┐            ┌──────▼──────┐
                    │ Fallback │            │   Timeout   │
                    │ Response │            └──────┬──────┘
                    └─────────┘                    │
                                                ┌────▼────┐
                                                │  Retry  │
                                                │ (3x)    │
                                                └────┬────┘
                                                     │
                                                ┌────▼────┐
                                                │ Service │
                                                └─────────┘
```

## Consequences

### Positive
- **Resilience**: Services can fail without cascading
- **Performance**: Timeouts prevent hanging requests
- **Reliability**: Retries handle transient failures
- **Security**: Rate limiting prevents abuse
- **Observability**: Complete tracing

### Negative
- **Complexity**: More configuration to maintain
- **Latency**: Circuit breaker checks add small overhead
- **Dependency**: Requires Redis for rate limiting

## Implementation Plan

### Phase 1: Core Resilience
1. Add timeout configuration
2. Implement circuit breaker
3. Add retry logic

### Phase 2: Error Handling
1. Create global error handler
2. Standardize error response format
3. Add fallback endpoints

### Phase 3: Observability
1. Complete distributed tracing
2. Add metrics (Micrometer)
3. Create health check endpoints

### Phase 4: Security
1. Add rate limiting
2. Configure request size limits
3. Add security headers

### Phase 5: Testing
1. Load test with various scenarios
2. Chaos engineering tests
3. Circuit breaker trigger tests

## Monitoring Required

### Metrics
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,gateway
  metrics:
    tags:
      application: ${spring.application.name}
    export:
      prometheus:
        enabled: true
```

### Key Metrics to Track
- Request latency (p50, p95, p99)
- Error rate by endpoint
- Circuit breaker state transitions
- Rate limit rejections
- Retry counts

## References
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Microservices Patterns - Circuit Breaker](https://microservices.io/patterns/reliability/circuit-breaker.html)
