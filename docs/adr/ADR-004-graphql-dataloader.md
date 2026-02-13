# ADR-004: GraphQL DataLoader for N+1 Prevention

## Status
Accepted

## Context
What is the issue that we're seeing that is motivating this decision or change?

The EduNexus platform's GraphQL API resolves nested objects and relationships, leading to N+1 query problems where each resolver makes individual database calls. For example, fetching courses with their enrolled students might result in one query for courses plus N additional queries for each course's students, creating significant performance bottlenecks as the data grows.

## Decision
What is the change that we're proposing and/or doing?

We implement DataLoader pattern for GraphQL resolvers where:
1. Each resolver collects identifiers needed for batch loading
2. DataLoader batches these requests and makes single database queries
3. Results are cached and distributed back to individual resolvers
4. Batch loading replaces N+1 individual queries with B batched queries
5. Caching prevents redundant loads for the same identifiers within a request
6. DataLoader instances are scoped to each GraphQL request to prevent memory leaks

For example, course enrollment resolution:
- Collect all course IDs needing enrollment data
- Single query fetches enrollments for all courses
- Results distributed to respective resolvers

## Consequences
What becomes easier or more difficult to do because of this change?

### Positive consequences
- **Performance improvement**: Reduces database queries from N+1 to B
- **Better scalability**: Handles larger datasets with fewer database connections
- **Reduced database load**: Fewer concurrent queries to database servers
- **Improved response times**: Faster GraphQL API responses
- **Clean resolver code**: Resolvers focus on data relationships rather than optimization

### Negative consequences
- **Added complexity**: DataLoader management adds another layer of abstraction
- **Memory usage**: Caching within requests increases memory per request
- **Debugging difficulty**: Harder to trace which batch a particular query belongs to
- **Batch size limits**: Very large batches can cause memory or timeout issues
- **Learning curve**: Team must understand batching and caching concepts