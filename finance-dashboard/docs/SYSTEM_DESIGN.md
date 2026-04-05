# Finance Dashboard System Design

## 1) High-Level Architecture

The project is split into two applications:

- `finance-dashboard` (Spring Boot backend)
- `frontend` (React + Vite UI)

Runtime interaction:

1. User opens frontend (`localhost:3000`)
2. Frontend sends API requests to backend (`localhost:8080/api/v1`)
3. Backend authenticates using JWT and applies role checks
4. Backend reads/writes MySQL data using Spring Data JPA
5. Backend returns JSON/CSV responses

## 2) Backend Layers

### Controller Layer

REST endpoints are grouped by domain:

- `AuthController` (`/api/v1/auth`)
- `UserController` (`/api/v1/users`)
- `DashboardController` (`/api/v1/dashboard`)
- `FinancialRecordController` (`/api/v1/records`)

Responsibilities:

- Input validation (`@Valid`)
- Route-level authorization (`@PreAuthorize` where needed)
- HTTP status mapping
- Delegation to services

### Service Layer

Core business logic is in service implementations:

- `AuthServiceImpl`: register/login/refresh/logout
- `FinancialRecordServiceImpl`: CRUD, filter/search, ownership checks
- `DashboardServiceImpl`: aggregation (income/expense/net/category/month/week)
- `UserServiceImpl`: profile/admin user operations
- `RefreshTokenServiceImpl`: token persistence and rotation
- `CsvExportServiceImpl`: export records as CSV

### Repository Layer

Spring Data JPA repositories:

- `UserRepository`
- `FinancialRecordRepository` (+ `JpaSpecificationExecutor`)
- `RefreshTokenRepository`

Highlights:

- Pagination and dynamic query filtering via Specifications
- Aggregation queries for dashboard category totals

### Data Layer

MySQL is the primary datastore.

Main entities:

- `User`
- `FinancialRecord`
- `RefreshToken`

Soft-delete strategy:

- `User` and `FinancialRecord` use Hibernate soft-delete annotations.
- Deleted rows are excluded from normal reads via `@Where`.

## 3) Security Design

### Authentication

- Login/Register returns JWT access token + refresh token.
- `JwtAuthenticationFilter` extracts bearer token from `Authorization` header.
- `CustomUserDetailsService` loads user and role from DB.

### Authorization

- Global security config:
  - `/api/v1/auth/**` is public
  - Swagger/docs paths are public
  - all other routes require authentication
- Method-level authorization enforces roles:
  - `VIEWER`: dashboard read
  - `ANALYST`: financial operations
  - `ADMIN`: user administration

### Session Model

- Stateless (`SessionCreationPolicy.STATELESS`)
- No server HTTP session state

## 4) Resilience and Protection

### Rate Limiting

`RateLimitingFilter` protects `/api/**` with:

- per-client fixed-window throttling
- global fixed-window throttling
- in-flight concurrency guard

Throttle behavior:

- responds with HTTP `429`
- adds retry/limit headers
- returns structured error payload (`RATE_LIMIT_EXCEEDED`)

### Refresh Token Concurrency Safety

Refresh token rotation is serialized per-user in service logic to reduce race conditions under burst login traffic.

## 5) Caching Strategy

Dashboard summary is cached (`@Cacheable`) by username.

Cache invalidation happens on record writes/deletes (`@CacheEvict`) so dashboard values remain fresh after mutations.

## 6) API Error Handling Design

`GlobalExceptionHandler` standardizes errors:

- Consistent JSON shape (`status`, `message`, `code`, `details`, `timestamp`)
- Maps common validation/auth/access/system exceptions to stable API codes

This keeps client-side handling predictable.

## 7) Frontend Integration Design

Frontend uses Vite and proxies `/api` to backend.

Key integration points:

- auth token lifecycle (access + refresh token)
- protected API calls with bearer token
- dashboard and records screens mapped to backend endpoints

## 8) Deployment Notes

Current implementation is designed for a single backend instance with in-memory rate limiter counters.

For horizontally scaled production:

- move rate-limiter counters to a shared store (for example Redis)
- keep JWT secret/configuration managed via secure env variables
- use managed MySQL and connection pooling defaults tuned for workload
