# ADR-002: JWT-Based Authentication

## Status
Accepted

## Context
What is the issue that we're seeing that is motivating this decision or change?

The EduNexus platform consists of multiple microservices that need to authenticate users. Traditional server-side session storage would create state management challenges across services and potential bottlenecks. We need a scalable, stateless authentication mechanism that can work across service boundaries without requiring shared session storage.

## Decision
What is the change that we're proposing and/or doing?

We implement JWT-based authentication where:
1. Users authenticate with credentials and receive a JWT token containing user claims
2. JWT tokens are signed with a secret key and include expiration time
3. Services validate JWT signatures locally for authentication
4. Redis cache tracks revoked tokens for immediate invalidation
5. Stateless operations - no server-side session storage required
6. Token claims include user roles, permissions, and service-specific information
7. Refresh tokens allow for extended sessions without re-authentication

## Consequences
What becomes easier or more difficult to do because of this change?

### Positive consequences
- **Scalability**: No shared session state needed across services
- **Performance**: Fast token validation without database lookups
- **Statelessness**: Easier to scale horizontally and handle service failures
- **Cross-service compatibility**: JWT tokens can be validated by any microservice
- **Reduced infrastructure**: No need for distributed session storage

### Negative consequences
- **Token size**: Larger than session IDs, increases network overhead
- **Revocation complexity**: Immediate token revocation requires additional Redis tracking
- **Storage of secrets**: Requires secure management of JWT signing keys
- **Token expiration**: Users must re-authenticate periodically
- **Security considerations**: Token theft allows access until expiration (unless revoked)