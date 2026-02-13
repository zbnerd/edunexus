# Enrollment Service

## Overview

The Enrollment Service manages course enrollments, subscription plans, and distributed transaction coordination using the Saga pattern. It orchestrates multi-service transactions to ensure data consistency across the EduNexus platform.

## Features

- **Course Enrollment**: Register users for specific courses
- **Subscription Management**: Handle subscription plans and renewals
- **Access Control**: Verify user access to courses and subscriptions
- **Saga Orchestration**: Coordinate distributed transactions across services
- **gRPC Integration**: Communicate with other services via gRPC
- **Transaction Monitoring**: Track saga state and compensation

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- MySQL 8.0 for persistence
- Kafka for event-driven coordination
- gRPC for inter-service communication
- Saga pattern for distributed transactions

### Hexagonal Architecture

```
edu-nexus-enrollment-service/
├── adapter/
│   └── in/web/              # REST controllers
├── application/
│   ├── saga/               # Saga orchestration
│   │   ├── coordinator/    # Transaction state management
│   │   └── orchestrator/   # Saga workflow
│   └── service/            # Application services
├── domain/
│   ├── entity/             # JPA entities
│   ├── dto/               # Data transfer objects
│   ├── repository/         # Repository interfaces
│   ├── grpc/              # gRPC service implementations
│   └── template/          # gRPC error handling templates
└── port/                  # Use case interfaces
```

## API Endpoints

### Saga-Based Enrollment (Recommended)

#### Enroll with Saga
```http
POST /api/enrollments/saga/enroll
Content-Type: application/json

{
  "userId": 1,
  "courseId": 100
}
```

**Success Response:**
```json
{
  "success": true,
  "sagaId": "saga-uuid-123",
  "enrollmentId": 500,
  "paymentId": 700
}
```

**Failure Response:**
```json
{
  "success": false,
  "sagaId": "saga-uuid-123",
  "error": "Payment failed: insufficient funds"
}
```

#### Get Transaction Status
```http
GET /api/enrollments/saga/status/{sagaId}
```

**Response:**
```json
{
  "sagaId": "saga-uuid-123",
  "userId": 1,
  "courseId": 100,
  "status": "COMPLETED",
  "errorMessage": null,
  "completedSteps": ["VALIDATE_USER", "PROCESS_PAYMENT", "CREATE_ENROLLMENT", "CONFIRM_PAYMENT"]
}
```

### Legacy Endpoints (Direct DB Operations)

#### Register Course
```http
POST /enrollments
Content-Type: application/json

{
  "userId": 1,
  "courseId": 100,
  "paymentId": 500
}
```

#### Create Subscription
```http
POST /subscriptions
Content-Type: application/json

{
  "userId": 1,
  "planId": 1,
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-12-31T23:59:59"
}
```

#### Renew Subscription
```http
PUT /subscriptions/{subscriptionId}
Content-Type: application/json

{
  "startDate": "2025-01-01T00:00:00",
  "endDate": "2025-12-31T23:59:59"
}
```

#### Check Course Access
```http
GET /enrollments/check?userId=1&courseId=100
```

**Response:** `true` if enrolled, `false` otherwise

#### Check Subscription Access
```http
GET /subscriptions/check?userId=1&currentDate=2024-06-15T12:00:00
```

**Response:** `true` if active subscription, `false` otherwise

## Saga Pattern

### Enrollment Saga Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    ENROLLMENT SAGA                          │
└─────────────────────────────────────────────────────────────────┘
      │
      ├─► Step 1: Validate User
      │   └── UserService (gRPC)
      │       Success: Continue
      │       Failure: → COMPENSATE: None (read-only)
      │
      ├─► Step 2: Process Payment
      │   └── PaymentService (via Kafka)
      │       Success: Continue
      │       Failure: → COMPENSATE: Cancel Payment
      │
      ├─► Step 3: Create Enrollment
      │   └── Local Database
      │       Success: Continue
      │       Failure: → COMPENSATE: Delete Enrollment, Refund Payment
      │
      ├─► Step 4: Confirm Payment
      │   └── PaymentService (via Kafka)
      │       Success: → SAGA COMPLETED
      │       Failure: → COMPENSATE: Delete Enrollment, Refund Payment
      │
      └─► Final State: COMPLETED or FAILED
