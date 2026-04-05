# Finance Dashboard Frontend

React frontend for the Spring Boot finance APIs.

## Stack
- React + Vite
- Tailwind CSS (v4 via `@tailwindcss/vite`)
- Recharts
- Axios + React Router

## Run
1. Install dependencies:
   - `npm install`
2. Start dev server (required by task):
   - `npm start`

The frontend runs on `http://localhost:3000` and proxies `/api/*` to `http://localhost:8080`.

## API Integration
- Login: `POST /api/v1/auth/login`
- Register: `POST /api/v1/auth/register`
- Dashboard summary: `GET /api/v1/dashboard/summary`
- Financial records: `GET /api/v1/records`

JWT access token is sent in the `Authorization: Bearer <token>` header. Refresh tokens are used for automatic token rotation on `401`.

## Optional Mock Mode (frontend-only fallback)
If backend is unavailable locally, you can still run UI flows in a mock mode:
- Create `.env.local` with: `VITE_USE_MOCK=true`
- Run: `npm start`

When mock mode is disabled (default), all calls use the real backend APIs.
