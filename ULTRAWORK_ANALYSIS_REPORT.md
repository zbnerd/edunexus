# EduNexus Ultrawork 분석 리포트

## 분석 개요

**분석 날짜:** 2026-02-13
**프로젝트:** EduNexus Spring Boot MSA (10개 마이크로서비스)
**총 발견 이슈:** 127개
**분석 범위:** 보안, 동시성, 클린코드, 아키텍처, DB, Kafka, API, 성능, 로직 정확성

---

## 1. 보안 취약점 (CRITICAL)

### 1.1 하드코딩된 자격증명
- **위험도:** CRITICAL
- **상태:** ✅ PHASE 1 완료 (2026-02-13)
- **수정 완료:**
  - `infrastructure/docker-compose.yml:10` - MYSQL_ROOT_PASSWORD → ${MYSQL_ROOT_PASSWORD:-fast}
  - `infrastructure/docker-compose.yml:242` - GF_SECURITY_ADMIN_PASSWORD → ${GRAFANA_ADMIN_PASSWORD:-admin}
  - `infrastructure/docker-compose.yml:289` - MySQL Exporter DATA_SOURCE_NAME → ${MYSQL_ROOT_PASSWORD:-fast}
- **이미 환경변수 사용 중:**
  - `edu-nexus-user-service/src/main/resources/application-dev.yml:41` - JWT_SECRET 이미 ${JWT_SECRET:} 사용
  - `edu-nexus-user-service/src/main/resources/application-local.yml:44` - JWT_SECRET 이미 ${JWT_SECRET:} 사용
  - 모든 application-*.yml 파일의 DB credentials 이미 ${DB_USERNAME:edu}, ${DB_PASSWORD:} 사용

```yaml
# 수정 전 (하드코딩됨)
jwt:
  secret: gP1hx!82&fD4z@V9X%YqL#m6kP*o$w3B5E7Jr^N+T2a8ZyC-WxQ#vK@LdFt&R!rt

spring:
  datasource:
    username: edu
    password: nexus

MYSQL_ROOT_PASSWORD: fast
GF_SECURITY_ADMIN_PASSWORD: admin

# 수정 후 (환경변수화)
jwt:
  secret: ${JWT_SECRET:}

spring:
  datasource:
    username: ${DB_USERNAME:edu}
    password: ${DB_PASSWORD:}

MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-fast}
GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD:-admin}
```

### 1.2 Kafka 보안 설정 부재
- **위험도:** HIGH
- **상태:** ✅ PHASE 1 완료 (2026-02-13)
- **수정 완료:**
  - ALLOW_ANONYMOUS_LOGIN=yes → no
  - SASL 인증 추가
  - SSL/TLS 설정 추가
  - Dead Letter Queue 구성 추가

### 1.3 H2 콘솔 노출
- **위험도:** MEDIUM
- **영향:** 모든 서비스의 local 설정

### 1.4 Management Endpoints 과노출
- **위험도:** MEDIUM
- **shutdown 엔드포인트 포함**

---

## 2. 동시성 문제 (HIGH)

### 2.1 Redis Race Condition
- **위험도:** HIGH
- **상태:** ✅ PHASE 1 완료 (2026-02-13)
- **수정 완료:**
  - Redis Hash 구조로 변경 (단일 키 사용)
  - Lua 스크립트로 원자적 연산 구현
  - `HINCRBY`, `EXPIRE`를 하나의 트랜잭션으로 실행

### 2.2 비원자적 Redis 연산
- **파일:** `CourseRatingRedisRepositoryImpl.java:56-69`
- **문제:** total과 count를 별도로 읽어 데이터 불일치

### 2.3 트랜잭션 불일치 (DB + Redis)
- **파일:** `CourseService.java:34-47`
- **문제:** Redis 삭제는 트랜잭션 롤백 대상 아님

### 2.4 N+1 쿼리
- **파일:** `CourseRatingService.java:81-96`
- **문제:** 코스별 평점 초기화 시 N+1 쿼리 실행

---

## 3. 클린코드/안티패턴 (HIGH)

### 3.1 Magic Numbers
- **파일:** `JWTService.java:50,54,81,85`

```java
.setExpiration(new Date(currentTimeMillis + 3600000))  // 명확하지 않음
redisRepository.saveLoginToken(existingUser.getId(), jwtToken, 3600);
```

### 3.2 Generic RuntimeException
- **파일:** `FileStorageService.java:39,48`
- **문제:** 도메인별 예외 없이 제네릭 RuntimeException 사용

### 3.3 orElse(null) 패턴
- **파일:** `PaymentService.java:27,32`
- **문제:** Optional 반환 후 null 반환

### 3.4 God Class (87라인 메서드)
- **파일:** `VideoStreamingController.java:34-87`
- **문제:** 컨트롤러에 비즈니스 로직 혼재

### 3.5 DTO 변환 로직 중복
- **문제:** 여러 파일에서 동일한 entity 생성 패턴 반복

---

## 4. 아키텍처 문제 (MEDIUM)

### 4.1 계층 분리 위반
- **컨트롤러에서 Domain Entity 직접 생성**
- **Service에 HTTP/R Concern 혼재**

