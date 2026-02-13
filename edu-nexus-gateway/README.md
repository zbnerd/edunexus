# API Gateway

## Overview

The API Gateway is the single entry point for all client requests in the EduNexus platform. It routes requests to appropriate microservices, provides cross-cutting concerns, and handles fallback logic when services are unavailable.

## Features

- **Service Routing**: Route requests to appropriate microservices
- **Load Balancing**: Distribute traffic across service instances
- **Circuit Breaker**: Prevent cascading failures with fallback
- **Service Discovery**: Dynamic routing via Eureka
- **Global CORS**: Centralized CORS configuration
- **Fallback Handling**: Graceful degradation when services are down

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- Spring Cloud Gateway for reactive routing
- Eureka Client for service discovery
- Resilience4j for circuit breaker
- Netty as embedded server

### Architecture

```
                    ┌─────────────────┐
                    │   Client       │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │  API Gateway    │
                    │   (Port 8080)   │
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼───────┐ ┌──────▼──────┐ ┌─────▼──────┐
│ Course Service   │ │ User Service  │ │ Enrollment  │
│     :8001       │ │    :8004     │ │   :8002     │
└─────────────────┘ └───────────────┘ └─────────────┘
```

## Configuration

### Application Properties

```yaml
spring:
  application:
    name: edu-nexus-gateway

  cloud:
    gateway:
      routes:
        # User Service Routes
        - id: user-service
          uri: lb://edu-nexus-user-service
          predicates:
            - Path=/api/users/**, /auth/**
          filters:
            - StripPrefix=0

        # Course Service Routes
        - id: course-service
          uri: lb://edu-nexus-course-service
          predicates:
            - Path=/api/courses/**
          filters:
            - StripPrefix=0

        # Enrollment Service Routes
        - id: enrollment-service
          uri: lb://edu-nexus-enrollment-service
          predicates:
            - Path=/api/enrollments/**
          filters:
            - StripPrefix=0

        # File Service Routes
        - id: file-service
          uri: lb://edu-nexus-file-manage-service
          predicates:
            - Path=/api/files/**, /stream/**
          filters:
            - StripPrefix=0

      default-filters:
        - Retry=3
        - CircuitBreaker=resilience4jCircuitBreaker

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/

resilience4j:
  circuitbreaker:
    instances:
      resilience4jCircuitBreaker:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 10
```

### Route Configuration

| Route ID | Path Pattern | Target Service | Prefix Stripped |
|-----------|---------------|------------------|-----------------|
| user-service | `/api/users/**`, `/auth/**` | edu-nexus-user-service | No |
| course-service | `/api/courses/**` | edu-nexus-course-service | No |
| enrollment-service | `/api/enrollments/**` | edu-nexus-enrollment-service | No |
| file-service | `/api/files/**`, `/stream/**` | edu-nexus-file-manage-service | No |

## Circuit Breaker

### Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      resilience4jCircuitBreaker:
        failure-rate-threshold: 50      # Open after 50% failure
        wait-duration-in-open-state: 30s   # Wait 30s before retry
        sliding-window-size: 10            # Last 10 requests
        minimum-number-of-calls: 5          # Minimum before calculating
```

### States

1. **Closed**: Requests pass through normally
2. **Open**: Requests fail fast, fallback triggered
3. **Half-Open**: Test if service has recovered

## Fallback Handlers

### When Service Unavailable

```http
GET /api/courses/1
```

**If course-service is down:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Course service is temporarily unavailable. Please try again later.",
  "path": "/api/courses/1"
}
```

## CORS Configuration

### Global CORS Settings

```java
@Bean
public CorsWebFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("*");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    return new CorsWebFilter(source);
}
```

Allows all origins, headers, and methods. Restrict in production.

## Monitoring

### Actuator Endpoints

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Gateway routes
curl http://localhost:8080/actuator/gateway/routes

# Circuit breaker state
curl http://localhost:8080/actuator/circuitbreakers
```

## Running Locally

```bash
# Build gateway
./gradlew :edu-nexus-gateway:build

# Start gateway (requires Eureka running)
./gradlew :edu-nexus-gateway:bootRun

# With Eureka infrastructure
cd ../infrastructure && docker-compose up -d eureka
```

## Testing Routes

### Via Gateway

```bash
# All requests go through gateway on port 8080

# Get course (routed to :8001)
curl http://localhost:8080/api/courses/1

# Login user (routed to :8004)
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'

# Get enrollment (routed to :8002)
curl http://localhost:8080/api/enrollments/saga/status/saga-123

# Stream video (routed to :8003)
curl http://localhost:8080/stream/1
```

### Direct to Service

```bash
# Bypass gateway (for testing)
curl http://localhost:8001/courses/1
curl http://localhost:8004/auth/login
```

## Troubleshooting

### 502 Bad Gateway

Service is registered but unreachable:
1. Check service is running: `curl http://localhost:8001/actuator/health`
2. Verify Eureka registration: Check http://localhost:8000/eureka/apps
3. Review gateway logs for routing errors

### 503 Service Unavailable

Circuit breaker is open:
1. Check target service health
2. Review failure rate metrics
3. Wait for circuit to close (30s default)
3. Check `CircuitBreakerConfig` settings

### Routes Not Working

1. Verify route predicates match request path
2. Check `StripPrefix` filter configuration
3. Ensure service name matches Eureka registration
4. Review all routes: `curl http://localhost:8080/actuator/gateway/routes`

### CORS Errors

If browser shows CORS error:
1. Verify `CorsWebFilter` is registered
2. Check browser console for preflight OPTIONS request
3. Ensure origin is in allowed list
4. Verify credentials are allowed if using cookies

## Production Considerations

### Security

- [ ] Rate limiting per route
- [ ] JWT validation filter
- [ ] Request logging for audit
- [ ] WAF integration
- [ ] DDoS protection
- [ ] Restrict CORS to specific domains

### Performance

- [ ] Response caching
- [ ] Request compression
- [ ] Connection pooling
- [ ] Keep-alive tuning
- [ ] Metrics dashboard

### High Availability

- [ ] Multiple gateway instances
- [ ] Load balancer in front of gateways
- [ ] Health check endpoint for LB
- [ ] Graceful shutdown

## Dependencies

- Spring Cloud Gateway
- Eureka Client
- Resilience4j
- Spring Boot Actuator

## Related Services

- **edu-nexus-discovery**: Service registry
- **edu-nexus-course-service**: :8001
- **edu-nexus-user-service**: :8004
- **edu-nexus-enrollment-service**: :8002
- **edu-nexus-file-manage-service**: :8003
