# ACREATE-001: Kafka Consumer Improvements

## Status
**PROPOSED** - Pending Review

## Context
Current Kafka consumers have several reliability and maintainability issues:

### Current Problems

1. **Hardcoded Topic Strings**
```java
@KafkaListener(topics = "course-rating-add", groupId = "course-rating-group")
@KafkaListener(topics = "course-rating-update", groupId = "course-rating-group")
```
- Typos possible
- No single source of truth
- Difficult to refactor

2. **No Dead Letter Topic (DLT)**
- Failed messages are lost
- No way to recover from errors
- Difficult to debug production issues

3. **No Idempotency Handling**
- Duplicate events cause duplicate processing
- At-least-once delivery not handled
- Data corruption possible

4. **Basic Error Handling**
```java
catch (Exception e) {
    // Just throw RuntimeException
    throw new RuntimeException("Failed to parse Kafka message", e);
}
```

## Decision
**Implement Production-Ready Kafka Consumer Pattern**

### Changes Required

#### 1. Centralized Topic Constants
```java
public final class KafkaTopics {
    public static final String COURSE_RATING_ADD = "course-rating-add";
    public static final String COURSE_RATING_UPDATE = "course-rating-update";
    public static final String COURSE_RATING_DELETE = "course-rating-delete";
    public static final String COURSE_RATING_DLT = "course-rating-dlt";

    private KafkaTopics() {} // Utility class
}
```

#### 2. Dead Letter Topic Configuration
```yaml
spring:
  kafka:
    consumer:
      properties:
        spring.json.trusted.packages: "*"
    listener:
      ack-mode: manual_immediate
    template:
      default-topic: course-rating-dlt
```

#### 3. Idempotency via Event ID
```java
@KafkaListener(topics = KafkaTopics.COURSE_RATING_ADD,
               groupId = "course-rating-group",
               properties = {
                   "spring.json.value.default.type=com.example.CourseRatingAddEvent"
               })
public void handleRatingAdd(
        @Payload CourseRatingAddEvent event,
        @Header(KafkaHeaders.RECEIVED_KEY) String eventId) {

    // Idempotency check using Redis/set
    if (idempotencyHelper.isDuplicate(eventId, "rating-add")) {
        log.info("Duplicate event ignored: {}", eventId);
        return;
    }

    idempotencyHelper.markProcessed(eventId, "rating-add");
    // Process event...
}
```

#### 4. Retry with Exponential Backoff
```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 1000, multiplier = 2.0),
    autoCreateTopics = "false",
    topicSuffixing = {
        @TopicSuffixingStrategy(
            topicPattern = "${topic}-retry-\\d",
            suffixDelimiter = "-",
            retryTopicSuffix = "-retry",
            dltTopicSuffix = "-dlt"
        )
    }
)
```

#### 5. Structured Error Handling
```java
public class KafkaErrorHandler implements ErrorHandler {
    private final MeterRegistry meterRegistry;
    private final DeadLetterTopicPublisher dltPublisher;

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
        // Metrics
        meterRegistry.counter("kafka.consumer.errors").increment();

        // Log with context
        log.error("Kafka consumer error: topic={}, partition={}, offset={}",
            data.topic(), data.partition(), data.offset(), thrownException);

        // Publish to DLT
        dltPublisher.publish(data, thrownException);
    }
}
```

### Consumer Architecture
```
                    ┌─────────────────────────────────────┐
                    │          Kafka Cluster              │
                    └─────────────────────────────────────┘
                                      │
                    ┌─────────────────▼──────────────────┐
                    │         Main Consumer               │
                    │  - Deserialize                      │
                    │  - Validate                         │
                    │  - Check Idempotency                │
                    └─────────────────┬──────────────────┘
                                      │
                    ┌─────────────────▼──────────────────┐
                    │         Process Event               │
                    └─────────────────┬──────────────────┘
                                      │
                         ┌────────────┴────────────┐
                         │                         │
                    Success                  Failure
                         │                         │
                         │                    ┌────▼────┐
                         │                    │ Retry   │
                    ┌────▼────┐               │ (3x)    │
                    │ Commit  │               └────┬────┘
                    └─────────┘                    │
                                           Failure │
                                              ┌────▼────┐
                                              │   DLT   │
                                              └─────────┘
```

## Consequences

### Positive
- **Reliability**: Failed messages recoverable via DLT
- **Maintainability**: Topics defined once
- **Correctness**: Idempotency prevents duplicate processing
- **Observability**: Metrics for monitoring
- **Debugging**: DLTs for offline analysis

### Negative
- **Complexity**: More configuration required
- **Infrastructure**: More topics (DLT, retry)
- **Testing**: More scenarios to test

## Implementation Plan
1. Create `KafkaTopics` constants class
2. Configure DLT in all consumers
3. Add idempotency helper with Redis
4. Implement structured error handling
5. Add consumer metrics
6. Update integration tests

## Monitoring Required
- Consumer lag
- Error rate
- DLT message count
- Processing time
- Idempotency cache hit rate

## References
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [Kafka Exactly-Once Semantics](https://kafka.apache.org/documentation/#semantics)
