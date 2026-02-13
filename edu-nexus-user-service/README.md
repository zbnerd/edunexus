# User Service

## Overview

The User Service manages user accounts, authentication, and authorization for the EduNexus platform. It handles user registration, login, JWT token generation/validation, and login history tracking.

## Features

- **User Registration**: Sign up new users with email uniqueness validation
- **Authentication**: JWT-based authentication with secure password handling
- **Token Management**: Login, validation, refresh, and verification endpoints
- **Login History**: Track user login events with IP addresses
- **Password Management**: Secure password updates with validation

## Architecture

### Technology Stack

- Java 21 with Spring Boot 3.4.0
- MySQL 8.0 for persistence
- Redis for token blacklisting/caching
- BCrypt for password encryption
- JWT (jsonwebtoken library) for token handling
- Snowflake ID generation for distributed unique IDs

### Hexagonal Architecture

```
edu-nexus-user-service/
├── domain/
│   ├── user/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Domain services
│   │   ├── entity/          # JPA entities
│   │   ├── repository/      # Repository interfaces
│   │   ├── dto/            # Data transfer objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── config/         # Configuration classes
│   │   └── util/          # Utilities (Snowflake, Redis keys)
└── exceptionhandler/          # Global exception handling
```

## API Endpoints

### Authentication

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Logs the user's IP address to `user_login_history` table.

#### Validate Token
```http
POST /auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:** Returns the user associated with the token.

#### Verify Token
```http
POST /auth/verify-token
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "isValid": true
}
```

#### Refresh Token
```http
POST /auth/refresh-token
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**
```json
{
  "token": "newTokenHere..."
}
```

### User Management

#### Sign Up
```http
POST /users/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

**Response:** Created user entity with generated ID.

#### Get User
```http
GET /users/{userId}
Authorization: Bearer {token}
```

#### Get User by Email
```http
GET /users/email/{userEmail}
Authorization: Bearer {token}
```

#### Update Password
```http
PUT /users/{userId}/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "oldPassword": "OldPass123!",
  "newPassword": "NewSecure456!"
}
```

Validates old password before updating. Uses optimistic locking to prevent concurrent modifications.

#### Get Login History
```http
GET /users/{userId}/login-history
Authorization: Bearer {token}
```

Returns list of previous logins with timestamps and IP addresses.

## Configuration

### Application Properties

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/next_user
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8004

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8000/eureka/

jwt:
  secret: ${JWT_SECRET:your-secret-key-min-32-chars}
  expiration: ${JWT_EXPIRATION:86400000}  # 24 hours in ms
```

### Environment Variables

| Variable | Description | Default |
|-----------|-------------|----------|
| `DB_USERNAME` | Database username | Required |
| `DB_PASSWORD` | Database password | Required |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT signing secret | Required (min 32 chars) |
| `JWT_EXPIRATION` | Token validity period (ms) | 86400000 (24 hours) |

## Security

### Password Storage

Passwords are encrypted using BCrypt with default strength of 10:
```java
passwordEncoder.encode(rawPassword)  // One-way hashing
passwordEncoder.matches(rawPassword, encodedPassword)  // Verification
```

### JWT Structure

```java
// JWT Claims
{
  "sub": "user@example.com",     // Subject (email)
  "userId": "1234567890",       // Custom claim
  "iat": 1234567890,            // Issued at
  "exp": 1234567890             // Expiration
}
```

### Token Blacklisting

Logged out tokens are stored in Redis:
```
Key: blacklisted:token:{tokenId}
Value: "true"
TTL: Until token expiration
```

## Database Schema

### User Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key (Snowflake ID) |
| name | VARCHAR(255) | User's full name |
| email | VARCHAR(255) | Unique email address |
| password_hash | VARCHAR(255) | BCrypt hashed password |
| created_at | TIMESTAMP | Account creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| version | BIGINT | Optimistic lock version |

### User_Login_History Table

| Column | Type | Description |
|---------|-------|-------------|
| id | BIGINT | Primary key |
| user_id | BIGINT | Foreign key to user |
| login_time | TIMESTAMP | When login occurred |
| ip_address | VARCHAR(45) | Client IP address (IPv6 compatible) |

## Exceptions

| Exception | HTTP Status | Description |
|-----------|--------------|-------------|
| `DuplicateUserException` | 409 Conflict | Email already exists |
| `InvalidPasswordException` | 400 Bad Request | Old password doesn't match |
| `NotFoundException` | 404 Not Found | User not found |
| `IllegalStateException` | 409 Conflict | Optimistic lock failure |

## Running Locally

```bash
# Build the service
./gradlew :edu-nexus-user-service:build

# Run with local profile
./gradlew :edu-nexus-user-service:bootRun --args='--spring.profiles.active=local'

# Run tests
./gradlew :edu-nexus-user-service:test

# With custom environment variables
DB_USERNAME=root DB_PASSWORD=password ./gradlew :edu-nexus-user-service:bootRun
```

## Testing

### Login Flow

```bash
# 1. Register user
curl -X POST http://localhost:8004/users/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Pass123!"}'

# 2. Login
TOKEN=$(curl -X POST http://localhost:8004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Pass123!"}' \
  | jq -r '.token')

# 3. Validate token
curl -X POST http://localhost:8004/auth/validate \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}"
```

### Password Update

```bash
curl -X PUT http://localhost:8004/users/1/password \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"Pass123!","newPassword":"NewPass456!"}'
```

## Troubleshooting

### JWT Validation Fails

Check JWT secret matches across all services:
```bash
# Verify secret is at least 32 characters
echo $JWT_SECRET | wc -c

# Test token manually
curl -X POST http://localhost:8004/auth/verify-token \
  -H "Content-Type: application/json" \
  -d '{"token":"your-token-here"}'
```

### Optimistic Lock Conflicts

If you see "User was modified by another transaction":
1. Retry the operation with fresh data
2. Check for concurrent updates to the same user
3. Ensure `version` column is incremented on updates

### Email Already Exists

When testing, use unique emails:
```bash
EMAIL="test-$(date +%s)@example.com"
curl -X POST http://localhost:8004/users/signup \
  -d "{\"email\":\"$EMAIL\",\"password\":\"Pass123!\"}"
```

## Utilities

### Snowflake ID Generation

Generates distributed unique IDs:
```java
long userId = SnowflakeUtil.generateId();  // e.g., 12345678901234567
```

### Redis Key Patterns

- `blacklisted:token:{tokenId}`: Blacklisted JWT tokens
- `login:token:{userId}`: Current active token per user

## Dependencies

- **edu-nexus-common**: Shared `NotFoundException` and error codes
- **edu-nexus-observability**: Metrics integration
- Spring Security: For endpoint protection (add as needed)
