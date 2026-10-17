# Namma Nivasa — Backend, Database & Security Execution Plan

**Status:** Approved Master Specification  
**Budget Target:** $0 Free-Tier (Local Dev + Neon.tech + Render + Firebase Auth)  
**Scale Target:** 5,000+ Societies, 500,000+ Users, Zero Schema Rewrites  

---

## Executive Overview & Architecture Stack

Namma Nivasa uses a 3-tier multi-tenant architecture designed for strict data privacy, sub-50ms API response times, and zero operating costs during development and initial deployment.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              MOBILE APP CLIENTS (ANDROID)                              │
│  • Jetpack Compose Material3 UI with Permission-Based View Rendering (`hasPermission`)  │
│  • Active Community Switcher (`SocietyViewModel.uiState.activeCommunityId`)           │
│  • Offline-Resilient Hybrid Repository (`HybridNammaNivasaRepository`)                │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ REST API (HTTPS + Firebase Bearer Token)
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              NODE.JS / TYPESCRIPT BACKEND API                          │
│  • Firebase Auth JWT Token Verification Middleware (`auth.ts`)                         │
│  • Zero-Trust Permission & Membership Middleware (`requirePermission`)                 │
│  • Explicit Scoped REST Slugs (`/api/v1/communities/:communityId/...`)                  │
│  • Gzip Payload Compression & In-Memory Response Caching                               │
└───────────────────────────────────────────┬────────────────────────────────────────────┘
                                            │ Atomic SQL Transactions (`withTransaction`)
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              SERVERLESS POSTGRESQL ENGINE                              │
│  • Multi-Tenant Row Isolation (`community_id` on every operational table)               │
│  • B-Tree Compound Indexes on `(community_id, ...)` for < 5ms query execution          │
│  • Versioned Migration Scripts (`backend/migrations/001`, `002`, `003`...)              │
└────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Database Schema, Compound Indexing & Seed System

### 1.1 Schema Rules & Multi-Tenancy Boundary
* **Mandatory Field:** Every operational table (`flats`, `community_memberships`, `roles`, `bills`, `payments`, `notices`, `visitors`, `maintenance_requests`) must include a non-null `community_id UUID` column with a foreign key referencing `communities(id)`.
* **Monetary Precision:** Stored as `NUMERIC(18,2)` (never `FLOAT`/`DOUBLE`).
* **Financial Idempotency:** Payments include a unique `idempotency_key VARCHAR(100)` to prevent double-charging on slow mobile networks.

### 1.2 B-Tree Compound Indexing Strategy (< 5ms Execution)
To maintain sub-5ms query speeds as tables scale to millions of rows, all queries filter by `community_id`. The following compound indexes are required in `backend/migrations/001_init_schema.js`:
* `idx_notices_community_status`: `(community_id, status, created_at DESC)`
* `idx_visitors_community_status`: `(community_id, status, entry_time DESC)`
* `idx_bills_community_flat_status`: `(community_id, flat_id, status)`
* `idx_payments_community_flat`: `(community_id, flat_id, status)`
* `idx_tickets_community_status`: `(community_id, status, created_at DESC)`

### 1.3 Core Role Pre-Population
The initial migration pre-populates 5 Core Roles and their default `PermissionCode` mappings:
1. `COMMUNITY_ADMINISTRATOR`: President / Secretary / Super Admin.
2. `TREASURER_FINANCE_MANAGER`: Treasurer / Finance Lead.
3. `OWNER`: Resident Owner.
4. `TENANT`: Resident Tenant.
5. `SECURITY_GUARD`: Gate Guard.

### 1.4 Automated Seed Script (`npm run seed`)
A single-command seed script (`backend/src/database/seed.ts`) will insert a complete test environment:
* **Community:** *"Green Valley Apartments"* (`community_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'`)
* **Building & Flats:** Tower A (Flats A-101 to A-105)
* **5 Core Test Accounts:**
  * `admin@nammanivasa.com` (Society Admin)
  * `treasurer@nammanivasa.com` (Treasurer)
  * `owner@nammanivasa.com` (Resident Owner)
  * `tenant@nammanivasa.com` (Resident Tenant)
  * `guard@nammanivasa.com` (Security Guard)

---

