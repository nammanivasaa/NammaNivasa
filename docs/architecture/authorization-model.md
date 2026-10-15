# Phase 2D Authorization and Security Model

Status: design-only, not implemented.

This document describes the expected production authorization design for Namma Nivasa. It is aligned with the Phase 2A/2B domain model and intentionally does not claim that the Android client or the current fake repository is production-secure.

## 1. Key security principles

- Firebase Authentication identifies the user.
- Backend authorization decides what the authenticated user may do.
- Community membership is evaluated in the active community context.
- Role checks are evaluated per user, per community, and per resource.
- Financial and personal data are restricted by explicit visibility rules.
- Bubbly and other AI flows must use authorized backend tools only; they never access the database directly.
- Hidden UI buttons are not treated as security controls.

## 2. Identity versus authorization

### Identity

Firebase Authentication provides a trusted identity and a stable `firebaseUid`.
The backend resolves this UID to the application user record.

### Authorization

Authorization is evaluated on the backend using:

- active user record
- active community membership
- active role assignments in the target community
- permission catalog derived from predefined roles
- resource community scoping
- ownership or occupancy relationship when relevant
- visibility policy for financial records
- audit and approval requirements for sensitive actions

A client-supplied user ID, community ID, role, or permission is never trusted as proof of authorization.

## 3. Community membership model

The domain model defines membership lifecycle values:

- PENDING
- ACTIVE
- SUSPENDED
- REVOKED
- EXPIRED

The backend must enforce these constraints:

- Only ACTIVE membership allows normal application access.
- PENDING is not enough to access protected resources.
- SUSPENDED and REVOKED memberships deny access until their state changes.
- EXPIRED requires a new membership or reapproval flow.
- Role assignment and membership access should be evaluated in the target community context only.

### Required behavior

- A user in Community A cannot access Community B resources by virtue of being a member there.
- A user must have an ACTIVE membership in the target community before any role-based permission is considered.
- Membership approvals, revocations, and suspensions must produce audit records.

## 4. Role and permission model

The current domain model includes the following roles:

- Owner
- Tenant
- Authorized household member
- Community administrator
- Committee member
- Treasurer / finance manager
- Maintenance manager / staff
- Security guard
- Facility manager

Permissions are defined independently and mapped to roles. A production backend should enforce them using resolved permission sets per community.

### Permission evaluation flow

1. Resolve the user’s active membership in the target community.
2. Resolve active community role assignments.
3. Expand to the permission set for those roles.
4. Validate the requested resource community and action.
5. Check whether the action is sensitive and requires approval.
6. Check any additional ownership, visibility, or resource-scope rules.
7. If all pass, execute the action and write an audit record.

### Important constraint

Role membership is not global. A role in Community A must never be interpreted as a role in Community B.

## 5. Resource-scoped authorization

### Community scoping

Every backend operation must validate:

- resource `community_id` matches the request community
- user belongs to the same community with ACTIVE membership
- user has the permission required for the action

A mismatched resource or community context should result in a `COMMUNITY_SCOPE_MISMATCH` error.

### Ownership and occupancy awareness

The domain model intentionally distinguishes:

- PropertyOwnership
- Occupancy

This distinction matters for authorization:

- ownership does not automatically imply occupancy
- occupancy does not automatically imply ownership
- a user may have multiple occupancy or ownership records across time or flats
- a flat may have multiple owners or multiple occupants in different status records

The backend must evaluate access using the actual community, flat, ownership, and occupancy record state, not simplistic assumptions.

## 6. Financial access boundary rules

The project requirements require strong financial privacy boundaries.

These rules must be represented in backend authorization logic:

- Residents can access their own authorized financial records.
- Owners’ private financial information is not automatically visible to tenants or household members.
- Household access is explicit, scoped, and revocable.
- Residents cannot access other residents’ individual payment records.
- Community financial summaries remain separate from individual private records.
- Only authorized finance or committee users may verify or reject payments.

