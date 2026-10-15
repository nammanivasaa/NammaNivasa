# Phase 2D PostgreSQL Schema Proposal

Status: design-only, not implemented.

This proposal intentionally follows the existing Android domain model and Phase 2A/2B rules. It preserves the distinction between:

- user identity
- community membership
- property ownership
- occupancy
- role assignment
- financial visibility
- audit decisions

## 1. Design principles

- PostgreSQL is the authoritative shared-data store.
- Community data must be isolated by `community_id` on all multi-tenant resources.
- Every important permission-sensitive action is separately auditable.
- Monetary values are stored without floating-point risk.
- Sensitive files and evidence use storage references, not public URLs.
- The first release should prioritize operational essentials, not the full management suite.

## 2. Core data types

Use PostgreSQL types such as:

- `UUID` for primary keys and external references.
- `VARCHAR(255)` for short identifiers and labels.
- `TEXT` for longer descriptions and messages.
- `TIMESTAMPTZ` for all timestamps.
- `NUMERIC(18,2)` or integer minor-unit amounts for money.
- `BOOLEAN` for flags.
- `JSONB` only where structured metadata is truly needed and not better represented by a separate table.

## 3. First-release tables

### 3.1 users

Purpose: identity and profile data for each person.

Columns:
- `id UUID PRIMARY KEY`
- `firebase_uid VARCHAR(255) NOT NULL UNIQUE`
- `full_name VARCHAR(255) NOT NULL`
- `email VARCHAR(255) NULL`
- `phone VARCHAR(50) NULL`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INVITED','SUSPENDED','DELETED'))`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Constraints:
- `firebase_uid` must be non-empty.
- `full_name` must be non-empty.

Indexes:
- `idx_users_firebase_uid` on `firebase_uid`
- `idx_users_email` on `email`

### 3.2 communities

Purpose: apartment community or society boundary.

Columns:
- `id UUID PRIMARY KEY`
- `name VARCHAR(255) NOT NULL`
- `slug VARCHAR(100) NOT NULL UNIQUE`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'))`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Indexes:
- `idx_communities_slug` on `slug`

### 3.3 buildings

Purpose: per-community buildings or blocks.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `name VARCHAR(255) NOT NULL`
- `block_code VARCHAR(50) NOT NULL`
- `floors INTEGER NOT NULL CHECK (floors > 0)`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'))`

Unique constraints:
- `UNIQUE (community_id, block_code, name)`

Indexes:
- `idx_buildings_community_id` on `community_id`

### 3.4 flats

Purpose: resident units within a building.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE RESTRICT`
- `flat_number VARCHAR(100) NOT NULL`
- `unit_type VARCHAR(100) NOT NULL`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','ARCHIVED'))`
- `area_sq_ft NUMERIC(10,2) NULL CHECK (area_sq_ft IS NULL OR area_sq_ft > 0)`

Unique constraints:
- `UNIQUE (community_id, building_id, flat_number)`

Indexes:
- `idx_flats_community_id` on `community_id`
- `idx_flats_building_id` on `building_id`

### 3.5 community_memberships

Purpose: membership lifecycle within one community.

Columns:
- `id UUID PRIMARY KEY`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `membership_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (membership_status IN ('PENDING','ACTIVE','SUSPENDED','REVOKED','EXPIRED'))`
- `approved_by_user_id UUID NULL REFERENCES users(id)`
- `joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `ended_at TIMESTAMPTZ NULL`
- `status_updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `status_reason TEXT NULL`

Constraints:
- `CHECK (ended_at IS NULL OR ended_at >= joined_at)`
- `CHECK (status_updated_at >= joined_at)`
- `CHECK (membership_status <> 'REVOKED' OR ended_at IS NOT NULL OR status_reason IS NOT NULL)`

Unique constraints:
- `UNIQUE (user_id, community_id)`

Indexes:
- `idx_memberships_user_community` on `(user_id, community_id)`
- `idx_memberships_community_status` on `(community_id, membership_status)`

