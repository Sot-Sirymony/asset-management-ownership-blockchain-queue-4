# Version 3 – Test and Performance Plan

Supports [V3 Business Requirements](V3-Business-Requirements.md) and [V3 Backlog](V3-Backlog.md). Use this to scope test cases, automation, and performance baselines.

---

## 1. Critical flows (in scope for test cases)

Derived from `ownership-api-master` and `ownership-ui-master`. Each flow should have at least one documented test case (manual or automated).

**200 test cases:** See [V3-200-Test-Cases-Blockchain.md](V3-200-Test-Cases-Blockchain.md) for 200 test cases targeting blockchain network and APIs (auth, assets, requests, verification, report issue, users, departments, files). Run automated batch with `node scripts/run-test-cases-api.js`.

### 1.1 Authentication

| Flow | API / UI | Notes |
|------|----------|--------|
| Login | `POST /rest/auth/login` | JWT returned; used by UI (next-auth). |

### 1.2 Assets (admin and user)

| Flow | API | Notes |
|------|-----|--------|
| Create asset (admin) | `POST /api/v1/admin/createAsset` | |
| Update asset (admin) | `PUT /api/v1/admin/updateAsset/{id}` | |
| Transfer asset (admin) | `PUT /api/v1/admin/transferAsset/{id}` | |
| Get asset by id (user) | `GET /api/v1/user/getAsset/{id}` | |
| Get all assets (user) | `GET /api/v1/user/getAllAsset` | |
| Delete asset (user) | `DELETE /api/v1/user/deleteAsset/{id}` | |
| Get all history | `GET /api/v1/getAllHistory` | |
| Get history by id (admin) | `GET /api/v1/admin/getHistoryById/{id}` | |

### 1.3 Asset requests (user)

| Flow | API | Notes |
|------|-----|--------|
| List (admin) | `GET /api/v1/admin/assetRequest` | |
| Get by id | `GET /api/v1/assetRequest/{id}` | |
| List by user | `GET /api/v1/user/assetRequest` | |
| Create | `POST /api/v1/user/createAssetRequest` | |
| Update | `PUT /api/v1/user/updateAssetRequest/{id}` | |
| Delete | `DELETE /api/v1/user/deleteAssetRequest/{id}` | |

### 1.4 Verification (user)

| Flow | API | Notes |
|------|-----|--------|
| Verify asset | `GET /api/v1/user/verifyAsset/{id}` | |
| Verify asset external | `GET /api/v1/user/verifyAssetExternal/{id}` | |
| Verification trail | `GET /api/v1/user/verificationTrail/{id}` | |

### 1.5 Report issue (user)

| Flow | API | Notes |
|------|-----|--------|
| Create issue | `POST /api/v1/user/createIssue` | |
| Get by id | `GET /api/v1/user/getIssueById/{id}` | |
| Get all | `GET /api/v1/user/getAllIssue` | |
| Update | `PUT /api/v1/user/updateIssue/{id}` | |
| Delete | `DELETE /api/v1/user/deleteIssue/{id}` | |

### 1.6 Users and profile (admin / user)

| Flow | API | Notes |
|------|-----|--------|
| Register user (admin) | `POST /api/v1/admin/register_user` | |
| Get all users (admin) | `GET /api/v1/admin/getAllUser` | |
| Get user (admin) | `GET /api/v1/admin/getUser/{id}` | |
| Update user (admin) | `PUT /api/v1/admin/updateUser/{id}` | |
| Delete user (admin) | `DELETE /api/v1/admin/deleteUser/{id}` | |
| Get profile | `GET /api/v1/getProfile` | |
| Update profile | `PUT /api/v1/updateProfile` | |
| Change password | `PUT /api/v1/changePassword` | |

### 1.7 Departments (admin)

| Flow | API | Notes |
|------|-----|--------|
| List | `GET /api/v1/admin/department` | |
| Get by id | `GET /api/v1/admin/department/{id}` | |
| Create | `POST /api/v1/admin/department` | |
| Update | `PUT /api/v1/admin/department/{id}` | |
| Delete | `DELETE /api/v1/admin/department/{id}` | |
| Dashboard | `GET /api/v1/admin/dashboard` | |

### 1.8 Files

| Flow | API | Notes |
|------|-----|--------|
| Upload | `POST /api/v1/files` (multipart) | |
| List | `GET /api/v1/files` | |

