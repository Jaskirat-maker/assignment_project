# Finance Dashboard Workflow Guide

## Overview

This document describes how data moves through the system in common user journeys and how backend components interact.

## End-to-End Request Lifecycle

For every incoming API request:

1. HTTP request reaches Spring Boot controller layer.
2. `JwtAuthenticationFilter` validates bearer token (if present).
3. `RateLimitingFilter` applies:
   - global request budget
   - per-client request budget
   - max concurrent in-flight limit
4. Spring Security authorization checks role/access.
5. Controller validates request body/query/path.
6. Service layer runs business logic.
7. Repository layer executes DB operations.
8. Response is returned as JSON (or CSV for export).
9. Exceptions are normalized by `GlobalExceptionHandler`.

## Core User Journeys

## 1) Register and Login

### Register (`POST /api/v1/auth/register`)

1. Client sends username/email/password.
2. `AuthServiceImpl` validates uniqueness (username + email).
3. Password is hashed and user row is created.
4. Access token is generated (`JwtTokenProvider`).
5. Refresh token is created and persisted.
6. Client receives `JwtResponse`.

### Login (`POST /api/v1/auth/login`)

1. Client sends username/password.
2. Spring `AuthenticationManager` verifies credentials.
3. Access token is generated.
4. Refresh token is rotated for user.
5. Client receives new `JwtResponse`.

## 2) Create and View Financial Records

### Create record (`POST /api/v1/records`)

1. Authenticated user submits record payload.
2. Controller validates required/positive fields.
3. Service maps request into `FinancialRecord`.
4. Ownership + audit fields (`createdBy`, `updatedBy`) are assigned.
5. Record is persisted.
6. Dashboard cache for that user is evicted.
7. Created record response is returned.

### List records (`GET /api/v1/records`)

1. Authenticated user sends optional filters.
2. Service builds dynamic specification:
   - not deleted
   - owner user id
   - optional type/category/search/date range
3. Repository executes paginated query.
4. API returns Spring page response.

## 3) Dashboard Summary

### Load dashboard (`GET /api/v1/dashboard/summary`)

1. Authenticated user requests summary.
2. Service checks cache first (`@Cacheable`).
3. On cache miss, computes:
   - total income/expense/net
   - category totals
   - monthly summary
   - weekly trends
   - recent transactions
4. Response is cached by username.
5. Aggregated response is returned.

## 4) Refresh and Logout

### Refresh (`POST /api/v1/auth/refresh`)

1. Client submits refresh token.
2. Service validates token presence + expiration.
3. New access token generated.
4. Refresh token rotated.
5. Updated `JwtResponse` returned.

### Logout (`POST /api/v1/auth/logout`)

1. Authenticated user calls logout.
2. Service deletes refresh token(s) for the user.
3. API returns success message.

## Authorization Workflow

## Request Classifications

- Public:
  - `/api/v1/auth/**`
  - Swagger and OpenAPI paths
- Protected:
  - all remaining APIs

## Role Checks

- Endpoint-level authorization:
  - dashboard summary: `VIEWER|ANALYST|ADMIN`
  - records CRUD: `ANALYST|ADMIN`
  - user management: `ADMIN` (via management authority)

## Data and State Workflow

## Soft Delete Behavior

- Users and financial records are soft-deleted (`deleted=true`).
- Default queries exclude deleted rows.
- Deletion updates timestamps and actor fields.

## Token Lifecycle

- Access token: short-lived bearer JWT used per request.
- Refresh token: persisted token used only on refresh endpoint.
- Token rotation is serialized per user to avoid race issues under concurrent login traffic.

## Cache Invalidation

- Dashboard summary cache key: username.
- Record create/update/delete invalidates that user's cached summary.

## Error and Recovery Workflow

- Validation errors return `400 VALIDATION_ERROR`.
- Authentication failures return `401 AUTHENTICATION_FAILED`.
- Authorization failures return `403 ACCESS_DENIED`.
- Resource misses return `404 RESOURCE_NOT_FOUND`.
- Rate-limited requests return `429 RATE_LIMIT_EXCEEDED`.
- Unexpected exceptions return `500 INTERNAL_SERVER_ERROR`.

## Operational Workflow (Local Development)

1. Start MySQL and ensure schema exists.
2. Start backend with Maven.
3. Start frontend with npm.
4. Open frontend on `localhost:3000`.
5. Use Swagger UI for backend endpoint testing.

