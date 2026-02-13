# ACREATE-000: Cache Strategy - Remove Compensating Transactions

## Status
**PROPOSED** - Pending Review

## Context
The current course rating system implements a complex saga pattern with compensating transactions:

1. User adds rating -> DB save
2. Kafka event sent -> Redis update
3. Redis response event received
4. **If Redis failed** -> DB rollback (compensating transaction)

This pattern creates tight coupling between cache and database, treating cache as a source of truth rather than an optimization layer.

### Current Implementation Issues
```java
// CourseRatingRedisResponseConsumerService.java
if (event.getResponse().equals(KafkaCourseRatingResponse.FAIL.getResponse())) {
    courseRatingRepository.deleteById(event.getCourseRatingId()); // DB rollback!
    log.info("Course rating redis adding failed. Compensating Transaction was activated.");
}
```

### Problems
1. **Anti-pattern**: Cache failures should never trigger DB rollbacks
2. **Complexity**: Requires multiple Kafka topics and response handlers
3. **Eventual consistency violated**: System behaves as if cache is synchronous source of truth
4. **Cascading failures**: Cache issues cause data loss in primary DB
5. **Operational overhead**: More topics = more monitoring complexity

## Decision
**Adopt Cache-Aside Pattern with Fire-and-Forget Cache Updates**

### Principles
1. **DB is source of truth** - All writes go to DB first
2. **Cache is ephemeral** - Can be rebuilt from DB at any time
3. **Fire-and-forget updates** - Async cache updates without response handling
4. **Idempotent operations** - Duplicate events don't cause issues
5. **TTL-based expiration** - Cache expires naturally, forcing refresh

### New Architecture
```
Rating Request
    |
    v
[DB Transaction] -- Success --> Publish Kafka Event (Fire & Forget)
    |                                      |
    |                                      v
    |                              [Cache Update Service]
    |                                      |
    v                                      v
Response to User                     [Redis] (Async)
                                        (No response back to DB)
```

### Consequences

#### Positive
- **Simpler code**: Remove compensating transaction logic
- **Fewer Kafka topics**: Remove response topics
- **Better separation**: Cache is truly an optimization layer
- **Resilience**: Cache failures don't affect data persistence
- **Rebuildable**: Cache can be wiped and rebuilt from DB

#### Negative
- **Stale cache possible**: Cache may be briefly outdated
- **Eventual consistency**: Users may see old ratings briefly
- **Monitoring needed**: Must track cache health separately

### Migration Steps
1. Create `CacheAsideTemplate` for generic cache operations
2. Add TTL to all Redis keys (e.g., 5 minutes)
3. Remove response consumer (`CourseRatingRedisResponseConsumerService`)
4. Remove compensating transaction logic
5. Add cache initialization/rebuild endpoint
6. Update integration tests

## Alternatives Considered

### Alternative 1: Current Pattern (Keep)
- **Pros**: "Stronger" consistency
- **Cons**: Anti-pattern, complex, cache is SOT
- **Rejected**: Violates cache-as-optimization principle

### Alternative 2: Write-Through Cache
- **Pros**: Always consistent
- **Cons**: Poor performance, Redis is SOT
- **Rejected**: Makes Redis critical path

### Alternative 3: Read-Through Only
- **Pros**: Simple, no write complexity
- **Cons**: Stale on every read, high DB load
- **Rejected**: Defeats cache purpose

## Implementation Plan
1. Phase 1: Create cache-aside infrastructure
2. Phase 2: Migrate rating system
3. Phase 3: Remove old saga code
4. Phase 4: Add monitoring and metrics

## References
- [Martin Fowler - Cache-Aside](https://martinfowler.com/bliki/CacheAside.html)
- [Redis Caching Best Practices](https://redis.io/docs/manual/patterns/caching/)
- Current issue: `CourseRatingRedisResponseConsumerService.java`