Notes:
- This table is community-scoped by design and must not be used to infer global access.
- Membership lifecycle transitions are validated by backend logic and audit events.

### 3.6 roles

Purpose: community-scoped predefined roles.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `code VARCHAR(60) NOT NULL`
- `label VARCHAR(120) NOT NULL`
- `description TEXT NOT NULL`
- `is_customizable BOOLEAN NOT NULL DEFAULT FALSE`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Unique constraints:
- `UNIQUE (community_id, code)`

Check constraints:
- `code` must be one of the predefined role values: `OWNER`, `TENANT`, `AUTHORIZED_HOUSEHOLD_MEMBER`, `COMMUNITY_ADMINISTRATOR`, `COMMITTEE_MEMBER`, `TREASURER_FINANCE_MANAGER`, `MAINTENANCE_MANAGER`, `SECURITY_GUARD`, `FACILITY_MANAGER`

### 3.7 role_permissions

Purpose: many-to-many mapping between roles and permission codes.

Columns:
- `role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE`
- `permission_code VARCHAR(80) NOT NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Primary key:
- `PRIMARY KEY (role_id, permission_code)`

This table is intentional because permissions are independent from role names and should be evaluated in the application permission catalog and backend authorizer.

### 3.8 community_role_assignments

Purpose: assign a specific role to a user within a community and track lifecycle.

Columns:
- `id UUID PRIMARY KEY`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','REVOKED','EXPIRED'))`
- `assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `revoked_at TIMESTAMPTZ NULL`

Checks:
- `CHECK (revoked_at IS NULL OR revoked_at >= assigned_at)`

Unique constraints:
- `UNIQUE (user_id, community_id, role_id, status)` is optional, but a practical backend may use a unique active assignment constraint depending on business rules.

Indexes:
- `idx_role_assignments_user_community` on `(user_id, community_id)`
- `idx_role_assignments_role_id` on `role_id`

### 3.9 property_ownership

Purpose: legal or formal property ownership records.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `ownership_type VARCHAR(25) NOT NULL CHECK (ownership_type IN ('OWNER','CO_OWNER','TRUST','LEGAL_ENTITY'))`
- `ownership_share NUMERIC(5,4) NOT NULL CHECK (ownership_share > 0 AND ownership_share <= 1)`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','TRANSFERRED','REVOKED'))`
- `start_date TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `end_date TIMESTAMPTZ NULL`

Constraints:
- `CHECK (end_date IS NULL OR end_date >= start_date)`
- `CHECK (status <> 'REVOKED' OR end_date IS NOT NULL OR ownership_share >= 0)`

Indexes:
- `idx_property_ownership_flat` on `(community_id, flat_id)`
- `idx_property_ownership_user` on `(user_id, community_id)`

### 3.10 occupancy

Purpose: current or historical occupancy records, separate from ownership.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `flat_id UUID NOT NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `occupancy_type VARCHAR(25) NOT NULL CHECK (occupancy_type IN ('OWNER','TENANT','FAMILY_MEMBER','STAFF'))`
- `status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE','PENDING','EXPIRED'))`
- `start_date TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `end_date TIMESTAMPTZ NULL`

Constraints:
- `CHECK (end_date IS NULL OR end_date >= start_date)`

Indexes:
- `idx_occupancy_flat` on `(community_id, flat_id)`
- `idx_occupancy_user` on `(user_id, community_id)`

### 3.11 notices

Purpose: community announcements and notices.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `title VARCHAR(255) NOT NULL`
- `message TEXT NOT NULL`
- `status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','ARCHIVED'))`
- `starts_at TIMESTAMPTZ NULL`
- `ends_at TIMESTAMPTZ NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Constraints:
- `CHECK (ends_at IS NULL OR ends_at >= starts_at)`

Indexes:
- `idx_notices_community_status` on `(community_id, status)`

### 3.12 maintenance_requests

Purpose: resident-maintenance track and work requests.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `flat_id UUID NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `category VARCHAR(80) NOT NULL`
- `title VARCHAR(255) NOT NULL`
- `description TEXT NOT NULL`
- `priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW','MEDIUM','HIGH','URGENT'))`
- `status VARCHAR(25) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED','CLOSED','REJECTED'))`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Indexes:
- `idx_maintenance_requests_community_status` on `(community_id, status)`
- `idx_maintenance_requests_created_by` on `(created_by_user_id)`

