# Finance Dashboard API

A Spring Boot 3.2.3 application providing secure financial record management with role-based access control.

## Features
- JWT authentication with access + refresh tokens
- User roles: VIEWER, ANALYST, ADMIN
- CRUD for financial records (create/update/delete by owner; view with filters and pagination)
- Dashboard summary with total income/expense/net worth
- Recent transactions and weekly trend analytics
- Data export to CSV
- Caffeine caching for dashboard summary
- Exception handling and validation
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

## Role-based endpoints
- `VIEWER`: read dashboard 
- `ANALYST`: CRUD records, dashboard read
- `ADMIN`: user management and all functionality

## Tests
- `mvn test` runs unit/integration tests

## API Docs
- Swagger UI at `/swagger-ui/index.html`
- OpenAPI JSON at `/v3/api-docs`
