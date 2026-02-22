

# Version 3 – 100 Front-End Test Cases (UI + API)

Test cases to find UI/UX and integration bugs when the front-end talks to the API. Use with a running stack (UI + API + optional blockchain). Mark **Result** (Pass / Fail / Blocked) and **Bug ID** when executing.

**How to use:**
- **Automated (Playwright):** Start UI (`cd ownership-ui-master && npm run dev`), then `cd ownership-ui-master && npm run test:e2e`. Runs a subset of cases in `tests/frontend-cases.spec.ts`.
- **Manual:** Execute cases from the tables below; record **Result** and **Bug ID** in the execution tracking section.

**Prerequisites:** API at http://localhost:8081, UI at http://localhost:3000 (e.g. `./start-all-projects.sh` or start API + UI separately).

---

## Legend

| Type | Meaning |
|------|--------|
| **Pos** | Positive – valid flow, expect success |
| **Neg** | Negative – invalid input or unauthorized, expect error/redirect |
| **Bound** | Boundary – empty, long text, special chars |
| **API** | **Y** = flow calls API (login, assets, etc.); **N** = UI-only |

---

## 1. Landing & Login (FE-001 – FE-020)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-001 | Home page loads | Open / | Page loads, no crash | Pos | N |
| FE-002 | Login button visible on home | Open / | Button/link "Login" visible | Pos | N |
| FE-003 | Click Login opens login modal/popup | Click Login | Modal or login form visible | Pos | N |
| FE-004 | Login with valid admin credentials | Open login, enter admin/adminpw, submit | Redirect to dashboard or success, session established | Pos | Y |
| FE-005 | Login with valid user credentials | Open login, enter user creds, submit | Redirect to user area, session established | Pos | Y |
| FE-006 | Login with wrong password | Valid username, wrong password, submit | Error message, stay on login | Neg | Y |
| FE-007 | Login with non-existent username | Unknown username, submit | Error message (e.g. "Username not exists") | Neg | Y |
| FE-008 | Login with empty username | Leave username blank, submit | Validation error or error message | Neg | Y |
| FE-009 | Login with empty password | Enter username, leave password blank, submit | Validation error or error message | Neg | Y |
| FE-010 | Close login modal without submitting | Open login, click close/outside | Modal closes, no API call | Pos | N |
| FE-011 | After login, home shows user state | Login, go to / | Logged-in state (e.g. profile/dashboard link) | Pos | Y |
| FE-012 | Logout clears session | Login, then logout | Redirect to home or login, session cleared | Pos | Y |
| FE-013 | Access protected route without login redirects | Not logged in, open /admin/dashboard | Redirect to login or home | Neg | N |
| FE-014 | Session persists on refresh | Login, refresh page | Still logged in | Pos | Y |
| FE-015 | Login then navigate to admin dashboard | Login as admin, go to dashboard | Dashboard page loads | Pos | Y |
| FE-016 | Login then navigate to user asset list | Login as user, go to user asset page | Asset list or empty state loads | Pos | Y |
| FE-017 | Feature section visible on home | Scroll to #feature on / | Feature content visible | Pos | N |
| FE-018 | Logo and brand visible | Open / | OWNERSHIP or brand text visible | Pos | N |
| FE-019 | Login with very long password | 500+ char password, submit | Error or safe handling | Bound | Y |
| FE-020 | Multiple failed logins | 3x wrong password | Error each time, no crash | Neg | Y |

---

## 2. Admin – Dashboard & Navigation (FE-021 – FE-035)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-021 | Admin dashboard loads | Login as admin, open /admin/dashboard | Dashboard with counts or widgets | Pos | Y |
| FE-022 | Dashboard shows asset count | Dashboard loaded | Asset count displayed (number or 0) | Pos | Y |
| FE-023 | Dashboard shows request/issue counts | Dashboard loaded | Counts visible | Pos | Y |
| FE-024 | Sidebar/nav has Asset link | On admin area | Asset menu/link visible | Pos | N |
| FE-025 | Sidebar has Asset Request link | On admin area | Asset Request link visible | Pos | N |
| FE-026 | Sidebar has Department link | On admin area | Department link visible | Pos | N |
| FE-027 | Sidebar has User link | On admin area | User link visible | Pos | N |
| FE-028 | Sidebar has Report Issue link | On admin area | Report Issue link visible | Pos | N |
| FE-029 | Sidebar has Profile link | On admin area | Profile link visible | Pos | N |
| FE-030 | Click Asset opens asset list | Click Asset in nav | Asset list page loads | Pos | Y |
| FE-031 | Click Department opens department list | Click Department | Department list loads | Pos | Y |
| FE-032 | Click User opens user list | Click User | User list loads | Pos | Y |
| FE-033 | Profile dropdown or menu visible when logged in | Login, look for profile/avatar | Profile menu or logout visible | Pos | N |
| FE-034 | Navigate Asset → Dashboard → Asset | Click Asset, then Dashboard, then Asset | No crash, correct page each time | Pos | N |
| FE-035 | Breadcrumb or page title shows current section | On /admin/asset | Title or breadcrumb indicates "Asset" | Pos | N |

