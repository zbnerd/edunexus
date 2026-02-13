# ADR-003: Saga Pattern for Distributed Transactions

## Status
Accepted

## Context
What is the issue that we're seeing that is motivating this decision or change?

The EduNexus platform requires atomic operations across multiple microservices, particularly for course enrollment which involves payment processing, seat allocation, and enrollment creation. Traditional database transactions cannot span multiple services, leading to potential consistency issues when operations fail partway through.

## Decision
What is the change that we're proposing and/or doing?

We implement choreography-based saga pattern where:
1. Each service owns its own database and participates in the transaction
2. Initiation triggers a sequence of events via Kafka
3. Each service processes events and either commits or publishes a compensating event
4. Events are idempotent to handle retries safely
5. Compensating transactions reverse completed steps when failures occur
6. Transaction state is tracked in each service's database
7. Timeout mechanisms ensure failed transactions don't leave systems in unknown states

For example, course enrollment follows:
1. Payment service processes payment → event: PaymentProcessed
2. Enrollment service creates enrollment → event: EnrollmentCreated
3. Course service updates seat count → event: SeatAllocated
If any step fails, compensating events are published to reverse previous steps.

## Consequences
What becomes easier or more difficult to do because of this change?

### Positive consequences
- **Distributed coordination**: Allows atomic operations across multiple services
- **Resilience**: System continues working even if some services fail
- **Data consistency**: Eventually consistent state through compensating transactions
- **Service autonomy**: Each service remains independent and owns its data
- **Scalability**: Services can scale independently based on their transaction volume

### Negative consequences
- **Increased complexity**: More complex than single-database transactions
- **Eventual consistency**: Not immediate consistency, requires propagation time
- **Debugging challenges**: Hard to trace distributed transaction flows
- **Compensation logic**: Need to design and implement reverse operations
- **Monitoring requirements**: Need to track saga completion across services