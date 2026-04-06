# ☕ 커피숍 주문 시스템

> 다수 서버 환경에서도 안정적으로 동작하는 커피숍 주문 시스템

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [ERD](#erd)
- [API 명세](#api-명세)
- [설계 의도](#설계-의도)
- [문제 해결 전략](#문제-해결-전략)
- [기술적 선택 이유](#기술적-선택-이유)
- [트러블슈팅](#트러블슈팅)

---

## 프로젝트 소개

채용 사전과제로 구현한 커피숍 주문 시스템이다.

단순한 CRUD가 아닌 **다수 서버 환경**에서의 동시성, 데이터 일관성, 확장성을 고려하여 설계했다.

### 핵심 구현 기능

- 커피 메뉴 목록 조회
- 포인트 충전 (동시성 제어 적용)
- 커피 주문 / 결제 (포인트 차감 + 외부 데이터 플랫폼 전송)
- 인기 메뉴 Top 3 조회 (Redis 캐싱 + 스케줄러 집계)

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8.x |
| Cache | Redis |
| Build | Gradle |
| Test | JUnit 5, Spring Boot Test |

---

## 실행 방법

### 사전 요구사항

- Java 17
- MySQL 8.x
- Redis

### 1. MySQL DB 생성

```sql
CREATE DATABASE coffee_shop;
```

### 2. Redis 실행

```bash
brew services start redis
```

### 3. application.properties 설정

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/coffee_shop?serverTimezone=Asia/Seoul
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 4. 실행

```bash
./gradlew bootRun
```

---

## ERD

```
USERS
├── id              BIGINT      PK
├── name            VARCHAR(100)
├── point_balance   INT
├── created_at      DATETIME
└── updated_at      DATETIME

POINT_HISTORIES
├── id              BIGINT      PK
├── user_id         BIGINT      FK → USERS
├── amount          INT
├── type            ENUM(CHARGE, ORDER_PAYMENT)
├── balance_after   INT
└── created_at      DATETIME

MENUS
├── id              BIGINT      PK
├── name            VARCHAR(100)
├── price           INT
├── is_available    BOOLEAN
└── created_at      DATETIME

ORDERS
├── id              BIGINT      PK
├── user_id         BIGINT      FK → USERS
├── menu_id         BIGINT      FK → MENUS
├── price           INT
├── status          ENUM(ORDERED, PAID, CANCELLED)
└── ordered_at      DATETIME

POPULAR_MENU_CACHE
├── id              BIGINT      PK
├── menu_id         BIGINT      FK → MENUS
├── order_count     INT
├── aggregated_date DATE
└── updated_at      DATETIME
```

| 테이블 | 설명 |
|---|---|
| USERS | 사용자 정보 및 포인트 잔액 |
| POINT_HISTORIES | 포인트 충전 / 차감 전체 이력 |
| MENUS | 커피 메뉴 목록 |
| ORDERS | 주문 내역 (결제 금액 스냅샷 포함) |
| POPULAR_MENU_CACHE | 인기 메뉴 집계 캐시 테이블 |

---

## API 명세

### 1. 메뉴 목록 조회

```
GET /api/v1/menus
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "menus": [
      { "menuId": 1, "name": "아메리카노", "price": 4500 },
      { "menuId": 2, "name": "카페라떼", "price": 5000 }
    ]
  },
  "message": null
}
```

---

### 2. 포인트 충전

```
POST /api/v1/users/{userId}/points/charge
```

**Request Body**
```json
{ "amount": 10000 }
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "chargedAmount": 10000,
    "currentBalance": 15000
  },
  "message": null
}
```

---

### 3. 커피 주문 / 결제

```
POST /api/v1/orders
```

**Request Body**
```json
{ "userId": 1, "menuId": 1 }
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "orderId": 1,
    "userId": 1,
    "menuId": 1,
    "menuName": "아메리카노",
    "price": 4500,
    "remainingBalance": 5500,
    "orderedAt": "2026-04-05T19:19:47.268612"
  },
  "message": null
}
```

---

### 4. 인기 메뉴 Top 3 조회

```
GET /api/v1/menus/popular
```

**Response 200**
```json
{
  "success": true,
  "data": {
    "popularMenus": [
      { "rank": 1, "menuId": 1, "name": "아메리카노", "price": 4500, "orderCount": 20 },
      { "rank": 2, "menuId": 2, "name": "카페라떼", "price": 5000, "orderCount": 15 },
      { "rank": 3, "menuId": 3, "name": "바닐라라떼", "price": 5500, "orderCount": 8 }
    ],
    "aggregatedAt": "2026-04-05T20:25:04"
  },
  "message": null
}
```

---

### 공통 에러 응답

```json
{
  "code": "U003",
  "message": "포인트 잔액이 부족합니다.",
  "timestamp": "2026-04-05T20:25:04"
}
```

| HTTP 상태 | 코드 | 설명 |
|---|---|---|
| 400 | U002 | 충전 금액이 0 이하 |
| 400 | U003 | 포인트 잔액 부족 |
| 404 | U001 | 존재하지 않는 사용자 |
| 404 | M001 | 존재하지 않는 메뉴 |
| 409 | M002 | 판매 중단된 메뉴 |
| 500 | S001 | 서버 내부 오류 |

---

## 설계 의도

### 핵심 문제 인식

"다수 서버 환경에서 안정적으로 동작"이라는 요구사항에서 세 가지 핵심 문제를 도출했다.

1. **포인트 동시성** — 같은 유저가 동시에 주문하면 포인트가 음수가 될 수 있다
2. **인기 메뉴 집계 성능** — 매 요청마다 7일치를 집계하면 데이터가 쌓일수록 느려진다
3. **주문과 외부 전송의 일관성** — 외부 플랫폼 전송 실패 시 주문을 롤백해야 하는가?

모든 설계 결정은 이 세 문제를 해결하는 방향으로 이루어졌다.

### USERS 테이블에 point_balance를 직접 보관한 이유

포인트 잔액을 매번 POINT_HISTORIES를 집계해서 계산하면 읽기 요청마다 SUM 쿼리가 발생한다. 잔액은 읽기 빈도가 높으므로 USERS에 비정규화된 필드로 유지하는 것이 실용적이다. 대신 포인트 변경은 반드시 트랜잭션 내에서 두 테이블을 함께 갱신하여 정합성을 보장한다.

### ORDERS에 price를 스냅샷으로 저장한 이유

주문 시점의 금액을 영구적으로 보존하기 위해서다. menu_id만 저장하고 가격을 MENUS에서 조인하면 나중에 메뉴 가격이 변경될 경우 과거 주문 금액이 달라진다.

### MENUS에 is_available 컬럼을 둔 이유

Hard Delete 시 ORDERS의 외래 키 참조가 깨진다. Soft Delete(is_available = false)로 이력 데이터를 보존하면서 사용자에게는 노출하지 않을 수 있다.

---

## 문제 해결 전략

### 1. 포인트 동시성 — 비관적 락(Pessimistic Lock)

다수 서버 환경에서 같은 유저가 동시에 포인트를 차감하면 Lost Update 문제가 발생한다.

```
잔액: 1000P
스레드 A: SELECT balance → 1000P 읽음
스레드 B: SELECT balance → 1000P 읽음
스레드 A: 500P 차감 → UPDATE balance = 500P
스레드 B: 500P 차감 → UPDATE balance = 500P  ← 잘못된 결과!
```

세 가지 선택지를 검토했다.

| 방식 | 장점 | 단점 | 선택 |
|---|---|---|---|
| 낙관적 락 | 충돌 없을 때 성능 우수 | 충돌 시 재시도 필요, 포인트 차감 실패 노출 | ✗ |
| 비관적 락 | 정합성 강력 보장, 구현 단순 | 락 대기로 처리량 감소 | ✅ |
| Redis 분산 락 | 글로벌 락 가능 | 구현 복잡, Redis 장애 시 SPOF 발생 | ✗ |

비관적 락을 선택한 이유는 포인트가 금전적 가치가 있는 데이터이기 때문이다. 낙관적 락의 재시도 실패를 사용자에게 노출하는 설계는 적합하지 않다. 비관적 락은 DB 레벨에서 `SELECT ... FOR UPDATE`로 row를 잠그기 때문에 서버가 몇 대든 정합성을 보장한다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT u FROM User u WHERE u.id = :userId")
Optional<User> findByIdWithLock(@Param("userId") Long userId);
```

### 2. 인기 메뉴 집계 — 스케줄러 + Redis 캐싱 이중 구조

매 요청마다 7일치 ORDERS를 집계하면 데이터가 쌓일수록 응답 시간이 늘어난다. 인기 메뉴는 실시간성보다 안정적인 응답 속도가 중요하다고 판단했다. 이를 **허용 가능한 오래된 데이터(acceptable staleness)** 전략이라고 표현한다.

```
[주문 발생] → [orders 테이블 저장]
                      ↓
          [스케줄러: 10분마다 집계]
                      ↓
     [popular_menu_cache 테이블 갱신]
                      ↓
          [Redis 캐싱 (TTL 10분)]
                      ↓
     [인기 메뉴 조회 API: Redis에서 즉시 응답]
                      ↓
          [Redis miss → DB fallback]
```

Redis 장애 시 popular_menu_cache 테이블에서 fallback 조회하여 단일 장애 지점을 방지했다.

### 3. 주문과 외부 데이터 전송 — @Async 비동기 분리

외부 데이터 플랫폼 전송은 분석을 위한 부가 로직이다. 부가 로직의 실패가 핵심 비즈니스(주문/결제)의 실패를 유발하면 안 된다고 판단했다.

카페에서 카드 결제가 완료됐는데 "영수증 출력기가 고장났으니 결제를 취소하겠습니다"라고 하는 것과 같다.

```
[주문/결제 트랜잭션 commit]
          ↓
[외부 플랫폼 전송 — @Async 비동기]
          ↓
  성공 → 정상 처리
  실패 → 로그 기록 (주문 자체는 유효)
```

---

## 기술적 선택 이유

### Spring Boot + JPA

`@Lock` 어노테이션을 통한 비관적 락 구현과 `@Transactional`을 통한 원자성 보장이 이 과제의 핵심 요구사항과 잘 맞아떨어진다. 동시성 제어 의도를 코드 레벨에서 명확하게 표현할 수 있다.

### MySQL

포인트 차감과 주문 생성은 반드시 하나의 트랜잭션으로 묶여야 한다. ACID를 보장하는 관계형 DB가 이 요구사항을 가장 안전하게 처리할 수 있다.

### Redis

인기 메뉴 조회는 읽기 부하가 집중될 수 있는 API다. 인메모리 특성으로 DB 부하 없이 빠른 응답이 가능하다. TTL을 스케줄러 주기와 맞춰두면 캐시와 DB의 정합성도 자연스럽게 유지된다.

---

## 트러블슈팅

### Redis LocalDateTime 직렬화 문제

- **원인**: Jackson 기본 설정이 Java 8 날짜 타입을 지원하지 않음
- **해결**: `JavaTimeModule` 등록 + 객체를 JSON 문자열로 변환하여 저장하는 방식으로 변경

### 테스트 DB 데이터 오염 문제

- **원인**: 실제 DB를 공유하여 이전 테스트 데이터가 집계에 포함됨
- **해결**: `@BeforeEach`에서 외래 키 순서에 맞게 `deleteAll()` 처리

```java
// 외래 키 순서에 맞게 삭제
popularMenuCacheRepository.deleteAll();
pointHistoryRepository.deleteAll();
orderRepository.deleteAll();
userRepository.deleteAll();
menuRepository.deleteAll();
```

### Spring Boot 버전 문제

- **원인**: 프로젝트 생성 시 Spring Boot 4.x (불안정 버전)로 설정됨
- **해결**: Spring Boot 3.4.4로 다운그레이드