---

## 3. Admin – Assets (FE-036 – FE-055)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-036 | Asset list loads | Open /admin/asset | List or table or empty state | Pos | Y |
| FE-037 | Asset list shows at least one asset when data exists | Seed asset exists, open list | Asset row/card visible | Pos | Y |
| FE-038 | Create Asset button/link visible | On asset list | Create / Add Asset visible | Pos | N |
| FE-039 | Click Create Asset opens form | Click Create Asset | Form with name, qty, assignTo, etc. | Pos | N |
| FE-040 | Submit create asset with valid data | Fill form (name, qty, assignTo), submit | Success, new asset in list or redirect | Pos | Y |
| FE-041 | Create asset with empty name | Leave name blank, submit | Validation error | Neg | Y |
| FE-042 | Create asset with invalid assignTo | assignTo non-existent user, submit | Error message or 404 handling | Neg | Y |
| FE-043 | Cancel create asset | Open form, click Cancel | Form closes, no create | Pos | N |
| FE-044 | Click asset row opens detail | Click existing asset | Detail/show page loads | Pos | Y |
| FE-045 | Asset detail shows name, qty, assignee | On asset show page | Fields populated | Pos | Y |
| FE-046 | Edit Asset button on detail | On asset show | Edit button visible | Pos | N |
| FE-047 | Edit asset and save | Open edit, change name, save | Success, updated value shown | Pos | Y |
| FE-048 | Transfer Asset action | On asset, trigger transfer | Transfer form/modal, select new user, submit | Pos | Y |
| FE-049 | Delete asset confirmation | Click delete, confirm | Asset removed or success message | Pos | Y |
| FE-050 | Delete asset cancel | Click delete, cancel | No delete, dialog closes | Pos | N |
| FE-051 | Asset list empty state | No assets, open list | Empty message or "No data" | Pos | Y |
| FE-052 | Asset list pagination or scroll | Many assets | List scrollable or paginated | Bound | Y |
| FE-053 | Open asset with non-existent ID in URL | Manually open /admin/asset/show/bad-id | 404 or error message | Neg | Y |
| FE-054 | Create asset then see it in list | Create asset, go back to list | New asset appears | Pos | Y |
| FE-055 | Asset list refresh | On list, refresh page | List reloads without crash | Pos | Y |

---

## 4. Admin – Asset Requests, Departments, Users (FE-056 – FE-075)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-056 | Asset Request list loads | Open /admin/asset-request | List or empty state | Pos | Y |
| FE-057 | Asset request list shows requests when exist | Data exists | Rows visible | Pos | Y |
| FE-058 | Department list loads | Open /admin/department | List or empty state | Pos | Y |
| FE-059 | Create Department opens form | Click Create Department | Form visible | Pos | N |
| FE-060 | Create department with name and save | Fill name, submit | Success, department in list | Pos | Y |
| FE-061 | Edit department | Edit existing, save | Success | Pos | Y |
| FE-062 | Delete department | Delete existing, confirm | Success or message | Pos | Y |
| FE-063 | User list loads | Open /admin/user | List or empty state | Pos | Y |
| FE-064 | Create User button visible | On user list | Create User visible | Pos | N |
| FE-065 | Create user form opens | Click Create User | Form with username, password, etc. | Pos | N |
| FE-066 | Create user with valid data | Fill form, submit | Success (or 200 from API) | Pos | Y |
| FE-067 | Create user with duplicate username | Same username twice | Error message | Neg | Y |
| FE-068 | User list shows users | After create | New user in list | Pos | Y |
| FE-069 | Edit user opens form | Click edit on user | Edit form with pre-filled data | Pos | Y |
| FE-070 | Admin report issue list loads | Open /admin/report-issue | List or empty state | Pos | Y |
| FE-071 | View report issue detail | Click issue | Detail modal or page | Pos | Y |
| FE-072 | Admin history page loads | Open /admin/history | Page loads, history or empty | Pos | Y |
| FE-073 | Admin profile page loads | Open /admin/profile | Profile list or current user profile | Pos | Y |
| FE-074 | Admin can access user profile by ID | Open /admin/profile/show/1 | Profile detail or 404 | Pos | Y |
| FE-075 | Navigation from dashboard to each admin section | From dashboard, click each nav item | Each page loads | Pos | Y |

