# GraphQL API Gateway

## Overview

The GraphQL API Gateway provides an alternative to REST for accessing EduNexus platform data. It aggregates data from multiple services into a single GraphQL schema, allowing clients to fetch exactly what they need in a single request.

## Features

- **GraphQL API**: Single endpoint for all queries and mutations
- **Schema Federation**: Aggregates schemas from multiple services
- **DataLoader**: Batch loading to prevent N+1 queries
- **Authorization Directive**: Field-level access control
- **Request Logging**: Audit all GraphQL operations
- **Type Safety**: Generated DTOs and resolvers

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- GraphQL Java Tools for schema management
- DataLoader for batch optimization
- Custom directives for authorization
- gRPC clients for service communication

### Architecture

```
                    ┌─────────────────┐
                    │   Client       │
                    │ (GraphQL)      │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ GraphQL API      │
                    │   :8006         │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────▼────┐  ┌───▼────┐  ┌───▼────┐
        │ Course    │  │ User    │  │Enroll   │
        │  Service   │  │ Service │  │ Service  │
        │  (gRPC)   │  │(gRPC)  │  │(gRPC)  │
        └───────────┘  └─────────┘  └─────────┘
```

## GraphQL Schema

### Core Types

```graphql
type Course {
  id: ID!
  title: String!
  description: String
  instructorId: ID!
  sessions: [CourseSession!]!
  ratings: [CourseRating!]!
  averageRating: Float
}

type CourseSession {
  id: ID!
  sessionId: ID!
  sessionTitle: String!
  sessionDescription: String
  videoUrl: String!
  orderIndex: Int!
  file: SessionFile
}

type CourseRating {
  id: ID!
  rating: Int!
  comment: String
  createdAt: String
}

type User {
  id: ID!
  name: String!
  email: String!
}

type Enrollment {
  id: ID!
  courseId: ID!
  userId: ID!
  registrationDate: String
  payment: Payment
}

type PlaybackRecord {
  id: ID!
  sessionId: ID!
  userId: ID!
  watchedSeconds: Int!
  lastWatchedPosition: Int
}
```

### Queries

```graphql
# Get course with sessions
query GetCourseWithSessions($courseId: ID!) {
  course(id: $courseId) {
    id
    title
    description
    sessions {
      id
      sessionTitle
      videoUrl
      orderIndex
    }
    averageRating
  }
}

# Get user's enrollments
query GetUserEnrollments($userId: ID!) {
  user(id: $userId) {
    id
    name
    enrollments {
      id
      course {
        title
        description
      }
      registrationDate
    }
  }
}

# Search courses
query SearchCourses($title: String) {
  courses(title: $title) {
    id
    title
    description
    averageRating
  }
}
```

### Mutations

```graphql
# Update password
mutation ChangePassword($userId: ID!, $oldPassword: String!, $newPassword: String!) {
  changePassword(
    userId: $userId
    oldPassword: $oldPassword
    newPassword: $newPassword
  ) {
    id
    email
  }
}

# Record playback
mutation RecordPlayback($sessionId: ID!, $userId: ID!, $watchedSeconds: Int!) {
  recordPlayback(
    sessionId: $sessionId
    userId: $userId
    watchedSeconds: $watchedSeconds
  ) {
    id
    watchedSeconds
  }
}
```

## Configuration

### Application Properties

```yaml
spring:
  application:
    name: edu-nexus-graphql

graphql:
  graphiql:
    enabled: true  # Enable GraphQL playground
  path: /graphql

server:
  port: 8006

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/

grpc:
  clients:
    course-service:
      host: localhost
      port: 9001  # gRPC port
    user-service:
      host: localhost
      port: 9004
    enrollment-service:
      host: localhost
      port: 9002
```

## Endpoints

### GraphQL Endpoint

```
POST http://localhost:8006/graphql
Content-Type: application/json

{
  "query": "query { courses { id title } }"
}
```

### GraphiQL Playground

```
http://localhost:8006/graphiql
```

Interactive IDE for exploring schema and testing queries.

### Health Check

```bash
curl http://localhost:8006/actuator/health
```

## DataLoader Pattern

### Problem: N+1 Queries

Without DataLoader:

```graphql
query {
  courses {
    id
    title
    instructor {        # N+1: 1 query per course
      name
      email
    }
    sessions {           # N+1: 1 query per course
      id
      sessionTitle
    }
  }
}
```

Results in: `1 + N + N` queries

### Solution: DataLoader

With DataLoader, all instructors and sessions are batched:

