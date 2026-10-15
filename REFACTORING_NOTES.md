# Namma Nivasa — Refactoring Work Log

Working notes. Distinguishes **implemented** changes from **recommendations**.
No builds/tests/emulators were run (per instructions); changes are validated by
inspection only.

---

## 1. Findings (existing architecture / design-system problems)

1. **`ui/NammaNivasaApp.kt` is a 3,391-line monolith.** It contains the launch
   stage machine, branded splash, every onboarding/community-onboarding screen,
   the entire 4-tab shell, ~40 private composables (Home hero, attention group,
   quick actions, event cards, Money/Community/More tabs), permission helpers and
   ISO-date utilities. Biggest maintainability risk.
2. **Text-encoding corruption.** `NammaNivasaApp.kt` had UTF-8 characters that
   were decoded as Windows-1252 and re-encoded ~4×, producing mojibake in 13
   comments (em-dash) and 7 **user-facing strings** (bullet separators such as
   `"Tower B • Flat 1004"`, `"$unpaid to pay • $awaiting…"`, ticket/visitor
   subtitles). This was a real, visible defect.
3. **Unused dependencies** (0 references in source; confirmed): CameraX
   (camera2/core/lifecycle/view), Accompanist permissions, play-services-location,
   kotlinx-serialization (+ plugin), Navigation3 (ui/runtime), Material3 adaptive
   (+layout, +adaptive-navigation3), lifecycle-viewmodel-navigation3, Coil,
   DataStore. Room and `com.google.android.material` were on the list but are
   **retained** (see §3). `.agent/plan.md` documents Room was "intentionally
   superseded by the Phase 2D backend".
4. **Unused duplicate resource:** `drawable-nodpi/onboarding_finance_hero.png`
   (1.79 MB) — only `onboarding_finance_hero_trimmed.png` is referenced.
5. **Heavy duplication NOT using the shared components** in `ui/components/`:
   - Auth screens (CreateAccount, LogIn, ForgotPassword, VerifyEmail) each
     re-implement the identical primary pill button (56.dp, `ShapeFull`, 17.sp
     bold, loading spinner), the outlined secondary button, and the bulleted
     error summary.
   - The 5 sub-screens (FinancialAccess, PaymentVerification, MaintenanceRequest,
     NoticeManagement, ManagementDashboard) each re-implement the "title + Close"
     header, a bespoke error card, and bespoke empty states instead of using
     `NnEmptyState` / `NnErrorState` / `NnSectionHeader`.
6. **Fabricated / hardcoded production data:**
   - `FinancialAccessScreen` sends `ownerUserId = "user-1"` (a fabricated id)
     instead of the signed-in user's id.
   - `RealNammaNivasaRepository.dashboardTiles()` returns a hardcoded list of
     zeroed tiles — but `dashboardTiles` is never consumed by any screen.
   - Community-join apartment screen hardcodes towers (`Tower A/B/C`), floors and
     flats (`A-101`, `A-203`, …). No backend endpoint exists for structures.
   - `DiscoverableCommunity.location = "Location unavailable"` and
     `towerCount = "Towers unavailable"` placeholders.
7. **Duplicate models:** `data.api.ApiModels` and `data.model.AppModels` declare
   near-identical DTOs (`CommunityMembershipInfo`, `CreateNoticeRequest`,
   `CreateMaintenanceRequestRequest`, `CreatePaymentRequest`,
   `FinancialAccessGrant…`, `MaintenanceRequest…`, `NoticeItem`, `PaymentItem`).
   The api→model mapping layer in `RealNammaNivasaRepository` depends on both.
8. **No WorkManager / offline queue.** Despite the brief's offline-first section,
   there is no WorkManager dependency or sync worker; offline behavior is the
   "Hybrid" demo-fallback (in-memory `FakeNammaNivasaRepository`), not queued
   mutations. Documented, not a regression.
9. **Design tokens live partly in `Color.kt`** (shape + elevation dp tokens are
   declared in the color file). Spacing is entirely ad-hoc inline `dp` literals.