### 1.9 UI flows (ownership-ui-master)

- Login (next-auth) and session.
- Admin: asset list/create/edit/transfer, asset requests, report issue, user list, department list, dashboard.
- User: asset list, asset request create/list, report issue, profile.

Cover these with Playwright where feasible (see Section 2).

---

## 2. Tooling and automation

### 2.1 API (ownership-api-master)

- **Stack:** Spring Boot 3.3, Java 17, `spring-boot-starter-test`.
- **Use:** JUnit 5 + MockMvc or `TestRestTemplate` / `WebTestClient` for integration tests against `/api/v1/*` and `/rest/auth/*`.
- **Scope for V3:** Smoke tests for auth and at least one representative endpoint per controller (e.g. create asset, create asset request, create issue, get profile). Run on build: `mvn test`.
- **Optional:** REST Assured or Postman/Newman for API-only regression if you prefer not to rely only on Java tests.

### 2.2 UI (ownership-ui-master)

- **Stack:** Next.js 14, Refine; **Playwright** already in `devDependencies`.
- **Use:** Playwright for critical UI flows (login, asset list, create asset request, report issue).
- **Scope for V3:** One smoke suite (login + 2–3 key pages); run on demand or in CI. Example: `npx playwright test`.

### 2.3 Performance (API)

- **Goal:** Measure p95 response time and error rate for main APIs; compare to baselines (±10%).
- **Script:** Run the built-in measurement script (no extra install):
  ```bash
  node scripts/api-perf-measure.js [baseUrl]
  ```
  Env: `API_BASE_URL`, `API_USER`, `API_PASSWORD`, `PERF_ITERATIONS` (default 10). Output is a markdown table to paste into Section 3 below.
- **Options:** k6 or Gatling for load; use this script for quick baseline/regression capture. Run periodically (e.g. pre-release).

---

## 3. Performance baselines (to be filled after first run)

Agree with stakeholders and update after first performance run. Target: within ±10% for p95; error rate &lt;0.5%.

**Environment:** Local, API at http://localhost:8081. Captured with `node scripts/api-perf-measure.js` (10 iterations per endpoint).

| Endpoint / flow | p95 (ms) | min (ms) | avg (ms) | max (ms) | Error rate | Notes |
|-----------------|----------|----------|----------|----------|------------|--------|
| POST /rest/auth/login | 267 | 90 | 109 | 267 | 0% | |
| GET /api/v1/user/getAllAsset | 27 | 4 | 7 | 27 | 0% | |
| GET /api/v1/admin/assetRequest | 6 | 3 | 4 | 6 | 0% | |
| GET /api/v1/user/getAllIssue | 5 | 4 | 4 | 5 | 0% | |
| GET /api/v1/admin/dashboard | 16 | 4 | 6 | 16 | 0% | |
| GET /api/v1/admin/department | 30 | 3 | 6 | 30 | 0% | |
| GET /api/v1/getAllHistory | 8 | 3 | 4 | 8 | 0% | |
| POST /api/v1/admin/createAsset | TBD | — | — | — | &lt;0.5% | Add when measured |
| POST /api/v1/user/createIssue | TBD | — | — | — | &lt;0.5% | Add when measured |

Add rows for other critical APIs as needed. Re-run the script before each release and compare p95 to this baseline (±10%).

---

## 4. Test environment and data

- **Environments:** See **[V3-Test-Environment.md](V3-Test-Environment.md)** for URLs, ports, and how to run controller tests, full suite, UI smoke, and the performance script. See V3-Backlog ticket V3-T4.
- **Data:** Minimal dataset for repeatable runs: at least one admin user, one standard user, one asset, one asset request, one issue. Document how to create or reset in the env doc.
- **Fabric:** If tests hit Hyperledger Fabric, note network and channel; consider mocks or a dedicated test network for CI if needed.

---

## 5. Governance checkpoints (from BRD Section 7)

- **By 15 March 2025:** Critical flows (Section 1) and this plan agreed; baselines (Section 3) drafted or marked TBD with date for first run.
- **By 15 April 2025:** Test execution and at least one performance run completed; results compared to baselines; defects and regressions triaged.
- **By 30 April 2025:** Full test pass and performance sign-off (or approved exceptions) for V3 release.
- **Within 2 weeks of release:** Review defect escape and performance metrics for next version.
