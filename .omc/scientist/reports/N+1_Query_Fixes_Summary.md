# N+1 Query Fixes and IO Optimization Summary

**Date:** 2026-02-13
**Task:** Code Quality I - N+1 queries and inefficient IO
**Status:** Completed

## Overview

Fixed N+1 query patterns and inefficient database access across the EduNexus course service. The changes significantly reduce database round-trips and improve overall query performance.

## Issues Identified and Fixed

### 1. **N+1 Query in CourseController.getAllCourses()**

**Location:** `CourseController.java:88-93`

**Problem:**
```java
// BEFORE - N+1 query problem
List<CourseInfoResponse> responses = courses.stream()
    .map(course -> CourseInfoResponse.from(course,
        courseRatingService.getAverageRatingByCourseId(course.getId()))) // N queries!
    .collect(Collectors.toList());
```

The `getAllCourses()` endpoint was calling `getAverageRatingByCourseId()` for each course in a loop, causing N+1 queries (1 initial query + N rating queries).

**Fix:**
Added batch rating lookup method:
```java
// AFTER - Batch lookup
Map<Long, Double> averageRatings = courseRatingService.getAverageRatingsByCourseIds(courseIds);
```

**Impact:** Reduced N queries to 1 batch query via Redis cache lookup.

---

### 2. **Missing Pagination in Repository Queries**

**Locations:**
- `CourseSessionRepository.findByCourseId()`
- `CourseRatingRepository.findByCourseId()`

**Problem:**
These methods returned entire collections without pagination, potentially loading thousands of records into memory.

**Fix:**
Added paginated versions and deprecated old methods:
```java
// NEW - Paginated version
Page<CourseSession> findByCourseId(Long courseId, Pageable pageable);

// DEPRECATED - Non-paginated version
@Deprecated
List<CourseSession> findByCourseId(Long courseId);
```

Also added optimized JOIN FETCH queries:
```java
@Query("SELECT cs FROM CourseSession cs JOIN FETCH cs.course WHERE cs.id = :sessionId AND cs.course.id = :courseId")
Optional<CourseSession> findByIdAndCourseId(@Param("sessionId") Long sessionId, @Param("courseId") Long courseId);
```

**Impact:** Prevents memory issues with large datasets and reduces lazy loading.

---

### 3. **No @EntityGraph for Eager Loading**

**Location:** `CourseRepository.java`

**Problem:**
When accessing `@OneToMany` relationships (sessions, ratings) outside transaction context, lazy loading caused additional queries.

**Fix:**
Added EntityGraph methods for eager loading when needed:
```java
@EntityGraph(attributePaths = {"sessions"})
Optional<Course> findWithSessionsById(Long id);

@EntityGraph(attributePaths = {"ratings"})
Optional<Course> findWithRatingsById(Long id);

@EntityGraph(attributePaths = {"sessions", "ratings"})
Optional<Course> findWithSessionsAndRatingsById(Long id);
```

Also added JOIN FETCH alternatives:
```java
@Query("SELECT c FROM Course c LEFT JOIN FETCH c.sessions WHERE c.id = :id")
Optional<Course> findWithSessionsFetch(@Param("id") Long id);
```

**Impact:** Enables single-query loading of related entities when needed.

---

### 4. **Inefficient GraphQL Batch Loading**

**Location:** `GraphQL CourseService.findCoursesByIds()`

**Problem:**
The batch loading method used query parameters for course IDs, which could cause URL length issues with large lists.

**Fix:**
Created dedicated batch endpoint accepting POST body:
```java
@PostMapping("/batch")
public ResponseEntity<List<CourseInfoResponse>> getCoursesByIds(@RequestBody List<Long> courseIds)
```

Added `getCoursesByIds()` to service layer using JPA's optimized `findAllById()`:
```java
public List<Course> getCoursesByIds(List<Long> courseIds) {
    return courseRepository.findAllById(courseIds); // JPA optimizes this
}
```

**Impact:** Better performance for GraphQL DataLoader patterns with large ID lists.

---

## Files Modified

| File | Changes |
|------|---------|
| `CourseController.java` | Added batch rating lookup, added `/batch` endpoint |
| `CourseUseCase.java` | Added `getCoursesByIds()` interface method |
| `CourseService.java` | Implemented `getCoursesByIds()` batch method |
| `CourseRatingUseCase.java` | Added `getAverageRatingsByCourseIds()` interface |
| `CourseRatingService.java` | Implemented batch rating lookup |
| `CourseSessionRepository.java` | Added pagination, @Deprecated old method, added JOIN FETCH |
| `CourseRatingRepository.java` | Added pagination, @Deprecated old method, added JOIN FETCH |
| `CourseRepository.java` | Added @EntityGraph methods and JOIN FETCH queries |
| `CourseSessionUseCase.java` | Added `getSessionsByCourseIdPaged()` interface |
| `CourseSessionService.java` | Implemented paginated sessions method |
| `GraphQL CourseService.java` | Updated to use POST body for batch queries |

---

## Performance Improvements

### Before (N+1 Pattern)
```
getAllCourses(100 courses):
- 1 query for courses
- 100 queries for ratings (N+1)
Total: 101 database round-trips
```

### After (Batch Loading)
```
getAllCourses(100 courses):
- 1 query for courses
- 1 batch Redis lookup for ratings
Total: 2 round-trips (1 DB + 1 cache)
```

**Reduction:** 98% fewer queries for this endpoint

---

## Additional Improvements

### SOLID Principles Applied

1. **Single Responsibility:**
   - Separated query optimization logic from business logic
   - Batch methods in service layer, not controllers

2. **Interface Segregation:**
   - Added focused methods to use case interfaces
   - Deprecated methods clearly marked for future removal

---

## Migration Guide

### For Existing Code Using Non-Paginated Methods

**Before:**
```java
List<CourseSession> sessions = courseSessionRepository.findByCourseId(courseId);
```

**After (Recommended):**
```java
Page<CourseSession> sessions = courseSessionRepository.findByCourseId(
    courseId, PageRequest.of(0, 20)
);
```

### For Accessing Course Relationships

**Before (N+1 risk):**
```java
Course course = courseRepository.findById(id).get();
List<CourseSession> sessions = course.getSessions(); // Lazy load!
```

**After (Eager load when needed):**
```java
Course course = courseRepository.findWithSessionsById(id).get();
List<CourseSession> sessions = course.getSessions(); // Already loaded
```

---

## Testing Recommendations

1. **Load Testing:** Test `getAllCourses()` with 1000+ courses
2. **Pagination Testing:** Verify paginated methods return correct page sizes
3. **Cache Testing:** Verify Redis batch lookup returns correct values
4. **GraphQL Testing:** Test DataLoader with 100+ course IDs

---

## Future Improvements

1. **Add Database Indexes:** Add composite indexes on `(course_id, created_at)` for frequently queried collections
2. **Query Result Caching:** Consider caching paginated results for frequently accessed pages
3. **DTO Projections:** Use Spring Data Projections for read-only views to avoid entity overhead
4. **Batch Size Configuration:** Make batch sizes configurable via application.yml
5. **Monitoring:** Add metrics for query execution times to track optimization impact

---

## Verification

Build status: ✅ Successful
```bash
./gradlew :edu-nexus-course-service:build
```

No compilation errors, only a deprecation warning for the intentionally deprecated `findByCourseId()` method.

---

## Related Documentation

- **ADR-000:** Cache-Aside Pattern (Redis)
- **Spring Data JPA:** EntityGraph documentation
- **GraphQL:** DataLoader pattern for batch loading

---

*Generated by Scientist Agent - N+1 Query Optimization Task*
