# Community Registration & Role-Based Access Control (RBAC) Design Plan

This document outlines the complete architectural design for registering a community from the Android 4-step wizard, persisting its structure and auto-generated unit inventory in PostgreSQL, and provisioning community-scoped roles and administrative permissions.

---

## 1. End-to-End Registration Flow

```
┌────────────────────────────────────────────────────────────────────────┐
│                   ANDROID 4-STEP REGISTRATION WIZARD                   │
├───────────────────┬───────────────────┬────────────────┬───────────────┤
│ Step 1: Details   │ Step 2: Structure │ Step 3: Admin  │ Step 4: Review│
│ Name, Type, State,│ Towers, Floors,   │ First/Last Name│ Summary card  │
│ City, Area, PIN   │ Flats/Floor, Villa│ Email, Mobile, │ & Submit to   │
│ Address           │ counts (Auto-gen) │ Role & Flat    │ Backend       │
└───────────────────┴───────────────────┴────────────────┴───────────────┘
                                        │
                                        ▼
┌────────────────────────────────────────────────────────────────────────┐
│                   POSTGRESQL TRANSACTION (Backend)                     │
├────────────────────────────────────────────────────────────────────────┤
│ 1. `communities`           : Insert society metadata & slug            │
│ 2. `roles`                 : Provision system RBAC roles for tenant    │
│ 3. `buildings`             : Insert configured towers/blocks/phases    │
│ 4. `flats`                 : Insert all auto-generated units           │
│ 5. `community_memberships` : Link founding admin as ACTIVE member      │
│ 6. `community_role_assignments`: Map selected role to admin user       │
│ 7. `occupancy` / `ownership`: Link admin to their selected flat        │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Database Schema & Multi-Tenant Isolation

Every table in PostgreSQL is strictly scoped via `community_id` foreign keys:

- **`communities`**: Stores society metadata (`id`, `name`, `slug`, `community_type`, `state`, `city`, `pin_code`, etc.).
- **`buildings`**: Stores towers, blocks, or phases (`community_id`, `name`, `block_code`, `floors`).
- **`flats`**: Stores all individual units/villas generated in Step 2 (`community_id`, `building_id`, `flat_number`, `unit_type`).
- **`roles`**: Community-scoped RBAC definitions (`community_id`, `code`, `label`, `is_system_role`).
- **`community_memberships`**: Tracks whether a user's membership in a community is `PENDING`, `ACTIVE`, or `REVOKED`.
- **`community_role_assignments`**: Assigns specific community roles to members (e.g., `COMMUNITY_ADMINISTRATOR`).
- **`occupancy` / `property_ownership`**: Links residents to specific flats as `OWNER`, `TENANT`, or `FAMILY_MEMBER`.

---

## 3. Role-Based Access Control (RBAC) Mapping

When the founding admin selects their role in Step 3 (e.g., *President*, *Secretary*, *Treasurer*, *Resident Owner*), the backend maps it to the appropriate system permission bundle:

| Selected Role (UI) | System Role Code (`roles.code`) | Key Privileges & Permissions |
| :--- | :--- | :--- |
| **President / Secretary** | `COMMUNITY_ADMINISTRATOR` | Full access: manage notices, approve visitors, verify payments, manage members, configure settings. |
| **Treasurer / Joint Treasurer** | `TREASURER_FINANCE_MANAGER` | Financial records, verify payment receipts, view billing ledgers. |
| **Executive Committee** | `COMMITTEE_MEMBER` | Notice creation, community management oversight. |
| **Resident Owner** | `OWNER` | Pay personal dues, vote in polls, raise maintenance requests, approve visitors. |
| **Tenant** | `TENANT` | Pay personal dues, raise maintenance requests, approve visitors. |

---

## 4. Next Steps for Implementation
1. **Extend Backend `createCommunity` Repository**: Ensure all auto-generated flats from Step 2 are batch-inserted into the `flats` table.
2. **Dynamic Role Mapping**: Map the UI role selection from Step 3 directly to the inserted `role_id` in `community_role_assignments`.
3. **Property Linking**: Insert an `occupancy` and `property_ownership` record for the founding admin matching their selected flat.
