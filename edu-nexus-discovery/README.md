# Service Discovery (Eureka Server)

## Overview

The Service Discovery module runs Netflix Eureka Server, acting as the central registry for all microservices in the EduNexus platform. Services register themselves on startup and discover each other through this server.

## Features

- **Service Registration**: Auto-registration of all microservices
- **Service Discovery**: Dynamic service location
- **Health Monitoring**: Heartbeat-based health checks
- **Load Balancing**: Support for client-side load balancing
- **Dashboard**: Web UI for monitoring service instances

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- Netflix Eureka Server
- Spring Cloud for service integration

### Architecture

```
┌─────────────────────────────────────────────────────┐
│              EUREKA SERVER (Port 8000)            │
│                                                    │
│  ┌────────────────────────────────────────────┐         │
│  │         EUREKA DASHBOARD               │         │
│  │     http://localhost:8000          │         │
│  └────────────────────────────────────────────┘         │
│                                                    │
└────────────────────┬────────────────────────────────┘
                     │
        ┌────────────┼────────────┬────────────┐
        │            │            │            │
   Registers    Registers    Registers    Registers
        │            │            │            │
┌───────▼────┐ ┌───▼────┐ ┌───▼────┐ ┌────▼────┐
│   Course     │ │  User   │ │Enroll   │ │  File    │
│  Service     │ │ Service │ │ Service  │ │ Service  │
│   :8001      │ │  :8004  │ │  :8002  │ │  :8003   │
└──────────────┘ └─────────┘ └──────────┘ └──────────┘
```

## Configuration

### Application Properties

```yaml
server:
  port: 8000

eureka:
  instance:
    hostname: ${EUREKA_HOSTNAME:localhost}
  client:
    register-with-eureka: false   # Don't register self
    fetch-registry: false          # Don't fetch registry
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8000/eureka/}

  server:
    enable-self-preservation: true   # Keep running during network issues
    eviction-interval-timer-in-ms: 5000  # Check every 5s
    renewal-percent-threshold: 0.85
    response-cache-update-interval-ms: 30000

spring:
  application:
    name: edu-nexus-discovery
```

### Key Settings

| Setting | Description | Default |
|----------|-------------|----------|
| `register-with-eureka` | Server registers itself | false |
| `fetch-registry` | Server fetches registry | false |
| `enable-self-preservation` | Prevent mass eviction | true |
| `eviction-interval-timer-in-ms` | Heartbeat check interval | 5000ms |
| `renewal-percent-threshold` | Renewal threshold | 0.85 (85%) |

## Eureka Dashboard

### Access

```
http://localhost:8000
```

### Information Displayed

- **Registered Services**: List of all service names
- **Instances per Service**: Replicas and availability status
- **Status**: UP (available) or DOWN (unreachable)
- **Last Heartbeat**: Time since last renewal

### Example Dashboard View

```
Environment: test
Data center: default

┌────────────────────────────────────────────────────────────┐
│ Applications                                      │
└────────────────────────────────────────────────────────────┘
  ├─ EDU-NEXUS-COURSE-SERVICE
  │   ├─ availability zones: default
  │   └─ instances
  │       └─ localhost:edu-nexus-course-service:8001
  │           status: UP
  │
  ├─ EDU-NEXUS-USER-SERVICE
  │   └─ instances
  │       └─ localhost:edu-nexus-user-service:8004
  │           status: UP
  │
  ├─ EDU-NEXUS-ENROLLMENT-SERVICE
  │   └─ instances
  │       └─ localhost:edu-nexus-enrollment-service:8002
  │           status: UP
  │
  └─ EDU-NEXUS-FILE-MANAGE-SERVICE
      └─ instances
          └─ localhost:edu-nexus-file-manage-service:8003
              status: UP
```

## Service Registration

### Client Configuration

Each microservice configures Eureka client:

```yaml
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8000/eureka/

  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

### Registration Flow

```
1. Service Startup
   ↓
2. Register with Eureka (POST /eureka/apps/{appName})
   ↓
3. Start Heartbeat (PUT /eureka/apps/{appName}/{id} every 30s)
   ↓
4. Eureka marks instance as UP
   ↓
5. Other services can now discover it
```

### Eviction Policy

- **Heartbeat missed** for 90 seconds → Instance marked DOWN
- **Eviction interval**: Every 5 seconds, Eureka checks for expired instances
- **Self-preservation**: If renewal rate drops below 85%, stop evicting

## Running Locally

```bash
# Start Eureka server (must be first)
./gradlew :edu-nexus-discovery:bootRun

# Or with Docker infrastructure
cd ../infrastructure && docker-compose up -d eureka

# Access dashboard
open http://localhost:8000
```

## Testing Discovery

### Check Registration

```bash
# List all registered applications
curl http://localhost:8000/eureka/apps

# Get specific application status
curl http://localhost:8000/eureka/apps/edu-nexus-course-service

# Get specific instance
curl http://localhost:8000/eureka/apps/edu-nexus-course-service/course-service:localhost:8001
```

### Service Discovery from Client

```java
// Using Spring Cloud LoadBalancer
@LoadBalanced
private RestTemplate restTemplate;

// Automatically discovers and load balances
Course course = restTemplate.getForObject(
    "http://edu-nexus-course-service/courses/" + id,
    Course.class
);
```

## Troubleshooting

### Services Not Registering

1. Check Eureka server is running: `curl http://localhost:8000/eureka/apps`
2. Verify service configuration: `eureka.client.service-url`
3. Check service logs for registration errors
4. Ensure network connectivity to Eureka

### Services Show as DOWN

1. Check heartbeat interval: `lease-renewal-interval-in-seconds`
2. Verify service health endpoint: `/actuator/health`
3. Check for network latency issues
4. Review Eureka eviction settings

### Self-Preservation Mode

If Eureka enters self-preservation:
```
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP
WHEN THEY ARE NOT. RENEWALS ARE LESSER THAN THRESHOLD
AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.
```

This is normal during network issues. Services will still work even if some are expired.

### Dashboard Not Loading

1. Check browser compatibility (modern browser required)
2. Verify port 8000 is accessible
3. Check for firewall blocking
4. Review Eureka server logs

## Production Deployment

### High Availability

For production, run Eureka cluster:

```yaml
eureka:
  client:
    service-url:
      defaultZone:
        - http://eureka1:8000/eureka/
        - http://eureka2:8000/eureka/
        - http://eureka3:8000/eureka/
  server:
    enable-self-preservation: true
    registry-sync-retry-wait-ms: 30000
```

### AWS Deployment

```yaml
eureka:
  instance:
    hostname: ${EC2_PRIVATE_IP}
    non-secure-port: ${server.port}
    lease-renewal-interval-in-seconds: 30
    metadata-map:
      instanceId: ${spring.application.name}:${server.port}
```

### DNS Configuration

For easier access:

```
eureka.local → EIP or Load Balancer
```

Services then use: `http://eureka.local:8000/eureka/`

## Monitoring

### Key Metrics

- **Registered instances**: Count per application
- **Renewal rate**: Percentage of successful heartbeats
- **Eviction count**: How many instances expired
- **Threshold breaches**: When self-preservation activates

### Health Check

```bash
# Eureka server health
curl http://localhost:8000/actuator/health

# Expected response
{
  "status": "UP"
}
```

## Dependencies

- Spring Cloud Netflix Eureka Server
- Spring Boot Actuator

## Related Services

All EduNexus services depend on Eureka:
- edu-nexus-course-service
- edu-nexus-user-service
- edu-nexus-enrollment-service
- edu-nexus-file-manage-service
- edu-nexus-gateway
- edu-nexus-graphql
