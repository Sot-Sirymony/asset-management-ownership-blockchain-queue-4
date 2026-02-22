# Version 3 – Test Environment (V3-T4)

Document test and performance environments so runs are repeatable. Update this as you add staging or CI.

---

## Quick start – run V3 checks

1. **API controller tests (no services needed):**  
   `cd ownership-api-master && mvn test -Dtest="*ControllerTest"`  
   Expect: all controller tests pass.

2. **Capture performance baselines:**  
   Start the API (e.g. via `./start-all-projects.sh` or run the API alone on port 8081). Then from repo root:  
   `node scripts/api-perf-measure.js`  
   Paste the printed table into [V3-Test-And-Performance-Plan.md](V3-Test-And-Performance-Plan.md) Section 3.

3. **200 test cases (blockchain):**  
   With API + blockchain running: `node scripts/run-test-cases-api.js`  
   Runs a batch of cases from [V3-200-Test-Cases-Blockchain.md](V3-200-Test-Cases-Blockchain.md). Full 200 cases can be run manually or extended in the script.

4. **UI smoke:**  
   Start the UI (`cd ownership-ui-master && npm run dev`). In another terminal:  
   `cd ownership-ui-master && npm run test:e2e`

5. **100 front-end test cases:**  
   See [V3-100-Test-Cases-Frontend.md](V3-100-Test-Cases-Frontend.md). Automated batch runs via Playwright:  
   Start UI + API (e.g. `./start-all-projects.sh`), then `cd ownership-ui-master && npm run test:e2e`  
   (runs `tests/smoke.spec.ts` and `tests/frontend-cases.spec.ts`). Use `UI_TEST_USER` / `UI_TEST_PASSWORD` for login credentials.

---

## 1. Local development

| Component | URL / location | Port | Notes |
|-----------|----------------|------|--------|
| API | http://localhost:8081 | 8081 | Spring Boot; see `ownership-api-master/src/main/resources/application.properties` |
| UI | http://localhost:3000 | 3000 | Next.js/Refine; `npm run dev` in `ownership-ui-master` |
| PostgreSQL | localhost | 55432 (or from `SPRING_DATASOURCE_URL`) | Used by API when running full stack |
| Fabric | — | — | Required for `BlockchainIntegrationTest` and full API; optional for controller-only tests |

---

## 2. How to run tests

- **API tests (default – no Fabric required):**  
  `cd ownership-api-master && mvn test`  
  Runs 33 tests (controller + exception handler). Integration tests tagged `@Tag("integration")` are excluded so the build passes without Fabric.

- **API controller tests only:**  
  `cd ownership-api-master && mvn test -Dtest="*ControllerTest"`  
  Runs only `*ControllerTest` classes.

- **API full suite including integration (Fabric + DB required):**  
  Start PostgreSQL and Fabric as per `start-all-projects.sh`, then:  
  `cd ownership-api-master && mvn test -DexcludedGroups=`  
  Runs all 38 tests including `BlockchainIntegrationTest` (requires Fabric CA cert at configured path).

- **UI smoke (Playwright):**  
  Start UI: `cd ownership-ui-master && npm run dev`  
  Then: `npm run test:e2e`  
  Optional: `PLAYWRIGHT_BASE_URL=http://localhost:3000 npm run test:e2e`

- **UI 100 front-end cases (Playwright):**  
  Same as above; `test:e2e` runs both `smoke.spec.ts` and `frontend-cases.spec.ts` (subset of [V3-100-Test-Cases-Frontend.md](V3-100-Test-Cases-Frontend.md)). For full coverage, run manual cases from the doc and log bugs.

- **API performance baseline:**  
  Start API (and dependencies if needed). From repo root:  
  `node scripts/api-perf-measure.js`  
  Or: `API_BASE_URL=http://localhost:8081 node scripts/api-perf-measure.js`  
  Paste the printed table into [V3-Test-And-Performance-Plan.md](V3-Test-And-Performance-Plan.md) Section 3.

---

## 3. Minimal data for repeatable tests

- **Admin user:** username/password as configured for login (e.g. default `admin` / `adminpw` in perf script).
- **Standard user:** at least one non-admin user for user-scoped API tests.
- **Asset / asset request / issue:** at least one of each if running integration or E2E flows that query by id.

Document here how to create or reset this data (e.g. seed script, SQL, or manual steps).

---

## 4. Staging / CI (to be filled)

| Environment | API URL | UI URL | Purpose |
|-------------|---------|--------|---------|
| Staging | TBD | TBD | Pre-release test and performance runs |
| CI | TBD | TBD | Automated test runs on build |

---

*Update this document when you add environments or change ports. See [V3-Backlog](V3-Backlog.md) ticket V3-T4.*
