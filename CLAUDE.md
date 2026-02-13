# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**EduNexus** is an archived educational academy reservation system built as a learning project for Microservices Architecture (MSA) and distributed systems. The project is intentionally left in an archived state as a case study for over-engineering - see the README.md for detailed retrospectives.

**Status:** ARCHIVED - See [MapleExpectation](https://github.com/zbnerd/MapleExpectation) for the developer's improved approach focusing on fundamentals and performance optimization.

## Tech Stack

- **Java 21** with Spring Boot 3.4.0
- **Spring Cloud 2024.0.0** for microservices (Eureka, Gateway)
- **Kafka** 3-node cluster for event-driven communication
- **gRPC** for inter-service communication (grpc-common module)
- **GraphQL** as an alternative API gateway
- **QueryDSL** for type-safe queries
- **Redis** for caching with embedded Redis for testing
- **MySQL 8.0** - each service has its own database
- **Gradle** for build automation

## Build and Run Commands

```bash
# Build entire project
./gradlew build

# Build specific service
./gradlew :edu-nexus-course-service:build

# Run tests (JUnit 5)
./gradlew test

# Run specific service locally
./gradlew :edu-nexus-course-service:bootRun

# Build all Docker images
./build-docker-image.sh

# Start all infrastructure (MySQL, Redis, Kafka cluster, Kafka UI)
cd infrastructure && docker-compose up -d
```

## Architecture

### Microservices Structure
The project consists of 11 separate modules (defined in `settings.gradle`):

| Service | Port | Purpose |
|---------|------|---------|
| edu-nexus-discovery | 8000 | Eureka Server (service discovery) |
| edu-nexus-course-service | 8001 | Course and session management |
| edu-nexus-enrollment-service | 8002, 9002 | Course enrollment |
| edu-nexus-file-manage-service | 8003 | File upload/management |
| edu-nexus-user-service | 8004 | User management |
| edu-nexus-playback-service | 8005, 9005 | Video playback |
| edu-nexus-graphql | 8006 | GraphQL API gateway |
| edu-nexus-coupon-service | - | Coupon system |
| edu-nexus-attendance-service | - | Attendance tracking |
| edu-nexus-gateway | - | Spring Cloud Gateway |
| edu-nexus-grpc-common | - | Shared gRPC definitions |

### Hexagonal Architecture
Services are structured following hexagonal architecture patterns:
- `adapter/` - External interfaces (REST controllers, persistence adapters)
- `application/` - Application services and use cases
- `domain/` - Core business logic and entities
- `port/` - Ports/interfaces for external communication

### Environment Profiles
- `dev` - Development environment
- `local` - Local development with embedded services

## Key Implementation Details

### Kafka Event-Driven Architecture
- 3-broker Kafka cluster for high availability
- Course rating system uses Kafka with Redis-based DP (Dynamic Programming) for average calculation
- Compensating transactions ensure Redis-DB data integrity
- Kafka UI available at http://localhost:8089

### Service Discovery
- All services register with Eureka (port 8000)
- Services communicate via Eureka for service location

### QueryDSL Configuration
Q-classes are generated in `build/generated` directory. The build configuration includes source root handling to avoid compilation issues with newer Gradle/QueryDSL versions.

## Testing

- **JUnit 5** for unit testing
- **Embedded Redis** (`it.ozimov:embedded-redis:0.7.3`) for integration tests
- Tests located in standard `src/test/java/` location

## Database

- Each microservice has its own MySQL database
- Example: `next_course` for course service
- Init scripts in `infrastructure/db/mysql/init/`