## Phase 2: Database Setup & Automated Security Testing

### 2.1 Connection Pool Setup (`backend/src/config/db.ts`)
* Uses `pg.Pool` with connection timeout (5s) and idle timeout (30s).
* Implements `withTransaction<T>` wrapper for atomic SQL transactions (`BEGIN ... COMMIT / ROLLBACK`).

### 2.2 Migration Execution
* Single command setup: `npm run migrate && npm run seed` creates all tables, foreign keys, indexes, and test accounts in < 3 seconds.
* Runs against local PostgreSQL (`127.0.0.1:5432`) or cloud Neon.tech PostgreSQL.

### 2.3 Automated Integration Test Suite (`backend/src/tests/`)
Jest integration tests verify security and multi-tenancy before deployment:
1. **Tenant Isolation Test:** User in Community A attempting to query Community B data returns `403 Forbidden`.
2. **Role Data Filtering Test:**
   * Resident requesting `/bills` receives ONLY bills for their flat.
   * Treasurer requesting `/bills` receives ALL bills for the society.
   * Security Guard requesting `/bills` receives `403 Forbidden`.

---

## Phase 3: Backend API, Middleware & Performance Layer

### 3.1 Zero-Trust Authorization Middleware (`backend/src/middleware/auth.ts`)
1. Decodes Firebase JWT from `Authorization: Bearer <token>` header to extract `firebase_uid`.
2. Resolves `firebase_uid` to internal `user_id`.
3. Verifies user holds an `ACTIVE` membership in the target `:communityId`.
4. Expands user's roles (`community_role_assignments`) into a permission set.
5. Enforces `requirePermission(PermissionCode)` before allowing handler execution.

### 3.2 Scoped REST Slugs
All multi-tenant API routes follow explicit REST path scoping:
* `GET /api/v1/communities/:communityId/bills`
* `POST /api/v1/communities/:communityId/payments`
* `GET /api/v1/communities/:communityId/notices`
* `POST /api/v1/communities/:communityId/visitors`
* `POST /api/v1/communities/:communityId/tickets`

### 3.3 RFC 7807 Error Response Specification
All error responses return standardized JSON payloads:
```json
{
  "type": "https://nammanivasa.com/errors/permission-denied",
  "title": "Forbidden",
  "status": 403,
  "code": "COMMUNITY_PERMISSION_DENIED",
  "detail": "You do not have APPROVE_PAYMENT permission in this community.",
  "timestamp": "2025-02-15T12:00:00Z"
}
```

### 3.4 Performance & Acceleration
* **Gzip Payload Compression:** Express `compression()` middleware reduces JSON network transfer by 70–80%.
* **In-Memory Response Caching:** Low-change endpoints (*Community Info, Rules, Emergency Contacts*) are cached in memory for sub-15ms response times.

---

## Phase 4: Android App Integration & End-to-End Validation

### 4.1 Retrofit Network Integration
* `ApiService` and `RealNammaNivasaRepository` send HTTP requests to the backend REST endpoints.
* Every request automatically attaches `Authorization: Bearer <Firebase_JWT>` and target `community_id`.

### 4.2 Permission-Driven UI Rendering
Compose UI evaluates `hasPermission(PermissionCode)` dynamically from `SocietyViewModel`:
* **Admin View:** Shows Member Approvals, Society Settings, Notice Editor.
* **Treasurer View:** Shows Payment Verification sub-screen and Dues Ledger.
* **Resident View:** Shows Flat Bills, Maintenance Request Button, Pre-Approve Visitor Card.
* **Gate Guard View:** Shows Gate Entry Kiosk (Quick OTP, Visitor Log).

### 4.3 End-to-End Verification Pipeline
1. **User Sign-Up & Sign-In:** Authenticates via Firebase Auth.
2. **Community Registration:** 5-step wizard creates community, buildings, flats, and assigns `COMMUNITY_ADMINISTRATOR` role.
3. **Resident Joining Flow:** Resident searches society ➔ Selects Flat A-102 & Role ➔ Submits pending request.
4. **Two-Level Approval:** Flat Owner approves ➔ Admin confirms ➔ Membership becomes `ACTIVE`.
5. **Role Switching:** Switching active community in top bar re-fetches permissions and instantly refreshes UI state.
