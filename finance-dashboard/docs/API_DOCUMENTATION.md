# Finance Dashboard API Documentation

## Base URL

- Local base URL: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication Model

- Auth type: JWT Bearer token.
- Access token header:
  - `Authorization: Bearer <access_token>`
- Refresh flow:
  - Login/Register returns both `token` and `refreshToken`.
  - Use refresh token on `/auth/refresh` to rotate tokens.

## Standard Error Response

Most failures return this structure:

- `status` (HTTP status code)
- `message` (human-readable message)
- `code` (stable error code)
- `details` (nullable map of field errors or metadata)
- `timestamp` (server timestamp)

Example:

`{"status":400,"message":"Validation failed for one or more fields.","code":"VALIDATION_ERROR","details":{"username":"Username is required"},"timestamp":"2026-04-05T15:47:41.729645066"}`

## Rate Limiting

Rate limiting is applied to `/api/**` endpoints.

- Per-client limit (`app.rate-limit.max-requests-per-client`)
- Global limit (`app.rate-limit.max-global-requests`)
- In-flight concurrency limit (`app.rate-limit.max-concurrent-requests`)
- Window size (`app.rate-limit.window-seconds`)

When throttled, API returns:

- HTTP `429 Too Many Requests`
- Headers: `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`, `X-RateLimit-Scope`
- Error code: `RATE_LIMIT_EXCEEDED`

## Roles and Access

- `VIEWER`: can read dashboard summary.
- `ANALYST`: CRUD financial records + dashboard.
- `ADMIN`: all analyst actions + user management endpoints.

---

## Endpoints

### 1) Authentication

#### POST `/auth/register`

Registers a new user and returns token pair.

Request body:

`{"username":"jane","email":"jane@example.com","password":"password123"}`

Success:

- `201 Created` with `JwtResponse`

Possible failures:

- `400 VALIDATION_ERROR`
- `409 CONFLICT`

---

#### POST `/auth/login`

Authenticates user and returns token pair.

Request body:

`{"username":"jane","password":"password123"}`

Success:

- `200 OK` with `JwtResponse`

Possible failures:

- `400 BAD_REQUEST`
- `401 AUTHENTICATION_FAILED`

---

#### POST `/auth/refresh`

Rotates access and refresh tokens.

Request body:

`{"refreshToken":"<refresh_token>"}`  

Success:

- `200 OK` with `JwtResponse`

Possible failures:

- `400 BAD_REQUEST` (token not found/invalid/expired)

---

#### POST `/auth/logout`

Invalidates current user refresh tokens.

Headers:

- `Authorization: Bearer <access_token>`

Success:

- `200 OK` with message

---

### 2) User APIs

#### GET `/users/me`

Returns current authenticated user profile.

Headers:

- `Authorization: Bearer <access_token>`

Success:

- `200 OK` with `UserResponse`

---

#### GET `/users/{id}` (ADMIN)

Returns user profile by id.

Success:

- `200 OK`

Possible failures:

- `401`, `403`, `404`

---

#### GET `/users` (ADMIN)

Returns non-deleted users.

Success:

- `200 OK` with `UserResponse[]`

---

#### PUT `/users/{id}` (ADMIN)

Updates user by id.

Request body shape is same as register payload.

Success:

- `200 OK` with `UserResponse`

---

#### DELETE `/users/{id}` (ADMIN)

Soft-deletes user.

Success:

- `204 No Content`

---

### 3) Dashboard APIs

#### GET `/dashboard/summary`

Returns aggregated dashboard metrics for current user.

Headers:

- `Authorization: Bearer <access_token>`

Success:

- `200 OK` with:
  - `totalIncome`
  - `totalExpense`
  - `netBalance`
  - `categoryWiseTotals`
  - `monthlySummary`
  - `recentTransactions`
  - `weeklyTrends`

---

### 4) Financial Record APIs

#### POST `/records`

Creates record for current user.

Headers:

- `Authorization: Bearer <access_token>`

Request body:

`{"title":"Salary","description":"April","amount":5000,"type":"INCOME","category":"Salary","transactionDate":"2026-04-01"}`

Success:

- `201 Created` with `FinancialRecordResponse`

---

#### GET `/records`

Returns paginated records for current user with optional filters.

Headers:

- `Authorization: Bearer <access_token>`

Query params:

- `type` (`INCOME` | `EXPENSE`)
- `category` (`string`)
- `search` (`string`, title/description/category)
- `startDate` (`yyyy-MM-dd`)
- `endDate` (`yyyy-MM-dd`)
- `page`, `size`, `sort`

Success:

- `200 OK` with Spring `Page<FinancialRecordResponse>`

---

#### GET `/records/{id}`

Returns single record (owner-only).

Success:

- `200 OK`

Possible failures:

- `400 ACCESS DENIED`
- `404`

---

#### PUT `/records/{id}`

Updates record (owner-only).

Success:

- `200 OK`

---

#### DELETE `/records/{id}`

Soft-deletes record (owner-only).

Success:

- `204 No Content`

---

#### GET `/records/type/{type}`

Returns paginated records filtered by type.

Path variable:

- `type`: `INCOME` or `EXPENSE`

Success:

- `200 OK`

---

#### GET `/records/export`

Exports current user records as CSV.

Success:

- `200 OK`
- Content disposition: attachment (`transactions.csv`)

---

## Primary Data Models

### `JwtResponse`

- `token`
- `type` (`Bearer`)
- `username`
- `email`
- `primaryRole`
- `refreshToken`

### `FinancialRecordRequest`

- `title` (required)
- `description` (optional)
- `amount` (required, positive)
- `type` (required: `INCOME`/`EXPENSE`)
- `category` (required)
- `transactionDate` (required)

### `FinancialRecordResponse`

- Record fields + audit fields
- Includes `createdBy` and `updatedBy` user summaries
