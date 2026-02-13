# Course Service

## Overview

The Course Service is a core microservice in the EduNexus platform, responsible for managing courses, sessions, and ratings. It implements hexagonal architecture with clean separation between domain logic and external adapters.

## Features

- **Course Management**: Create, update, and retrieve courses
- **Session Management**: Manage course sessions (lectures/lessons within a course)
- **Rating System**: Comprehensive course rating with Kafka-based cache invalidation
- **Search**: Full-text search across course titles and descriptions
- **Caching**: Redis-based caching with cache-aside pattern
- **Observability**: Metrics collection via Micrometer and custom business metrics

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- MySQL 8.0 for persistence
- Redis for caching
- Kafka for event-driven cache invalidation
- QueryDSL for type-safe queries
- Micrometer for metrics

### Hexagonal Architecture Layers

```
edu-nexus-course-service/
├── adapter/
│   ├── in/web/          # REST controllers (inbound adapters)
│   └── out/persistence/   # JPA entities & repositories (outbound adapters)
├── application/
│   └── service/          # Application services
├── domain/
│   └── course/           # Domain logic (DTOs, utilities)
└── port/
    └── in/               # Use case interfaces (ports)
```

## API Endpoints

### Courses

#### Create Course
```http
POST /courses
Content-Type: application/json

{
  "title": "Introduction to Microservices",
  "description": "Learn MSA patterns",
  "instructorId": 1
}
```

#### Update Course
```http
PUT /courses/{courseId}
Content-Type: application/json

{
  "title": "Advanced Microservices",
  "description": "Deep dive into MSA",
  "instructorId": 1
}
```

#### Get Course
```http
GET /courses/{courseId}
```

Returns course details with average rating.

#### List Courses
```http
GET /courses?title=java&page=0&size=20
```

Supports search by `title` and `description`.

#### Batch Get Courses
```http
POST /courses/batch
Content-Type: application/json

[1, 2, 3, 4, 5]
```

Optimized for GraphQL batch loading to avoid N+1 queries.

### Course Sessions

#### Add Session to Course
```http
POST /courses/{courseId}/sessions
Content-Type: application/json

{
  "sessionTitle": "Lesson 1",
  "sessionDescription": "Introduction",
  "videoUrl": "https://example.com/video.mp4",
  "orderIndex": 1
}
```

#### Update Session
```http
PUT /courses/{courseId}/sessions/{sessionId}
Content-Type: application/json

{
  "sessionTitle": "Lesson 1 - Updated",
  "sessionDescription": "Introduction to concepts",
  "videoUrl": "https://example.com/video2.mp4",
  "orderIndex": 1
}
```

#### Get Session
```http
GET /courses/{courseId}/sessions/{sessionId}
```

#### List Sessions
```http
GET /courses/{courseId}/sessions
```

### Course Ratings

#### Add Rating
```http
POST /courses/{courseId}/ratings
Content-Type: application/json

{
  "userId": 1,
  "rating": 5,
  "comment": "Excellent course!"
}
```

Rating must be between 1-5.

#### Update Rating
```http
PUT /courses/{courseId}/ratings/{ratingId}
Content-Type: application/json

{
  "rating": 4,
  "comment": "Updated review"
}
```

#### Delete Rating
```http
DELETE /courses/{courseId}/ratings/{ratingId}
```

#### Get Average Rating
```http
GET /courses/{courseId}/ratings/average
```

Returns average rating from Redis cache.

#### Get Average Rating from DB
```http
GET /courses/{courseId}/ratings/average/db
```

Bypasses cache - useful for debugging.

#### List All Ratings
```http
GET /courses/{courseId}/ratings
```

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/next_course
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092,localhost:9093,localhost:9094}

server:
  port: 8001

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/
```

### Environment Variables

| Variable | Description | Default |
|-----------|-------------|----------|
| `DB_USERNAME` | Database username | Required |
| `DB_PASSWORD` | Database password | Required |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | localhost:9092,9093,9094 |

## Kafka Events

### Topics

- `course-rating-added`: Published when a new rating is added
- `course-rating-updated`: Published when a rating is modified
- `course-rating-deleted`: Published when a rating is deleted

### Event Payloads

```java
// Rating Added Event
{
  "eventId": "...",
  "eventType": "RATING_ADDED",
  "courseId": 1,
  "rating": 5,
  "ratingId": 100
}

// Rating Updated Event
{
  "eventId": "...",
  "eventType": "RATING_UPDATED",
  "courseId": 1,
  "oldRating": 4,
  "newRating": 5
}
```

## Caching Strategy

The service implements a **Cache-Aside** pattern:

1. **Read**: Check Redis cache first, fallback to DB
2. **Write**: Update DB immediately, publish Kafka event for async cache update
3. **Cache Invalidation**: Fire-and-forget via Kafka (no rollback on failure)

This ensures the database remains the source of truth while maintaining cached performance.

## Metrics

### Business Metrics

- `course.creation.count`: Total course creation attempts
- `course.retrieval.timer`: Course retrieval timing
- `rating.created.count`: Total ratings created
- `rating.updated.count`: Total ratings updated
- `rating.deleted.count`: Total ratings deleted

### Prometheus Endpoints

- `/actuator/prometheus`: Metrics in Prometheus format
- `/actuator/health`: Health check endpoint
- `/actuator/metrics`: All available metrics

## Database Schema

### Course Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| title | VARCHAR(255) | Course title |
| description | TEXT | Course description |
| instructor_id | BIGINT | Foreign key to instructor |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### Course_Session Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| course_id | BIGINT | Foreign key to course |
| session_title | VARCHAR(255) | Session title |
| session_description | TEXT | Session content |
| video_url | VARCHAR(512) | Video URL |
| order_index | INT | Display order |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### Course_Rating Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| course_id | BIGINT | Foreign key to course |
| user_id | BIGINT | User who rated |
| rating | INT | Score (1-5) |
| comment | TEXT | Review text |
| created_at | TIMESTAMP | Rating timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

## Running Locally

```bash
# Build the service
./gradlew :edu-nexus-course-service:build

# Run with local profile
./gradlew :edu-nexus-course-service:bootRun --args='--spring.profiles.active=local'

# Run tests
./gradlew :edu-nexus-course-service:test
```

## Troubleshooting

### Cache Inconsistency

If Redis cache shows stale data:

1. Check Kafka consumer is running: `kafka-console-consumer --topic course-rating-added`
2. Verify Redis connection: `redis-cli ping`
3. Clear cache: `redis-cli FLUSHDB`

### Rating Average Incorrect

1. Get DB average: `GET /courses/{id}/ratings/average/db`
2. Get cache average: `GET /courses/{id}/ratings/average`
3. Compare and manually clear cache if needed: `redis-cli DEL course:rating:{id}`

### N+1 Query Issues

Use batch endpoints:
- Use `/courses/batch` for multiple courses
- Check `CourseService.getCoursesByIds()` uses `findAllById()`
- Verify rating lookups are batched via `getAverageRatingsByCourseIds()`

## Dependencies

- **edu-nexus-common**: Shared exception classes
- **edu-nexus-observability**: Custom metrics and tracing
- **edu-nexus-grpc-common**: gRPC definitions (if using gRPC)
