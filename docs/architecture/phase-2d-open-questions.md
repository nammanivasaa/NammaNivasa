# Phase 2D Open Questions and Design Decisions

Status: design-only, not implemented.

This file captures unresolved product and architecture questions that should be approved before a real backend implementation proceeds.

## 1. Membership and approval defaults

### Question 1: What should be the default status for a newly created membership?
Recommended default: `PENDING`.

Reason:
- This supports an approval flow before access is granted.
- It is consistent with a controlled community onboarding process.

Alternative:
- `ACTIVE` would reduce friction but would also bypass explicit approval for new members.

### Question 2: Can a user be a member of multiple roles in the same community?
Recommended default: yes.

Reason:
- The Phase 2A and Phase 2B design explicitly allows multiple roles per user within a community.
- This supports operational flexibility without creating a role for every combination.

### Question 3: Should role customizations be allowed within a community?
Recommended default: no, not in the first backend release.

Reason:
- The current design uses predefined roles with permission mappings.
- A custom-role administration system would expand scope and require governance and approval policies.

## 2. Financial visibility policy

### Question 4: How should household members gain access to owners' financial records?
Recommended default: explicit grant by owner/admin, with a scoped authorization record and revocation support.

Reason:
- This matches the project requirement that household access is explicit and revocable.
- The current domain model exposes `explicitlyAuthorizedUserIds` for this purpose.

### Question 5: Should community financial summaries be visible to all residents or only authorized roles?
Recommended default: only to roles with the relevant visibility permissions.

Reason:
- This preserves privacy and aligns with the domain requirement that community summaries are separate from private records.

### Question 6: What is the exact policy for payment verification approval?
Recommended default: finance manager or committee member with `APPROVE_PAYMENT` may verify or reject, with audit logs required.

Reason:
- This matches the domain permission model and the required financial integrity controls.

## 3. Ownership and occupancy policy

### Question 7: Can one user be both owner and occupant in the same flat?
Recommended default: yes, there is no explicit prohibition in the current domain model.

Reason:
- Ownership and occupancy are separate concepts by design.
- The app should not assume legal or business exclusivity without a product decision.

### Question 8: Is there an expected limit on the number of co-owners or active occupants per flat?
Recommended default: no hard limit in the domain model yet.

Reason:
- The domain model allows multiple concurrent ownership and occupancy records where applicable.
- A strict legal cap is a business rule and should be reviewed if required.

## 4. Visitor and security workflow

### Question 9: Who can approve a visitor request by default?
Recommended default: security guard or community administrator with `APPROVE_VISITOR`.

Reason:
- This aligns with the permission catalog and the prototype App UI.

### Question 10: Should visitor evidence be uploaded before or after approval?
Recommended default: allow evidence reference before or during approval, but the backend must validate storage reference validity and user authorization.

Reason:
- This avoids premature approval while allowing required evidence capture.

## 5. Notice and maintenance governance

### Question 11: Should notice publishing require dual approval in practice?
Recommended default: not for the first release; publish action requires `PUBLISH_NOTICE` and can be audited.

Reason:
- The domain model marks `PUBLISH_NOTICE` as requiring approval, but the implementation can allow direct admin execution depending on operational workflow.

### Question 12: Should maintenance requests auto-assign to a manager or remain manually assigned?
Recommended default: manual assignment in the first release.

Reason:
- This matches the minimal operational scope and avoids overengineering.

## 6. Multi-community UX

### Question 13: When a user belongs to multiple communities, should the app show a community switcher automatically?
Recommended default: yes, but only when the user belongs to more than one community.

Reason:
- This matches the agreed design requirement and keeps the active community context explicit.

### Question 14: Should a global user profile be shared across communities or should communities manage user-specific community attributes?
Recommended default: shared profile identity plus separate community-specific membership state.

Reason:
- This aligns with the domain model and the requirement to separate identity from community membership.

## 7. Data retention and audit policy

### Question 15: How long should audit logs be retained?
Recommended default: at least one year for sensitive operations, with policy-based retention later.

Reason:
- Financial and security actions should remain reviewable for disputes and investigations.

### Question 16: Should deleted user records be physically removed or soft-deleted?
Recommended default: soft-delete in the first release.

Reason:
- Membership history, ownership, and audit trails require stable references to the person even if access is ended.

## 8. Consequences of defaulting decisions

### Default decisions chosen in this proposal

- Membership default status: `PENDING`
- Multi-role support: yes, per community
- Role customization: no, initial release only predefined roles
- Household access: explicit and revocable
- Financial summaries: authorized role-only access
- Payment verification: controlled by finance or committee roles
- Community switcher: only when more than one active community is available

### Consequences

- More operational clarity and fewer surprising access holes.
- Slightly more administrative overhead for onboarding and approvals.
- Better compliance with privacy and multi-community isolation requirements.

## 9. Recommended approval gate before implementation

The following decisions should be approved by product or operations before Phase 2E begins:

- default membership onboarding state
- exact payment verification authority and approval chain
- household financial-access policy and revocation process
- role customization policy
- retention rules for audit logs and file evidence
