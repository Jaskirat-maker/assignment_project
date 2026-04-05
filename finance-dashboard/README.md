# Finance Dashboard API

A Spring Boot 3.2.3 application providing secure financial record management with role-based access control.

## Documentation
- API reference: `docs/API_DOCUMENTATION.md`
- System design: `docs/SYSTEM_DESIGN.md`
- End-to-end workflow: `docs/WORKFLOW.md`
- Internship submission brief: `docs/INTERNSHIP_SUBMISSION.md`

## Features
- JWT authentication with access + refresh tokens
- User roles: VIEWER, ANALYST, ADMIN
- CRUD for financial records (soft-delete for removals, filters, pagination, and keyword search)
- Dashboard summary with total income/expense/net balance
- Recent transactions and weekly trend analytics
- Data export to CSV
- Caffeine caching for dashboard summary
- Exception handling and validation with structured API errors
- OpenAPI docs at `/swagger-ui/index.html`

## Running Locally
1. Create `.env` from `.env.example` or set env vars.
2. Run with Maven:
   - `mvn clean spring-boot:run`
3. API base URL: `http://localhost:8080/api/v1`

## Authentication
- POST `/api/v1/auth/register` (returns access + refresh token)
- POST `/api/v1/auth/login`
- POST `/api/v1/auth/refresh`
- POST `/api/v1/auth/logout`

## Financial Records Search and Filters
- GET `/api/v1/records?search=rent`
- Optional query parameters:
  - `type` (INCOME | EXPENSE)
  - `category`
  - `search` (matches title, description, category; case-insensitive)
  - `startDate` / `endDate` (`yyyy-MM-dd`)
  - `page`, `size`, `sort`

Example:
- `/api/v1/records?type=EXPENSE&search=travel&startDate=2026-01-01&endDate=2026-12-31&page=0&size=20`

## Role-based endpoints
- `VIEWER`: read dashboard 
- `ANALYST`: CRUD records, dashboard read
- `ADMIN`: user management and all functionality
- User admin endpoints:
  - `GET /api/v1/users`
  - `GET /api/v1/users/{id}`
  - `PUT /api/v1/users/{id}` (supports role/status/password updates)
  - `PATCH /api/v1/users/{id}/status?active=true|false`
  - `PATCH /api/v1/users/{id}/role?role=VIEWER|ANALYST|ADMIN`
  - `DELETE /api/v1/users/{id}`

## Tests
- `mvn test` runs unit/integration tests

## API Docs
- Swagger UI at `/swagger-ui/index.html`
- OpenAPI JSON at `/v3/api-docs`