### Preserved intentionally (working / approved)
- Launch-stage state machine and post-auth routing (`CheckUserStatus`,
  `CommunityOnboarding`, `PendingApproval`, `UserStatusError`) and its
  lookup-failure vs. no-membership distinction.
- Hybrid repository backend-first + flagged demo fallback; `RepoResult`
  success/empty vs. failure contract.
- `domain.model` pure-Kotlin model + rule layer with unit tests (`RoleDefinitions`,
  `PermissionCatalog`, membership transition rules). Only the role/permission
  subset is consumed by the UI today.
- Approved onboarding/auth visual styling (burgundy pill buttons, `ShapeFull`,
  56.dp, 17.sp bold) — reproduced exactly by the new shared components.
- Firebase auth interceptor, token redaction in logs, backend-authoritative
  permission gating in the UI.

---

## 2. Implemented changes

### Design system / shared components
- **`ui/theme/Spacing.kt`** — spacing token scale (a token file existed for
  shapes/elevation in `Color.kt`; spacing tokens are new).
- **`ui/components/Buttons.kt`** — `NnPrimaryButton`, `NnSecondaryButton`,
  `NnFormErrorSummary`, reproducing the approved burgundy pill styling exactly
  (56.dp, `ShapeFull`, 17.sp bold, inline loading spinner).
- **`ui/components/Components.kt`** — added `NnSubScreenHeader`, `NnSubmitErrorCard`,
  and `NnInlineEmptyCard` (the compact "nothing here yet" placeholder card).
- **Auth screens migrated** (CreateAccount, LogIn, ForgotPassword, VerifyEmail):
  bespoke primary/secondary buttons and error summaries replaced with the shared
  components; visual output unchanged.
- **Sub-screens migrated** (FinancialAccess, PaymentVerification,
  MaintenanceRequest, NoticeManagement, ManagementDashboard): bespoke
  "title + Close" headers → `NnSubScreenHeader`; bespoke error cards →
  `NnSubmitErrorCard`; **8 byte-identical bespoke empty cards → `NnInlineEmptyCard`**
  (the now-unused `CardDefaults` imports were removed from all five files).
- **`Color.kt`** — design-token additions; `999.dp` literals replaced with
  `ShapeFull`; `NnChip` bug fix.

### Correctness / defect fixes
- **`FinancialAccessScreen` `ownerUserId`** now uses the signed-in user's id
  instead of the fabricated `"user-1"`.
- **Text-encoding corruption (finding 2)** fixed in user-facing strings and
  comments (mojibake `•` separators restored).
- **`ApiClient.setBaseUrl()` removed** — it was dead *and* broken (it would throw
  if the singleton had been initialized; otherwise it mutated an unused field).
  Unused `apiExceptionHandler` and the resulting `CoroutineExceptionHandler`
  import removed too. `getApiService(context)` signature kept (caller unchanged).

### Dead-code removal
- **`CreateAccountViewModel`** — removed write-only `persistEmail` /
  `clearPersistedEmail` (SharedPreferences `last_created_email` was written but
  never read) and their call; removed the unused `verificationTriggered` local;
  removed never-called `onResetSubmission()`; consolidated the duplicate
  `triggerEmailVerificationAfterRegistration()` into `triggerEmailVerification()`
  (both did the identical send-verification, swallow-error work; the boolean
  result was never used); removed the now-unused `context: Context` constructor
  parameter (call site in `OnboardingScreens.kt` updated; `Context` import
  dropped).
- **`NnProfileAvatar`** — removed the dead `photoUrl` parameter (Coil was removed
  earlier; the avatar has never rendered an actual photo, only the monogram).
  Fixed the now-inaccurate "Rendered with Coil" KDoc on
  `AppModels.UserProfile.photoUrl` (the field itself stays — it is part of the
  data contract).
- **`RealNammaNivasaRepository.dashboardTiles()`** — returns
  `RepoResult.Failure` (not-implemented) instead of a fabricated zeroed tile list;
  the established pattern for missing backend endpoints (`emergencyContacts()`
  uses the same shape).

