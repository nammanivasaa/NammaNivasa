# Final Locked App Navigation, Features, and RBAC Mapping

This document locks in the final architecture, interconnections, and feature placement across NammaNivasa's 4 primary navigation tabs.

---

## 1. Locked 4-Tab Navigation & Interconnections

```
┌────────────────────────────────────────────────────────────────────────┐
│                        4 PRIMARY NAVIGATION TABS                       │
├─────────────────┬────────────────┬─────────────────┬───────────────────┤
│ 1. Home Tab     │ 2. Payments Tab│ 3. Community Tab│ 4. More Tab       │
├─────────────────┼────────────────┼─────────────────┼───────────────────┤
│ • Hero Welcome  │ • Bills Feed   │ • Notices Feed  │ • Profile Summary │
│ • Attention     │ • Pay Actions  │ • Resident Dir. │ • Community Switch│
│   Group         │ • Society      │ • Facility Book │ • Emergency Cont. │
│ • Quick Actions │   Expense      │ • Polls & Voting│ • Settings &      │
│ • Events Feed   │   Transparency │ • Service Tick. │   Log Out         │
│ • Bubbly AI     │ • Payment Ver. │ • Mgmt Dash.    │                   │
└─────────────────┴────────────────┴─────────────────┴───────────────────┘
```

---

## 2. Detailed Tab Feature & Table Mapping

| Navigation Tab / Feature | Purpose & Interconnections | Backend PostgreSQL Tables | Required RBAC / Permissions |
| :--- | :--- | :--- | :--- |
| **Home Tab** (Resident Hub) | • Hero welcome header<br>• Attention summary (links to Payments/Community)<br>• Quick actions (Pay Dues, Request, Visitors, Notices)<br>• Events carousel & Bubbly AI | `users`, `communities`, `community_memberships`, `bills`, `visitor_requests`, `maintenance_requests`, `notices` | Read personal records, read notices |
| **Payments Tab** (Financial Ledger) | • Outstanding bills & instant pay<br>• **Society Expense Transparency** (monthly expense breakdown posted by Treasurer)<br>• Payment verification tool | `bills`, `payment_records`, `expenses` | `READ_PERSONAL_FINANCIAL_RECORDS`, `PAY_BILLS`, `POST_EXPENSE` (`TREASURER_FINANCE_MANAGER`) |
| **Community Tab** (Society Hub) | • Notices feed & service tickets<br>• **Resident Directory** (neighbour lookup)<br>• **Facility Booking** (clubhouse/hall)<br>• **Society Polls & Voting** (AGM polls)<br>• Management Dashboard (Gatepass & Helpdesk) | `notices`, `service_tickets`, `maintenance_requests`, `visitor_requests`, `users`, `facilities`, `facility_bookings`, `polls`, `poll_options`, `poll_votes` | `READ_NOTICES`, `APPROVE_VISITOR` (`SECURITY_GUARD`), `BOOK_FACILITY`, `VOTE_IN_POLL` |
| **More Tab** (Profile & Settings) | • Profile summary<br>• Multi-community switcher<br>• Emergency contacts & SOS dialer<br>• Account settings & log out | `users`, `community_memberships`, `emergency_contacts` | All community members |
