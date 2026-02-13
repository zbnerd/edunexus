# Cache-Aside Pattern Implementation - Decisions

## Decision 1: Remove Compensating Transactions
**Status:** IMPLEMENTED

**Context:**
- Previous pattern: Cache failures triggered DB rollbacks
- Response consumer deleted DB records when Redis failed
- This treated cache as source of truth (anti-pattern)

**Decision:**
- Removed all response topics and consumers
- Cache failures are logged but don't affect DB
- Fire-and-forget pattern via Kafka

**Rationale:**
- Cache is optimization layer, not source of truth
- DB is single source of truth
- Cache can be rebuilt from DB at any time
- Simplified code: 3 fewer topics, 1 fewer consumer class

## Decision 2: Replace KEYS with SCAN
**Status:** IMPLEMENTED

**Context:**
- `redisTemplate.keys("*")` is BLOCKING operation
- Pauses Redis for large keysets
- Dangerous in production

**Decision:**
- Implemented `CacheAsideTemplate.scan()` using SCAN
- Non-blocking incremental key iteration
- Added `deleteByPattern()` helper method

**Rationale:**
- SCAN doesn't block Redis
- Safe for production with large datasets
- Per Redis best practices

## Decision 3: 5-Minute TTL
**Status:** IMPLEMENTED

**Context:**
- Previous: 60-minute TTL (too long)
- No TTL refresh on updates

**Decision:**
- 5-minute TTL for all cached ratings
- TTL refreshed on each increment operation

**Rationale:**
- Balances cache hit rate vs data freshness
- Per ADR-000 recommendation
- Forces periodic cache refresh from DB

## Decision 4: Lazy Loading vs Full Cache Load
**Status:** IMPLEMENTED

**Context:**
- Previous: `@PostConstruct` loaded ALL ratings on startup
- Blocked application startup
- Wasted resources for unused courses

**Decision:**
- Removed `initCourseRatings()` method
- Cache populated on first access via Kafka events
- Optional warmup via `@ConditionalOnProperty`

**Rationale:**
- Faster startup (no blocking DB load)
- Only cache accessed data
- Simpler code (no init logic)

## Decision 5: Non-Blocking Cache Failures
**Status:** IMPLEMENTED

**Context:**
- Previous: Cache failures could propagate exceptions
- Risked cascading failures

**Decision:**
- All cache operations wrapped in try-catch
- Log warnings but don't throw exceptions
- Business logic continues even if cache is down

**Rationale:**
- Cache is optimization, not requirement
- System should function with cache down
- Matches ADR-000 principles

## Decision 6: JSON Serialization
**Status:** IMPLEMENTED

**Context:**
- Previous: String serialization (duplicated config)
- Lost type information

**Decision:**
- `GenericJackson2JsonRedisSerializer` for values
- `StringRedisSerializer` for keys (human-readable)
- Fixed duplicate serializer bug in RedisConfig

**Rationale:**
- Preserves type information
- Enables complex object caching
- Debuggable keys in Redis CLI

## Decision 7: Simplified Kafka Topics
**Status:** IMPLEMENTED

**Context:**
- Previous: 6 topics (3 event + 3 response)
- Response topics unnecessary per ADR-000

**Decision:**
- 3 topics: course-rating-add, course-rating-update, course-rating-delete
- 3 DLT topics for failed events
- Removed all response topics

**Rationale:**
- Fewer topics = simpler monitoring
- Reduced operational overhead
- Aligns with fire-and-forget pattern

## Decision 8: Cache Metrics Collection
**Status:** PARTIALLY IMPLEMENTED

**Context:**
- Need visibility into cache performance
- Track hit/miss rates

**Decision:**
- Added `CacheMetrics` class to `CacheAsideTemplate`
- Tracks hits, misses, total requests, hit rate
- Removed Actuator endpoint (dependency not available)

**Rationale:**
- Simple in-memory metrics (no external deps)
- Can be logged via scheduled task
- Future: Integrate with Micrometer

## Trade-offs Accepted

### 1. Brief Cache Staleness
**Trade-off:** Up to 5-minute staleness vs strong consistency
**Acceptable:** Ratings don't change frequently
**Mitigation:** TTL ensures eventual consistency

### 2. Cache Stampede Risk
**Trade-off:** No stampede protection vs simpler code
**Acceptable:** Low write frequency for ratings
**Mitigation:** Future: Implement single-flight pattern

### 3. Manual Cache Warmup
**Trade-off:** No automatic warmup vs faster startup
**Acceptable:** Cache loads on first access
**Mitigation:** Optional warmup via property flag

## Alternative Approaches Considered

### Alternative 1: Write-Through Cache
**Rejected:** Makes Redis critical path
**Cache-Aside preferred:** DB is always source of truth

### Alternative 2: Read-Through Only
**Rejected:** High DB load, defeats cache purpose
**Cache-Aside preferred:** Async updates via Kafka

### Alternative 3: Keep Compensating Transactions
**Rejected:** Anti-pattern per ADR-000
**Cache-Aside preferred:** Cache is optimization, not SOT

## Implementation Details

### Deleted Files
1. `CourseRatingRedisResponseConsumerService.java` - Compensating transaction logic

### Modified Files
1. `RedisConfig.java` - Fixed serializers, TTL to 5min
2. `RedisInit.java` - Removed @PostConstruct, added conditional warmup
3. `CourseRatingRedisRepositoryImpl.java` - Added TTL, non-blocking failures
4. `CourseRatingConsumerService.java` - Removed response handling
5. `CourseRatingProducerService.java` - Removed response methods
6. `KafkaTopic.java` - Removed response topics
7. `CourseRatingService.java` - Removed initCourseRatings
8. `CourseRatingUseCase.java` - Removed initCourseRatings from interface

### New Files
1. `CacheAsideTemplate.java` - Generic cache-aside implementation with SCAN, metrics

### Build Status
- Compile: SUCCESS
- No compilation errors
- No dependency issues