### Recommended pattern

- `READ_PERSONAL_FINANCIAL_RECORDS` allows a resident to view their own payment records and supported evidence.
- `READ_FINANCIAL_SUMMARY` allows summary-level access for authorized roles only.
- `APPROVE_PAYMENT` / `REVERSE_PAYMENT` require finance-level permissions and an audit trail.
- `PRIVATE_TO_OWNER` and `AUTHORIZED_HOUSEHOLD_ONLY` visibility should be enforced at the API layer and database query layer.

## 7. Approval and sensitive operations

The project explicitly requires risk-based authorization and approval for sensitive actions.

Examples of sensitive actions:

- membership revocation or suspension
- payment verification or rejection
- financial reversal
- security access changes
- significant admin action changes
- role assignment changes

These operations require:

- permission check
- optional approval by a higher-privilege role or policy engine
- audit record creation
- immutable status transitions

## 8. Bubbly AI authorization boundary

Bubbly is not allowed to bypass the backend permission model.

The expected pattern is:

- Answer -> Evidence -> Action

A permitted AI flow can:

- read only authorized, scoped records
- summarize them
- prepare an operation request

It cannot:

- directly query the database
- directly mutate records without backend validation
- bypass community or financial access policies

All consequential actions must be sent to backend endpoints that enforce permission rules.

## 9. Request flow and enforcement details

### Step 1: Firebase token validation

The backend validates the Firebase ID token and resolves the associated `firebaseUid`.

### Step 2: user resolution

The backend loads the application `users` record.
If the user does not exist yet, the backend creates an application-level profile or returns a controlled error depending on policy.

### Step 3: community lookup

The backend resolves the target community from the route and ensures the user has an active membership.

### Step 4: role expansion

The backend reads role assignments for the user in that community and expands them into effective permissions.

### Step 5: resource validation

The backend validates the requested resource’s community and any resource-specific access conditions.

### Step 6: action execution

The backend executes the mutation only after permission and safety checks pass.

### Step 7: auditing

Successful and rejected sensitive actions write audit log entries with the actor, resource, action, and timestamp.

## 10. Cross-community isolation

This rule must be strictly enforced in backend code and database queries:

- The user’s effective permissions are always evaluated in the active community context.
- Community membership is not transitive across communities.
- A role assignment in Community A has no effect in Community B.
- A payment record from Community A cannot be read in Community B through an ID alone.
- A notice or flat owned by another community cannot be disclosed by a route mismatch or query path.

Any cross-community mismatch is treated as a violation and logged.

## 11. Membership status handling

The backend must respect the lifecycle states explicitly defined in the app model:

- PENDING: not active yet
- ACTIVE: normal access
- SUSPENDED: temporarily blocked
- REVOKED: explicit withdrawal
- EXPIRED: ended due to lifecycle rules

A suspended or revoked membership must not authorize access to community resources.

## 12. Payment and security integrity

The backend should enforce the following financial safeguards:

- payment submissions create pending records
a- approval or rejection cannot be silent or anonymous
- verification writes an audit event
- payment reversals are separate from normal verification
- only finance or committee-authorized users may verify or reject
- sensitive evidence references are stored separately from public URLs

## 13. Audit record requirements

Every sensitive action should capture:

- actor user id
- community id
- action name
- resource type and id
- timestamp
- correlation id (for multi-step workflows)
- outcome and reason when relevant

Examples:

- Payment verified
- Payment rejected
- Membership suspended
- Role reassigned
- Visitor approval decision
- Financial reversal recorded

Do not log passwords, tokens, or unnecessary PII.

## 14. Design limitations and future enforcement

This document describes the intended backend enforcement model, but the current Android project is still using a fake in-memory repository and not production backend services.

This design does not claim that client-side code is secure, and it does not consider Android code a trusted security boundary.

The eventual backend implementation must enforce these rules server-side, with database constraints, transaction logic, and permission checks in API handlers or service code.
