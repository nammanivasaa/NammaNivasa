# Implementation Plan: NammaNivasa Full Feature Roadmap & RBAC Architecture

This document finalizes the complete technical design and feature roadmap for NammaNivasa, incorporating core management tools as well as extended community features (Resident Directory, Facility Booking, Society Polls, and Advanced Maintenance Helpdesk).

## User Review Required

> [!IMPORTANT]
> **Modular Rollout**: The core community registration, RBAC, billing, visitor management, and notice workflows are fully implemented. The extended features (Resident Directory, Facility Booking, and Society Polls) are structured in the database mapping plan and can be developed modularly.

## Proposed Architecture & Feature Roadmap

### Phase 1: Core Foundation & Registration [COMPLETED]
- Android 4-step Community Registration wizard with PIN-code lookup.
- Pure PostgreSQL backend integration (`createCommunity`, `communities`, `buildings`, `flats`, memberships).
- Authentication, token validation (`reload()`), and RBAC permission enforcement.

### Phase 2: Daily Operations & Management [COMPLETED]
- Home dashboard with signature burgundy hero and attention summary.
- Bills & Payments tracking with receipt verification.
- Visitor management and notice publishing.

### Phase 3: Extended Community Living Features [DESIGNED]
- **Resident Directory**: Searchable neighbour lookup (`users`, `flats`, `occupancy`).
- **Facility Booking**: Clubhouse & amenity reservations (`facilities`, `facility_bookings`).
- **Society Polls & Voting**: Digital AGM voting (`polls`, `poll_options`, `poll_votes`).
- **Advanced Maintenance**: Worker task assignment (`work_assignments`).

## Verification Plan

### Automated Tests
- Backend database integration tests (`npm test`).
- Android UI & instrumentation tests (`app:testDebugUnitTests`).

### Manual Verification
- Deploy app to device, register community, test role-based access control, and verify PostgreSQL persistence.