```

### Transaction States

| State | Description |
|--------|-------------|
| `STARTED` | Saga initiated |
| `VALIDATING_USER` | Checking user exists |
| `PROCESSING_PAYMENT` | Payment in progress |
| `CREATING_ENROLLMENT` | Enrollment record creation |
| `CONFIRMING_PAYMENT` | Finalizing payment |
| `COMPLETED` | All steps successful |
| `COMPENSATING` | Rolling back changes |
| `FAILED` | Saga failed after compensation |

### Compensation Actions

| Step | Compensation |
|-------|--------------|
| Validate User | None (read-only operation) |
| Process Payment | Cancel payment via Kafka |
| Create Enrollment | Delete from database |
| Confirm Payment | Refund if needed |

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/next_enrollment
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092,localhost:9093,localhost:9094}

grpc:
  server:
    port: 9002  # gRPC server port

server:
  port: 8002  # REST API port

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
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | localhost:9092,9093,9094 |

## Kafka Events

### Topics

- `payment-request`: Request payment processing
- `payment-response`: Receive payment results
- `payment-confirmation`: Finalize payment
- `enrollment-saga-events`: Saga state tracking (optional)

### Event Payloads

```java
// Payment Request
{
  "sagaId": "uuid-123",
  "userId": 1,
  "amount": 99.99,
  "action": "PROCESS"
}

// Payment Response
{
  "sagaId": "uuid-123",
  "success": true,
  "paymentId": 500,
  "transactionId": "txn-456"
}
```

## Database Schema

### Enrollment Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | User enrolled |
| course_id | BIGINT | Course enrolled in |
| payment_id | BIGINT | Associated payment |
| registration_date | TIMESTAMP | Enrollment timestamp |
| created_at | TIMESTAMP | Record creation |
| updated_at | TIMESTAMP | Last update |

### Payment Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | Payer |
| amount | DECIMAL(10,2) | Payment amount |
| payment_type | VARCHAR(50) | CREDIT_CARD, PAYPAL, etc. |
| status | VARCHAR(50) | PENDING, COMPLETED, FAILED |
| transaction_id | VARCHAR(255) | External transaction ID |
| created_at | TIMESTAMP | Payment timestamp |

### Subscription Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | Subscriber |
| plan_id | BIGINT | Plan type |
| start_date | TIMESTAMP | Subscription start |
| end_date | TIMESTAMP | Subscription end |
| created_at | TIMESTAMP | Record creation |
| updated_at | TIMESTAMP | Last update |

## Running Locally

```bash
# Build the service
./gradlew :edu-nexus-enrollment-service:build

# Run with REST and gRPC ports
./gradlew :edu-nexus-enrollment-service:bootRun

# Run tests
./gradlew :edu-nexus-enrollment-service:test

# With Kafka infrastructure running
cd ../infrastructure && docker-compose up -d kafka kafka-ui
```

## Testing Saga Flow

```bash
# 1. Start enrollment
SAGA_RESPONSE=$(curl -X POST http://localhost:8002/api/enrollments/saga/enroll \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"courseId":100}')

# 2. Extract saga ID
SAGA_ID=$(echo $SAGA_RESPONSE | jq -r '.sagaId')

# 3. Check status periodically
watch -n 2 "curl -s http://localhost:8002/api/enrollments/saga/status/$SAGA_ID | jq"

# 4. Monitor Kafka topics
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic payment-request --from-beginning
```

## Troubleshooting

### Saga Stuck in COMPENSATING

1. Check Kafka consumer logs for payment response
2. Verify payment-service is running
3. Manually check `distributed_transaction` table state
4. Use `/status/{sagaId}` endpoint for current state

### Payment Timeout

If saga fails at payment step:
1. Verify payment-service is accessible via gRPC
2. Check Kafka connectivity
3. Review payment-service logs for errors

### Enrollment Already Exists

The service allows multiple enrollments per user-course. Add validation if needed:
```java
if (enrollmentRepository.findByUserIdAndCourseId(userId, courseId).isPresent()) {
    throw new IllegalStateException("Already enrolled");
}
```

## Dependencies

- **edu-nexus-common**: Shared entities and exceptions
- **edu-nexus-grpc-common**: gRPC service definitions
- **edu-nexus-observability**: Metrics and tracing

## gRPC Services

### EnrollmentGrpcService

Exposed for other services to query enrollments:
```protobuf
service EnrollmentService {
  rpc GetUserEnrollments(UserRequest) returns (EnrollmentList);
  rpc CheckCourseAccess(CheckRequest) returns (CheckResponse);
}
```

### PaymentGrpcService

Consumes payment service (client):
```protobuf
service PaymentService {
  rpc ProcessPayment(PaymentRequest) returns (PaymentResponse);
}
```
