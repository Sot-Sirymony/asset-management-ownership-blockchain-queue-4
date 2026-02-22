# Version 3 – Backlog (BR-derived tickets)

Use these items in your issue tracker. Each maps to the [V3 Business Requirements](V3-Business-Requirements.md).

---

## V3 actions completed (objective: improve code & tests, fix issues, measurable/repeatable)

- **Error handling:** `GlobalExceptionHandle` now handles `IllegalStateException` (503 Service Unavailable). `VerificationServiceImp` checks current user null and throws `NotFoundException` instead of NPE. `AuthServiceImpl` has structured logging for login failures (username not found, account disabled, invalid password).
- **Tests:** `AuthControllerTest` – added login failure test (404 + detail). `VerificationControllerTest` – added 404 test when asset not found. `GlobalExceptionHandleTest` – new unit tests for NotFound, IllegalState, AccessDenied, BadCredentials, and generic exception (ensures error responses are consistent and measurable).
- **Repeatability:** Controller test suite runs with `mvn test -Dtest="*ControllerTest"`; performance baseline script `scripts/api-perf-measure.js`; test env doc [V3-Test-Environment.md](V3-Test-Environment.md).

---

## Testing (BR-T1–T4)

| ID | Title | Description | Acceptance criteria | BR |
|---|------|-------------|----------------------|-----|
| V3-T1 | Document test cases for critical flows | Create and document test cases for login, asset lifecycle, asset requests, report issue, user/admin actions, and verification. | Test cases documented and traceable to flows; ≥ 80% of critical flows covered. | BR-T1 |
| V3-T2 | Define test execution process and templates | Define when and how test runs happen (per release candidate / milestone), and templates for recording results (pass/fail, env, date). | Process doc and result template in place; execution evidence available for sign-off. | BR-T2 |
| V3-T3 | Automate API regression tests | Add automated tests for main API endpoints (auth, assets, asset requests, issues, users, departments, files) and run on build or on demand. | A defined API test suite runs automatically; maintained in repo. | BR-T3 |
| V3-T4 | Document test environment and data | Document test/staging environment(s), ownership, and minimal steps to get repeatable test data for core suites. | Environment doc; core suites runnable with minimal manual setup. | BR-T4 |

---

## Reliability (BR-R1–R3)

| ID | Title | Description | Acceptance criteria | BR |
|---|------|-------------|----------------------|-----|
| V3-R1 | Harden error handling and logging on critical paths | Review and improve error handling and structured logging for auth, asset create/update/transfer, asset requests, report issue, and user enrollment. | Critical paths have defined error handling and structured logs; no silent failures. | BR-R1 |
| V3-R2 | Triage and fix critical/high defects | From production or staging, list known critical/high defects; fix or explicitly defer with rationale. | All critical/high defects closed or deferred with documented rationale. | BR-R2 |
| V3-R3 | Add reliability practices to external/integration calls | Apply timeouts, retries, and idempotency where appropriate for Fabric and other external calls. | External calls have bounded impact; timeouts/retries documented. | BR-R3 |

---

## Performance (BR-P1–P3)

| ID | Title | Description | Acceptance criteria | BR |
|---|------|-------------|----------------------|-----|
| V3-P1 | Define performance baselines | Document baselines for main APIs (e.g. p95 response time, throughput, error rate) and agree with stakeholders. | Baselines documented in [V3-Test-And-Performance-Plan.md](V3-Test-And-Performance-Plan.md); agreed. | BR-P1 |
| V3-P2 | Run performance tests and compare to baselines | Execute performance test run for release candidate and compare to baselines. | One performance run per release; results within ±10% or exception documented. | BR-P2 |
| V3-P3 | Triage and fix performance regressions | Address regressions and bottlenecks found in performance tests or production. | Regressions tracked; remediated or accepted with documented exception. | BR-P3 |

---

## Suggested sprint / order

1. **Kick-off:** V3-T4 (env doc), V3-T1 (test cases), V3-P1 (baselines).
2. **Execution:** V3-T2 (process), V3-T3 (automation), V3-R1 (error handling), V3-R2 (defects).
3. **Hardening:** V3-R3 (integration reliability), V3-P2 (perf run), V3-P3 (regressions).

Reserve capacity for test execution and performance runs at mid-cycle and release-candidate checkpoints (see [V3-Business-Requirements.md](V3-Business-Requirements.md) Section 7).