```java
@DataLoader(courseIds = "courses")
public List<Course> courses(List<Long> courseIds) {
    // Single batch query
    return courseService.getCoursesByIds(courseIds);
}
```

Results in: `1` batch query for courses, `1` for instructors, `1` for sessions

## Authorization Directive

### Schema Usage

```graphql
type Query {
  @Permission(action: READ, resource: COURSE)
  course(id: ID!): Course

  @Permission(action: READ, resource: USER)
  user(id: ID!): User
}

type Mutation {
  @Permission(action: WRITE, resource: ENROLLMENT)
  enrollInCourse(courseId: ID!): Enrollment
}
```

### Implementation

```java
@DirectiveLocations({DirectiveLocation.FIELD_DEFINITION, DirectiveLocation.OBJECT})
public @interface Permission {
    PermissionAction action() default READ;
    String resource() required;
}

public enum PermissionAction {
    READ, WRITE, DELETE, ADMIN
}
```

## Instrumentation

### Field Access Logging

Tracks every accessed field:

```java
@ExecutionData("courseAccessLog")
public List<Course> courses() {
    return courseService.getAllCourses();
}
```

Logs:
```
[INFO] FieldAccess: Query.courses - 5ms
[INFO] FieldAccess: Course.title - 1ms
[INFO] FieldAccess: Course.description - 1ms
```

## gRPC Integration

### Service Communication

GraphQL gateway uses gRPC for efficient service-to-service calls:

```java
@GrpcClient("edu-nexus-course-service")
private CourseServiceGrpc.CourseServiceBlockingStub courseStub;

public Course getCourse(Long id) {
    CourseRequest request = CourseRequest.newBuilder()
        .setCourseId(id)
        .build();

    CourseResponse response = courseStub.getCourse(request);
    return Course.fromProto(response);
}
```

## Running Locally

```bash
# Build gateway
./gradlew :edu-nexus-graphql:build

# Run (requires other services)
./gradlew :edu-nexus-graphql:bootRun

# Access GraphiQL
open http://localhost:8006/graphiql
```

## Testing

### Query Testing

```bash
# Simple query
curl -X POST http://localhost:8006/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { courses { id title } }"
  }'

# With variables
curl -X POST http://localhost:8006/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query GetCourse($id: ID!) { course(id: $id) { id title } }",
    "variables": { "id": "1" }
  }'
```

### Using GraphiQL

1. Open http://localhost:8006/graphiql
2. Explore schema on left panel
3. Write query in center panel
4. View results on right panel
5. Check variables panel for parameterized queries

## Troubleshooting

### Schema Not Loading

1. Check gRPC connections to services
2. Verify services are registered in Eureka
3. Review schema scanner logs
4. Ensure resolver beans are being created

### DataLoader Issues

If queries are slow:
1. Verify DataLoader is registered
2. Check batch size in DataLoader config
3. Review resolver code for non-batched calls
4. Enable GraphQL query logging

### Directive Not Working

1. Check directive is registered in schema
2. Verify directive handler bean exists
3. Review directive implementation
4. Check for schema validation errors

## Production Considerations

### Performance

- [ ] Query complexity analysis
- [ ] Query depth limiting
- [ ] Persistent query support
- [ ] Response caching
- [ ] Rate limiting per query type

### Security

- [ ] JWT validation in resolvers
- [ ] Query allowlist
- [ ] Field-level authorization enforcement
- [ ] Introspection disable in production
- [ ] Apollo tracing header validation

### Monitoring

- [ ] Query logging for analytics
- [ ] Error rate tracking
- [ ] Latency per query
- [ ] DataLoader cache hit rate
- [ ] Field access patterns

## Advantages Over REST

### Single Request

```graphql
# One GraphQL request
query {
  courses {
    id
    title
    instructor { name }
    sessions { sessionTitle }
  }
}
```

vs

```http
# Multiple REST requests
GET /api/courses
GET /api/courses/1/instructor
GET /api/courses/1/sessions
```

### No Overfetching

Client specifies exact fields needed:
```graphql
query {
  course(id: "1") {
    title    # Only these fields
    rating   # returned
  }
}
```

REST would return entire object.

### No Underfetching

Get related data in single query:
```graphql
query {
  course(id: "1") {
    sessions {
      file {          # Nested data
        fileName
      }
    }
  }
}
```

## Dependencies

- **edu-nexus-grpc-common**: gRPC definitions
- **edu-nexus-observability**: Metrics
- GraphQL Java Tools
- Spring Boot Starter GraphQL