### Community-join data honesty
- The join flow's hardcoded tower/floor/flat dropdown options are **centralized**
  into `PlaceholderTowers` / `PlaceholderFloors` / `PlaceholderFlats` constants in
  `NammaNivasaApp.kt`, with a `TODO(backend)` explaining they are demo values
  pending a real unit-structure schema (the backend community record exposes only
  id / name / status / member count). The flow's default selections now derive
  from these constants (`.first()`) so options and defaults cannot drift.
  *Behavior note:* the initial floor/flat preselection changes from
  "4th Floor"/"A-404" to "Ground Floor"/"A-101".
- `DiscoverableCommunity`'s "Location unavailable" / "Towers unavailable" strings
  are **kept** as honest fallbacks (no fabricated location data) — see §3.

### Dependencies / build
- Removed (0 source references): Accompanist, CameraX, Material3 adaptive /
  windowsizeclass / adaptive-navigation3, lifecycle-viewmodel-navigation3,
  Navigation3, Coil, DataStore, kotlinx-serialization (+ its Gradle plugin),
  play-services-location.
- `gradle/libs.versions.toml` cleaned to match.
- Deleted duplicate 1.79 MB `drawable-nodpi/onboarding_finance_hero.png` (only
  the `_trimmed` variant is referenced).

---

## 3. Decisions — kept deliberately (with rationale)

- **Room retained** — the app has an offline-first requirement; also Room's KSP
  setup is needed for Moshi codegen regardless, so dropping it would save nothing
  today.
- **`com.google.android.material` retained** — no XML layouts use Material
  widgets, but `res/values/themes.xml` declares
  `parent="Theme.MaterialComponents.Light"`, which requires the library. The
  dependency line (`implementation(libs.material)`) and the toml entry were
  verified present and consistent. *Possible future simplification:* switch the
  theme parent to a framework theme (`android:Theme.Material.Light`) and then
  drop the dependency — needs visual verification first.
- **`data.api.ApiModels` vs `data.model.AppModels` both retained** — the api
  models are Moshi-annotated wire DTOs (KSP codegen, used only by
  `RealNammaNivasaRepository` for Retrofit); the app models are plain domain/UI
  models (some are pure UI types — `DashboardTile` carries `Color`/`ImageVector`).
  The repository is the only consumer of the api models and maps between the two
  layers. Merging them would couple the UI directly to the wire format and erase
  the mapping seam — a larger change than this pass intends.
- **`DiscoverableCommunity` "unavailable" placeholders kept** — they are honest
  about missing data rather than fabricated; the real fix is a backend field.
- **No WorkManager / offline queue** — documented as a gap (finding 8); the
  hybrid demo-fallback behavior is unchanged.
- **`NnInlineEmptyCard` rather than `NnEmptyState`** for the sub-screens' empty
  lists — `NnEmptyState` is a full-screen, icon-centred state; the sub-screens
  use compact inline placeholders inside forms. Swapping them would be a visual
  redesign, not a refactor.

---

## 4. Recommendations (not implemented)

1. **Split `ui/NammaNivasaApp.kt`** (still ~3.4k lines) into feature packages;
   the launch stage machine and each onboarding/tab screen are separable units.
2. **Backend-driven unit structure** for the join flow (towers/floors/flats per
   community) — replace the `Placeholder*` constants; see the `TODO(backend)` in
   `NammaNivasaApp.kt`.
3. **Sub-screen section headers → `NnSectionHeader`** only after visual
   sign-off: `NnSectionHeader` injects 16.dp horizontal / 12.dp vertical padding,
   so adopting it would shift the current un-padded headings (a layout change).
4. **Adopt `Spacing` tokens for remaining ad-hoc `dp` literals** incrementally;
   token file exists, most call sites still use raw literals.
5. **Verify the post-registration verification re-send** in
   `CreateAccountViewModel` during manual testing (it intentionally sends the
   verification email twice — once from the registration callback, once
   explicitly afterwards).

---

## 5. Unverified

- **No builds / tests / emulators / screenshots were run** (per instructions).
  All edits were validated by inspection: exact-text editor diffs, import and
  usage greps, and brace/paren balance checks per touched file. Please run a
  full assemble + manual smoke test (auth flows, sub-screens, community join)
  to confirm.