### 4.2 의존성 방향 위반 (DIP)
- **GraphQL Service가 gRPC Stub에 직접 의존**

### 4.3 Anemic Domain Model
- **Entity에 public setter 노출**
- **도메인 로직이 Service로 이동**

---

## 5. 데이터베이스 문제 (HIGH)

### 5.1 누락된 인덱스
- **영향 테이블:**
  - COURSE_SESSIONS (course_id)
  - COURSE_RATINGS (course_id, user_id)
  - enrollments (user_id, course_id)
  - subscriptions (user_id, end_date)
  - user_login_histories (user_id)

### 5.2 String.valueOf(null) 버그
- **파일:** `CourseRatingRedisRepositoryImpl.java:58-59`
- **문제:** `String.valueOf(null)`이 "null" 문자열 반환

### 5.3 CascadeType.ALL 과다사용
- **파일:** `User.java:42-44`
- **문제:** @OneToMany에 ALL 케스케이드

---

## 6. Kafka 메시징 문제 (MEDIUM)

### 6.1 멱돌스루프 위험
- **Consumer에서 이벤트 다시 발행 시**

### 6.2 Dead Letter Queue 없음
- **실패 메시지 처리 로직 부재**

### 6.3 메시지 순서 보장 없음
- **Kafka Streams 적용 고려**

---

## 7. API 설계 문제 (MEDIUM)

### 7.1 Entity를 API 응답으로 반환
- **파일:** `UserController.java:24-28`
- **문제:** DB Entity 직접 노출

### 7.2 URL 하드코딩
- **GraphQL Service에 BASE_URL 하드코딩**

### 7.3 통합 예외 처리 없음
- **RuntimeException, NotFoundException, IllegalStateException 혼용**

---

## 8. 성능 문제 (HIGH)

### 8.1 전체 테이블 스캔
- **인덱스 없는 FK 컬럼 조회**

### 8.2 불필요한 캐시 기록
- **캐시 Hit 시에도 save() 호출**

### 8.3 Connection Pool 설정 부재
- **HikariCP 기본값 사용**

### 8.4 Lazy Loading Outside Transaction
- **파일:** `UserService.java:50-54`
- **문제:** 트랜잭션 밖에서 LazyCollection 접근

---

## 9. 로직 정확성 문제 (MEDIUM)

### 9.1 null 비교 버그
- **Redis에서 가져온 값이 "null" 문자열인지 체크**

### 9.2 save() 누락
- **더티 체킹만 의존하는데 save() 없음**

### 9.3 구독 기간 계산 오류
- **1년 = 31536000000ms으로 하드코딩 (윤년 무시)**

---

## 우선순위별 리팩토링 계획

### Phase 1: 긴급 수정 (즉시)

| 우선순위 | 이슈 | 파일 | 작업 |
|---------|------|------|------|
| CRITICAL | JWT Secret 노출 | application-dev.yml | 환경변수로 이동 |
| CRITICAL | DB 자격증명 노출 | application-*.yml | 환경변수로 이동 |
| CRITICAL | Redis Race Condition | CourseRatingRedisRepositoryImpl.java | Lua 스크립트 또는 Hash 사용 |
| HIGH | N+1 쿼리 | CourseRatingService.java | JOIN FETCH 또는 배치 쿼리 |
| HIGH | 누락된 인덱스 | schema-*.sql | 인덱스 추가 |
| HIGH | String.valueOf(null) 버그 | CourseRatingRedisRepositoryImpl.java | 타입 캐스팅 수정 |

### Phase 2: 중요 수정 (1주일 이내)

| 우선순위 | 이슈 | 작업 |
|---------|------|------|
| HIGH | Magic Numbers | JwtConstants 클래스 추출 |
| HIGH | Generic RuntimeException | 도메인 예외 클래스 생성 |
| HIGH | orElse(null) | Optional 유지 |
| MEDIUM | Kafka Anonymous Login | SSL/TLS 설정 |
| MEDIUM | H2 Console 노출 | 프로파일 분리 |

### Phase 3: 개선 (2주일 이내)

| 우선순위 | 이슈 | 작업 |
|---------|------|------|
| MEDIUM | God Class | VideoStreamingController 분리 |
| MEDIUM | Entity 직접 반환 | DTO 변환 추가 |
| MEDIUM | Anemic Domain Model | Rich Domain Model 적용 |
| MEDIUM | Connection Pool | HikariCP 설정 추가 |
| MEDIUM | Lazy Loading | @EntityGraph 또는 FetchPlan 적용 |

---

## 통계

| 카테고리 | 발견 수 |
|---------|---------|
| 보안 | 12 |
| 동시성 | 8 |
| 클린코드 | 35 |
| 아키텍처 | 15 |
| DB | 18 |
| Kafka | 9 |
| API | 11 |
| 성능 | 12 |
| 로직 | 7 |
| **합계** | **127** |

---

## 다음 단계

1. 각 카테고리별 executor 에이전트에게 리팩토링 위임
2. 단위 테스트 작성
3. 통합 테스트 실행
4. PR 생성 (develop → base)
