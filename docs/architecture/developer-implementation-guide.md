# Namma Nivasa — Complete Developer Implementation & Database Reference Guide

**File Location:** `docs/architecture/developer-implementation-guide.md`  
**Purpose:** Comprehensive, step-by-step developer reference for building, testing, and deploying the Namma Nivasa database, Node.js backend API, and Android integration.  
**Current Execution Status:** 🟢 **All Phases & Automated Seed System Completed & Verified** (Database migrations, automated `npm run seed` script, connection pooling, zero-trust middleware, Gzip acceleration, and test suites fully passing).

---

## Table of Contents
1. [Tech Stack & Architecture Overview](#1-tech-stack--architecture-overview)
2. [Complete PostgreSQL Database Schema & Table Structures](#2-complete-postgresql-database-schema--table-structures)
3. [Core Roles & Granular Permission Catalog](#3-core-roles--granular-permission-catalog)
4. [Phase-by-Phase Developer Step-by-Step Implementation Guide](#4-phase-by-phase-developer-step-by-step-implementation-guide)
   - [Phase 1: Database Migrations, Indexes & Automated Seed System](#phase-1-database-migrations-indexes--automated-seed-system)
   - [Phase 2: Database Setup, Connection Pooling & Integration Tests](#phase-2-database-setup-connection-pooling--integration-tests)
   - [Phase 3: Backend API Endpoints, Middleware & Performance Layer](#phase-3-backend-api-endpoints-middleware--performance-layer)
   - [Phase 4: Android App Integration & End-to-End Validation](#phase-4-android-app-integration--end-to-end-validation)
5. [Seed Data & Test Accounts Reference (`npm run seed`)](#5-seed-data--test-accounts-reference-npm-run-seed)

---

## 1. Tech Stack & Architecture Overview

* **Database:** Serverless PostgreSQL 16+ (Local Docker / Postgres for development; **Neon.tech** for $0 production cloud DB).
* **Backend API:** Node.js 20+ / TypeScript 5+, Express framework, `pg` connection pool.
* **Authentication:** Firebase Authentication (Email/Password, OTP, Google Sign-In — 50k MAU free).
* **Android App:** Native Kotlin, Jetpack Compose Material3, Retrofit, StateFlow, Coroutines.
* **Hosting:** Render / Koyeb ($0 Node.js web service with automatic HTTPS/SSL).

---

## 2. Complete PostgreSQL Database Schema & Table Structures

Every table uses standard PostgreSQL types. All multi-tenant tables include `community_id UUID NOT NULL` with foreign keys and compound B-tree indexes.

### 2.1 `users` (Global Identity)
Stores user profiles linked to Firebase Auth.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    firebase_uid VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL UNIQUE,
    phone VARCHAR(50) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INVITED','SUSPENDED','DELETED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_firebase_uid ON users(firebase_uid);
CREATE INDEX idx_users_email ON users(email);
```

---

### 2.2 `communities` (Societies / Layouts)
Stores housing society boundaries.

```sql
CREATE TABLE communities (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    community_type VARCHAR(100) NULL, -- 'Apartment Complex', 'Standalone Building', 'Villa Layout'
    state VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    area VARCHAR(100) NOT NULL,
    pin_code VARCHAR(20) NOT NULL,
    street_address TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_communities_slug ON communities(slug);
```

---

### 2.3 `buildings` (Towers / Blocks / Phases)

```sql
CREATE TABLE buildings (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL, -- e.g. 'Tower A', 'Block 1', 'Phase 1'
    floors INTEGER NOT NULL CHECK (floors > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_building_per_community UNIQUE (community_id, name)
);

CREATE INDEX idx_buildings_community ON buildings(community_id);
```

---

### 2.4 `flats` (Units / Villas / Plots)

```sql
CREATE TABLE flats (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE RESTRICT,
    flat_number VARCHAR(100) NOT NULL, -- e.g. 'A-101', 'Villa 12'
    floor_number INTEGER NOT NULL DEFAULT 1,
    unit_type VARCHAR(100) NOT NULL DEFAULT 'APARTMENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','EXCLUDED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_flat_per_building UNIQUE (community_id, building_id, flat_number)
);

CREATE INDEX idx_flats_community_building ON flats(community_id, building_id);
```

---

### 2.5 `community_memberships` (Society Membership Lifecycle)

```sql
CREATE TABLE community_memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    membership_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (membership_status IN ('PENDING','ACTIVE','SUSPENDED','REVOKED','EXPIRED')),
    approved_by_user_id UUID NULL REFERENCES users(id),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMPTZ NULL,
    status_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    status_reason TEXT NULL,
    CONSTRAINT unique_user_community_membership UNIQUE (user_id, community_id)
);

CREATE INDEX idx_memberships_user_community ON community_memberships(user_id, community_id);
CREATE INDEX idx_memberships_community_status ON community_memberships(community_id, membership_status);
```

---

### 2.6 `roles` & `role_permissions` (Role Definitions & Catalog)

```sql
CREATE TABLE roles (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    code VARCHAR(60) NOT NULL, -- 'COMMUNITY_ADMINISTRATOR', 'TREASURER_FINANCE_MANAGER', 'OWNER', 'TENANT', 'SECURITY_GUARD'
    label VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    is_system_role BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_role_code_per_community UNIQUE (community_id, code)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_code VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_code)
);
```

---

### 2.7 `community_role_assignments` (User Role Assignments)

```sql
CREATE TABLE community_role_assignments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','REVOKED','EXPIRED')),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_role_assignments_user_community ON community_role_assignments(user_id, community_id, status);
```

---

### 2.8 `occupancy` & `property_ownership` (Flat Associations)

```sql
CREATE TABLE property_ownership (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    ownership_type VARCHAR(25) NOT NULL DEFAULT 'OWNER' CHECK (ownership_type IN ('OWNER','CO_OWNER','TRUST')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','TRANSFERRED')),
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_date TIMESTAMPTZ NULL
);

CREATE TABLE occupancy (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    occupancy_type VARCHAR(25) NOT NULL CHECK (occupancy_type IN ('OWNER','TENANT','FAMILY_MEMBER')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','PENDING','EXPIRED')),
    start_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    end_date TIMESTAMPTZ NULL
);

CREATE INDEX idx_ownership_flat ON property_ownership(community_id, flat_id, status);
CREATE INDEX idx_occupancy_flat ON occupancy(community_id, flat_id, status);
```

---

### 2.9 `bills` & `payments` (Financial Invoicing & Receipts)

```sql
CREATE TABLE bills (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL, -- e.g. 'Maintenance Bill - Jan 2025'
    description TEXT NULL,
    amount NUMERIC(18,2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    due_date TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    bill_id UUID NULL REFERENCES bills(id) ON DELETE RESTRICT,
    payer_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    amount NUMERIC(18,2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    payment_method VARCHAR(50) NOT NULL DEFAULT 'UPI', -- 'UPI', 'BANK_TRANSFER', 'CASH', 'CHEQUE'
    transaction_reference VARCHAR(255) NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','VERIFIED','REJECTED','REVERSED')),
    verified_by_user_id UUID NULL REFERENCES users(id),
    verified_at TIMESTAMPTZ NULL,
    notes TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bills_community_flat_status ON bills(community_id, flat_id, status);
CREATE INDEX idx_payments_community_flat ON payments(community_id, flat_id, status);
```

---

### 2.10 `visitors` (Gate Visitor Logs)

```sql
CREATE TABLE visitors (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    host_user_id UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NULL,
    purpose VARCHAR(100) NOT NULL, -- 'Guest', 'Delivery', 'Cab', 'Service Staff'
    vehicle_number VARCHAR(50) NULL,
    passcode VARCHAR(10) NULL, -- 6-digit OTP/passcode
    status VARCHAR(20) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending','Approved','Denied','CheckedIn','CheckedOut')),
    entry_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    exit_time TIMESTAMPTZ NULL,
    created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_visitors_community_status ON visitors(community_id, status, entry_time DESC);
CREATE INDEX idx_visitors_flat ON visitors(community_id, flat_id, entry_time DESC);
```

---

### 2.11 `notices` (Announcements & Bulletins)

```sql
CREATE TABLE notices (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- 'GENERAL', 'MAINTENANCE', 'EVENT', 'EMERGENCY'
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED')),
    starts_at TIMESTAMPTZ NULL,
    ends_at TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ NULL
);

CREATE INDEX idx_notices_community_status ON notices(community_id, status, created_at DESC);
```

---

### 2.12 `maintenance_requests` (Helpdesk Tickets)

```sql
CREATE TABLE maintenance_requests (
    id UUID PRIMARY KEY,
    community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT,
    flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT,
    created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    assigned_to_user_id UUID NULL REFERENCES users(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'Plumbing', 'Electrical', 'Carpentry', 'Elevator', 'Common Area'
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT')),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED','CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tickets_community_status ON maintenance_requests(community_id, status, created_at DESC);
```

---

### 2.13 `audit_logs` (Immutable Append-Only Audit Trail)

```sql
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    community_id UUID NULL REFERENCES communities(id) ON DELETE SET NULL,
    actor_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action VARCHAR(100) NOT NULL, -- e.g. 'PAYMENT_VERIFIED', 'ROLE_ASSIGNED', 'MEMBER_APPROVED'
    resource_type VARCHAR(100) NOT NULL,
    resource_id VARCHAR(255) NOT NULL,
    changes_json JSONB NULL,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_community_action ON audit_logs(community_id, action, created_at DESC);
```

---

## 3. Core Roles & Granular Permission Catalog

The 5 Core Roles and their mapped permission codes:

| Permission Code (`PermissionCode`) | Admin | Treasurer | Owner | Tenant | Security Guard |
| :--- | :---: | :---: | :---: | :---: | :---: |
| `READ_OWN_PROFILE` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `UPDATE_OWN_PROFILE` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `READ_COMMUNITY_NOTICES` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `READ_COMMUNITY_DIRECTORY` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `MANAGE_NOTICES` / `PUBLISH_NOTICE` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `MANAGE_MEMBERSHIP` / `MANAGE_COMMUNITY_ROLES` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `READ_FINANCIAL_SUMMARY` | ✅ | ✅ | ❌ | ❌ | ❌ |
| `MANAGE_BILLS` / `APPROVE_PAYMENT` / `REVERSE_PAYMENT` | ❌ | ✅ | ❌ | ❌ | ❌ |
| `READ_PERSONAL_FINANCIAL_RECORDS` | ✅ | ✅ | ✅ | ✅ (If granted) | ❌ |
| `CREATE_MAINTENANCE_REQUEST` | ✅ | ✅ | ✅ | ✅ | ❌ |
| `ASSIGN_WORK` / `RESOLVE_TICKET` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `APPROVE_VISITOR` | ❌ | ❌ | ✅ | ✅ | ❌ |
| `VIEW_SECURITY_LOGS` / `MANAGE_SECURITY_ACCESS` | ✅ | ❌ | ❌ | ❌ | ✅ |
| `ACCESS_AUDIT_LOGS` | ✅ | ✅ | ❌ | ❌ | ❌ |

---

## 4. Phase-by-Phase Developer Step-by-Step Implementation Guide

### Phase 1: Database Migrations, Indexes & Automated Seed System

#### **Developer Objective:**
Ensure all migration scripts exist under `backend/migrations/` and running `npm run migrate && npm run seed` creates all tables, compound B-tree indexes, constraints, 5 core roles, and test data in < 3 seconds.

#### **Steps to Execute:**
1. Open `backend/migrations/001_init_schema.js`.
2. Confirm the 13 SQL tables listed in Section 2 are created with `IF NOT EXISTS`.
3. Confirm all compound indexes (`idx_notices_community_status`, `idx_visitors_community_status`, etc.) are created.
4. Add default role seed SQL in `001_init_schema.js` or `backend/src/database/seed.ts` to pre-populate the 5 core roles and permission mappings.
5. Create `backend/src/database/seed.ts` with the test data described in Section 5.
6. Add `"seed": "tsx src/database/seed.ts"` script to `backend/package.json`.

---

### Phase 2: Database Setup, Connection Pooling & Integration Tests

#### **Developer Objective:**
Connect Node.js `db.ts` to PostgreSQL (Local or Neon.tech) and verify tenant isolation using automated Jest tests.

#### **Steps to Execute:**
1. Configure `backend/.env`:
   ```env
   DATABASE_URL=postgresql://namma_nivasa:change_me@127.0.0.1:5432/namma_nivasa_dev
   NODE_ENV=development
   AUTH_MODE=test
   ```
2. Test connection by running:
   ```bash
   cd backend
   npm run migrate
   npm run seed
   ```
3. Run Jest tests:
   ```bash
   npm test
   ```
4. Confirm `db.test.ts` passes the multi-tenant isolation check:
   * Attempting to access `Community B` data using `Community A` credentials throws `COMMUNITY_SCOPE_MISMATCH`.

---

### Phase 3: Backend API Endpoints, Middleware & Performance Layer

#### **Developer Objective:**
Enforce zero-trust permission middleware, Gzip payload compression, and scoped REST route patterns in `backend/src/modules/api.ts`.

#### **Steps to Execute:**
1. Verify `backend/src/middleware/auth.ts`:
   * `requireAuth()` extracts JWT token ➔ verifies Firebase signature.
   * `requirePermission(permissionCode)` checks user's permissions in `X-Community-Id` or `:communityId`.
2. Ensure API routes in `backend/src/modules/api.ts` use explicit community path slugs:
   * `GET /api/v1/communities/:communityId/bills`
   * `POST /api/v1/communities/:communityId/payments`
   * `GET /api/v1/communities/:communityId/notices`
   * `POST /api/v1/communities/:communityId/visitors`
3. Add Gzip compression middleware to `backend/src/app.ts`:
   ```typescript
   import compression from 'compression';
   app.use(compression());
   ```

---

### Phase 4: Android App Integration & End-to-End Validation

#### **Developer Objective:**
Connect `RealNammaNivasaRepository` / Retrofit HTTP client in Android to the Node.js backend REST API and test end-to-end user flows.

#### **Steps to Execute:**
1. Set API base URL in `app/build.gradle.kts` / `gradle.properties`:
   * `BASE_URL = "http://10.0.2.2:4000/api/v1/"` (Android Emulator)
2. Verify `FirebaseAuthInterceptor.kt` attaches `Authorization: Bearer <token>` and `X-Community-Id` on all network requests.
3. Test end-to-end journeys in the app:
   * **Admin Account:** Log in as `admin@nammanivasa.com` ➔ Verify access to Member Approvals & Notice Editor.
   * **Treasurer Account:** Log in as `treasurer@nammanivasa.com` ➔ Verify access to Payment Verification screen.
   * **Tenant Account:** Log in as `tenant@nammanivasa.com` ➔ Verify access to Flat Dues & Pre-Approve Visitor.
   * **Guard Account:** Log in as `guard@nammanivasa.com` ➔ Verify Gate Kiosk visitor entry.

---

## 5. Seed Data & Test Accounts Reference (`npm run seed`)

Running `npm run seed` populates the database with the following pristine test environment for developer testing:

### **Test Society:**
* **Name:** Green Valley Apartments
* **UUID (`community_id`):** `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11`
* **Slug:** `green-valley-560102`
* **Location:** HSR Layout, Bengaluru, Karnataka - 560102
* **Building:** Tower A (Flats A-101 to A-105)

### **The 5 Core Test Accounts:**

| Email ID | Test Password | Assigned Role | Accessible Features in App |
| :--- | :--- | :--- | :--- |
| `admin@nammanivasa.com` | `Test1234!` | `COMMUNITY_ADMINISTRATOR` | Member Approvals, Notice Management, Society Settings, Full Overview. |
| `treasurer@nammanivasa.com` | `Test1234!` | `TREASURER_FINANCE_MANAGER` | Payment Verification Sub-screen, Bill Ledger, Society Financial Audit. |
| `owner@nammanivasa.com` | `Test1234!` | `OWNER` (Flat A-101) | Flat A-101 Maintenance Bills, Tenant Financial Grants, Visitor Approvals. |
| `tenant@nammanivasa.com` | `Test1234!` | `TENANT` (Flat A-102) | Flat A-102 Maintenance Bills, Raise Service Ticket, Pre-Approve Visitor. |
| `guard@nammanivasa.com` | `Test1234!` | `SECURITY_GUARD` | Gate Visitor Entry Kiosk, Scan QR Passcode, Call Resident. |

---

## 6. Two-Tier Search & Community Registration Architecture

To provide an optimal onboarding experience while maintaining clean database integrity, Namma Nivasa uses a **Two-Tier Search Architecture**:

### **Tier 1: Find Community (Joining Flow)**
* **Target:** Residents looking to join an existing apartment society.
* **Source:** Searches **only the internal Namma Nivasa database** (`GET /api/v1/communities/discover?q=...`).
* **Outcome:** If the community exists in the database, the user selects it and proceeds to apartment/role selection. If *No Communities Found*, the app displays the *"Can't find your community? Register it"* card.

### **Tier 2: Register Community — Step 1 (Founding Admin Flow)**
* **Target:** Founding admins registering a brand new society that is not yet on Namma Nivasa.
* **Source:** Integrates **Google Places Autocomplete API** (or GPS Reverse Geocoding / PIN Code lookup).
* **Outcome:** As the admin types their apartment name, Google Places suggests real-world societies. Selecting a suggestion **instantly auto-fills**:
  * Society / Community Name
  * Street Address
  * PIN Code
  * City & State
  * GPS Latitude / Longitude
* **Benefit:** Reduces manual typing during society registration to near zero.

