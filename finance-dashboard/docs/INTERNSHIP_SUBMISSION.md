# Internship Assignment Submission Note

## Candidate Submission

This repository contains my backend submission for the **Finance Data Processing and Access Control Backend** assignment.

Backend project path:

- `finance-dashboard`

Frontend project path (for integration testing):

- `frontend`

## Assignment-to-Implementation Mapping

### 1) User and Role Management

Implemented:

- User creation and login/registration flow
- Role model: `VIEWER`, `ANALYST`, `ADMIN`
- User active/inactive status management
- Role reassignment endpoints (admin-only)
- Admin-only user listing, read-by-id, update, and soft-delete

Key APIs:

- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/status?active=true|false`
- `PATCH /api/v1/users/{id}/role?role=VIEWER|ANALYST|ADMIN`
- `DELETE /api/v1/users/{id}`

### 2) Financial Records Management

Implemented:

- Create, read, update, soft-delete records
- Filtering by type/category/search/date range
- Pagination support

Key APIs:

- `POST /api/v1/records`
- `GET /api/v1/records`
- `GET /api/v1/records/{id}`
- `PUT /api/v1/records/{id}`
- `DELETE /api/v1/records/{id}`
- `GET /api/v1/records/type/{type}`
- `GET /api/v1/records/export`

### 3) Dashboard Summary APIs

Implemented:

- Total income
- Total expenses
- Net balance
- Category-wise totals
- Monthly summary
- Weekly trends
- Recent activity

Key API:

- `GET /api/v1/dashboard/summary`

### 4) Access Control Logic

Implemented:

- JWT bearer auth
- Stateless security model
- Method-level authorization using role authorities
- Admin-only user management endpoints
- Viewer/Analyst/Admin behavior split

### 5) Validation and Error Handling

Implemented:

- Request DTO validation with field-level constraints
- Standardized API error payload through global exception handler
- Proper HTTP status codes for validation/auth/access/resource/system errors

### 6) Data Persistence

Implemented:

- MySQL persistence via Spring Data JPA/Hibernate
- Entity modeling for users, records, refresh tokens
- Soft delete for users and records

## Optional Enhancements Included

- JWT authentication and refresh lifecycle
- Pagination and search/filter support
- Soft delete strategy
- Rate limiting (per-client, global, concurrency guard)
- Unit/web-layer/integration tests
- API documentation and system design/workflow documents

## System Design Summary

- Layered backend architecture: controller -> service -> repository -> database
- Security filters for JWT + rate limiting
- Cache-backed dashboard summaries with cache eviction on mutating record writes
- Structured error contracts for predictable client behavior

See:

- `docs/SYSTEM_DESIGN.md`
- `docs/WORKFLOW.md`

## Assumptions

- Single backend instance for local/development execution
- Roles are represented as string authorities matching enum names
- MySQL is available locally with configured credentials

## Tradeoffs

- Rate limiting uses in-memory counters (simple and effective for single instance).
  - For distributed production scale, counters should be moved to a shared store (for example Redis).
- User update API currently allows role/status/password updates in one admin endpoint for operational simplicity.

## How to Run

1. Ensure MySQL is running.
2. Ensure schema exists: `finance_dashboard`.
3. Start backend:
   - `cd finance-dashboard`
   - `mvn spring-boot:run`
4. (Optional) Start frontend:
   - `cd frontend`
   - `npm install`
   - `npm start`

## Verification Evidence

Verified in this submission:

- Swagger endpoint reachable
- Auth register/login/refresh/logout flow
- Authorized financial record create/list
- Dashboard summary response
- Current user profile endpoint
- Focused backend tests for auth, records, dashboard, users, rate limiting, integration flow

## Notes for Reviewer

- This project is intentionally structured for clarity over unnecessary complexity.
- Business logic and authorization decisions are centralized and testable.
- Documentation is provided for API, architecture, and workflow to support maintainability and onboarding.
