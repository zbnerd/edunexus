# ACREATE-004: Service Consolidation Strategy

## Status
**PROPOSED** - Pending Review

## Context
EduNexus currently implements 11 separate microservices for a learning project with minimal traffic. The project README explicitly acknowledges this as "over-engineering."

### Current Architecture
```
┌─────────────────────────────────────────────────────────────────┐
│                        11 Microservices                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │  Gateway         │  │  GraphQL         │                   │
│  │  (API Gateway)   │  │  (API Gateway)   │                   │
│  └────────┬─────────┘  └────────┬─────────┘                   │
│           │                     │                               │
│           └──────────┬──────────┘                               │
│                      │                                          │
│           ┌──────────▼──────────┐                               │
│           │   Eureka Discovery  │                               │
│           └─────────────────────┘                               │
│                      │                                          │
│      ┌───────────────┼───────────────┬─────────────┐           │
│      ▼               ▼               ▼             ▼           │
│ ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│ │ Course   │  │Enrollment│  │  User    │  │  File    │        │
│ │ Service  │  │ Service  │  │ Service  │  │ Service  │        │
│ └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                                                                  │
│ ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│ │ Playback │  │  Coupon  │  │Attendance│  │  gRPC    │        │
│ │ Service  │  │ Service  │  │ Service  │  │  Common   │        │
│ └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Problems Identified (from README)
1. **"Purposeless technology adoption"** - MSA, Kafka without real need
2. **"Complexity explosion"** - Simple queries require multiple service hops
3. **"Lack of depth"** - Focus on architecture over fundamentals (concurrency, query optimization)
4. **11 services** for a learning project with minimal traffic

### Traffic Analysis
- Learning/development project
- No production scale requirements
- README states project is **ARCHIVED**

## Decision
**Three Options Presented for User Selection**

---

### Option A: Modular Monolith (RECOMMENDED for Learning)

**Architecture:**
```
┌─────────────────────────────────────────────────────────────────┐
│                    Single Deployment Unit                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Presentation Layer                   │    │
│  │  Controllers (REST, GraphQL)                            │    │
│  └────────────────────────────────────────────────────────┘    │
│                           │                                    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Application Layer                    │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │    │
│  │  │  Course  │ │ Enrollment││   User   │ │   File   │   │    │
│  │  │  Module  │ │  Module   ││  Module  │ │  Module  │   │    │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │    │
│  └────────────────────────────────────────────────────────┘    │
│                           │                                    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                    Domain Layer                         │    │
│  │  Core business logic, entities, value objects           │    │
│  └────────────────────────────────────────────────────────┘    │
│                           │                                    │
│  ┌────────────────────────────────────────────────────────┐    │
│  │                  Infrastructure Layer                   │    │
│  │  Repositories, external services, config               │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Benefits:**
- Single JVM to deploy, monitor, debug
- Faster development (no distributed complexity)
- Clear module boundaries enforce good design
- Can extract microservices later if needed
- Easier testing (no network mocking)

**Costs:**
- Harder to scale individual modules
- Single point of failure (but can run multiple instances)
- Technology boundaries less strict

**Migration Effort:** Medium (3-4 weeks)

---

### Option B: Reduced MSA (3-4 Services)

**Architecture:**
```
┌─────────────────────────────────────────────────────────────────┐
│                    3-4 Microservices                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌───────────────────────────────────────────────┐             │
│  │              API Gateway                       │             │
│  └──────────────┬────────────────────────────────┘             │
│                 │                                              │
│      ┌──────────┼──────────┐                                   │
│      ▼          ▼          ▼                                   │
│ ┌──────────┐ ┌──────────┐ ┌──────────┐                        │
│ │ Core     │ │ User     │ │ Content  │                        │
│ │ Business │ │ Service  │ │ Service  │                        │
│ │ Service  │ │          │ │          │                        │
│ │(Course+  │ │(Auth+    ││(File+    │                        │
│ │Enroll+   │ │User)     ││Playback) │                        │
│ │Rating)   │ │          ││          │                        │
│ └──────────┘ └──────────┘ └──────────┘                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**Service Grouping:**
- **Core Business**: Course, Enrollment, Coupon, Attendance, Rating
- **User**: User, Authentication
- **Content**: File, Playback

**Benefits:**
- Some isolation of concerns
- Can scale business logic independently
- Reduced operational overhead vs 11 services
- Team autonomy if team grows

**Costs:**
- Still distributed complexity
- Network latency for cross-service calls
- Multiple deployments to coordinate

**Migration Effort:** High (4-6 weeks)

---

### Option C: Current MSA (NOT RECOMMENDED)

**Keep 11 services as-is**

**Only valid if:**
- Learning purpose for distributed systems
- Resume/CV demonstration
- Explicit educational intent

**Not recommended for:**
- Production use
- Real product development
- Team productivity

---

## Recommendation Matrix

| Criteria | Modular Monolith | Reduced MSA | Current MSA |
|----------|------------------|-------------|-------------|
| Development Speed | ★★★★★ | ★★★☆☆ | ★☆☆☆☆ |
| Operational Overhead | ★★★★★ | ★★★☆☆ | ★☆☆☆☆ |
| Scalability | ★★★☆☆ | ★★★★★ | ★★★★★ |
| Team Autonomy | ★★☆☆☆ | ★★★★☆ | ★★★★★ |
| Testing Simplicity | ★★★★★ | ★★★☆☆ | ★☆☆☆☆ |
| Debugging | ★★★★★ | ★★★☆☆ | ★☆☆☆☆ |
| Learning Value | ★★★★☆ | ★★★★★ | ★★★☆☆ |

## Migration Path (If Modular Monolith Chosen)

### Phase 1: Preparation
1. Create module boundaries
2. Define module interfaces
3. Add ArchUnit rules for module enforcement

### Phase 2: Consolidation
1. Merge related services
2. Remove Eureka (not needed for single JVM)
3. Replace inter-service calls with direct method calls
4. Remove gRPC (use interfaces)

### Phase 3: Cleanup
1. Remove Kafka (use in-memory events)
2. Simplify configuration
3. Update documentation

### Phase 4: Optimization
1. Focus on query optimization
2. Add proper caching
3. Improve test coverage

## User Decision Required

**Please select one option:**
1. **Modular Monolith** - Recommended for learning and productivity
2. **Reduced MSA** - Balance between autonomy and complexity
3. **Current MSA** - Keep as-is for learning distributed systems
4. **Defer** - Address P0 issues first, decide later

## References
- [Modular Monolith: Primer](https://herbertograca.com/2017/09/14/are-microservices-really-the-future/)
- [Retiring a Microservice Anti-pattern](https://blog.filipski.me/2019/12/21/retiring-a-microservices-anti-pattern/)
- [Monolith First](https://martinfowler.com/bliki/MonolithFirst.html)