---

## 5. User – Flows (FE-076 – FE-090)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-076 | User asset list loads | Login as user, open /user/asset | List or empty state | Pos | Y |
| FE-077 | User sees only assigned assets | User has 1 asset | Only that asset in list | Pos | Y |
| FE-078 | User asset detail opens | Click asset in user list | Detail page loads | Pos | Y |
| FE-079 | User asset request list loads | Open /user/asset-request | List or empty state | Pos | Y |
| FE-080 | Create asset request as user | Click create, fill form, submit | Success | Pos | Y |
| FE-081 | User history page loads | Open /user/history | History list or empty | Pos | Y |
| FE-082 | User report issue list loads | Open /user/report-issue | List or empty state | Pos | Y |
| FE-083 | Create report issue as user | Open create, fill problem, asset, submit | Success | Pos | Y |
| FE-084 | User profile page loads | Open /user/profile or profile show | Profile data or form | Pos | Y |
| FE-085 | User edit profile and save | Edit name or field, save | Success | Pos | Y |
| FE-086 | User cannot see admin dashboard link | Login as user | No admin dashboard in nav or redirect | Neg | N |
| FE-087 | User cannot open /admin/dashboard directly | As user, open /admin/dashboard | Redirect or 403 | Neg | Y |
| FE-088 | User verify asset from detail | On asset detail, verify action | Verification result or message | Pos | Y |
| FE-089 | User change password | Profile, change password, valid old/new | Success | Pos | Y |
| FE-090 | User logout | Click logout | Redirect to home, session cleared | Pos | Y |

---

## 6. Error Handling & Edge (FE-091 – FE-100)

| ID | Title | Steps | Expected | Type | API |
|----|-------|--------|----------|------|-----|
| FE-091 | API down – graceful message | Stop API, perform action that calls API | Error toast or message, no white screen | Neg | Y |
| FE-092 | Network error – retry or message | Simulate offline, click action | Error message, no crash | Neg | Y |
| FE-093 | 404 page or not-found handling | Open /admin/asset/show/nonexistent | 404 page or error message | Neg | Y |
| FE-094 | Form validation – required fields | Submit create asset without name | Inline or toast validation | Neg | N |
| FE-095 | Long asset name in list | Asset with 200-char name | Truncated or wrapped, no layout break | Bound | Y |
| FE-096 | Special characters in form | Name with <script> or quotes | Escaped or validation, no XSS | Neg | Y |
| FE-097 | Session expired – redirect to login | Invalidate token, click action | Redirect to login or 401 handling | Neg | Y |
| FE-098 | List empty state has message | Open list with no data | "No data" or empty state text | Pos | Y |
| FE-099 | Modal overlay closes on outside click | Open any modal, click outside | Modal closes | Pos | N |
| FE-100 | Page load within 10s | Open dashboard with API up | Dashboard visible within 10s | Pos | Y |

---

## Execution tracking (template)

| ID range | Result | Bug ID | Date |
|----------|--------|--------|------|
| FE-001 – FE-020 | | | |
| FE-021 – FE-035 | | | |
| FE-036 – FE-055 | | | |
| FE-056 – FE-075 | | | |
| FE-076 – FE-090 | | | |
| FE-091 – FE-100 | | | |

---

*Run with UI + API up (e.g. `./start-all-projects.sh`). Playwright: `cd ownership-ui-master && npm run test:e2e`. See [V3-Test-Environment.md](V3-Test-Environment.md).*
