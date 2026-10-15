# Namma Nivasa Backend

This backend is a design-first Node.js/TypeScript service for the Namma Nivasa apartment-management platform.

## Prerequisites

- Node.js 20+
- PostgreSQL 16+ or Docker for a local isolated instance
- Firebase project with Admin SDK enabled for production auth validation

## Environment setup

Copy `.env.example` to `.env` and adjust values for your local environment.

```bash
cp .env.example .env
```

Do not commit real credentials or secrets.

Required environment variables:

- `DATABASE_URL`: PostgreSQL connection string for the primary development or production database.
- `TEST_DATABASE_URL`: isolated PostgreSQL connection string for test or local validation work; keep it separate from production.
- `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, `FIREBASE_PRIVATE_KEY`: required when validating Firebase ID tokens in production or staging.

## Local PostgreSQL setup

### Option 1: local PostgreSQL instance

Create a database and user for Namma Nivasa, then fill in the connection strings in `.env`.

```bash
createdb namma_nivasa_dev
psql -d namma_nivasa_dev -c "CREATE USER namma_nivasa WITH PASSWORD 'change_me';"
```

### Option 2: Dockerized PostgreSQL (recommended for isolated validation)

```bash
docker run --name namma-nivasa-postgres   -e POSTGRES_USER=namma_nivasa   -e POSTGRES_PASSWORD=change_me   -e POSTGRES_DB=namma_nivasa_dev   -p 5432:5432   -d postgres:16-alpine
```

Then point `DATABASE_URL` to:

```bash
postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_dev
```

For an isolated test database, use a different database name, for example `namma_nivasa_test`.

## Dependency install

```bash
npm install
```

## Database migration

Run the migration against the configured `DATABASE_URL` only.

```bash
npm run migrate
```

For a fresh local database, ensure the database exists before running the migration.

## Development

```bash
npm run dev
```

## Test commands

```bash
npm test
```

Test authentication is only enabled when `NODE_ENV` is not `production` and `AUTH_MODE=test`. Production startup fails fast if test auth is enabled, preventing an insecure fallback.

## Readiness checks

```bash
curl http://localhost:4000/health
curl http://localhost:4000/ready
```

The `/ready` endpoint verifies that a configured PostgreSQL database is reachable before reporting the service as ready. Without `DATABASE_URL` or `TEST_DATABASE_URL`, readiness returns `503` instead of silently proceeding.

## Production build

```bash
npm run build
npm start
```

## Notes

- The backend preserves the approved Phase 2A/2B domain rules and Phase 2D schema decisions.
- Missing PostgreSQL configuration results in a clear error when a database connection is attempted.
- Visitor requests, audit entries and application notifications are backed by PostgreSQL when configured; demo data remains only for isolated test-mode fallback.
- Firebase validation remains required in production or staging; it is not bypassed by default.
- Full payment, household-grant, external storage provider, and AI-provider integrations remain configuration-dependent and are intentionally isolated behind backend service boundaries.
