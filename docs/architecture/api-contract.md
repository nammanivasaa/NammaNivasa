# Phase 2D API Contract Proposal

Status: design-only, not implemented.

This document proposes a practical first backend release for Namma Nivasa under `/api/v1`.
It is intentionally scoped to apartment operations already represented in the current Android prototype and the Phase 2A/2B domain model.

## 1. Scope and principles

- API version prefix: `/api/v1`
- Authentication: Firebase ID token validated by backend.
- Authorization: server-side role and community membership checks only.
- Community context: every resource must be scoped to a community.
- Privacy: personal financial records are never exposed beyond the authorized scope.
- Audit: sensitive actions must produce append-only audit records.
- Idempotency: actions with retry risk must accept idempotency keys and replay safely.

## 2. Core conventions

### Common headers

- `Authorization: Bearer <firebase-id-token>`
- `X-Community-Id: <community-id>` is not treated as proof of access and is validated server-side.
- `Idempotency-Key: <uuid>` for mutating endpoints where retries are possible.

### Common response format

```json
{
  "data": { },
  "meta": {
    "communityId": "community-123",
    "requestId": "uuid",
    "timestamp": "2026-10-10T12:00:00Z"
  }
}
```

### Error response format

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "User is not authorized for this community action.",
    "details": null
  },
  "meta": {
    "requestId": "uuid",
    "timestamp": "2026-10-10T12:00:00Z"
  }
}
```

### Error codes

- `INVALID_INPUT`
- `UNAUTHENTICATED`
- `UNAUTHORIZED`
- `NOT_FOUND`
- `COMMUNITY_SCOPE_MISMATCH`
- `STATE_TRANSITION_INVALID`
- `IDEMPOTENCY_REPLAY`
- `RATE_LIMITED`
- `SERVER_ERROR`

## 3. Authentication and authorization flow

1. Android obtains a valid Firebase ID token.
2. Android sends the token to `/api/v1/auth/session` or a secured API call.
3. Backend verifies token with Firebase.
4. Backend resolves the application user record by `firebaseUid`.
5. Backend resolves active community membership(s) and roles.
6. Backend checks the requested resource community and permission set.
7. Backend records audit metadata for sensitive actions.

This path must not trust a client-supplied `userId`, `communityId`, or permission claim.

## 4. First-release endpoint inventory

### 4.1 Current user and memberships

#### GET /api/v1/me
Purpose: return the authenticated user profile and active community summary.
Authentication: required.
Permission: none beyond authenticated user.
Response:

```json
{
  "id": "user-uuid",
  "fullName": "Asha Nair",
  "email": "asha@example.com",
  "phone": "+91-98765-43210",
  "status": "ACTIVE",
  "activeCommunities": [
    {
      "communityId": "community-123",
      "membershipStatus": "ACTIVE",
      "roles": ["OWNER"]
    }
  ]
}
```

#### GET /api/v1/me/communities
Purpose: list user communities and current roles.
Authentication: required.
Permission: authenticated user.

#### GET /api/v1/communities/{communityId}/memberships
Purpose: list memberships visible to current caller.
Authentication: required.
Permission: `MANAGE_MEMBERSHIP` or equivalent membership-view permission for the community.

#### POST /api/v1/communities/{communityId}/memberships/{memberId}/status
Purpose: update membership lifecycle status.
Request:

```json
{
  "status": "SUSPENDED",
  "reason": "Temporary account review",
  "effectiveAt": "2026-10-10T12:00:00Z"
}
```

Required permission: `MANAGE_MEMBERSHIP`
Validation: transitions must follow Phase 2B rules. Suspended/revoked memberships must not retain access to community resources.

### 4.2 Community and flat context

#### GET /api/v1/communities/{communityId}
Purpose: current community summary.
Authentication: required.
Permission: community member with read access.

#### GET /api/v1/communities/{communityId}/buildings
Purpose: list buildings in community.
Authentication: required.
Permission: read community context.

#### GET /api/v1/communities/{communityId}/flats
Purpose: list flats and property summaries.
Authentication: required.
Permission: read community context or authorized flat access.

#### GET /api/v1/communities/{communityId}/flats/{flatId}
Purpose: get flat details and ownership/occupancy relationships.
Authentication: required.
Permission: read flat details for authorized residents or management.

### 4.3 Notices

#### GET /api/v1/communities/{communityId}/notices
Purpose: community notices and announcements.
Authentication: required.
Permission: `READ_COMMUNITY_NOTICES` or equivalent.
Query: `status`, `category`, `page`, `pageSize`.

#### POST /api/v1/communities/{communityId}/notices
Purpose: draft or create a notice.
Authentication: required.
Permission: `MANAGE_NOTICES`.
Request:

```json
{
  "title": "Water tank maintenance",
  "message": "Tank cleaning scheduled for Sunday 7 AM.",
  "published": false,
  "startsAt": "2026-10-12T00:00:00Z",
  "endsAt": "2026-10-15T00:00:00Z"
}
```

#### POST /api/v1/communities/{communityId}/notices/{noticeId}/publish
Purpose: publish a previously created notice.
Authentication: required.
Permission: `PUBLISH_NOTICE`.

### 4.4 Maintenance requests

#### GET /api/v1/communities/{communityId}/maintenance-requests
Purpose: list user-visible maintenance requests.
Authentication: required.
Permission: read own maintenance tickets or management role.
Filters: `status`, `assignedTo`, `createdByMe`.

#### POST /api/v1/communities/{communityId}/maintenance-requests
Purpose: create a maintenance request.
Authentication: required.
Permission: `CREATE_MAINTENANCE_REQUEST`.
Request:

```json
{
  "flatId": "flat-88",
  "category": "PLUMBING",
  "title": "Kitchen sink leaking",
  "description": "Water leakage under sink basin.",
  "priority": "MEDIUM",
  "photoRefs": ["file-ref-123"]
}
```

#### PATCH /api/v1/communities/{communityId}/maintenance-requests/{id}
Purpose: update maintenance request fields.
Authentication: required.
Permission: owner of request or management assignment role.

#### POST /api/v1/communities/{communityId}/maintenance-requests/{id}/assign
Purpose: assign work order to a staffing role.
Authentication: required.
Permission: `ASSIGN_WORK`.

### 4.5 Visitor requests and approvals

#### GET /api/v1/communities/{communityId}/visitors
Purpose: list visitor requests for the community.
Authentication: required.
Permission: relevant gate/security or management permission.

#### POST /api/v1/communities/{communityId}/visitors
Purpose: create a visitor request or digital visitor pass record.
Authentication: required.
Permission: resident or authorized household user.
Request:

```json
{
  "visitorName": "Arihant Sharma",
  "purpose": "Family Visit",
  "expectedArrival": "2026-10-10T09:15:00Z",
  "flatId": "flat-88",
  "hostUserId": "user-123",
  "evidenceRef": "file-ref-456"
}
```

#### POST /api/v1/communities/{communityId}/visitors/{id}/approve
Purpose: approve a visitor request.
Authentication: required.
Permission: `APPROVE_VISITOR`.

#### POST /api/v1/communities/{communityId}/visitors/{id}/reject
Purpose: reject or deny a visitor request.
Authentication: required.
Permission: `APPROVE_VISITOR`.

### 4.6 Bills and personal payment history

#### GET /api/v1/communities/{communityId}/bills
Purpose: list bills visible to the caller.
Authentication: required.
Permission: read own or authorized bills, or finance/admin summary permission.
Filters: `status`, `flatId`, `dueBefore`, `dueAfter`.

#### GET /api/v1/communities/{communityId}/bills/{billId}
Purpose: detail view for a specific bill.
Authentication: required.
Permission: authorized visibility according to bill privacy and role.

#### GET /api/v1/communities/{communityId}/payments/me
Purpose: list personal payment history for the caller.
Authentication: required.
Permission: `READ_PERSONAL_FINANCIAL_RECORDS` or user own scope.

#### POST /api/v1/communities/{communityId}/payments
Purpose: submit a payment or payment record.
Authentication: required.
Permission: payment owner or authorized payer.
Request:

```json
{
  "billId": "bill-99",
  "payerUserId": "user-123",
  "amount": "4200.00",
  "currency": "INR",
  "paymentMethod": "UPI",
  "reference": "UPI-REF-9821",
  "visibility": "PRIVATE_TO_OWNER",
  "evidenceRef": "file-ref-789"
}
```

Validation: amount must be positive, payer must be in the same community, and only the owning user or authorized payer can submit.

#### POST /api/v1/communities/{communityId}/payments/{paymentId}/verify
Purpose: finance user verifies payment.
Authentication: required.
Permission: `APPROVE_PAYMENT`.
Request:

```json
{
  "decision": "VERIFIED",
  "notes": "UPI receipt matches ledger entry",
  "verifiedByUserId": "finance-user-44"
}
```

#### POST /api/v1/communities/{communityId}/payments/{paymentId}/reject
Purpose: reject payment with reason and audit trail.
Authentication: required.
Permission: `APPROVE_PAYMENT` or `REVERSE_PAYMENT` according to policy.

#### POST /api/v1/communities/{communityId}/payments/{paymentId}/reverse
Purpose: record a financial reversal or correction.
Authentication: required.
Permission: `REVERSE_PAYMENT`.

### 4.7 Future endpoints (not first release)

- `/api/v1/communities/{communityId}/committee`
- `/api/v1/communities/{communityId}/assets`
- `/api/v1/communities/{communityId}/amc-contracts`
- `/api/v1/communities/{communityId}/vendors`
- `/api/v1/communities/{communityId}/financial-summary`
- `/api/v1/communities/{communityId}/audit-events`
- `/api/v1/communities/{communityId}/events`
- `/api/v1/communities/{communityId}/directory`

## 5. Response and validation rules

### Validation rules

- User IDs, community IDs, and flat IDs must be UUID or application-assigned stable identifiers.
- Payment amounts must be stored as integer minor units or a decimal type; not floating point in storage.
- Community-scoped resources must belong to the same community as the route.
- Membership must be ACTIVE to authorize access, except explicitly allowed admin operations for approved transitions.
- Principal and resource community must match.
- No resource may be returned if it fails the community scope check.

### Pagination

Support `page` and `pageSize` for list endpoints.
Default page size: 25.
Max page size: 100.

### Idempotency

Mutating operations with retry risk should accept `Idempotency-Key` and reuse the previous result if the same key is replayed.
Examples:
- payment submission
- visitor approval
- notice publish
- payment verification

## 6. Example error cases

### 401 Unauthenticated

```json
{
  "error": {
    "code": "UNAUTHENTICATED",
    "message": "Authentication required."
  }
}
```

### 403 Unauthorized

```json
{
  "error": {
    "code": "UNAUTHORIZED",
    "message": "User does not have permission to approve payments in this community."
  }
}
```

### 409 Conflict / invalid transition

```json
{
  "error": {
    "code": "STATE_TRANSITION_INVALID",
    "message": "Membership status cannot move from REVOKED to ACTIVE."
  }
}
```

### 404 Not found

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "Community not found."
  }
}
```

## 7. Data contract notes for Android migration

The existing Android code uses in-memory UI models and a fake repository. In the future backend integration, DTOs can be introduced alongside these models without replacing the current domain layer.

Planned DTO boundary:
- Android domain models remain as-is for business logic.
- Network DTOs are backend-specific and may map 1:1 or near 1:1 to API responses.
- Repository interfaces eventually resolve to remote API data sources.

This milestone does not implement Retrofit or Room integration.