### 3.13 work_assignments

Purpose: assignment of maintenance work to staff or facilities members.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `maintenance_request_id UUID NOT NULL REFERENCES maintenance_requests(id) ON DELETE CASCADE`
- `assignee_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `status VARCHAR(20) NOT NULL DEFAULT 'ASSIGNED' CHECK (status IN ('ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED'))`
- `assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `completed_at TIMESTAMPTZ NULL`

Indexes:
- `idx_work_assignments_request` on `(maintenance_request_id)`

### 3.14 visitor_requests

Purpose: gate approvals and visitor pass records.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `flat_id UUID NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `host_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `visitor_name VARCHAR(255) NOT NULL`
- `purpose VARCHAR(255) NOT NULL`
- `expected_arrival TIMESTAMPTZ NOT NULL`
- `status VARCHAR(25) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','CANCELLED'))`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `decision_by_user_id UUID NULL REFERENCES users(id)`
- `decision_at TIMESTAMPTZ NULL`
- `decision_reason TEXT NULL`

Indexes:
- `idx_visitors_community_status` on `(community_id, status)`

### 3.15 bills

Purpose: recurring maintenance and community bills.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `flat_id UUID NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `bill_type VARCHAR(80) NOT NULL`
- `amount NUMERIC(18,2) NOT NULL CHECK (amount > 0)`
- `currency CHAR(3) NOT NULL DEFAULT 'INR'`
- `period_start TIMESTAMPTZ NOT NULL`
- `period_end TIMESTAMPTZ NOT NULL`
- `due_at TIMESTAMPTZ NOT NULL`
- `status VARCHAR(25) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','PARTIALLY_PAID','PAID','OVERDUE','VOID'))`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Check constraints:
- `CHECK (period_end >= period_start)`
- `CHECK (due_at >= period_start)`

Indexes:
- `idx_bills_community_flat` on `(community_id, flat_id)`
- `idx_bills_due_at` on `(due_at)`

### 3.16 payment_records

Purpose: payment submissions and ledger state.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `bill_id UUID NULL REFERENCES bills(id) ON DELETE RESTRICT`
- `payer_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `flat_id UUID NULL REFERENCES flats(id) ON DELETE RESTRICT`
- `amount NUMERIC(18,2) NOT NULL CHECK (amount > 0)`
- `currency CHAR(3) NOT NULL DEFAULT 'INR'`
- `payment_method VARCHAR(40) NULL`
- `reference VARCHAR(255) NULL`
- `status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','PAID','REJECTED','PARTIALLY_PAID','REVERSED'))`
- `visibility VARCHAR(35) NOT NULL DEFAULT 'PRIVATE_TO_OWNER' CHECK (visibility IN ('PRIVATE_TO_OWNER','AUTHORIZED_HOUSEHOLD_ONLY','COMMUNITY_SUMMARY_ONLY','RESIDENT_VISIBLE','ADMIN_ONLY'))`
- `evidence_ref VARCHAR(255) NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `verified_by_user_id UUID NULL REFERENCES users(id)`
- `verified_at TIMESTAMPTZ NULL`
- `rejected_by_user_id UUID NULL REFERENCES users(id)`
- `rejected_at TIMESTAMPTZ NULL`
- `reversal_reason TEXT NULL`

Constraints:
- `CHECK (status <> 'PAID' OR verified_by_user_id IS NOT NULL)`
- `CHECK (status <> 'REJECTED' OR rejected_by_user_id IS NOT NULL)`
- `CHECK (verified_at IS NULL OR verified_by_user_id IS NOT NULL)`
- `CHECK (rejected_at IS NULL OR rejected_by_user_id IS NOT NULL)`

Indexes:
- `idx_payment_records_payer` on `(payer_user_id, community_id)`
- `idx_payment_records_status` on `(community_id, status)`

### 3.17 payment_verification_events

Purpose: append-only audit trail for verification and rejection decisions.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `payment_id UUID NOT NULL REFERENCES payment_records(id) ON DELETE CASCADE`
- `action VARCHAR(30) NOT NULL CHECK (action IN ('VERIFIED','REJECTED','REVERSED','ADJUSTED'))`
- `actor_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `reason TEXT NULL`
- `performed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`
- `correlation_id UUID NULL`

