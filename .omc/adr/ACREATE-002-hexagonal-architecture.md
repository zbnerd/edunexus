# ACREATE-002: Hexagonal Architecture Layer Rules

## Status
**PROPOSED** - Pending Review

## Context
The project claims to follow hexagonal (ports and adapters) architecture, but has layer violations:

### Current Violation
```java
@RestController
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;           // VIOLATION
    private final CourseRatingUseCase courseRatingService; // OK
}
```

The controller depends directly on `CourseService` (application layer) instead of only on `port/in` interfaces.

### What is Hexagonal Architecture?

Hexagonal architecture defines clear boundaries around the domain:

```
┌─────────────────────────────────────────────────────────────────┐
│                        ADAPTERS (In/Out)                        │
│  ┌──────────────┐                  ┌──────────────────────────┐ │
│  │   REST API   │                  │  Persistence (JPA)       │ │
│  │   Controllers│                  │  Message Publishers      │ │
│  └──────┬───────┘                  └────────────┬─────────────┘ │
├─────────┼──────────────────────────────────────┼─────────────────┤
│         │              PORTS (In/Out)          │                 │
│  ┌──────▼──────────────────────────────────────▼───────┐       │
│  │              Port Interfaces (Use Cases)              │       │
│  │    CourseUseCase, CourseRatingUseCase, etc.          │       │
│  └──────┬──────────────────────────────────────┬───────┘       │
├─────────┼──────────────────────────────────────┼─────────────────┤
│         │          APPLICATION SERVICES         │                 │
│  ┌──────▼──────────────────────────────────────▼───────┐       │
│  │       CourseService, CourseRatingService, etc.       │       │
│  └──────┬──────────────────────────────────────┬───────┘       │
├─────────┼──────────────────────────────────────┼─────────────────┤
│         │              DOMAIN MODEL              │                 │
│  ┌──────▼──────────────────────────────────────▼───────┐       │
│  │     Course, CourseRating, Value Objects, etc.       │       │
│  └────────────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```

## Decision
**Strict Hexagonal Architecture with Dependency Rule Enforcement**

### Layer Dependency Rules

#### Rule 1: Controllers depend ONLY on port/in
```java
// ✓ CORRECT
@RestController
public class CourseController {
    private final CourseUseCase courseUseCase;
    private final CourseRatingUseCase ratingUseCase;
}

// ✗ WRONG
@RestController
public class CourseController {
    private final CourseService courseService;
}
```

#### Rule 2: Application Services implement port/in interfaces
```java
// ✓ CORRECT
@Service
@Transactional
public class CourseService implements CourseUseCase {
    // Implementation
}

// Port interface
public interface CourseUseCase {
    Course saveCourse(Course course);
    Optional<Course> getCourseById(Long courseId);
}
```

#### Rule 3: Domain does NOT depend on outer layers
```java
// Domain is pure Java - no Spring annotations
public class Course {
    // No @Entity, @Service, etc.
    // Pure business logic
}

// Entities go in adapter/out/persistence
@Entity
@Table(name = "courses")
public class CourseEntity {
    // JPA annotations
}
```

#### Rule 4: Adapters depend on ports, not vice versa
```java
// ✓ CORRECT - Adapter depends on port
public class CourseController {
    private final CourseUseCase useCase; // Port
}

// ✗ WRONG - Port depends on adapter
public interface CourseUseCase {
    ResponseEntity<?> something(CourseDto dto); // No web types!
}
```

### Package Structure
```
com.edunexuscourseservice/
├── adapter/
│   ├── in/
│   │   └── web/              # Controllers (REST)
│   ├── out/
│   │   ├── persistence/      # JPA repositories, entities
│   │   └── messaging/        # Kafka producers
│   └── config/               # Spring config
├── application/
│   └── service/              # @Service classes
├── port/
│   ├── in/                   # Use case interfaces (controllers depend on these)
│   └── out/                  # Repository interfaces (services depend on these)
├── domain/
│   ├── model/                # Domain entities, value objects
│   └── service/              # Domain services (if needed)
└── common/                   # Shared utilities, exceptions
```

### ArchUnit Rules
```java
@AnalyzeClasses(packages = "com.edunexuscourseservice")
public class HexagonalArchitectureTest {
    @ArchTest
    static final ArchRule controllers_should_only_depend_on_ports =
        classes().that().resideInAPackage("..adapter.in..")
            .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                    "..port.in..",
                    "..common..",
                    "java..",
                    "org.springframework..",
                    "lombok.."
                );

    @ArchTest
    static final ArchRule domain_should_not_depend_on_adapters =
        classes().that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                    "..domain..",
                    "java..",
                    "lombok.."
                );
}
```

## Consequences

### Positive
- **Clear boundaries**: Easy to understand what depends on what
- **Testability**: Ports can be mocked easily
- **Flexibility**: Adapters can be swapped
- **Enforced rules**: ArchUnit prevents violations

### Negative
- **More interfaces**: Additional port interfaces
- **Learning curve**: Team must understand hexagonal rules
- **Indirection**: One more layer of abstraction

## Implementation Plan
1. Create missing port/in interfaces
2. Refactor controllers to use only ports
3. Move business logic from controllers to services
4. Add ArchUnit tests
5. Update documentation

## Migration Checklist
- [ ] All controllers use only port/in interfaces
- [ ] All services implement port interfaces
- [ ] Domain has no Spring dependencies
- [ ] ArchUnit tests pass
- [ ] Documentation updated

## References
- [Alistair Cockburn - Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [ArchUnit Documentation](https://www.archunit.org/)