Indexes:
- `idx_payment_verification_events_payment` on `(payment_id, performed_at)`

### 3.18 audit_logs

Purpose: append-only audit trail for sensitive operations.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `actor_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT`
- `resource_type VARCHAR(80) NOT NULL`
- `resource_id VARCHAR(100) NOT NULL`
- `action VARCHAR(80) NOT NULL`
- `details JSONB NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Index:
- `idx_audit_logs_community_time` on `(community_id, created_at)`

### 3.19 file_refs

Purpose: private storage references for sensitive evidence, receipts, and photos.

Columns:
- `id UUID PRIMARY KEY`
- `community_id UUID NOT NULL REFERENCES communities(id) ON DELETE RESTRICT`
- `owner_user_id UUID NULL REFERENCES users(id)`
- `storage_bucket VARCHAR(255) NOT NULL`
- `storage_path TEXT NOT NULL`
- `content_type VARCHAR(120) NULL`
- `size_bytes BIGINT NULL`
- `checksum_sha256 VARCHAR(128) NULL`
- `created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()`

Constraints:
- `CHECK (storage_path <> '')`

This table is deliberately separated from public URLs to avoid exposing private evidence or attachments directly.

## 4. Relationship summary

- `users` to `communities`: through `community_memberships`, `community_role_assignments`
- `communities` to `buildings` to `flats`
- `flats` to `property_ownership` and `occupancy`
- `communities` to `notices`
- `communities` to `maintenance_requests`
- `maintenance_requests` to `work_assignments`
- `communities` to `visitor_requests`
- `communities` to `bills`, `payment_records`, and `audit_logs`

## 5. First-release vs future tables

### First release required

- users
- communities
- buildings
- flats
- community_memberships
- roles
- role_permissions
- community_role_assignments
- property_ownership
- occupancy
- notices
- maintenance_requests
- work_assignments
- visitor_requests
- bills
- payment_records
- payment_verification_events
- audit_logs
- file_refs

### Waiting for later phases

- vendor catalogue
- AMC contracts
- asset registry
- committee records
- meeting/event docs
- resident directory and role-specific admin records
- daily staff attendance beyond basic assignment metadata
- full financial ledger and general accounting dimensions

## 6. Community-isolation and privacy strategies

- Every resource table with community scope should also include `community_id` and indexes.
- Permission checks must compare the resolved authenticated community with the record's `community_id`.
- `payment_records.visibility` and `property_ownership` or `occupancy` scoping should be enforced in server authorization, not left to app clients.
- `audit_logs` should remain append-only and not be mutable by regular users.

## 7. Constraints that need backend transaction logic

Some invariants are best enforced by database constraints plus backend transactional rules:

- membership transition validity
- role permission consistency
- ownership share totals per flat
- payment verification after submission
- reversal and adjustment accounting
- idempotent action replay
- financial audit entries with proper references

## 8. PostgreSQL-specific implementation notes

- Use `NUMERIC` rather than `DOUBLE PRECISION` for money.
- Use `TIMESTAMPTZ` for all time values in the backend.
- Use CHECK constraints on statuses and legal ranges.
- Use indexes on foreign keys and common query filters.
- Store audit data as append-only events; avoid UPDATE/DELETE of canonical audit rows.
- Use row-level security in a later production backend if desired, but do not assume it replaces application-level permission checks.

This document is a design proposal only and does not implement code or DB migration scripts.
